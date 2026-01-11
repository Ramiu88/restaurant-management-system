package ma.emsi.restaurant.actors;

import ma.emsi.restaurant.Restaurant;
import ma.emsi.restaurant.entities.Table;
import ma.emsi.restaurant.managers.TableManager;

public class Client extends Thread {
    private final boolean isVip;
    private final TableManager tableManager;

    public Client(String name, boolean isVip) {
        super(name);
        this.isVip = isVip;
        this.tableManager = Restaurant.getInstance().getTableManager();
    }

    @Override
    public void run() {
        try {
            System.out.println(getName() + " arrived.");
            
            // 1. Get Table
            Table table = tableManager.acquireTable(isVip);
            if (table != null) {
                System.out.println(getName() + " got table " + table.getId());
                
                // 2. Order (simulated interaction with Server)
                // ...
                
                // 3. Eat
                Thread.sleep(2000); 

                // 4. Pay & Leave
                tableManager.releaseTable(table);
                System.out.println(getName() + " left.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
