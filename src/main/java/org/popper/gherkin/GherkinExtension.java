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
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.popper.gherkin.listener.GherkinListener;
import org.popper.gherkin.listener.XmlGherkinListener;

/**
 * Glue class between JUnit 5 and InlineGherkin
 * 
 * @author Michael
 *
 */
public class GherkinExtension implements BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback,
		TestExecutionExceptionHandler, ParameterResolver {
	private static final Map<Class<?>, GherkinRunner> activeRunners = new ConcurrentHashMap<>();

	private final String baseDir;

	private final Set<Class<? extends GherkinListener>> listenersClasses;
	
	private final boolean catchCompleteOutput;
	
	@SuppressWarnings("unchecked")
	public GherkinExtension() {
		if (System.getProperty("gherkin.baseDir") == null) {
			baseDir = "./target/gherkin";
		} else {
			baseDir = System.getProperty("gherkin.baseDir");
		}
		
		if (System.getProperty("gherkin.listeners") == null) {
			listenersClasses = new HashSet<>(Arrays.asList(XmlGherkinListener.class));
		} else {
			listenersClasses = Arrays.stream(System.getProperty("gherkin.listeners").split(","))
					.map(className -> uncheck(() -> ((Class<GherkinListener>) Class.forName(className)))).collect(Collectors.toSet());
		}
		
		if (System.getProperty("gherkin.catchCompleteOutput") == null) {
			catchCompleteOutput = false;
		} else {
			catchCompleteOutput = Boolean.valueOf(System.getProperty("gherkin.catchCompleteOutput"));
		}
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		getOrCreateRunner(context.getRequiredTestClass()).startClass(context.getRequiredTestClass());
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		getOrCreateRunner(context.getRequiredTestClass()).startMethod(context.getRequiredTestMethod());
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		getOrCreateRunner(context.getRequiredTestClass()).endMethod(context.getRequiredTestMethod(),
				context.getExecutionException());
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		getOrCreateRunner(context.getRequiredTestClass()).endClass(context.getRequiredTestClass());
	}

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
		if (throwable instanceof StepFailedException) {
			throw throwable.getCause();
		}

		throw throwable;
	}
	
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return parameterContext.getParameter().getType().equals(LocalReference.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return new LocalReference<>();
	}

	synchronized GherkinRunner getOrCreateRunner(Class<?> testClass) {
		GherkinRunner runner = activeRunners.get(testClass);
		if (runner == null) {
			runner = new GherkinRunner(catchCompleteOutput, listeners(), baseDir);
			activeRunners.put(testClass, runner);
		}

		return runner;
	}

	synchronized static GherkinRunner getRunner(Class<?> testClass) {
		GherkinRunner runner = activeRunners.get(testClass);
		if (runner == null) {
			throw new IllegalStateException("no runner configured for: " + testClass.getSimpleName());
		}

		return runner;
	}

	@SuppressWarnings("deprecation")
	private Set<GherkinListener> listeners() {
		return listenersClasses.stream().map(c -> uncheck(() -> c.newInstance())).collect(Collectors.toSet());
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
