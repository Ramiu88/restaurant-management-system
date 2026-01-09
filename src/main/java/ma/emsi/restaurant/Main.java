package ma.emsi.restaurant;

import ma.emsi.restaurant.actors.Client;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== RESTAURANT SIMULATION INITIALIZING ===");
        
        // 1. Initialize the Singleton (creates all Monitors)
        Restaurant restaurant = Restaurant.getInstance();

        // 2. Start Service Staff (Servers, Cooks, etc.)
        // TODO: Start Server threads
        // TODO: Start Cook threads

        // 3. Start Clients
        for (int i = 1; i <= 5; i++) {
            new Client("Client-" + i, i % 2 == 0).start();
        }
        
        System.out.println("=== SYSTEM RUNNING ===");
    }
}