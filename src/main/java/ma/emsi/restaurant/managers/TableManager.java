package ma.emsi.restaurant.managers;

import ma.emsi.restaurant.entities.Table;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Monitor for Table Management - Implements wait/notify pattern with VIP priority
 * 
 * KEY CONCURRENCY FEATURES:
 * - ReentrantLock with tryLock for VIP tables (30s timeout)
 * - wait/notifyAll for normal table waiting queue
 * - Synchronized methods for thread safety
 * - Stream API for functional programming style
 * 
 * @author Walid - Table Management Module
 */
public class TableManager {
    
    // Configuration constants
    private static final int NUM_NORMAL_TABLES = 10;
    private static final int NUM_VIP_TABLES = 5;
    private static final long VIP_TIMEOUT_SECONDS = 30;
    
    private final List<Table> normalTables;
    private final List<Table> vipTables;
    private int availableNormalTables;

    /**
     * Constructor - Initialize tables using Stream API (functional programming)
     */
    public TableManager() {
        // Use Stream to create 10 Normal Tables (IDs 1-10)
        this.normalTables = IntStream.rangeClosed(1, NUM_NORMAL_TABLES)
                .mapToObj(id -> new Table(id, false))
                .collect(Collectors.toList());
        
        this.availableNormalTables = NUM_NORMAL_TABLES;

        // Use Stream to create 5 VIP Tables (IDs 11-15)
        this.vipTables = IntStream.rangeClosed(11, 15)
                .mapToObj(id -> new Table(id, true))
                .collect(Collectors.toList());
        
        System.out.println(" TableManager initialized: " + NUM_NORMAL_TABLES + 
                         " normal tables, " + NUM_VIP_TABLES + " VIP tables");
    }

    /**
     * Attempts to acquire a table with VIP priority handling
     * 
     * VIP LOGIC:
     * 1. VIP clients try VIP tables first with 30s timeout using tryLock
     * 2. If timeout or all VIP tables occupied, fallback to normal queue
     * 
     * NORMAL LOGIC:
     * 1. Wait in synchronized queue using wait()
     * 2. Wake up on notifyAll() when table is released
     * 
     * @param isVipClient Whether the client has VIP status
     * @return Table object if acquired, null otherwise
     * @throws InterruptedException if thread is interrupted while waiting
     */
    public Table acquireTable(boolean isVipClient) throws InterruptedException {
        String clientName = Thread.currentThread().getName();
        
        // PHASE 1: VIP Priority - Try VIP tables first with timeout
        if (isVipClient) {
            System.out.println( clientName + " attempting to acquire VIP table...");
            
            // Use Stream API to find available VIP table
            Table vipTable = vipTables.stream()
                    .filter(table -> {
                        try {
                            // Try to acquire lock with 30-second timeout
                            if (table.getLock().tryLock(VIP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                                synchronized (table) {
                                    if (!table.isOccupied()) {
                                        return true; // Found available VIP table
                                    }
                                }
                                table.getLock().unlock(); // Not available, release lock
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return false;
                    })
                    .findFirst()
                    .orElse(null);
            
            // If VIP table acquired successfully
            if (vipTable != null) {
                synchronized (vipTable) {
                    vipTable.setOccupied(true);
                }
                System.out.println( clientName + " got VIP Table " + vipTable.getId());
                return vipTable;
            }
            
            // VIP timeout - fallback to normal queue
            System.out.println( clientName + " timeout! Joining normal queue...");
        } else {
            System.out.println( clientName + " requesting normal table...");
        }

        // PHASE 2: Normal Queue - Wait for normal table using wait/notify
        synchronized (this) {
            // Wait while no normal tables available
            while (availableNormalTables <= 0) {
                System.out.println( clientName + " waiting for normal table... " +
                                 "(Available: " + availableNormalTables + ")");
                wait(); // Release lock and wait for notifyAll()
            }
            
            // Find and acquire first available normal table using Stream API
            Table normalTable = normalTables.stream()
                    .filter(table -> {
                        synchronized (table) {
                            return !table.isOccupied();
                        }
                    })
                    .findFirst()
                    .orElse(null);
            
            if (normalTable != null) {
                synchronized (normalTable) {
                    normalTable.setOccupied(true);
                }
                availableNormalTables--;
                System.out.println( clientName + " got Normal Table " + 
                                 normalTable.getId() + 
                                 " (Available: " + availableNormalTables + ")");
                return normalTable;
            }
        }
        
        return null; // Should never reach here
    }

    /**
     * Releases a table and notifies waiting clients
     * 
     * @param table The table to release
     */
    public synchronized void releaseTable(Table table) {
        if (table == null) return;
        
        String clientName = Thread.currentThread().getName();

        synchronized (table) {
            table.setOccupied(false);
            
            if (table.isVip()) {
                // Release VIP lock
                table.getLock().unlock();
                System.out.println( clientName + " released VIP Table " + table.getId());
            } else {
                // Increment available normal tables and notify waiting clients
                availableNormalTables++;
                System.out.println(clientName + " released Normal Table " + 
                                 table.getId() + 
                                 " (Available: " + availableNormalTables + ")");
                notifyAll(); // Wake up ALL waiting clients in the queue
            }
        }
    }

    /**
     * Get current status of all tables (for monitoring/debugging)
     * Uses Stream API for functional filtering and counting
     */
    public synchronized void printStatus() {
        System.out.println("\n" + "═".repeat(60));
        System.out.println(" TABLE STATUS REPORT");
        System.out.println("═".repeat(60));
        
        // Count occupied tables using Stream API
        long occupiedNormal = normalTables.stream()
                .filter(Table::isOccupied)
                .count();
        
        long occupiedVip = vipTables.stream()
                .filter(Table::isOccupied)
                .count();
        
        System.out.println("Normal Tables: " + (NUM_NORMAL_TABLES - occupiedNormal) + 
                         "/" + NUM_NORMAL_TABLES + " available");
        System.out.println("VIP Tables: " + (NUM_VIP_TABLES - occupiedVip) + 
                         "/" + NUM_VIP_TABLES + " available");
        
        // Show occupied tables using Stream API
        if (occupiedNormal > 0) {
            System.out.println("\nOccupied Normal Tables:");
            normalTables.stream()
                    .filter(Table::isOccupied)
                    .forEach(t -> System.out.println("  Table " + t.getId()));
        }
        
        if (occupiedVip > 0) {
            System.out.println("\nOccupied VIP Tables:");
            vipTables.stream()
                    .filter(Table::isOccupied)
                    .forEach(t -> System.out.println("   VIP Table " + t.getId()));
        }
        
        System.out.println("═".repeat(60) + "\n");
    }

    // Getters for testing purposes
    public int getAvailableNormalTables() {
        return availableNormalTables;
    }

    public List<Table> getAllTables() {
        return IntStream.concat(
                normalTables.stream().mapToInt(t -> t.getId()),
                vipTables.stream().mapToInt(t -> t.getId())
        ).mapToObj(id -> {
            if (id <= NUM_NORMAL_TABLES) {
                return normalTables.get(id - 1);
            } else {
                return vipTables.get(id - NUM_NORMAL_TABLES - 1);
            }
        }).collect(Collectors.toList());
    }
}