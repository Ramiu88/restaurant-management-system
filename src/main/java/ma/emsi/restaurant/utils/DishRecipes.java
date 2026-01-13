package ma.emsi.restaurant.utils;

/**
 * Recipe Guide - Maps dishes to required kitchen equipment
 *
 * This is a reference/helper class for Cooks to know:
 * - What equipment a dish needs
 * - Which KitchenManager methods to call
 *
 * @author Marwan
 */
public class DishRecipes {

    // Dish name constants
    public static final String PIZZA = "Pizza";
    public static final String STEAK = "Steak";
    public static final String PASTA = "Pasta";
    public static final String FRIES = "Fries";
    public static final String GRILLED_FISH = "Grilled Fish";
    public static final String COMPLEX_DISH = "Complex Dish";

    /**
     * Get description of equipment needed for a dish
     */
    public static String getEquipmentNeeded(String dishName) {
        switch (dishName) {
            case PIZZA:
                return "Oven + Fryer (use prepareComplexDish)";
            case STEAK:
                return "Grill (use useGrill)";
            case PASTA:
                return "Oven (use useOven)";
            case FRIES:
                return "Fryer (use useFryer)";
            case GRILLED_FISH:
                return "Grill (use useGrill)";
            case COMPLEX_DISH:
                return "Oven + Fryer (use prepareComplexDish)";
            default:
                return "Oven (use useOven)";
        }
    }

    /**
     * Get preparation time estimate in milliseconds
     */
    public static long getPreparationTime(String dishName) {
        switch (dishName) {
            case PIZZA:
                return 3000;
            case STEAK:
                return 2000;
            case PASTA:
                return 2500;
            case FRIES:
                return 1000;
            case GRILLED_FISH:
                return 2500;
            case COMPLEX_DISH:
                return 4000;
            default:
                return 2000;
        }
    }

    /**
     * Print complete recipe guide to console
     */
    public static void printRecipeGuide() {
        System.out.println("\n=== KITCHEN RECIPE GUIDE ===");
        System.out.println("Pizza         : Oven + Fryer (prepareComplexDish)");
        System.out.println("Steak         : Grill (useGrill)");
        System.out.println("Pasta         : Oven (useOven)");
        System.out.println("Fries         : Fryer (useFryer)");
        System.out.println("Grilled Fish  : Grill (useGrill)");
        System.out.println("Complex Dish  : Oven + Fryer (prepareComplexDish)");
        System.out.println("============================\n");
    }

    /**
     * Example usage for Cooks
     */
    public static void printUsageExample() {
        System.out.println("\n=== USAGE EXAMPLE FOR COOKS ===");
        System.out.println("// Making Pizza:");
        System.out.println("kitchenManager.prepareComplexDish(3000);");
        System.out.println();
        System.out.println("// Making Steak:");
        System.out.println("kitchenManager.useGrill(2000);");
        System.out.println();
        System.out.println("// Making Pasta:");
        System.out.println("kitchenManager.useOven(2500);");
        System.out.println("===============================\n");
    }

    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        System.out.println("Testing DishRecipes class...\n");

        printRecipeGuide();

        System.out.println("Pizza needs: " + getEquipmentNeeded(PIZZA));
        System.out.println("Steak needs: " + getEquipmentNeeded(STEAK));
        System.out.println("Pizza prep time: " + getPreparationTime(PIZZA) + "ms");

        printUsageExample();
    }
}
