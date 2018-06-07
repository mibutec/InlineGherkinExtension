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

import static org.popper.gherkin.GherkinMixin.ActionType.GIVEN;
import static org.popper.gherkin.GherkinMixin.ActionType.THEN;
import static org.popper.gherkin.GherkinMixin.ActionType.WHEN;

import java.util.Map;

import org.junit.jupiter.api.extension.ExtendWith;
import org.popper.gherkin.table.Table;

/**
 * Mixin to be added to a test providing the syntax methods to be used when writing tests
 * 
 * @author Michael
 *
 */
@ExtendWith(GherkinExtension.class)
public interface GherkinMixin {
	default void Given(String step, ExecutableWithException action) {
		GherkinExtension.getRunner(getClass()).executeAction(GIVEN, step, (table) -> action.run(), null, null);
	}
	
	default<T> void Given(String step, Class<T> tableType, ExecutableWithExceptionAndTable<T> action) {
		GherkinExtension.getRunner(getClass()).executeAction(GIVEN, step, action, tableType, null);
	}
	
	default void Given(String step, ExecutableWithExceptionAndTable<Map<String, String>> action) {
		GherkinExtension.getRunner(getClass()).executeAction(GIVEN, step, action, Map.class, null);
	}
	
	default void When(String step, ExecutableWithException action) {
		GherkinExtension.getRunner(getClass()).executeAction(WHEN, step, (table) -> action.run(), null, null);
	}
	
	default<T> void When(String step, Class<T> tableType, ExecutableWithExceptionAndTable<T> action) {
		GherkinExtension.getRunner(getClass()).executeAction(WHEN, step, action, tableType, null);
	}
	
	default void When(String step, ExecutableWithExceptionAndTable<Map<String, String>> action) {
		GherkinExtension.getRunner(getClass()).executeAction(WHEN, step, action, Map.class, null);
	}
	
	default void Then(String step, ExecutableWithException action) {
		GherkinExtension.getRunner(getClass()).executeAction(THEN, step, (table) -> action.run(), null, null);
	}
	
	default<T> void Then(String step, Class<T> tableType, ExecutableWithExceptionAndTable<T> action) {
		GherkinExtension.getRunner(getClass()).executeAction(THEN, step, action, tableType, null);
	}
	
	default void Then(String step, ExecutableWithExceptionAndTable<Map<String, String>> action) {
		GherkinExtension.getRunner(getClass()).executeAction(THEN, step, action, Map.class, null);
	}
	
	default void Then(String step, ExecutableWithException action, EventuallyConfiguration eventuelly) {
		GherkinExtension.getRunner(getClass()).executeAction(THEN, step, (table) -> action.run(), null, eventuelly);
	}
	
	default<T> void Then(String step, Class<T> tableType, ExecutableWithExceptionAndTable<T> action, EventuallyConfiguration eventuelly) {
		GherkinExtension.getRunner(getClass()).executeAction(THEN, step, action, tableType, eventuelly);
	}
	
	default void Then(String step, ExecutableWithExceptionAndTable<Map<String, String>> action, EventuallyConfiguration eventuelly) {
		GherkinExtension.getRunner(getClass()).executeAction(THEN, step, action, Map.class, eventuelly);
	}
	
	default EventuallyConfiguration eventually() {
		return new EventuallyConfiguration();
	}
	
	public static interface ExecutableWithException {
		public void run() throws Exception;
	}
	
	public static interface ExecutableWithExceptionAndTable<T> {
		public void run(Table<T> table) throws Exception;
	}
	
	public static enum ActionType {
		GIVEN, WHEN, THEN
	}
}
