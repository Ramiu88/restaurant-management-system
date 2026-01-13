package ma.emsi.restaurant.entities;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JUnit tests for Dish entity
 * Tests pre-defined dishes, equipment requirements, and ingredients
 */
public class DishTest {

    // ===== PRE-DEFINED DISH TESTS =====

    @Test
    public void testDessertDish() {
        Dish dessert = Dish.DESSERT;

        assertEquals("Dessert name should be Ice Cream", "Ice Cream", dessert.getName());
        assertEquals("Dessert prep time should match urgent", 500, dessert.getPreparationTime());
        assertEquals("Dessert should have no equipment", Collections.emptyList(), dessert.getRequiredEquipment());
        assertNotNull("Dessert should have ingredients", dessert.getIngredients());
        assertTrue("Dessert should contain Milk", dessert.getIngredients().containsKey("Milk"));
        assertTrue("Dessert should contain Sugar", dessert.getIngredients().containsKey("Sugar"));
    }

    @Test
    public void testSteakDish() {
        Dish steak = Dish.STEAK;

        assertEquals("Steak name should be Grilled Steak", "Grilled Steak", steak.getName());
        assertEquals("Steak prep time should be normal", 3000, steak.getPreparationTime());
        assertEquals("Steak should require Grill", Arrays.asList("Grill1"), steak.getRequiredEquipment());
        assertNotNull("Steak should have ingredients", steak.getIngredients());
        assertTrue("Steak should contain Meat", steak.getIngredients().containsKey("Meat"));
    }

    @Test
    public void testPizzaDish() {
        Dish pizza = Dish.PIZZA;

        assertEquals("Pizza name should be Pizza Margherita", "Pizza Margherita", pizza.getName());
        assertEquals("Pizza prep time should be slow", 5000, pizza.getPreparationTime());
        assertEquals("Pizza should require Oven", Arrays.asList("Oven1"), pizza.getRequiredEquipment());
        assertNotNull("Pizza should have ingredients", pizza.getIngredients());
        assertTrue("Pizza should contain Dough", pizza.getIngredients().containsKey("Dough"));
        assertTrue("Pizza should contain Cheese", pizza.getIngredients().containsKey("Cheese"));
        assertTrue("Pizza should contain Tomato", pizza.getIngredients().containsKey("Tomato"));
    }

    // ===== CONSTRUCTOR TESTS =====

    @Test
    public void testCustomDish() {
        List<String> equipment = Arrays.asList("Oven1", "Grill1");
        Map<String, Integer> ingredients = Map.of("Flour", 2, "Eggs", 3);

        Dish customDish = new Dish("Custom Pancakes", 2000, equipment, ingredients);

        assertEquals("Name should match", "Custom Pancakes", customDish.getName());
        assertEquals("Prep time should match", 2000, customDish.getPreparationTime());
        assertEquals("Equipment should match", equipment, customDish.getRequiredEquipment());
        assertEquals("Ingredients should match", ingredients, customDish.getIngredients());
    }

    @Test
    public void testCustomDish_NoEquipment() {
        Map<String, Integer> ingredients = Map.of("Water", 1);

        Dish simpleDish = new Dish("Water", 0, Collections.emptyList(), ingredients);

        assertEquals("Should have no equipment", 0, simpleDish.getRequiredEquipment().size());
        assertEquals("Should have ingredients", 1, simpleDish.getIngredients().size());
    }

    @Test
    public void testCustomDish_NoIngredients() {
        List<String> equipment = Arrays.asList("Plate");

        Dish emptyDish = new Dish("Empty Plate", 0, equipment, Map.of());

        assertTrue("Should have equipment", emptyDish.getRequiredEquipment().contains("Plate"));
        assertTrue("Should have no ingredients", emptyDish.getIngredients().isEmpty());
    }

    // ===== GETTER TESTS =====

    @Test
    public void testGetName() {
        Dish dish = new Dish("Test Dish", 1000, Collections.emptyList(), Map.of());
        assertEquals("getName should return dish name", "Test Dish", dish.getName());
    }

    @Test
    public void testGetPreparationTime() {
        Dish dish = new Dish("Test", 2500, Collections.emptyList(), Map.of());
        assertEquals("getPreparationTime should return 2500", 2500, dish.getPreparationTime());
    }

    @Test
    public void testGetPreparationTimeMs_BackwardCompatibility() {
        Dish dish = new Dish("Test", 1500, Collections.emptyList(), Map.of());
        assertEquals("getPreparationTimeMs should match getPreparationTime", 1500, dish.getPreparationTimeMs());
    }

    @Test
    public void testGetRequiredEquipment() {
        List<String> equipment = Arrays.asList("Oven1", "Fryer", "Grill1");
        Dish dish = new Dish("Complex", 3000, equipment, Map.of());

        List<String> retrieved = dish.getRequiredEquipment();
        assertEquals("Should have 3 equipment items", 3, retrieved.size());
        assertTrue("Should contain Oven1", retrieved.contains("Oven1"));
        assertTrue("Should contain Fryer", retrieved.contains("Fryer"));
        assertTrue("Should contain Grill1", retrieved.contains("Grill1"));
    }

    @Test
    public void testGetIngredients() {
        Map<String, Integer> ingredients = Map.of(
            "Flour", 500,
            "Sugar", 100,
            "Eggs", 200
        );
        Dish dish = new Dish("Cake", 4000, Collections.emptyList(), ingredients);

        Map<String, Integer> retrieved = dish.getIngredients();
        assertEquals("Should have 3 ingredients", 3, retrieved.size());
        assertEquals("Flour amount should be 500", 500, (int) retrieved.get("Flour"));
        assertEquals("Sugar amount should be 100", 100, (int) retrieved.get("Sugar"));
        assertEquals("Eggs amount should be 200", 200, (int) retrieved.get("Eggs"));
    }

    // ===== TO STRING TEST =====

    @Test
    public void testToString() {
        Dish dish = new Dish("Test Dish", 1234, Collections.emptyList(), Map.of());
        String str = dish.toString();

        assertTrue("toString should contain name", str.contains("Test Dish"));
        assertTrue("toString should contain prep time", str.contains("1234"));
    }

    // ===== INGREDIENT QUANTITY TESTS =====

    @Test
    public void testIngredientQuantities_Pizza() {
        Dish pizza = Dish.PIZZA;
        Map<String, Integer> ingredients = pizza.getIngredients();

        assertEquals("Pizza should need 1 Dough", 1, (int) ingredients.get("Dough"));
        assertEquals("Pizza should need 2 Cheese", 2, (int) ingredients.get("Cheese"));
        assertEquals("Pizza should need 2 Tomato", 2, (int) ingredients.get("Tomato"));
    }

    @Test
    public void testIngredientQuantities_Dessert() {
        Dish dessert = Dish.DESSERT;
        Map<String, Integer> ingredients = dessert.getIngredients();

        assertEquals("Dessert should need 1 Milk", 1, (int) ingredients.get("Milk"));
        assertEquals("Dessert should need 1 Sugar", 1, (int) ingredients.get("Sugar"));
    }

    // ===== PREPARATION TIME CATEGORIES =====

    @Test
    public void testPreparationTimeCategories() {
        assertTrue("Dessert should be urgent (500ms)", Dish.DESSERT.getPreparationTime() <= 500);
        assertTrue("Steak should be normal (3000ms)", Dish.STEAK.getPreparationTime() <= 3000 && Dish.STEAK.getPreparationTime() > 500);
        assertTrue("Pizza should be slow (5000ms)", Dish.PIZZA.getPreparationTime() <= 5000 && Dish.PIZZA.getPreparationTime() > 3000);
    }

    // ===== EQUIPMENT REQUIREMENTS =====

    @Test
    public void testEquipmentRequirements_DessertHasNone() {
        assertTrue("Dessert should require no equipment", Dish.DESSERT.getRequiredEquipment().isEmpty());
    }

    @Test
    public void testEquipmentRequirements_SteakNeedsGrill() {
        List<String> equipment = Dish.STEAK.getRequiredEquipment();
        assertEquals("Steak should need 1 equipment", 1, equipment.size());
        assertEquals("Steak should need Grill1", "Grill1", equipment.get(0));
    }

    @Test
    public void testEquipmentRequirements_PizzaNeedsOven() {
        List<String> equipment = Dish.PIZZA.getRequiredEquipment();
        assertEquals("Pizza should need 1 equipment", 1, equipment.size());
        assertEquals("Pizza should need Oven1", "Oven1", equipment.get(0));
    }

    // ===== IMMUTABILITY TESTS =====

    @Test
    public void testPreDefinedDishes_AreSingletons() {
        Dish dessert1 = Dish.DESSERT;
        Dish dessert2 = Dish.DESSERT;

        assertSame("DESSERT should be the same instance", dessert1, dessert2);
    }

    // ===== EDGE CASE TESTS =====

    @Test
    public void testZeroPreparationTime() {
        Dish instant = new Dish("Instant", 0, Collections.emptyList(), Map.of());
        assertEquals("Should handle zero prep time", 0, instant.getPreparationTime());
    }

    @Test
    public void testLargePreparationTime() {
        int largeTime = 1000000; // ~16 minutes
        Dish slow = new Dish("Very Slow", largeTime, Collections.emptyList(), Map.of());
        assertEquals("Should handle large prep time", largeTime, slow.getPreparationTime());
    }

    @Test
    public void testMultipleEquipment() {
        List<String> equipment = Arrays.asList("Oven1", "Oven2", "Grill1", "Fryer");
        Dish complex = new Dish("Complex", 5000, equipment, Map.of());

        assertEquals("Should have 4 equipment items", 4, complex.getRequiredEquipment().size());
    }

    @Test
    public void testEmptyIngredientList() {
        Dish noIngredients = new Dish("Air", 0, Collections.emptyList(), Map.of());
        assertTrue("Should have no ingredients", noIngredients.getIngredients().isEmpty());
    }
}
