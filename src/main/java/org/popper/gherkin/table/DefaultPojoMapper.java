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
package org.popper.gherkin.table;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Implementation of {@link PojoMapper} mapping each entry from map to a setter method or field of a given target class
 *
 * @author Michael
 */
public class DefaultPojoMapper<T> implements PojoMapper<T> {

    @Override
    public T mapToPojo(Map<String, String> map, T target) {
        try {
            for (Entry<String, String> entry : map.entrySet()) {
                if (!setFieldBySetter(target, entry.getKey(), entry.getValue())) {
                    if (!setField(target, entry.getKey(), entry.getValue())) {
                        throw new IllegalStateException("couldn't find any field or setter named " + entry.getKey()
                                + " in " + target.getClass().getSimpleName());
                    }
                }
            }

            return target;
        } catch (SecurityException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalStateException("could not map " + map + " to " + target.getClass().getSimpleName(), e);
        }
    }

    protected boolean setFieldBySetter(T target, String name, String value)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        for (Method m : target.getClass().getMethods()) {
            if ((m.getName().equalsIgnoreCase(name) || m.getName().equalsIgnoreCase("set" + name))
                    && (m.getParameterTypes().length == 1)) {
                m.setAccessible(true);
                m.invoke(target, stringToType(value, m.getParameterTypes()[0]));
                return true;
            }
        }

        return false;
    }

    protected boolean setField(T target, String name, String value)
            throws IllegalArgumentException, IllegalAccessException {
        Class<?> actualClass = target.getClass();

        while (actualClass != Object.class) {
            Field[] fields = actualClass.getDeclaredFields();
            for (Field f : fields) {
                if (f.getName().equalsIgnoreCase(name.replaceAll("\\s+", ""))) {
                    f.setAccessible(true);
                    f.set(target, stringToType(value, f.getType()));
                    return true;
                }
            }
            actualClass = actualClass.getSuperclass();
        }

        return false;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Object stringToType(String str, Class<?> targetType) {
        if (str == null) {
            return null;
        }
        if ((targetType == int.class) || (targetType == Integer.class)) {
            return Integer.valueOf(str);
        }
        if ((targetType == boolean.class) || (targetType == Boolean.class)) {
            return Boolean.valueOf(str);
        }
        if ((targetType == byte.class) || (targetType == Byte.class)) {
            return Byte.valueOf(str);
        }
        if ((targetType == char.class) || (targetType == Character.class)) {
            return Character.valueOf(str.charAt(0));
        }
        if ((targetType == short.class) || (targetType == Short.class)) {
            return Short.valueOf(str);
        }
        if ((targetType == long.class) || (targetType == Long.class)) {
            return Long.valueOf(str);
        }
        if ((targetType == float.class) || (targetType == Float.class)) {
            return Float.valueOf(str);
        }
        if ((targetType == double.class) || (targetType == Double.class)) {
            return Double.valueOf(str);
        }
        if (targetType == String.class) {
            return str;
        }
        if (targetType.isEnum()) {
            return Enum.valueOf((Class) targetType, str);
        }

        throw new IllegalStateException("unsupported type: " + targetType);
    }
}
