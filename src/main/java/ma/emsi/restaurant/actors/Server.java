package ma.emsi.restaurant.actors;

import ma.emsi.restaurant.Restaurant;
import ma.emsi.restaurant.managers.OrderQueue;

public class Server extends Thread {
    private final OrderQueue orderQueue;

    public Server(String name) {
        super(name);
        this.orderQueue = Restaurant.getInstance().getOrderQueue();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            // TODO: Wait for clients to request orders
            // For now, loop
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
