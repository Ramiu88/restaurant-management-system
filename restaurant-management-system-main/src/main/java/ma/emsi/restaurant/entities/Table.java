package ma.emsi.restaurant.entities;

/**
 * Entity representing a dining table.
 */
public class Table {
    private final int id;
    private final boolean isVip;
    private boolean isOccupied;

    public Table(int id, boolean isVip) {
        this.id = id;
        this.isVip = isVip;
        this.isOccupied = false;
    }

    public int getId() { return id; }
    public boolean isVip() { return isVip; }
    
    public synchronized boolean isOccupied() { return isOccupied; }
    public synchronized void setOccupied(boolean occupied) { isOccupied = occupied; }
}
