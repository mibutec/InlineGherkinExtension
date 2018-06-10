# Description
InlineGherkin is a JUnit 5 extension allowing to write tests in a Gherkin like syntax

# Examples
Definition of a simple Scenario written in InlineGherkin

    @Narrative(inOrderTo="write gherkin like tests", asA="Test developer", iWantTo="use InlineGherkin")
    public class MyTest implements InlinGherkinMixin {
      @Scenario("Some succeeding scenario")
      public void succeedingScenario() {
        Given("Some given condition", () -> {
          // some code
        });

        When("Some when condition", () -> {
          // some code	
        });

        Then("Some assertion", () -> {
          // some code
        });
      }
    }

When working with more complexe data, it may be usefull to describe this data by a table
    
    @Scenario("Some scenario containing table")
    public void scenarioContainingTable() {
      Given("Some given condition", () -> {
        // some code
		});
		
      When("You may use a table to create structured data:"
        + "||Header1|Header2|Header3||"
        + "| value1 |value2 |value3  |", (table) -> {
					
              assertEquals("value1", table.getRow(0).get("Header1"));
              assertEquals("value2", table.getRow(0).get("Header2"));
              assertEquals("value3", table.getRow(0).get("Header3"));
      });
		
      Then("You may use a tables to fill pojos:"
        + "|| SomeInt | SomeBoolean | SomeString ||"
        + "|  1       | true        | someString |", MyPojo.class, (table) -> {

              assertEquals(1, table.getRow(0).getSomeInt());
              assertEquals(true, table.getRow(0).isSomeBoolean());
              assertEquals("someString", table.getRow(0).getSomeString());
      });
    }
    
When using lambdas we have to work around limitations of final fields
    
    @Scenario("Some scenario with local reference")
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
    
InlineGherkin comes with direct support to check eventual consistent states

    @Scenario("Some scenario with eventually clause")
    public void scenarioWithEventuellyClause(LocalReference<Integer> waitTime, LocalReference<Long> startTime) {
      Given("Some actions execution time takes a long time", () -> {
        waitTime.value = new Random().nextInt(1000) + 2000;
      });

      When("That action is triggered", () -> {
        startTime.value = System.currentTimeMillis();
      });

      Then("Eventually clause will take care to wait for the result", () -> {
        assertTrue(System.currentTimeMillis() > startTime.value + waitTime.value);
      }, eventually());
    }    

# Getting started

When you want to use Inline Gherkin Extension add the following Maven dependency to your pom.xml

    <dependency>
      <groupId>org.popperfw</groupId>
      <artifactId>gherkin</artifactId>
      <version>0.1</version>
    </dependency>
