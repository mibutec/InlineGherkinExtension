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
package org.popper.gherkin.customizer;

/**
 * {@link ErrorHandler} to be used to store exception that happened during test execution
 * @author Michael
 *
 */
public class ErrorStore implements ErrorHandler {
	private Throwable lastCaughtThrowable;
	
	@Override
	public Throwable handleError(Throwable th) {
		lastCaughtThrowable = th;
		return null;
	}

	public Throwable getLastCaughtThrowable() {
		return lastCaughtThrowable;
	}
}
