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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.platform.commons.util.AnnotationUtils;
import org.popper.gherkin.GherkinRunner.DefaultRunnerFactory;
import org.popper.gherkin.customizer.DefaultErrorHandler;
import org.popper.gherkin.customizer.ErrorStore;
import org.popper.gherkin.listener.GherkinListener;
import org.popper.gherkin.listener.XmlGherkinListener;

/**
 * Glue class between JUnit 5 and InlineGherkin
 *
 * @author Michael
 *
 */
public class GherkinExtension
        implements BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback, ParameterResolver, TestInstancePostProcessor {
    private static final Map<Class<?>, GherkinRunner> activeRunners = new ConcurrentHashMap<>();

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
    	try {
    		getOrCreateRunner(context).startClass(context);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
    	try {
        getOrCreateRunner(context).startMethod(context.getRequiredTestInstance(), context.getRequiredTestMethod());
       	} catch (Exception e) {
    		e.printStackTrace();
    	}
     }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        getOrCreateRunner(context).endMethod(context.getRequiredTestInstance(), context.getRequiredTestMethod(),
                context.getExecutionException());
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        getOrCreateRunner(context).endClass(context.getRequiredTestClass());
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
    	Class<?> expectedType = parameterContext.getParameter().getType();
        return expectedType.equals(LocalReference.class) || expectedType.equals(Gherkin.class) || expectedType.equals(ErrorStore.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
    	Class<?> expectedType = parameterContext.getParameter().getType();
    	
    	if (expectedType.equals(LocalReference.class)) {
    		return new LocalReference<>();
    	} else if (expectedType.equals(ErrorStore.class)) {
    		return new ErrorStore();
    	} else {
    		GherkinImpl impl = new GherkinImpl();
    		impl.setRunner(getOrCreateRunner(extensionContext));
   			return impl;
    	}
    }
    
	@Override
	public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
		if (testInstance instanceof GherkinRunnerHolder) {
			((GherkinRunnerHolder) testInstance).setRunner(getOrCreateRunner(context));
		}
	}

    synchronized GherkinRunner getOrCreateRunner(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        GherkinRunner runner = activeRunners.get(testClass);
        if (runner == null) {
            GherkinConfiguration configAnnotation = AnnotationUtils
                    .findAnnotation(testClass, GherkinConfiguration.class).orElse(null);
            runner = runnerFactory(configAnnotation).createRunner(context, 
                    listeners(configAnnotation), new DefaultErrorHandler(), baseDir(configAnnotation));
            activeRunners.put(testClass, runner);
        }

        return runner;
    }

    private String baseDir(GherkinConfiguration configAnnotation) {
        if (System.getProperty("gherkin.baseDir") != null) {
            return System.getProperty("gherkin.baseDir");
        } else if (configAnnotation != null) {
            return configAnnotation.baseDir();
        } else {
            return "./target/gherkin";
        }
    }

    private RunnerFactory runnerFactory(GherkinConfiguration configAnnotation) {
        try {
            if (System.getProperty("gherkin.runnerFactory") != null) {
                return (RunnerFactory) Class.forName(System.getProperty("gherkin.runnerFactory")).newInstance();
            } else if (configAnnotation != null) {
                return configAnnotation.runnerFactory().newInstance();
            } else {
                return new DefaultRunnerFactory();
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Set<GherkinListener> listeners(GherkinConfiguration configAnnotation) {
        Set<Class<? extends GherkinListener>> listenerClasses;
        if (System.getProperty("gherkin.listeners") != null) {
            listenerClasses = Arrays.stream(System.getProperty("gherkin.listeners").split(","))
                    .map(className -> uncheck(() -> ((Class<GherkinListener>) Class.forName(className))))
                    .collect(Collectors.toSet());
        } else if (configAnnotation != null) {
            listenerClasses = new HashSet<>(Arrays.asList(configAnnotation.listeners()));
        } else {
            return new HashSet<>(Arrays.asList(new XmlGherkinListener()));
        }

        return listenerClasses.stream().map(c -> uncheck(() -> c.newInstance())).collect(Collectors.toSet());
    }

    public synchronized static GherkinRunner getRunner(Class<?> testClass) {
        GherkinRunner runner = activeRunners.get(testClass);
        if (runner == null) {
            throw new IllegalStateException("no runner configured for: " + testClass.getSimpleName());
        }

        return runner;
    }

    private <T> T uncheck(Callable<T> callable) {
        try {
            return callable.call();
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
