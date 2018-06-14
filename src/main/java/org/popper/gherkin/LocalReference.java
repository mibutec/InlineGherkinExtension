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

/**
 * Convenience class helping getting around java limitions.
 * Problem: When working with lambdas in methods, all used variables are made automatically final
 * Solution: Use a {@link LocalReference} as method variable. The inner value won't get final
 *
 * @author Michael
 *
 */
public final class LocalReference<T> {
    public T value;

    public LocalReference() {
        this(null);
    }

    public LocalReference(T value) {
        this.value = value;
    }
}
