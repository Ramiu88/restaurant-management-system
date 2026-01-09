# SALADIN - JUnit Testing Guide

## What is JUnit?

**JUnit** is a testing framework for Java that helps you verify your code works correctly.

### Think of it like this:
- You write your code (FinanceManager, StockManager, etc.)
- You write tests that CHECK if your code works
- JUnit runs all tests automatically
- JUnit tells you: ✅ PASS or ❌ FAIL

---

## Why Test?

```text
WITHOUT TESTS:
1. Write code
2. Run program manually
3. Check output manually
4. 😰 Did I break something?

WITH TESTS:
1. Write code
2. Write tests
3. Run tests automatically
4. ✅ All tests pass! Code works!
```

---

## JUnit Basics

### 1. Test Class Structure

```java
import org.junit.Test;
import static org.junit.Assert.*;

public class MyTest {

    @Test  // This marks a test method
    public void testSomething() {
        // Arrange: Set up test data
        int x = 5;
        int y = 3;

        // Act: Execute the code
        int result = x + y;

        // Assert: Check if result is correct
        assertEquals(8, result);  // Expected: 8, Actual: result
    }
}
```

### 2. Common Assertions

```java
// Check if equal
assertEquals(expected, actual);
assertEquals(5, 2 + 3);  // ✅ PASS

// Check if true/false
assertTrue(condition);
assertTrue(5 > 3);  // ✅ PASS

assertFalse(condition);
assertFalse(5 < 3);  // ✅ PASS

// Check if null
assertNull(object);
assertNotNull(object);

// Check if same object
assertSame(obj1, obj2);

// Custom message on failure
assertEquals("Addition failed!", 5, 2 + 3);
```

### 3. Test Lifecycle

```java
import org.junit.*;

public class MyTest {

    @BeforeClass  // Run ONCE before all tests
    public static void setUpClass() {
        System.out.println("Starting tests...");
    }

    @Before  // Run BEFORE each test
    public void setUp() {
        System.out.println("Setting up test...");
    }

    @Test
    public void test1() {
        System.out.println("Running test 1");
    }

    @Test
    public void test2() {
        System.out.println("Running test 2");
    }

    @After  // Run AFTER each test
    public void tearDown() {
        System.out.println("Cleaning up...");
    }

    @AfterClass  // Run ONCE after all tests
    public static void tearDownClass() {
        System.out.println("All tests done!");
    }
}
```

**Output:**
```
Starting tests...
Setting up test...
Running test 1
Cleaning up...
Setting up test...
Running test 2
Cleaning up...
All tests done!
```

---

## Testing Concurrent Code

Testing threads is tricky! Here are strategies:

### Strategy 1: Test Single-Threaded Behavior First

```java
@Test
public void testFinanceManager_SingleThread() {
    FinanceManager finance = new FinanceManager();

    finance.processPayment(10.0);
    finance.processPayment(20.0);
    finance.processPayment(30.0);

    assertEquals(60.0, finance.getTotalRevenue(), 0.01);
    assertEquals(3, finance.getCustomersServed());
}
```

### Strategy 2: Test Multi-Threaded Behavior

```java
@Test
public void testFinanceManager_MultipleThreads() throws InterruptedException {
    FinanceManager finance = new FinanceManager();

    // Create 10 threads
    Thread[] threads = new Thread[10];
    for (int i = 0; i < 10; i++) {
        threads[i] = new Thread(() -> {
            for (int j = 0; j < 100; j++) {
                finance.processPayment(10.0);
            }
        });
    }

    // Start all threads
    for (Thread t : threads) {
        t.start();
    }

    // Wait for all to finish
    for (Thread t : threads) {
        t.join();  // Wait for thread to complete
    }

    // Check result
    // 10 threads × 100 payments × $10 = $10,000
    assertEquals(10000.0, finance.getTotalRevenue(), 0.01);
    assertEquals(1000, finance.getCustomersServed());
}
```

### Strategy 3: Test Wait/Notify

```java
@Test(timeout = 5000)  // Fail if takes more than 5 seconds
public void testStockManager_WaitNotify() throws InterruptedException {
    StockManager stock = new StockManager();

    // Start background thread
    StockManagerThread stockThread = new StockManagerThread();
    Thread daemon = new Thread(stockThread);
    daemon.setDaemon(true);
    daemon.start();

    // Consume lots of stock
    Map<String, Integer> recipe = new HashMap<>();
    recipe.put("Cheese", 5);

    int successCount = 0;
    for (int i = 0; i < 20; i++) {
        if (stock.consumeIngredients(recipe)) {
            successCount++;
        }
        Thread.sleep(100);
    }

    // Should succeed multiple times (with replenishment)
    assertTrue(successCount > 10);
}
```

---

## Your Test Suite

I'll create tests for all your classes:

### Test Files Structure
```
src/test/java/ma/emsi/restaurant/
├── managers/
│   ├── FinanceManagerTest.java
│   └── StockManagerTest.java
└── actors/
    ├── CashierTest.java
    └── StockManagerThreadTest.java
```

---

## Test Categories

### 1. Unit Tests
Test individual methods in isolation:
```java
@Test
public void testProcessPayment_AddsToRevenue() {
    FinanceManager finance = new FinanceManager();
    finance.processPayment(25.50);
    assertEquals(25.50, finance.getTotalRevenue(), 0.01);
}
```

### 2. Integration Tests
Test how classes work together:
```java
@Test
public void testCook_ConsumesStock() {
    StockManager stock = Restaurant.getInstance().getStockManager();
    // Simulate cook behavior
    Map<String, Integer> recipe = new HashMap<>();
    recipe.put("Cheese", 2);
    assertTrue(stock.consumeIngredients(recipe));
}
```

### 3. Concurrency Tests
Test thread safety:
```java
@Test
public void testFinanceManager_RaceCondition() {
    // Test that synchronized prevents money loss
}
```

---

## Running Tests

### Command Line (Maven)
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=FinanceManagerTest

# Run specific test method
mvn test -Dtest=FinanceManagerTest#testProcessPayment
```

### Eclipse
1. Right-click on test class
2. Select "Run As" → "JUnit Test"
3. View results in JUnit panel

### IntelliJ IDEA
1. Right-click on test class
2. Select "Run 'TestClassName'"
3. View results in Run panel

---

## Test Best Practices

### 1. Test One Thing at a Time
```java
// GOOD - Tests one specific behavior
@Test
public void testProcessPayment_IncrementsCustomerCount() {
    FinanceManager finance = new FinanceManager();
    finance.processPayment(10.0);
    assertEquals(1, finance.getCustomersServed());
}

// BAD - Tests too many things
@Test
public void testEverything() {
    // Tests revenue, customers, statistics all in one
}
```

### 2. Use Descriptive Names
```java
// GOOD
@Test
public void testConsumeIngredients_ReturnsFalse_WhenStockInsufficient()

// BAD
@Test
public void test1()
```

### 3. Follow AAA Pattern
```java
@Test
public void testExample() {
    // Arrange - Set up test data
    FinanceManager finance = new FinanceManager();
    double payment = 25.50;

    // Act - Execute the code
    finance.processPayment(payment);

    // Assert - Verify results
    assertEquals(25.50, finance.getTotalRevenue(), 0.01);
}
```

### 4. Clean Up After Tests
```java
@After
public void tearDown() {
    // Clean up resources
    // Stop threads
    // Reset state
}
```

---

## Common Testing Mistakes

### Mistake 1: Not Testing Edge Cases
```java
// Test normal case
@Test
public void testConsumeIngredients_NormalCase() {
    // Test with available stock
}

// Also test edge cases!
@Test
public void testConsumeIngredients_EmptyStock() {
    // Test when stock is 0
}

@Test
public void testConsumeIngredients_ExactAmount() {
    // Test when stock exactly equals needed
}
```

### Mistake 2: Tests Depend on Each Other
```java
// BAD - Test order matters
@Test
public void test1() {
    finance.processPayment(10.0);  // Adds $10
}

@Test
public void test2() {
    // Expects $10 from test1 - WRONG!
    assertEquals(10.0, finance.getTotalRevenue());
}

// GOOD - Each test is independent
@Test
public void test1() {
    FinanceManager finance = new FinanceManager();  // New instance
    finance.processPayment(10.0);
    assertEquals(10.0, finance.getTotalRevenue());
}
```

### Mistake 3: Not Using Timeouts for Concurrent Tests
```java
// BAD - Could hang forever
@Test
public void testWait() throws InterruptedException {
    // If notify() never happens, test hangs forever!
}

// GOOD - Timeout prevents hanging
@Test(timeout = 5000)  // Fail after 5 seconds
public void testWait() throws InterruptedException {
    // Test with timeout
}
```

---

## Example: Complete Test Class

```java
package ma.emsi.restaurant.managers;

import org.junit.*;
import static org.junit.Assert.*;

public class FinanceManagerTest {

    private FinanceManager finance;

    @Before  // Run before each test
    public void setUp() {
        finance = new FinanceManager();
    }

    @Test
    public void testProcessPayment_AddsToRevenue() {
        finance.processPayment(25.50);
        assertEquals(25.50, finance.getTotalRevenue(), 0.01);
    }

    @Test
    public void testProcessPayment_IncrementsCustomerCount() {
        finance.processPayment(10.0);
        finance.processPayment(20.0);
        assertEquals(2, finance.getCustomersServed());
    }

    @Test
    public void testMultiplePayments_CorrectTotal() {
        finance.processPayment(10.0);
        finance.processPayment(20.0);
        finance.processPayment(30.0);

        assertEquals(60.0, finance.getTotalRevenue(), 0.01);
        assertEquals(3, finance.getCustomersServed());
    }

    @Test
    public void testInitialState() {
        assertEquals(0.0, finance.getTotalRevenue(), 0.01);
        assertEquals(0, finance.getCustomersServed());
    }

    @Test
    public void testConcurrentPayments() throws InterruptedException {
        // Create 10 threads, each processes 100 payments
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    finance.processPayment(10.0);
                }
            });
            threads[i].start();
        }

        // Wait for all threads
        for (Thread t : threads) {
            t.join();
        }

        // Verify no money lost
        assertEquals(10000.0, finance.getTotalRevenue(), 0.01);
        assertEquals(1000, finance.getCustomersServed());
    }
}
```

---

## Summary

**JUnit is:**
- A testing framework for Java
- Helps verify your code works correctly
- Automatically runs tests and reports results

**Key annotations:**
- `@Test` - Marks a test method
- `@Before` - Runs before each test
- `@After` - Runs after each test
- `@BeforeClass` - Runs once before all tests
- `@AfterClass` - Runs once after all tests

**Common assertions:**
- `assertEquals(expected, actual)`
- `assertTrue(condition)`
- `assertFalse(condition)`
- `assertNull(object)`
- `assertNotNull(object)`

**Testing concurrent code:**
- Test single-threaded first
- Use `Thread.join()` to wait for threads
- Use `@Test(timeout = milliseconds)` to prevent hanging
- Verify no race conditions with multiple threads

Now let's create the actual test classes for your code!
