package ma.emsi.restaurant.entities;

import ma.emsi.restaurant.Constants;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Entity representing a dish (Plat).
 * Contains preparation time, required equipment, and ingredients.
 * Enhanced by Anakin with equipment requirements and ingredients for stock integration.
 */
public class Dish {
    private final String name;
    private final int preparationTime; // milliseconds
    private final List<String> requiredEquipment;
    private final Map<String, Integer> ingredients;

    public Dish(String name, int preparationTime,
                List<String> requiredEquipment,
                Map<String, Integer> ingredients) {
        this.name = name;
        this.preparationTime = preparationTime;
        this.requiredEquipment = requiredEquipment;
        this.ingredients = ingredients;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getPreparationTime() {
        return preparationTime;
    }

    public long getPreparationTimeMs() {
        // Backward compatibility
        return preparationTime;
    }

    public List<String> getRequiredEquipment() {
        return requiredEquipment;
    }

    public Map<String, Integer> getIngredients() {
        return ingredients;
    }

    // Pre-defined dishes as per specification
    public static final Dish DESSERT = new Dish(
            "Ice Cream",
            Constants.PREP_TIME_URGENT,
            Collections.emptyList(), // No equipment needed
            Map.of("Milk", 1, "Sugar", 1)
    );

    public static final Dish STEAK = new Dish(
            "Grilled Steak",
            Constants.PREP_TIME_NORMAL,
            Arrays.asList("Grill1"),
            Map.of("Meat", 1)
    );

    public static final Dish PIZZA = new Dish(
            "Pizza Margherita",
            Constants.PREP_TIME_SLOW,
            Arrays.asList("Oven1"),
            Map.of("Dough", 1, "Cheese", 2, "Tomato", 2)
    );

    @Override
    public String toString() {
        return "Dish{" +
                "name='" + name + '\'' +
                ", prepTime=" + preparationTime + "ms" +
                '}';
    }
}
