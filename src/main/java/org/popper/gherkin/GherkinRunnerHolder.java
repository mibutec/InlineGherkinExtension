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
 * Describes a class having access to a {@link GherkinRunner}. If this interface
 * is implemented by a test, the set method will be called before tests are
 * executed.
 * 
 * @author Michael
 *
 */
public interface GherkinRunnerHolder {
    /**
     * Provides access to {@link GherkinRunner}
     */
    GherkinRunner getRunner();

    /**
     * Initializes this instance of a {@link GherkinRunnerHolder}. Will be called by
     * {@link GherkinExtension}
     */
    void setRunner(GherkinRunner runner);
}
