package ma.emsi.restaurant;

import ma.emsi.restaurant.actors.Cook;
import ma.emsi.restaurant.actors.Server;
import ma.emsi.restaurant.actors.StockManagerThread;

/**
 * Main class to test the Restaurant Order Queue System
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   RESTAURANT SIMULATION STARTING");
        System.out.println("========================================\n");

        // Get Restaurant singleton
        Restaurant restaurant = Restaurant.getInstance();

        // ===== START SALADIN'S BACKGROUND THREAD =====
        System.out.println("Starting Stock Manager (Saladin's module)...");
        StockManagerThread stockThread = new StockManagerThread();
        Thread stockDaemon = new Thread(stockThread, "StockManager-Daemon");
        stockDaemon.setDaemon(true);
        stockDaemon.start();

        // ===== START YOUR SERVERS (Producers) =====
        System.out.println("Starting Servers (YOUR module - Producers)...");
        Thread[] servers = new Thread[3]; // 3 servers
        for (int i = 0; i < 3; i++) {
            Server server = new Server(i + 1);
            servers[i] = new Thread(server, "Server-" + (i + 1));
            servers[i].start();
        }

        // ===== START YOUR COOKS (Consumers) =====
        System.out.println("Starting Cooks (YOUR module - Consumers)...");
        Thread[] cooks = new Thread[2]; // 2 cooks
        for (int i = 0; i < 2; i++) {
            Cook cook = new Cook(i + 1);
            cooks[i] = new Thread(cook, "Cook-" + (i + 1));
            cooks[i].start();
        }

        System.out.println("\n========================================");
        System.out.println("   SIMULATION RUNNING (20 seconds)");
        System.out.println("========================================\n");

        // ===== RUN SIMULATION FOR 20 SECONDS =====
        try {
            Thread.sleep(20000); // Run for 20 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // ===== STOP ALL THREADS =====
        System.out.println("\n========================================");
        System.out.println("   STOPPING SIMULATION");
        System.out.println("========================================\n");

        // Stop servers
        for (Thread server : servers) {
            server.interrupt();
        }

        // Stop cooks
        for (Thread cook : cooks) {
            cook.interrupt();
        }

        // Wait for threads to finish
        try {
            for (Thread server : servers) {
                server.join(1000);
            }
            for (Thread cook : cooks) {
                cook.join(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // ===== PRINT FINAL STATISTICS =====
        System.out.println("\n========================================");
        System.out.println("   FINAL STATISTICS");
        System.out.println("========================================");
        System.out.println("Queue size: " + restaurant.getOrderQueue().size());
        System.out.println("\nStock status:");
        System.out.println(restaurant.getStockManager().getStockStatus());
        System.out.println("\nFinance status:");
        System.out.println(restaurant.getFinanceManager().getStatistics());
        System.out.println("========================================");
        System.out.println("   SIMULATION ENDED");
        System.out.println("========================================");
    }
}