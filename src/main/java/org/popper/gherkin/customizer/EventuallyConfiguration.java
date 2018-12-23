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
package org.popper.gherkin.customizer;

/**
 * Timeout configurations when using eventually()
 *
 * @author Michael
 *
 */
public class EventuallyConfiguration implements Customizer {
    private int timeoutInMs;
    private int intervalInMs;

    public EventuallyConfiguration() {
        timeoutInMs = 5000;
        intervalInMs = 100;
    }

    public int getTimeoutInMs() {
        return timeoutInMs;
    }

    public EventuallyConfiguration timeoutInMs(int timeoutInMs) {
        this.timeoutInMs = timeoutInMs;
        return this;
    }

    public EventuallyConfiguration timeoutInS(int timeoutInS) {
        timeoutInMs = timeoutInS * 1000;
        return this;
    }

    public int getIntervalInMs() {
        return intervalInMs;
    }

    public EventuallyConfiguration intervalInMs(int intervalInMs) {
        this.intervalInMs = intervalInMs;
        return this;
    }

    public EventuallyConfiguration intervalInS(int intervalInS) {
        intervalInMs = intervalInS * 1000;
        return this;
    }
}
