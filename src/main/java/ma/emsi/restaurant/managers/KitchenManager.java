package ma.emsi.restaurant.managers;

/**
 * Monitor for Kitchen Equipment.
 * Manages locks for shared resources (Oven, Grill, etc.) to prevent deadlocks.
 */
public class KitchenManager {
    
    // TODO: Define ReentrantLocks for:
    // - Ovens (3)
    // - Grills (2)
    // - Fryer (1)

    public void useOven(long durationMs) throws InterruptedException {
        // TODO: Acquire lock, sleep, release lock
    }

    public void useGrill(long durationMs) throws InterruptedException {
        // TODO: Acquire lock, sleep, release lock
    }

    public void useFryer(long durationMs) throws InterruptedException {
        // TODO: Acquire lock, sleep, release lock
    }

    /**
     * Complex method requiring multiple resources.
     * Must use tryLock strategies to avoid Deadlock.
     */
    public void prepareComplexDish() {
        // TODO: Try to get Oven AND Fryer, back off if fail
    }
}