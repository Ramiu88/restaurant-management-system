package ma.emsi.restaurant;

/**
 * Constants for the restaurant simulation
 */
public class Constants {
    // Preparation times (milliseconds)
    public static final int PREP_TIME_URGENT = 500;   // 0.5 seconds
    public static final int PREP_TIME_NORMAL = 3000;  // 3 seconds
    public static final int PREP_TIME_SLOW = 5000;    // 5 seconds

    // Priority levels
    public static final int PRIORITY_URGENT = 1;
    public static final int PRIORITY_NORMAL = 2;
    public static final int PRIORITY_SLOW = 3;

    // Stock management
    public static final int STOCK_INITIAL = 50;
    public static final int STOCK_LOW_THRESHOLD = 10;
    public static final int STOCK_REPLENISH_AMOUNT = 30;
    public static final int STOCK_DELIVERY_TIME = 3000; // 3 seconds

    // Equipment timeout
    public static final int EQUIPMENT_TIMEOUT_SECONDS = 5;

    private Constants() {
        // Prevent instantiation
    }
}