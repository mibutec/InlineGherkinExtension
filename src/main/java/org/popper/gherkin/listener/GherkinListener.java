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
package org.popper.gherkin.listener;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import org.popper.gherkin.GherkinRunner;
import org.popper.gherkin.Narrative;
import org.popper.gherkin.table.Table;

/**
 * Listener to register for the events created by {@link GherkinRunner} when
 * executing tests.
 * 
 * In case you are using parallel test execution your listener must be
 * Thread-safe.
 *
 * @author Michael
 *
 */
public interface GherkinListener {
    default void storyStarted(Class<?> storyClass) {

    }

    default void narrative(Narrative narrative) {

    }

    default void scenarioStarted(String scenarioTitle, Method method) {

    }

    default void stepExecutionStarts(String type, String step, Optional<Table<Map<String, String>>> table) {

    }

    default void stepExecutionFailed(String type, String step, Optional<Table<Map<String, String>>> table,
            Throwable throwable) {

    }

    default void stepExecutionSucceed(String type, String step, Optional<Table<Map<String, String>>> table) {

    }

    default void stepExecutionSkipped(String type, String step, Optional<Table<Map<String, String>>> table) {

    }

    default void scenarioFailed(String scenarioTitle, Method method, Throwable throwable) {

    }

    default void scenarioSucceed(String scenarioTitle, Method method) {

    }

    default void storyFinished(Class<?> storyClass) {

    }
}
