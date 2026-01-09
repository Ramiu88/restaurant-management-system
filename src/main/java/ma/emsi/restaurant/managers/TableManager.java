package ma.emsi.restaurant.managers;

import ma.emsi.restaurant.entities.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Monitor for Table Management.
 * Handles synchronization for table acquisition and release.
 */
public class TableManager {
    
    private final List<Table> normalTables = new ArrayList<>();
    private final List<Table> vipTables = new ArrayList<>();
    private int availableNormalTables;

    public TableManager() {
        // Initialize 10 Normal Tables
        for (int i = 1; i <= 10; i++) {
            normalTables.add(new Table(i, false));
        }
        availableNormalTables = 10;

        // Initialize 5 VIP Tables
        for (int i = 11; i <= 15; i++) {
            vipTables.add(new Table(i, true));
        }
    }

    /**
     * Attempts to acquire a table.
     */
    public Table acquireTable(boolean isVipClient) throws InterruptedException {
        // 1. If VIP, try VIP tables first with tryLock
        if (isVipClient) {
            for (Table t : vipTables) {
                if (t.getLock().tryLock(500, TimeUnit.MILLISECONDS)) {
                    synchronized (t) {
                        if (!t.isOccupied()) {
                            t.setOccupied(true);
                            return t;
                        }
                    }
                    t.getLock().unlock();
                }
            }
        }

        // 2. Otherwise, or if no VIP tables available, wait for a normal table
        synchronized (this) {
            while (availableNormalTables <= 0) {
                System.out.println(Thread.currentThread().getName() + " is waiting for a table...");
                wait();
            }
            
            for (Table t : normalTables) {
                synchronized (t) {
                    if (!t.isOccupied()) {
                        t.setOccupied(true);
                        availableNormalTables--;
                        return t;
                    }
                }
            }
        }
        return null;
    }

    public synchronized void releaseTable(Table table) {
        if (table == null) return;

        synchronized (table) {
            table.setOccupied(false);
            if (table.isVip()) {
                table.getLock().unlock();
            } else {
                availableNormalTables++;
                notifyAll(); // Wake up clients waiting for a normal table
            }
        }
    }
}