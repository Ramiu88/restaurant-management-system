package ma.emsi.restaurant.demo;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Demonstrates a deliberate deadlock scenario
 *
 * This class shows WHY deadlock prevention in KitchenManager is important.
 * Creates 3 cooks with circular dependency that causes deadlock.
 *
 * WARNING: This code deliberately creates a deadlock for educational purposes.
 * DO NOT USE in production code.
 *
 * @author Marwan
 */
public class DemoDeadlock {

    private final ReentrantLock oven = new ReentrantLock();
    private final ReentrantLock fryer = new ReentrantLock();
    private final ReentrantLock grill = new ReentrantLock();

    /**
     * Creates a circular deadlock with 3 cooks
     *
     * Scenario:
     * Cook1: Locks Oven → tries to lock Fryer
     * Cook2: Locks Fryer → tries to lock Grill
     * Cook3: Locks Grill → tries to lock Oven
     *
     * Result: Circular dependency = DEADLOCK!
     */
    public void demonstrateCircularDeadlock() {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║     DEADLOCK DEMONSTRATION                ║");
        System.out.println("╚════════════════════════════════════════════╝");
        System.out.println("Creating 3 cooks with circular dependency...\n");

        // Cook 1: Oven → Fryer (locks in this order)
        Thread cook1 = new Thread(() -> {
            oven.lock();
            System.out.println("✓ Cook1: Locked Oven");
            System.out.println("⏳ Cook1: Waiting for Fryer...");
            sleep(100); // Small delay to ensure all threads get first lock

            System.out.println("🔒 Cook1: Trying to lock Fryer...");
            fryer.lock(); // ← DEADLOCK HERE! Fryer held by Cook2

            System.out.println("✓ Cook1: Got both locks! (This will never print)");
            fryer.unlock();
            oven.unlock();
        }, "Cook1");

        // Cook 2: Fryer → Grill (locks in this order)
        Thread cook2 = new Thread(() -> {
            fryer.lock();
            System.out.println("✓ Cook2: Locked Fryer");
            System.out.println("⏳ Cook2: Waiting for Grill...");
            sleep(100);

            System.out.println("🔒 Cook2: Trying to lock Grill...");
            grill.lock(); // ← DEADLOCK HERE! Grill held by Cook3

            System.out.println("✓ Cook2: Got both locks! (This will never print)");
            grill.unlock();
            fryer.unlock();
        }, "Cook2");

        // Cook 3: Grill → Oven (locks in this order)
        Thread cook3 = new Thread(() -> {
            grill.lock();
            System.out.println("✓ Cook3: Locked Grill");
            System.out.println("⏳ Cook3: Waiting for Oven...");
            sleep(100);

            System.out.println("🔒 Cook3: Trying to lock Oven...");
            oven.lock(); // ← DEADLOCK HERE! Oven held by Cook1

            System.out.println("✓ Cook3: Got both locks! (This will never print)");
            oven.unlock();
            grill.unlock();
        }, "Cook3");

        // Start all three cooks
        cook1.start();
        cook2.start();
        cook3.start();

        // Wait to let deadlock form
        sleep(2000);

        // Show the deadlock state
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║     ⚠️  DEADLOCK DETECTED!  ⚠️            ║");
        System.out.println("╚════════════════════════════════════════════╝");
        System.out.println("\n🔴 All threads are STUCK!");
        System.out.println("\nCircular dependency:");
        System.out.println("  Cook1: Has Oven  → Wants Fryer");
        System.out.println("  Cook2: Has Fryer → Wants Grill");
        System.out.println("  Cook3: Has Grill → Wants Oven");
        System.out.println("         ↑_________________________|");
        System.out.println("\n💡 Nobody can proceed = DEADLOCK!\n");

        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║     HOW KITCHENMANAGER PREVENTS THIS      ║");
        System.out.println("╚════════════════════════════════════════════╝");
        System.out.println("1. Uses tryLock() with timeout (not lock())");
        System.out.println("2. Backs off if can't get all resources");
        System.out.println("3. Releases already-acquired locks on failure");
        System.out.println("4. Always uses finally block for cleanup\n");

        // Force stop deadlocked threads
        cook1.interrupt();
        cook2.interrupt();
        cook3.interrupt();
    }

    /**
     * Demonstrates deadlock with just 2 threads (simpler example)
     */
    public void demonstrateSimpleDeadlock() {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║     SIMPLE DEADLOCK (2 Threads)           ║");
        System.out.println("╚════════════════════════════════════════════╝\n");

        ReentrantLock lockA = new ReentrantLock();
        ReentrantLock lockB = new ReentrantLock();

        // Thread 1: A → B
        Thread t1 = new Thread(() -> {
            lockA.lock();
            System.out.println("Thread1: Got Lock A, waiting for Lock B...");
            sleep(100);
            lockB.lock(); // DEADLOCK
            System.out.println("Thread1: Got both locks (never prints)");
            lockB.unlock();
            lockA.unlock();
        }, "Thread1");

        // Thread 2: B → A
        Thread t2 = new Thread(() -> {
            lockB.lock();
            System.out.println("Thread2: Got Lock B, waiting for Lock A...");
            sleep(100);
            lockA.lock(); // DEADLOCK
            System.out.println("Thread2: Got both locks (never prints)");
            lockA.unlock();
            lockB.unlock();
        }, "Thread2");

        t1.start();
        t2.start();

        sleep(1000);
        System.out.println("\n⚠️  DEADLOCK! Both threads stuck!\n");

        t1.interrupt();
        t2.interrupt();
    }

    /**
     * Helper method for sleep
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Main method - run the demonstrations
     */
    public static void main(String[] args) {
        DemoDeadlock demo = new DemoDeadlock();

        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║   DEADLOCK DEMONSTRATION PROGRAM          ║");
        System.out.println("║   Author: Marwan                          ║");
        System.out.println("╚════════════════════════════════════════════╝\n");

        // Demonstrate circular deadlock (3 threads)
        demo.demonstrateCircularDeadlock();

        demo.sleep(3000); // Pause between demos

        // Demonstrate simple deadlock (2 threads)
        demo.demonstrateSimpleDeadlock();

        demo.sleep(2000);

        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║   DEMONSTRATION COMPLETE                  ║");
        System.out.println("╚════════════════════════════════════════════╝");
        System.out.println("\nThis shows why KitchenManager uses:");
        System.out.println("- tryLock() instead of lock()");
        System.out.println("- Timeout mechanisms");
        System.out.println("- Back-off strategies\n");

        System.exit(0); // Force exit (threads are deadlocked)
    }
}
