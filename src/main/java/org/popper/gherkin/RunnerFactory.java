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

import java.util.Set;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.popper.gherkin.customizer.ErrorHandler;
import org.popper.gherkin.listener.GherkinListener;

/**
 * Factory to create instances of {@link GherkinRunner}
 *
 * @author Michael
 */
public interface RunnerFactory {
    GherkinRunner createRunner(ExtensionContext context, Set<GherkinListener> listeners, ErrorHandler errorHandler,
            String baseDir);
}
