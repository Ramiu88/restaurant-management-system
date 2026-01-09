package ma.emsi.restaurant;

/**
 * Constantes partagees du systeme de gestion du restaurant
 */
public class Constants {
    // Configuration des acteurs
    public static final int NUM_CLIENTS = 50;
    public static final int NUM_SERVERS = 4;
    public static final int NUM_COOKS = 3;
    public static final int NUM_CASHIERS = 2;

    // Configuration des tables
    public static final int NORMAL_TABLES = 10;
    public static final int VIP_TABLES = 5;
    public static final int TOTAL_TABLES = NORMAL_TABLES + VIP_TABLES;

    // Timeouts
    public static final long VIP_TIMEOUT_SECONDS = 30;
    public static final long EQUIPMENT_TIMEOUT_SECONDS = 2;

    // Priorites des commandes
    public static final int PRIORITY_URGENT = 1;
    public static final int PRIORITY_NORMAL = 2;
    public static final int PRIORITY_SLOW = 3;

    // Temps de preparation (millisecondes)
    public static final int PREP_TIME_URGENT = 500;
    public static final int PREP_TIME_NORMAL = 3000;
    public static final int PREP_TIME_SLOW = 5000;

    // Comportement client
    public static final int CLIENT_MENU_TIME_MIN = 1000;
    public static final int CLIENT_MENU_TIME_MAX = 2000;
    public static final int CLIENT_EATING_TIME_MIN = 3000;
    public static final int CLIENT_EATING_TIME_MAX = 5000;

    // Stock
    public static final int STOCK_INITIAL = 50;
    public static final int STOCK_LOW_THRESHOLD = 10;
    public static final int STOCK_REPLENISH_AMOUNT = 50;
    public static final int STOCK_DELIVERY_TIME = 3000;
}
