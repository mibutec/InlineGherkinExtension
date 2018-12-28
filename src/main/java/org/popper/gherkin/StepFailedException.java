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

/**
 * Exception thrown when a step failed execution
 * 
 * @author Michael
 *
 */
public class StepFailedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String failedStep;

    public StepFailedException(String failedStep, Throwable throwable) {
        super("Step failed: " + failedStep, throwable);
        this.failedStep = failedStep;
    }

    public String getFailedStep() {
        return failedStep;
    }
}
