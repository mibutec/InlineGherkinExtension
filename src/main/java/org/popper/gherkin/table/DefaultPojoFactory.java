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
package org.popper.gherkin.table;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Default implementation of PojoFactory using reflections to instantiate pojos
 * 
 * @author Michael
 *
 */
public class DefaultPojoFactory<T> implements PojoFactory<T> {

	@Override
	public T createPojo(Class<T> pojoClass, Map<String, String> valuesAsMap) {
	        try {
				Constructor<T> constructor = pojoClass.getDeclaredConstructor();
				constructor.setAccessible(true);
				return constructor.newInstance();
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new IllegalStateException("error instantiating pojo of type " + pojoClass.getSimpleName());
			}
	}
}
