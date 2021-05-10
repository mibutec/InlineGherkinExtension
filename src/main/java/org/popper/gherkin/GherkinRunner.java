/*
 * Copyright [2018] [Michael Bulla, michaelbulla@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.popper.gherkin;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.popper.gherkin.GherkinMixin.ExecutableWithExceptionAndTable;
import org.popper.gherkin.listener.GherkinFileListener;
import org.popper.gherkin.listener.GherkinListener;
import org.popper.gherkin.table.Table;
import org.popper.gherkin.table.TableMapper;

/**
 * Main class responsible to execute step actions, do error handling and delegating events to {@link GherkinListener}s
 *
 * @author Michael
 *
 */
public class GherkinRunner {
    private final Set<GherkinListener> listeners;

    private final File baseDir;

    private String lastType = "";

    private ExtensionContext methodContextInUse;

    /**
     * since version 0.6  no more need to use this constructor,use instead GherkinRunner(Set<GherkinListener> listeners, String baseDir)
     */
    @Deprecated
    public GherkinRunner(boolean catchCompleteOutput, Set<GherkinListener> listeners, String baseDir) {
        this(listeners, baseDir);
    }

    public GherkinRunner(Set<GherkinListener> listeners, String baseDir) {
        this.listeners = listeners;
        this.baseDir = new File(baseDir);
        this.baseDir.mkdirs();
    }

    public void startClass(ExtensionContext context) {
        fireEvent(l -> l.storyStarted(context, context.getRequiredTestClass()));
        Narrative narrative = context.getRequiredTestClass().getAnnotation(Narrative.class);
        if (narrative != null) {
            listeners.forEach(l -> l.narrative(context, narrative));
        }
    }

    public void startMethod(ExtensionContext context) {
        lastType = "";
        assert methodContextInUse == null;
        methodContextInUse = context;
        Object testInstance = context.getRequiredTestInstance();
        Method method = context.getRequiredTestMethod();
        fireEvent(l -> l.scenarioStarted(context, getScenarioTitle(testInstance, method), method));
    }

    public void executeAction(String type, String step, ExecutableWithExceptionAndTable<?> action,
            TableMapper<?> tableMapper, EventuallyConfiguration eventuall) {

        assert methodContextInUse != null;

        Optional<Table<Map<String, String>>> table;
        String stepWithoutTable;

        if (tableMapper != null) {
            table = Optional.ofNullable(tableMapper.createMapTable(step));
            stepWithoutTable = tableMapper.removeTable(step);
        } else {
            table = Optional.empty();
            stepWithoutTable = step;
        }

        if (lastType.contains(type)) {
            fireEvent(l -> l.stepExecutionStarts(methodContextInUse, "And", stepWithoutTable, table));
        } else {
            fireEvent(l -> l.stepExecutionStarts(methodContextInUse, type, stepWithoutTable, table));
        }
        try {
            Table<?> convertedTable = tableMapper != null ? tableMapper.createTable(step) : null;
            runAction(action, convertedTable, eventuall);
            if (lastType.contains(type)) {
                fireEvent(l -> l.stepExecutionSucceed(methodContextInUse, "And", stepWithoutTable, table));
            } else {
                fireEvent(l -> l.stepExecutionSucceed(methodContextInUse, type, stepWithoutTable, table));
            }
        } catch (Throwable th) {
            if (lastType.contains(type)) {
                fireEvent(l -> l.stepExecutionFailed(methodContextInUse, "And", stepWithoutTable, table, th));
            } else {
                fireEvent(l -> l.stepExecutionFailed(methodContextInUse, type, stepWithoutTable, table, th));
            }

            throw this.<RuntimeException> handleError(th);

        }

        lastType = type;

    }

    public void endMethod(ExtensionContext context) throws Exception {
        assert methodContextInUse == context;
        methodContextInUse = null;

        Object testInstance = context.getRequiredTestInstance();
        Method method = context.getRequiredTestMethod();

        fireEvent(l -> l.scenarioSucceed(context, getScenarioTitle(testInstance, method), method));
    }

    @SuppressWarnings("unchecked")
    private <E extends Exception> E handleError(Throwable th) throws E {
        if (th instanceof Error) {
            throw (Error) th;
        } else if (th instanceof RuntimeException) {
            throw (RuntimeException) th;
        } else {
            throw (E) th;
        }
    }

    public void endClass(ExtensionContext context) {
        fireEvent(l -> l.storyFinished(context, context.getRequiredTestClass()));
        fireEvent(l -> {
            if (l instanceof GherkinFileListener) {
                ((GherkinFileListener) l).toFile(baseDir);
            }
        });
    }

    protected void fireEvent(Consumer<GherkinListener> consumer) {
        listeners.forEach(consumer);
    }

    public String getScenarioTitle(Object testInstance, Method method) {
        Scenario scenario = method.getAnnotation(Scenario.class);
        if (scenario != null) {
            return scenario.value();
        } else {
            return method.getName();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void runAction(ExecutableWithExceptionAndTable<?> action, Table table, EventuallyConfiguration eventually)
            throws Throwable {
        Throwable throwableFromStep = null;
        if (eventually == null) {
            action.run(table);
        } else {
            long start = System.currentTimeMillis();
            while ((System.currentTimeMillis() - eventually.getTimeoutInMs()) < start) {
                try {
                    action.run(table);
                    throwableFromStep = null;
                    break;
                } catch (Throwable th) {
                    throwableFromStep = th;
                    Thread.sleep(eventually.getIntervalInMs());
                }
            }

            if (throwableFromStep != null) {
                throw throwableFromStep;
            }
        }
    }

    public static class UnhandledExceptionTypeException extends RuntimeException {
        UnhandledExceptionTypeException(Throwable cause) {
            super(cause);
        }
    }

    public static class DefaultRunnerFactory implements RunnerFactory {
        @Override
        public GherkinRunner createRunner(ExtensionContext context, boolean catchCompleteOutput,
                Set<GherkinListener> listeners, String baseDir) {
            return new GherkinRunner(catchCompleteOutput, listeners, baseDir);
        }
    }
}
