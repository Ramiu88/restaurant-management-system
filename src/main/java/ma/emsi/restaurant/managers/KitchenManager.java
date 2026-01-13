package ma.emsi.restaurant.managers;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Monitor for Kitchen Equipment.
 * Manages locks for shared resources (Oven, Grill, etc.) to prevent deadlocks.
 *
 * Original implementation by Marwan (Module 3: Equipment Management).
 * Generic acquire/release methods added by Anakin for Cook integration.
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

    // ===== Original Marwan's methods =====

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

    // ===== Anakin's generic methods for Cook integration =====

    /**
     * Try to acquire equipment by name.
     * Generic method used by Cook to acquire any equipment needed.
     *
     * @param equipmentNames List of equipment names needed (e.g., ["Oven1", "Grill1"])
     * @param timeoutSeconds How long to wait for each piece of equipment
     * @return true if all equipment acquired, false if timeout
     */
    public boolean acquireEquipment(List<String> equipmentNames, int timeoutSeconds) {
        if (equipmentNames == null || equipmentNames.isEmpty()) {
            return true; // No equipment needed
        }

        // Try to acquire each requested equipment
        for (String equipmentName : equipmentNames) {
            boolean acquired = false;

            if (equipmentName.startsWith("Oven")) {
                acquired = acquireOven(timeoutSeconds);
            } else if (equipmentName.startsWith("Grill")) {
                acquired = acquireGrill(timeoutSeconds);
            } else if (equipmentName.startsWith("Fryer")) {
                acquired = acquireFryer(timeoutSeconds);
            } else {
                System.out.println("[KitchenManager] Unknown equipment: " + equipmentName);
                return false;
            }

            if (!acquired) {
                // Failed to acquire one equipment, release what we acquired so far
                System.out.println("[KitchenManager] Failed to acquire: " + equipmentName);
                return false;
            }
        }

        System.out.println("[KitchenManager] Acquired all: " + equipmentNames);
        return true;
    }

    /**
     * Release equipment by name.
     *
     * @param equipmentNames List of equipment names to release
     */
    public void releaseEquipment(List<String> equipmentNames) {
        if (equipmentNames == null || equipmentNames.isEmpty()) {
            return;
        }

        for (String equipmentName : equipmentNames) {
            if (equipmentName.startsWith("Oven")) {
                releaseOven();
            } else if (equipmentName.startsWith("Grill")) {
                releaseGrill();
            } else if (equipmentName.startsWith("Fryer")) {
                releaseFryer();
            }
        }

        System.out.println("[KitchenManager] Released: " + equipmentNames);
    }

    // ===== Helper methods for lock tracking =====

    private ReentrantLock acquiredOven = null;
    private ReentrantLock acquiredGrill = null;
    private boolean acquiredFryer = false;

    private boolean acquireOven(int timeoutSeconds) {
        try {
            for (ReentrantLock oven : ovens) {
                if (oven.tryLock(timeoutSeconds, TimeUnit.SECONDS)) {
                    acquiredOven = oven;
                    return true;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    private boolean acquireGrill(int timeoutSeconds) {
        try {
            for (ReentrantLock grill : grills) {
                if (grill.tryLock(timeoutSeconds, TimeUnit.SECONDS)) {
                    acquiredGrill = grill;
                    return true;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    private boolean acquireFryer(int timeoutSeconds) {
        try {
            if (fryer.tryLock(timeoutSeconds, TimeUnit.SECONDS)) {
                acquiredFryer = true;
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    private void releaseOven() {
        if (acquiredOven != null) {
            acquiredOven.unlock();
            acquiredOven = null;
        }
    }

    private void releaseGrill() {
        if (acquiredGrill != null) {
            acquiredGrill.unlock();
            acquiredGrill = null;
        }
    }

    private void releaseFryer() {
        if (acquiredFryer) {
            fryer.unlock();
            acquiredFryer = false;
        }
    }
}
