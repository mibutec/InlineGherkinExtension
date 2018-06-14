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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.popper.gherkin.listener.GherkinListener;
import org.popper.gherkin.listener.XmlGherkinListener;

/**
 * Annotation to configure behavior of {@link GherkinExtension}. May be used on class level to configure
 * the whole test class or on method level to configure one test
 *
 * @author Michael
 *
 */
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@Inherited
public @interface GherkinConfiguration {
    String baseDir() default "./target/gherkin";

    Class<? extends GherkinListener>[] listeners() default {XmlGherkinListener.class};

    boolean catchCompleteOutput() default false;
}
