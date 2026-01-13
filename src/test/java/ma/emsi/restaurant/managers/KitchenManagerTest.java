package ma.emsi.restaurant.managers;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * JUnit tests for KitchenManager
 * Tests equipment locking, deadlock prevention, and timeout mechanisms
 */
public class KitchenManagerTest {

    private KitchenManager kitchenManager;

    @Before
    public void setUp() {
        kitchenManager = new KitchenManager();
    }

    @After
    public void tearDown() {
        kitchenManager = null;
    }

    // ===== BASIC FUNCTIONALITY TESTS =====

    @Test
    public void testInitialState() {
        assertNotNull("KitchenManager should be initialized", kitchenManager);
    }

    // ===== ORIGINAL MARWAN METHODS TESTS =====

    @Test
    public void testUseOven() throws InterruptedException {
        Thread cook = new Thread(() -> {
            try {
                kitchenManager.useOven(100);
            } catch (InterruptedException e) {
                fail("useOven should not interrupt");
            }
        });

        cook.start();
        cook.join(1000);

        assertFalse("Cook should have completed", cook.isAlive());
    }

    @Test
    public void testUseGrill() throws InterruptedException {
        Thread cook = new Thread(() -> {
            try {
                kitchenManager.useGrill(100);
            } catch (InterruptedException e) {
                fail("useGrill should not interrupt");
            }
        });

        cook.start();
        cook.join(1000);

        assertFalse("Cook should have completed", cook.isAlive());
    }

    @Test
    public void testUseFryer() throws InterruptedException {
        Thread cook = new Thread(() -> {
            try {
                kitchenManager.useFryer(100);
            } catch (InterruptedException e) {
                fail("useFryer should not interrupt");
            }
        });

        cook.start();
        cook.join(1000);

        assertFalse("Cook should have completed", cook.isAlive());
    }

    // ===== ACQUIRE/RELEASE METHODS TESTS =====

    @Test
    public void testAcquireEquipment_SingleEquipment() {
        List<String> equipment = Arrays.asList("Oven1");
        boolean acquired = kitchenManager.acquireEquipment(equipment, 2);

        assertTrue("Should acquire single equipment", acquired);

        // Release it
        kitchenManager.releaseEquipment(equipment);
    }

    @Test
    public void testAcquireEquipment_MultipleEquipment() {
        List<String> equipment = Arrays.asList("Oven1", "Grill1");
        boolean acquired = kitchenManager.acquireEquipment(equipment, 2);

        assertTrue("Should acquire multiple equipment", acquired);

        // Release them
        kitchenManager.releaseEquipment(equipment);
    }

    @Test
    public void testAcquireEquipment_NoEquipmentNeeded() {
        List<String> equipment = Collections.emptyList();
        boolean acquired = kitchenManager.acquireEquipment(equipment, 2);

        assertTrue("Should succeed with no equipment", acquired);
    }

    @Test
    public void testAcquireEquipment_NullEquipment() {
        boolean acquired = kitchenManager.acquireEquipment(null, 2);

        assertTrue("Should succeed with null equipment", acquired);
    }

    @Test
    public void testReleaseEquipment_NoEquipment() {
        // Should not throw exception
        kitchenManager.releaseEquipment(Collections.emptyList());
        kitchenManager.releaseEquipment(null);
    }

    // ===== CONCURRENT ACCESS TESTS =====

    @Test(timeout = 10000)
    public void testConcurrentEquipmentAccess() throws InterruptedException {
        final int NUM_COOKS = 5;
        final int[] successCount = {0};
        Thread[] cooks = new Thread[NUM_COOKS];

        for (int i = 0; i < NUM_COOKS; i++) {
            cooks[i] = new Thread(() -> {
                List<String> equipment = Arrays.asList("Oven1");
                if (kitchenManager.acquireEquipment(equipment, 5)) {
                    try {
                        Thread.sleep(100);
                        synchronized (successCount) {
                            successCount[0]++;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        kitchenManager.releaseEquipment(equipment);
                    }
                }
            });
            cooks[i].start();
        }

        for (Thread cook : cooks) {
            cook.join();
        }

        assertEquals("All cooks should successfully acquire equipment", NUM_COOKS, successCount[0]);
    }

    @Test(timeout = 10000)
    public void testMultipleOvens_SimultaneousAccess() throws InterruptedException {
        final int NUM_COOKS = 3; // We have 3 ovens
        final int[] successCount = {0};
        Thread[] cooks = new Thread[NUM_COOKS];

        // All cooks try to use Oven at the same time
        for (int i = 0; i < NUM_COOKS; i++) {
            final int cookNum = i;
            cooks[i] = new Thread(() -> {
                List<String> equipment = Arrays.asList("Oven" + (cookNum + 1));
                if (kitchenManager.acquireEquipment(equipment, 5)) {
                    try {
                        Thread.sleep(200);
                        synchronized (successCount) {
                            successCount[0]++;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        kitchenManager.releaseEquipment(equipment);
                    }
                }
            }, "Cook-" + cookNum);
            cooks[i].start();
        }

        // Start all at approximately the same time
        for (Thread cook : cooks) {
            cook.join();
        }

        assertEquals("All cooks should use ovens simultaneously", NUM_COOKS, successCount[0]);
    }

    @Test(timeout = 10000)
    public void testLimitedGrills_QueueBehavior() throws InterruptedException {
        // We only have 2 grills, but 3 cooks want them
        final int NUM_COOKS = 3;
        final int[] successCount = {0};
        Thread[] cooks = new Thread[NUM_COOKS];

        for (int i = 0; i < NUM_COOKS; i++) {
            cooks[i] = new Thread(() -> {
                List<String> equipment = Arrays.asList("Grill1");
                if (kitchenManager.acquireEquipment(equipment, 5)) {
                    try {
                        Thread.sleep(100);
                        synchronized (successCount) {
                            successCount[0]++;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        kitchenManager.releaseEquipment(equipment);
                    }
                }
            });
            cooks[i].start();
        }

        for (Thread cook : cooks) {
            cook.join();
        }

        assertEquals("All cooks should eventually get grill (serialized access)", NUM_COOKS, successCount[0]);
    }

    // ===== TIMEOUT TESTS =====

    @Test(timeout = 5000)
    public void testAcquireTimeout_SingleResource() throws InterruptedException {
        // First thread acquires the fryer (only 1 available)
        Thread holder = new Thread(() -> {
            List<String> equipment = Arrays.asList("Fryer");
            kitchenManager.acquireEquipment(equipment, 10);
            try {
                Thread.sleep(500); // Hold for 500ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                kitchenManager.releaseEquipment(equipment);
            }
        });

        // Second thread tries to acquire with short timeout
        Thread waiter = new Thread(() -> {
            try {
                Thread.sleep(50); // Ensure holder acquires first
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            List<String> equipment = Arrays.asList("Fryer");
            boolean acquired = kitchenManager.acquireEquipment(equipment, 0); // 0 second timeout = immediate fail

            assertFalse("Should fail to acquire with no timeout", acquired);
        });

        holder.start();
        waiter.start();

        holder.join();
        waiter.join();
    }

    // ===== DEADLOCK PREVENTION TESTS =====

    @Test(timeout = 10000)
    public void testDeadlockPrevention_NoCircularWaiting() throws InterruptedException {
        // This test verifies that multiple cooks with different equipment needs
        // don't cause deadlock
        final int NUM_ROUNDS = 5;
        final boolean[] failed = {false};

        Thread cook1 = new Thread(() -> {
            for (int i = 0; i < NUM_ROUNDS; i++) {
                List<String> equipment = Arrays.asList("Oven1", "Fryer");
                if (kitchenManager.acquireEquipment(equipment, 2)) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        failed[0] = true;
                        break;
                    } finally {
                        kitchenManager.releaseEquipment(equipment);
                    }
                }
            }
        }, "Cook1");

        Thread cook2 = new Thread(() -> {
            for (int i = 0; i < NUM_ROUNDS; i++) {
                List<String> equipment = Arrays.asList("Fryer", "Grill1");
                if (kitchenManager.acquireEquipment(equipment, 2)) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        failed[0] = true;
                        break;
                    } finally {
                        kitchenManager.releaseEquipment(equipment);
                    }
                }
            }
        }, "Cook2");

        Thread cook3 = new Thread(() -> {
            for (int i = 0; i < NUM_ROUNDS; i++) {
                List<String> equipment = Arrays.asList("Grill1", "Oven1");
                if (kitchenManager.acquireEquipment(equipment, 2)) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        failed[0] = true;
                        break;
                    } finally {
                        kitchenManager.releaseEquipment(equipment);
                    }
                }
            }
        }, "Cook3");

        cook1.start();
        cook2.start();
        cook3.start();

        cook1.join();
        cook2.join();
        cook3.join();

        assertFalse("No thread should be interrupted (no deadlock)", failed[0]);
        assertFalse("Cook1 should complete", cook1.isAlive());
        assertFalse("Cook2 should complete", cook2.isAlive());
        assertFalse("Cook3 should complete", cook3.isAlive());
    }

    // ===== STRESS TEST =====

    @Test(timeout = 30000)
    public void stressTest_HighConcurrency() throws InterruptedException {
        final int NUM_COOKS = 5;
        final int OPERATIONS_PER_COOK = 10;
        final int[] successCount = {0};
        final int[] failCount = {0};

        Thread[] cooks = new Thread[NUM_COOKS];

        for (int i = 0; i < NUM_COOKS; i++) {
            final int cookId = i;
            cooks[i] = new Thread(() -> {
                for (int j = 0; j < OPERATIONS_PER_COOK; j++) {
                    // Vary equipment needs (use simpler cases to reduce contention)
                    List<String> equipment;
                    int choice = (cookId + j) % 3;
                    switch (choice) {
                        case 0:
                            equipment = Arrays.asList("Oven1");
                            break;
                        case 1:
                            equipment = Arrays.asList("Grill1");
                            break;
                        default:
                            equipment = Arrays.asList("Fryer");
                            break;
                    }

                    if (kitchenManager.acquireEquipment(equipment, 3)) {
                        try {
                            Thread.sleep(10);
                            synchronized (successCount) {
                                successCount[0]++;
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        } finally {
                            kitchenManager.releaseEquipment(equipment);
                        }
                    } else {
                        synchronized (failCount) {
                            failCount[0]++;
                        }
                    }
                }
            });
            cooks[i].start();
        }

        for (Thread cook : cooks) {
            cook.join(5000); // Individual timeout per cook
        }

        int total = successCount[0] + failCount[0];
        assertEquals("Total operations should match", NUM_COOKS * OPERATIONS_PER_COOK, total);

        // Most operations should succeed (some may timeout due to contention)
        assertTrue("At least 70% should succeed", successCount[0] >= (NUM_COOKS * OPERATIONS_PER_COOK * 7 / 10));
    }
}
