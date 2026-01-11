package ma.emsi.restaurant.managers;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * STUB for Marwan's KitchenManager
 * This is a simplified version so your Cook code compiles
 * Replace with Marwan's actual implementation when ready
 */
public class KitchenManager {

    /**
     * Try to acquire equipment for cooking
     * @param equipment List of equipment names needed
     * @param timeoutSeconds How long to wait
     * @return true if acquired, false if timeout
     */
    public boolean acquireEquipment(List<String> equipment, int timeoutSeconds) {
        if (equipment == null || equipment.isEmpty()) {
            return true; // No equipment needed
        }

        // STUB: Always succeed immediately
        System.out.println("[KitchenManager STUB] Acquired: " + equipment);
        return true;

        // TODO: Replace with Marwan's actual implementation
        // - Check if equipment available
        // - Wait if not available
        // - Timeout after timeoutSeconds
    }

    /**
     * Release equipment after cooking
     * @param equipment List of equipment names to release
     */
    public void releaseEquipment(List<String> equipment) {
        if (equipment == null || equipment.isEmpty()) {
            return;
        }

        // STUB: Just log
        System.out.println("[KitchenManager STUB] Released: " + equipment);

        // TODO: Replace with Marwan's actual implementation
        // - Mark equipment as available
        // - Notify waiting cooks
    }
}