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

import java.util.Map;

import org.junit.jupiter.api.extension.ExtendWith;
import org.popper.gherkin.table.Table;
import org.popper.gherkin.table.TableMapper;

/**
 * Mixin to be added to a test providing the syntax methods to be used when writing tests
 *
 * @author Michael
 *
 */
@ExtendWith(GherkinExtension.class)
public interface GherkinMixin {
	
	
    default void Given(String step, ExecutableWithException action) {
        callRunner("Given", step, (table) -> action.run(), null, null);
    }

    default <T> void Given(String step, TableMapper<T> tableMapper, ExecutableWithExceptionAndTable<T> action) {
        callRunner("Given", step, action, tableMapper, null);
    }

    default void Given(String step, ExecutableWithExceptionAndTable<Map<String, String>> action) {
        callRunner("Given", step, action, mapTo(Map.class), null);
    }

    default void When(String step, ExecutableWithException action) {
        callRunner("When", step, (table) -> action.run(), null, null);
    }

    default <T> void When(String step, TableMapper<T> tableMapper, ExecutableWithExceptionAndTable<T> action) {
        callRunner("When", step, action, tableMapper, null);
    }

    default void When(String step, ExecutableWithExceptionAndTable<Map<String, String>> action) {
        callRunner("When", step, action, mapTo(Map.class), null);
    }

    default void Then(String step, ExecutableWithException action) {
        callRunner("Then", step, (table) -> action.run(), null, null);
    }

    default <T> void Then(String step, TableMapper<T> tableMapper, ExecutableWithExceptionAndTable<T> action) {
        callRunner("Then", step, action, tableMapper, null);
    }

    default void Then(String step, ExecutableWithExceptionAndTable<Map<String, String>> action) {
        callRunner("Then", step, action, mapTo(Map.class), null);
    }

    default void Then(String step, ExecutableWithException action, EventuallyConfiguration eventuelly) {
        callRunner("Then", step, (table) -> action.run(), null, eventuelly);
    }

    default <T> void Then(String step, TableMapper<T> tableMapper, ExecutableWithExceptionAndTable<T> action,
            EventuallyConfiguration eventuelly) {
        callRunner("Then", step, action, tableMapper, eventuelly);
    }

    default void Then(String step, ExecutableWithExceptionAndTable<Map<String, String>> action,
            EventuallyConfiguration eventuelly) {
        callRunner("Then", step, action, mapTo(Map.class), eventuelly);
    }

    default void callRunner(String type, String step, ExecutableWithExceptionAndTable<?> action,
            TableMapper<?> tableMapper, EventuallyConfiguration eventually) {
        GherkinExtension.getRunner(getClass()).executeAction(type, step, action, tableMapper, eventually);
    }

    default <T> TableMapper<T> mapTo(Class<T> targetType) {
        return new TableMapper<>(targetType);
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
}
