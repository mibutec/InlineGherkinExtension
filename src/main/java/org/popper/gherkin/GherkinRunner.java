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
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.popper.gherkin.GherkinMixin.ActionType;
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
	
	private final boolean catchCompleteOutput;

	private Throwable caughtException = null;
	
	public GherkinRunner(boolean catchCompleteOutput, Set<GherkinListener> listeners, String baseDir) {
		this.listeners = listeners;
		this.catchCompleteOutput = catchCompleteOutput;
		this.baseDir = new File(baseDir);
		this.baseDir.mkdirs();
	}
	
	public void startClass(Class<?> storyClass) {
		fireEvent(l -> l.storyStarted(storyClass));
		Narrative narrative = storyClass.getAnnotation(Narrative.class);
		if (narrative != null) {
			listeners.forEach(l -> l.narrative(narrative));
		}
	}
	
	public void startMethod(Method method) {
		fireEvent(l -> l.scenarioStarted(getScenarioTitle(method), method));
	}
	
	public void executeAction(ActionType type, String step, ExecutableWithExceptionAndTable<?> action, TableMapper<?> tableMapper, EventuallyConfiguration eventuall) {
		if (caughtException != null) {
			fireEvent(l -> l.stepExecutionSkipped(step));
		} else {
			fireEvent(l -> l.stepExecutionStarts(step));
			try {
				Table<?> table = null;
				if (tableMapper != null) {
					table = tableMapper.createTable(step);
				}
				runAction(action, table, eventuall);
				fireEvent(l -> l.stepExecutionSucceed(step));
			} catch (Throwable e) {
				fireEvent(l -> l.stepExecutionFailed(step, e));
				caughtException = e;
				if (!catchCompleteOutput) {
					throw new StepFailedException(step, e);
				}
			}
		}
	}
	
	public void endMethod(Method method, Optional<Throwable> throwable) {
		if (caughtException != null) {
			fireEvent(l -> l.scenarioFailed(getScenarioTitle(method), method, caughtException));
			caughtException = null;
		} else {
			fireEvent(l -> l.scenarioSucceed(getScenarioTitle(method), method));
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
	
	private void fireEvent(Consumer<GherkinListener> consumer) {
		listeners.forEach(consumer);
	}
	
	private String getScenarioTitle(Method method) {
		Scenario scenario = method.getAnnotation(Scenario.class);
		if (scenario != null) {
			return scenario.value();
		} else {
			return method.getName();
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void runAction(ExecutableWithExceptionAndTable<?> action, Table table, EventuallyConfiguration eventually) throws Exception {
		if (eventually == null) {
			action.run(table);
		} else {
			long start = System.currentTimeMillis();
			while (System.currentTimeMillis() - eventually.getTimeoutInMs() < start) {
				try {
					action.run(table);
					break;
				} catch (Throwable th) {
					Thread.sleep(eventually.getIntervalInMs());
				}
			}
		}
	}
}
