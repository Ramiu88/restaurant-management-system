package ma.emsi.restaurant.managers;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

/**
 * Monitor for Kitchen Equipment.
 * Manages locks for shared resources (Oven, Grill, etc.) to prevent deadlocks.
 */
public class KitchenManager {
    
    // Resources
    private final ReentrantLock[] ovens = new ReentrantLock[3];
    private final ReentrantLock[] grills = new ReentrantLock[2];
    private final ReentrantLock fryer = new ReentrantLock();

    public KitchenManager() {
        for (int i = 0; i < ovens.length; i++) ovens[i] = new ReentrantLock();
        for (int i = 0; i < grills.length; i++) grills[i] = new ReentrantLock();
    }

    public void useOven(long durationMs) throws InterruptedException {
        // Simple strategy: try to get any oven
        ReentrantLock acquiredOven = null;
        while (acquiredOven == null) {
            for (ReentrantLock oven : ovens) {
                if (oven.tryLock()) {
                    acquiredOven = oven;
                    break;
                }
            }
            if (acquiredOven == null) {
                Thread.sleep(100); // Wait a bit before retrying
            }
        }
        
        try {
            System.out.println(Thread.currentThread().getName() + " is using an Oven...");
            Thread.sleep(durationMs);
        } finally {
            acquiredOven.unlock();
        }
    }

    public void useGrill(long durationMs) throws InterruptedException {
         ReentrantLock acquiredGrill = null;
        while (acquiredGrill == null) {
            for (ReentrantLock grill : grills) {
                if (grill.tryLock()) {
                    acquiredGrill = grill;
                    break;
                }
            }
            if (acquiredGrill == null) {
                Thread.sleep(100);
            }
        }
        
        try {
            System.out.println(Thread.currentThread().getName() + " is using a Grill...");
            Thread.sleep(durationMs);
        } finally {
            acquiredGrill.unlock();
        }
    }

    public void useFryer(long durationMs) throws InterruptedException {
        fryer.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " is using the Fryer...");
            Thread.sleep(durationMs);
        } finally {
            fryer.unlock();
        }
    }

    /**
     * Complex method requiring multiple resources.
     * Must use tryLock strategies to avoid Deadlock.
     */
    public void prepareComplexDish() {
        // TODO: Try to get Oven AND Fryer, back off if fail
    }
}