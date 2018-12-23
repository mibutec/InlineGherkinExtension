/*
 * Copyright © 2018 Michael Bulla (michaelbulla@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.popper.gherkin.Gherkin.ExecutableWithExceptionAndTable;
import org.popper.gherkin.customizer.Customizer;
import org.popper.gherkin.customizer.ErrorHandler;
import org.popper.gherkin.customizer.EventuallyConfiguration;
import org.popper.gherkin.listener.GherkinFileListener;
import org.popper.gherkin.listener.GherkinListener;
import org.popper.gherkin.table.Table;
import org.popper.gherkin.table.TableMapper;

/**
 * Main class responsible to execute step actions, do error handling and
 * delegating events to {@link GherkinListener}s
 *
 * @author Michael
 *
 */
public class GherkinRunner {
	private final Set<GherkinListener> listeners;

	private final File baseDir;
	
	private final ErrorHandler defaultErrorHandler;

	public GherkinRunner(Set<GherkinListener> listeners, ErrorHandler defaultErrorHandler, String baseDir) {
		this.listeners = listeners;
		this.defaultErrorHandler = defaultErrorHandler;
		this.baseDir = new File(baseDir);
		this.baseDir.mkdirs();
	}

	public void startClass(ExtensionContext context) {
		fireEvent(l -> l.storyStarted(context.getRequiredTestClass()));
		Narrative narrative = context.getRequiredTestClass().getAnnotation(Narrative.class);
		if (narrative != null) {
			listeners.forEach(l -> l.narrative(narrative));
		}
	}

	public void startMethod(Object testInstance, Method method) {
		fireEvent(l -> l.scenarioStarted(getScenarioTitle(testInstance, method), method));
	}

	public void executeAction(String type, String step, ExecutableWithExceptionAndTable<?> action,
			TableMapper<?> tableMapper, List<Customizer> customizers) {
		ErrorHandler errorHandler = getCustomizer(customizers, ErrorHandler.class);
		if (errorHandler == null) {
			errorHandler = defaultErrorHandler;
		}
		EventuallyConfiguration eventually = getCustomizer(customizers, EventuallyConfiguration.class);
		
		Optional<Table<Map<String, String>>> table;
		String stepWithoutTable;
		if (tableMapper != null) {
			table = Optional.ofNullable(tableMapper.createMapTable(step));
			stepWithoutTable = tableMapper.removeTable(step);
		} else {
			table = Optional.empty();
			stepWithoutTable = step;
		}

		fireEvent(l -> l.stepExecutionStarts(type, stepWithoutTable, table));
		try {
			Table<?> convertedTable = tableMapper != null ? tableMapper.createTable(step) : null;
			runAction(action, convertedTable, eventually);
			fireEvent(l -> l.stepExecutionSucceed(type, stepWithoutTable, table));
		} catch (Throwable th) {
			Throwable handledError = errorHandler.handleError(th);
			if (handledError == null) {
				fireEvent(l -> l.stepExecutionSucceed(type, stepWithoutTable, table));
			} else {
				fireEvent(l -> l.stepExecutionFailed(type, stepWithoutTable, table, th));
				handleCheckedExceptions(th);
			}
		}
	}
	
	private <C> C getCustomizer(List<Customizer> customizers, Class<C> customizerToFind) {
		for (Customizer customizer : customizers) {
			if (customizer != null && customizerToFind.isAssignableFrom(customizer.getClass())) {
				return customizerToFind.cast(customizer);
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private <E extends Throwable> void handleCheckedExceptions(Throwable th) throws E {
		if (th instanceof Error) {
			throw (Error) th;
		} else if (th instanceof RuntimeException) {
			throw (RuntimeException) th;
		} else {
			throw (E) th;
		}
	}


	public void endMethod(Object testInstance, Method method, Optional<Throwable> throwable) throws Exception {
		if (throwable.isPresent()) {
			fireEvent(l -> l.scenarioFailed(getScenarioTitle(testInstance, method), method, throwable.get()));
		} else {
			fireEvent(l -> l.scenarioSucceed(getScenarioTitle(testInstance, method), method));
		}
	}

	public void endClass(Class<?> storyClass) {
		fireEvent(l -> l.storyFinished(storyClass));
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
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
		public GherkinRunner createRunner(ExtensionContext context, 
				Set<GherkinListener> listeners, ErrorHandler errorHandler,String baseDir) {
			return new GherkinRunner(listeners, errorHandler, baseDir);
		}
	}
}
