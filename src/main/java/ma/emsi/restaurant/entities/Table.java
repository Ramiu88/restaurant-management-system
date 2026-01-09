package ma.emsi.restaurant.entities;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Entity representing a dining table in the restaurant.
 * Thread-safe with synchronized methods for occupation status.
 * 
 * @author Walid - Table Management Module
 */
public class Table {
    private final int id;
    private final boolean isVip;
    private boolean isOccupied;
    private final ReentrantLock lock;

    /**
     * Constructor for Table
     * @param id Unique table identifier (1-10 for normal, 11-15 for VIP)
     * @param isVip Whether this is a VIP table
     */
    public Table(int id, boolean isVip) {
        this.id = id;
        this.isVip = isVip;
        this.isOccupied = false;
        // Initialize lock for all tables to simplify code structure
        this.lock = new ReentrantLock();
    }

    // Getters
    public int getId() { 
        return id; 
    }
    
    public boolean isVip() { 
        return isVip; 
    }
    
    /**
     * Thread-safe check if table is occupied
     */
    public synchronized boolean isOccupied() { 
        return isOccupied; 
    }
    
    /**
     * Thread-safe setter for occupation status
     */
    public synchronized void setOccupied(boolean occupied) { 
        this.isOccupied = occupied; 
    }

    /**
     * Get the ReentrantLock for this table
     * Used primarily for VIP tables with tryLock timeout
     */
    public ReentrantLock getLock() { 
        return lock; 
    }

    @Override
    public String toString() {
        return String.format("Table{id=%d, isVip=%s, occupied=%s}", 
                           id, isVip, isOccupied);
    }
}