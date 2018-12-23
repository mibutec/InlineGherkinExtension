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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.popper.gherkin.customizer.Customizer;
import org.popper.gherkin.customizer.EventuallyConfiguration;
import org.popper.gherkin.table.Table;
import org.popper.gherkin.table.TableMapper;

/**
 * Mixin to be added to a test providing the syntax methods to be used when writing tests
 *
 * @author Michael
 *
 */
public interface Gherkin extends GherkinRunnerHolder {
    default Gherkin Given(String step, ExecutableWithException action, Customizer... customizers) {
        callRunner("Given", step, (table) -> action.run(), null, Arrays.asList(customizers));
        return this;
    }

    default Gherkin Given(String step, ExecutableWithExceptionAndTable<Map<String, String>> action, Customizer... customizers) {
        callRunner("Given", step, action, mapTo(Map.class), Arrays.asList(customizers));
        return this;
    }

    default <T> Gherkin Given(String step, TableMapper<T> mapper, ExecutableWithExceptionAndTable<T> action, Customizer... customizers) {
        callRunner("Given", step, action, mapper, Arrays.asList(customizers));
        return this;
    }

    default Gherkin When(String step, ExecutableWithException action, Customizer... customizers) {
        callRunner("When", step, (table) -> action.run(), null, Arrays.asList(customizers));
        return this;
    }

    default Gherkin When(String step, ExecutableWithExceptionAndTable<Map<String, String>> action, Customizer... customizers) {
        callRunner("When", step, action, mapTo(Map.class), Arrays.asList(customizers));
        return this;
    }
    
    default <T> Gherkin When(String step, TableMapper<T> tableMapper, ExecutableWithExceptionAndTable<T> action, Customizer... customizers) {
        callRunner("When", step, action, tableMapper, Arrays.asList(customizers));
        return this;
    }

    default Gherkin Then(String step, ExecutableWithException action, Customizer... customizers) {
        callRunner("Then", step, (table) -> action.run(), null, Arrays.asList(customizers));
        return this;
    }

    default Gherkin Then(String step, ExecutableWithExceptionAndTable<Map<String, String>> action, Customizer... customizers) {
        callRunner("Then", step, action, mapTo(Map.class), Arrays.asList(customizers));
        return this;
    }
    
    default <T> Gherkin Then(String step, TableMapper<T> tableMapper, ExecutableWithExceptionAndTable<T> action, Customizer... customizers) {
        callRunner("Then", step, action, tableMapper, Arrays.asList(customizers));
        return this;
    }

    default <T> TableMapper<T> mapTo(Class<T> targetType) {
        return new TableMapper<>(targetType);
    }

    default EventuallyConfiguration eventually() {
        return new EventuallyConfiguration();
    }

    default void callRunner(String type, String step, ExecutableWithExceptionAndTable<?> action,
            TableMapper<?> tableMapper, List<Customizer> customizers) {
    	getRunner().executeAction(type, step, action, tableMapper, customizers);
    }
    
    public static interface ExecutableWithException {
        public void run() throws Exception;
    }

    public static interface ExecutableWithExceptionAndTable<T> {
        public void run(Table<T> table) throws Exception;
    }
    
}
