package ma.emsi.restaurant.entities;

/**
 * Entity representing a dish (Plat).
 */
public class Dish {
    private final String name;
    private final long preparationTimeMs;

    public Dish(String name, long preparationTimeMs) {
        this.name = name;
        this.preparationTimeMs = preparationTimeMs;
    }

    public String getName() { return name; }
    public long getPreparationTimeMs() { return preparationTimeMs; }
}
