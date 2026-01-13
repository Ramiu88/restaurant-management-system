package ma.emsi.restaurant;

import ma.emsi.restaurant.actors.Client;
import ma.emsi.restaurant.actors.Server;
import ma.emsi.restaurant.actors.Cook;
import ma.emsi.restaurant.managers.*;

/**
 * Main entry point for Restaurant Management System
 *
 * Usage:
 *   mvn exec:java                          - Run simulation
 *   mvn exec:java -Dexec.args="dashboard"   - Show dashboard
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== RESTAURANT SIMULATION INITIALIZING ===");

        // 1. Initialize the Singleton (creates all Monitors)
        Restaurant restaurant = Restaurant.getInstance();

        // Check if dashboard mode is requested
        boolean showDashboard = args.length > 0 && args[0].equalsIgnoreCase("dashboard");

        // 2. Start Service Staff (Servers, Cooks, etc.)
        if (showDashboard) {
            startServers(restaurant);
            startCooks(restaurant);
        }

        // 3. Start Clients
        int numClients = showDashboard ? 20 : 5;
        for (int i = 1; i <= numClients; i++) {
            new Client("Client-" + i, i % 3 == 0).start(); // Every 3rd is VIP
        }

        // 4. Show dashboard if requested
        if (showDashboard) {
            Thread.sleep(1000); // Let things start
            Dashboard dashboard = new Dashboard(restaurant);

            // Show dashboard snapshots every 2 seconds
            for (int i = 0; i < 20; i++) {
                dashboard.snapshot();
                Thread.sleep(2000);
            }
        }

        System.out.println("=== SYSTEM RUNNING ===");
    }

    private static void startServers(Restaurant restaurant) {
        OrderQueue orderQueue = restaurant.getOrderQueue();
        for (int i = 1; i <= Constants.NUM_SERVERS; i++) {
            new Thread(new Server(i), "Server-" + i).start();
        }
    }

    private static void startCooks(Restaurant restaurant) {
        for (int i = 1; i <= Constants.NUM_COOKS; i++) {
            new Thread(new Cook(i), "Cook-" + i).start();
        }
    }
}
