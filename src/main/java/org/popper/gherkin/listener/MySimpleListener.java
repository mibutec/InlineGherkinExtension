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
package org.popper.gherkin.listener;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.popper.gherkin.Narrative;
import org.popper.gherkin.table.Table;

/**
 * Simple test implementation of {@link GherkinListener}
 *
 * @author Michael
 *
 */
public class MySimpleListener implements GherkinListener {

    @Override
    public void storyStarted(ExtensionContext context, Class<?> storyClass) {
        System.out.println("storyStarted: " + storyClass.getSimpleName());
    }

    @Override
    public void narrative(ExtensionContext context, Narrative narrative) {
        System.out.println("narrative: As a " + narrative.asA() + " I want to " + narrative.iWantTo() + " in order to "
                + narrative.inOrderTo());
    }

    @Override
    public void scenarioStarted(ExtensionContext context, String scenarioTitle, Method method) {
        System.out.println("scenarioStarted: " + scenarioTitle);
    }

    @Override
    public void stepExecutionStarts(ExtensionContext context, String type, String step,
            Optional<Table<Map<String, String>>> table) {
        System.out.println("stepExecutionStarts: " + type + " " + step + ", " + table);
    }

    @Override
    public void stepExecutionFailed(ExtensionContext context, String type, String step,
            Optional<Table<Map<String, String>>> table, Throwable throwable) {
        System.out.println("stepExecutionFailed: " + type + " " + step + ", " + table);
        throwable.printStackTrace(System.out);
    }

    @Override
    public void stepExecutionSucceed(ExtensionContext context, String type, String step,
            Optional<Table<Map<String, String>>> table) {
        System.out.println("stepExecutionSucceed: " + type + " " + step + ", " + table);
    }

    @Override
    public void stepExecutionSkipped(ExtensionContext context, String type, String step,
            Optional<Table<Map<String, String>>> table) {
        System.out.println("stepExecutionSkipped: " + type + " " + step + ", " + table);
    }

    @Override
    public void storyFinished(ExtensionContext context, Class<?> storyClass) {
        System.out.println("storyFinished: " + storyClass.getSimpleName());
    }

    @Override
    public void scenarioFailed(ExtensionContext context, String scenarioTitle, Method method, Throwable throwable) {
        System.out.println("scenarioFailed: " + scenarioTitle);
        throwable.printStackTrace(System.out);
    }

    @Override
    public void scenarioSucceed(ExtensionContext context, String scenarioTitle, Method method) {
        System.out.println("scenarioSucceed: " + scenarioTitle);
    }
}
