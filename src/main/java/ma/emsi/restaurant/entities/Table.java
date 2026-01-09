package ma.emsi.restaurant.entities;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Entity representing a dining table.
 */
public class Table {
    private final int id;
    private final boolean isVip;
    private boolean isOccupied;
    private final ReentrantLock lock;

    public Table(int id, boolean isVip) {
        this.id = id;
        this.isVip = isVip;
        this.isOccupied = false;
        // Only VIP tables strictly need the lock per the plan, 
        // but initializing it for all simplifies the code structure.
        this.lock = new ReentrantLock();
    }

    public int getId() { return id; }
    public boolean isVip() { return isVip; }
    
    public synchronized boolean isOccupied() { return isOccupied; }
    public synchronized void setOccupied(boolean occupied) { isOccupied = occupied; }

    public ReentrantLock getLock() { return lock; }
}
