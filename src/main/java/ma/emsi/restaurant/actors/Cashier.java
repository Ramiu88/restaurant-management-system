package ma.emsi.restaurant.actors;

import ma.emsi.restaurant.Restaurant;
import ma.emsi.restaurant.managers.FinanceManager;

/**
 * Cashier actor - processes customer payments.
 * Multiple cashiers run concurrently, demonstrating the need
 * for synchronized access to shared revenue counter.
 *
 * Integration: Called by Client (Walid's module) when customer finishes eating.
 *
 * @author Saladin
 */
public class Cashier implements Runnable {

    private final int cashierId;
    private volatile boolean running = true;

    public Cashier(int cashierId) {
        this.cashierId = cashierId;
    }

    @Override
    public void run() {
        FinanceManager finance = Restaurant.getInstance().getFinanceManager();

        System.out.println("[Cashier-" + cashierId + "] Ready to process payments");

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                // Wait for a customer to pay (in real system, Client calls processPayment)
                // For simulation, we generate random payments
                Thread.sleep(500 + (int)(Math.random() * 1500));

                // Random payment between $10 and $50
                double amount = 10.0 + (Math.random() * 40.0);

                // Process payment through FinanceManager
                finance.processPayment(amount);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("[Cashier-" + cashierId + "] Interrupted, shutting down");
                break;
            }
        }

        System.out.println("[Cashier-" + cashierId + "] Shift ended");
    }

    /**
     * Stop the cashier thread gracefully
     */
    public void stop() {
        running = false;
    }

    /**
     * Direct payment processing (called by Client)
     * This is how Walid's Client will interact with your module
     */
    public void processClientPayment(int clientId, double amount) {
        FinanceManager finance = Restaurant.getInstance().getFinanceManager();
        System.out.println("[Cashier-" + cashierId + "] Processing payment for Client-" + clientId);
        finance.processPayment(amount);
    }
}
