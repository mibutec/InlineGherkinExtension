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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Random;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Narrative(inOrderTo = "write gherkin like tests", asA = "Test developer", iWantTo = "use InlineGherkin")
@GherkinConfiguration(catchCompleteOutput = true)
public class GherkinTestWithMixin implements Gherkin {
    @Test
    @Scenario("Some succeeding scenario")
    @DisplayName("Some succeeding scenario")
    public void succeedingScenario() {
        Given("Some given condition", () -> {

        });

        When("Some when condition", () -> {

        });
        Then("Some assertion succeeds", () -> {

        });
    }

    @Test
    @Disabled
    @Scenario("Some failing scenario")
    @DisplayName("Some failing scenario")
    public void failingScenario() {
        Given("Some given condition", () -> {

        });

        When("Some when condition", () -> {
            throw new Exception("TestException");
        });

        Then("Some assertion fails", () -> {

        });
    }

    @Test
    @Scenario("Some scenario containing table")
    @DisplayName("Some scenario containing table")
    public void scenarioContainingTable() {
        Given("Some given condition", () -> {

        });

        When("You may use a table to create structured data:" + "|Header1|Header2|Header3|"
                + "|value1 |value2 |value3 |", (table) -> {

                    assertEquals("value1", table.getRow(0).get("Header1"));
                    assertEquals("value2", table.getRow(0).get("Header2"));
                    assertEquals("value3", table.getRow(0).get("Header3"));
                });

        Then("You may use a tables to fill pojos:" + "| SomeInt | SomeBoolean | SomeString |"
                + "|  1      | true        | someString |" + "|  7      | false       |            |",
                mapTo(MyPojo.class), (table) -> {
                    assertEquals(1, table.getRow(0).getSomeInt());
                    assertEquals(true, table.getRow(0).isSomeBoolean());
                    assertEquals("someString", table.getRow(0).getSomeString());

                    assertEquals(7, table.getRow(1).getSomeInt());
                    assertEquals(false, table.getRow(1).isSomeBoolean());
                    assertEquals("", table.getRow(1).getSomeString());
                });

        Then("You may use name overrides to decouple Headers from Pojo field names:"
                + "| my business int expression | my business boolean expression | my business string expression |"
                + "| 23                         | true                           | for your interest             |",
                mapTo(MyPojo.class).mapHeader("my business int expression", "someInt")
                        .mapHeader("my business boolean expression", "someBoolean")
                        .mapHeader("my business string expression", "someString"),
                (table) -> {

                    assertEquals(23, table.getRow(0).getSomeInt());
                    assertEquals(true, table.getRow(0).isSomeBoolean());
                    assertEquals("for your interest", table.getRow(0).getSomeString());
                });

    }

    @Test
    @Scenario("Some scenario with local reference")
    @DisplayName("Some scenario with local reference")
    public void scenarioUsingLocalReference(LocalReference<String> stringHolder) {
        Given("A string 'Hello' exists", () -> {
            stringHolder.value = "Hello";
        });

        When("That string is extended by ', world'", () -> {
            stringHolder.value += ", world";
        });

        Then("The reuslt is 'Hello, world'", () -> {
            assertEquals("Hello, world", stringHolder.value);
        });
    }

    @Test
    @Scenario("Some scenario with eventually clause")
    @DisplayName("Some scenario with eventually clause")
    public void scenarioWithEventuellyClause(LocalReference<Integer> waitTime, LocalReference<Long> startTime) {
        Given("Some actions execution time takes a long time", () -> {
            waitTime.value = new Random().nextInt(1000) + 2000;
        });

        When("That action is triggered", () -> {
            startTime.value = System.currentTimeMillis();
        });

        Then("Eventually clause will take care to wait for the result", () -> {
            assertTrue(System.currentTimeMillis() > (startTime.value + waitTime.value));
        }, eventually());
    }

    @Test
    @Scenario("Eventually clause may also fail")
    @DisplayName("Eventually clause may also fail")
    @Disabled
    public void scenarioWithEventuellyClauseFails() {
        Then("Eventually clause will take care to wait for the result", () -> {
            fail("test error");
        }, eventually());
    }

    @SuppressWarnings("unused")
    private static class MyPojo {
        private int someInt;
        private boolean someBoolean;
        private String someString;

        public int getSomeInt() {
            return someInt;
        }

        public void setSomeInt(int someInt) {
            this.someInt = someInt;
        }

        public boolean isSomeBoolean() {
            return someBoolean;
        }

        public void setSomeBoolean(boolean someBoolean) {
            this.someBoolean = someBoolean;
        }

        public String getSomeString() {
            return someString;
        }

        public void setSomeString(String someString) {
            this.someString = someString;
        }
    }

    GherkinRunner runner;
    
	@Override
	public GherkinRunner getRunner() {
		return runner;
	}

	@Override
	public void setRunner(GherkinRunner runner) {
		this.runner = runner;
	}
}
