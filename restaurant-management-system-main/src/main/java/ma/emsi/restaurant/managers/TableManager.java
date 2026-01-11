package ma.emsi.restaurant.managers;

import ma.emsi.restaurant.entities.Table;

/**
 * Monitor for Table Management.
 * Handles synchronization for table acquisition and release.
 */
public class TableManager {
    
    // TODO: Maintain a list of Table objects

    public TableManager() {
        // TODO: Initialize tables (e.g., 10 Normal, 5 VIP)
    }

    /**
     * Attempts to acquire a table.
     * @param isVipClient if true, attempts to get a VIP table first
     * @return The acquired Table object
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public Table acquireTable(boolean isVipClient) throws InterruptedException {
        // TODO: Implement wait/notify logic here
        // If VIP, try tryLock() on VIP tables
        // If Normal, synchronized check on available normal tables
        return null; 
    }

    public void releaseTable(Table table) {
        // TODO: Reset table state and notifyAll() waiting clients
    }
}