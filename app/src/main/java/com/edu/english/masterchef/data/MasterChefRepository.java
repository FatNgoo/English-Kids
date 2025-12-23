package com.edu.english.masterchef.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Repository providing mock data for Master Chef game.
 * Contains ingredients, tools, recipes, chefs, and customer orders.
 */
public class MasterChefRepository {
    
    private static MasterChefRepository instance;
    
    private Map<String, Ingredient> ingredients;
    private Map<String, Tool> tools;
    private Map<String, Recipe> recipes;
    private Map<String, ChefCharacter> chefs;
    private Map<String, CustomerOrder> orders;
    
    private String lastOrderId = null;
    private Random random = new Random();
    
    private MasterChefRepository() {
        initIngredients();
        initTools();
        initChefs();
        initCustomerOrders();
        initRecipes();
    }
    
    public static synchronized MasterChefRepository getInstance() {
        if (instance == null) {
            instance = new MasterChefRepository();
        }
        return instance;
    }
    
    // ========================
    // CHEF CHARACTERS
    // ========================
    private void initChefs() {
        chefs = new HashMap<>();
        
        chefs.put("chef_bunny", new ChefCharacter.Builder()
            .id("chef_bunny")
            .displayName("Chef Bunny")
            .displayNameVi("Đầu bếp Thỏ")
            .avatar("ic_chef_bunny")
            .bubbleColor("#FFE0B2")  // Light orange
            .speechRate(0.85f)
            .pitch(1.2f)  // Slightly higher, friendly voice
            .unlocked(true)
            .unlockStars(0)
            .greeting("Hi there, little chef! Let's cook something yummy!")
            .praise("Wow! You're doing great!")
            .encourage("You can do it! Try again!")
            .build());
        
        chefs.put("chef_panda", new ChefCharacter.Builder()
            .id("chef_panda")
            .displayName("Chef Panda")
            .displayNameVi("Đầu bếp Gấu Trúc")
            .avatar("ic_chef_panda")
            .bubbleColor("#C8E6C9")  // Light green
            .speechRate(0.8f)
            .pitch(1.0f)  // Normal, calm voice
            .unlocked(true)
            .unlockStars(0)
            .greeting("Hello, young chef! Ready to make delicious food?")
            .praise("Excellent work! That looks delicious!")
            .encourage("Don't worry, let's try one more time!")
            .build());
        
        chefs.put("chef_cat", new ChefCharacter.Builder()
            .id("chef_cat")
            .displayName("Chef Kitty")
            .displayNameVi("Đầu bếp Mèo")
            .avatar("ic_chef_cat")
            .bubbleColor("#E1BEE7")  // Light purple
            .speechRate(0.9f)
            .pitch(1.3f)  // Higher pitch, cute voice
            .unlocked(false)
            .unlockStars(50)
            .greeting("Meow! Let's cook together, shall we?")
            .praise("Purr-fect! You're amazing!")
            .encourage("Meow, try again, little one!")
            .build());
    }
    
    // ========================
    // CUSTOMER ORDERS
    // ========================
    private void initCustomerOrders() {
        orders = new HashMap<>();
        
        // Order for Fried Rice
        orders.put("order_fried_rice", new CustomerOrder.Builder()
            .orderId("order_fried_rice")
            .recipeId("fried_rice")
            .customerName("Bear")
            .customerNameVi("Gấu")
            .customerAvatar("ic_customer_bear")
            .introTextEn("Hi! I'd like some yummy Fried Rice, please!")
            .introTextVi("Xin chào! Tôi muốn Cơm Chiên ngon nhé!")
            .thankYouEn("Wow, this Fried Rice is delicious! Thank you!")
            .thankYouVi("Wow, Cơm Chiên này ngon quá! Cảm ơn!")
            .difficulty(1)
            .bonusStars(2)
            .estimatedTime(180000)
            .build());
        
        // Order for Omelette
        orders.put("order_omelette", new CustomerOrder.Builder()
            .orderId("order_omelette")
            .recipeId("omelette")
            .customerName("Rabbit")
            .customerNameVi("Thỏ")
            .customerAvatar("ic_customer_rabbit")
            .introTextEn("Hello! Can you make me an Omelette?")
            .introTextVi("Xin chào! Bạn làm Trứng Ốp Lết cho tôi nhé?")
            .thankYouEn("This Omelette is perfect! Yummy!")
            .thankYouVi("Trứng Ốp Lết này hoàn hảo! Ngon quá!")
            .difficulty(1)
            .bonusStars(2)
            .estimatedTime(120000)
            .build());
        
        // Order for Carrot Soup
        orders.put("order_carrot_soup", new CustomerOrder.Builder()
            .orderId("order_carrot_soup")
            .recipeId("carrot_soup")
            .customerName("Mouse")
            .customerNameVi("Chuột")
            .customerAvatar("ic_customer_mouse")
            .introTextEn("I'm feeling cold. Can I have some Carrot Soup?")
            .introTextVi("Tôi thấy lạnh. Cho tôi Súp Cà Rốt nhé?")
            .thankYouEn("So warm and tasty! Thank you, chef!")
            .thankYouVi("Ấm và ngon quá! Cảm ơn đầu bếp!")
            .difficulty(1)
            .bonusStars(2)
            .estimatedTime(150000)
            .build());
        
        // Order for Grilled Fish
        orders.put("order_grilled_fish", new CustomerOrder.Builder()
            .orderId("order_grilled_fish")
            .recipeId("grilled_fish")
            .customerName("Cat")
            .customerNameVi("Mèo")
            .customerAvatar("ic_customer_cat")
            .introTextEn("Meow! I'd love some Grilled Fish!")
            .introTextVi("Meo meo! Tôi thích Cá Nướng!")
            .thankYouEn("Purr-fect fish! I love it!")
            .thankYouVi("Cá ngon tuyệt! Tôi thích lắm!")
            .difficulty(2)
            .bonusStars(3)
            .estimatedTime(200000)
            .build());
        
        // Order for Steamed Dumplings
        orders.put("order_dumplings", new CustomerOrder.Builder()
            .orderId("order_dumplings")
            .recipeId("steamed_dumplings")
            .customerName("Penguin")
            .customerNameVi("Chim cánh cụt")
            .customerAvatar("ic_customer_penguin")
            .introTextEn("Hello! I want some Steamed Dumplings!")
            .introTextVi("Xin chào! Tôi muốn Bánh Bao Hấp!")
            .thankYouEn("These dumplings are so soft! Amazing!")
            .thankYouVi("Bánh bao mềm quá! Tuyệt vời!")
            .difficulty(2)
            .bonusStars(3)
            .estimatedTime(180000)
            .build());
        
        // Order for Baked Cookies
        orders.put("order_cookies", new CustomerOrder.Builder()
            .orderId("order_cookies")
            .recipeId("baked_cookies")
            .customerName("Elephant")
            .customerNameVi("Voi")
            .customerAvatar("ic_customer_elephant")
            .introTextEn("Hi! Can you bake some Cookies for me?")
            .introTextVi("Xin chào! Bạn nướng Bánh Quy cho tôi nhé?")
            .thankYouEn("Yum! These cookies are so crunchy!")
            .thankYouVi("Ngon! Bánh quy giòn quá!")
            .difficulty(2)
            .bonusStars(3)
            .estimatedTime(220000)
            .build());
        
        // Order for French Fries
        orders.put("order_fries", new CustomerOrder.Builder()
            .orderId("order_fries")
            .recipeId("french_fries")
            .customerName("Dog")
            .customerNameVi("Chó")
            .customerAvatar("ic_customer_dog")
            .introTextEn("Woof! I want some crispy French Fries!")
            .introTextVi("Gâu gâu! Tôi muốn Khoai Tây Chiên giòn!")
            .thankYouEn("So crispy! Best fries ever!")
            .thankYouVi("Giòn quá! Khoai chiên ngon nhất!")
            .difficulty(2)
            .bonusStars(3)
            .estimatedTime(180000)
            .build());
        
        // Order for Noodle Soup
        orders.put("order_noodle_soup", new CustomerOrder.Builder()
            .orderId("order_noodle_soup")
            .recipeId("noodle_soup")
            .customerName("Pig")
            .customerNameVi("Heo")
            .customerAvatar("ic_customer_pig")
            .introTextEn("Oink! I'd like a big bowl of Noodle Soup!")
            .introTextVi("Ủn ỉn! Tôi muốn tô Mì Nước lớn!")
            .thankYouEn("Slurp! This noodle soup is amazing!")
            .thankYouVi("Húp! Mì nước ngon quá!")
            .difficulty(2)
            .bonusStars(3)
            .estimatedTime(200000)
            .build());
    }
    
    // ========================
    // INGREDIENTS (16 items)
    // ========================
    private void initIngredients() {
        ingredients = new HashMap<>();
        
        // Vegetables
        ingredients.put("carrot", new Ingredient(
            "carrot", "Carrot", "Cà rốt", "ic_ingredient_carrot",
            "vegetable", true, false, false
        ));
        ingredients.put("onion", new Ingredient(
            "onion", "Onion", "Hành tây", "ic_ingredient_onion",
            "vegetable", true, false, false
        ));
        ingredients.put("potato", new Ingredient(
            "potato", "Potato", "Khoai tây", "ic_ingredient_potato",
            "vegetable", true, false, false
        ));
        ingredients.put("tomato", new Ingredient(
            "tomato", "Tomato", "Cà chua", "ic_ingredient_tomato",
            "vegetable", true, false, false
        ));
        ingredients.put("garlic", new Ingredient(
            "garlic", "Garlic", "Tỏi", "ic_ingredient_garlic",
            "vegetable", true, false, false
        ));
        ingredients.put("mushroom", new Ingredient(
            "mushroom", "Mushroom", "Nấm", "ic_ingredient_mushroom",
            "vegetable", true, false, false
        ));
        ingredients.put("broccoli", new Ingredient(
            "broccoli", "Broccoli", "Súp lơ xanh", "ic_ingredient_broccoli",
            "vegetable", true, false, false
        ));
        
        // Protein
        ingredients.put("egg", new Ingredient(
            "egg", "Egg", "Trứng", "ic_ingredient_egg",
            "protein", false, false, false
        ));
        ingredients.put("chicken", new Ingredient(
            "chicken", "Chicken", "Thịt gà", "ic_ingredient_chicken",
            "protein", true, true, false
        ));
        ingredients.put("beef", new Ingredient(
            "beef", "Beef", "Thịt bò", "ic_ingredient_beef",
            "protein", true, true, false
        ));
        ingredients.put("shrimp", new Ingredient(
            "shrimp", "Shrimp", "Tôm", "ic_ingredient_shrimp",
            "protein", false, false, false
        ));
        
        // Carbs
        ingredients.put("rice", new Ingredient(
            "rice", "Rice", "Gạo", "ic_ingredient_rice",
            "carb", false, false, false
        ));
        ingredients.put("noodle", new Ingredient(
            "noodle", "Noodle", "Mì", "ic_ingredient_noodle",
            "carb", false, false, false
        ));
        
        // Liquids
        ingredients.put("oil", new Ingredient(
            "oil", "Oil", "Dầu ăn", "ic_ingredient_oil",
            "liquid", false, false, true
        ));
        ingredients.put("water", new Ingredient(
            "water", "Water", "Nước", "ic_ingredient_water",
            "liquid", false, false, true
        ));
        
        // Seasoning
        ingredients.put("salt", new Ingredient(
            "salt", "Salt", "Muối", "ic_ingredient_salt",
            "seasoning", false, false, false
        ));
        
        // === NEW INGREDIENTS ===
        
        // Baking
        ingredients.put("dough", new Ingredient(
            "dough", "Dough", "Bột nhào", "ic_ingredient_dough",
            "baking", false, false, false
        ));
        ingredients.put("butter", new Ingredient(
            "butter", "Butter", "Bơ", "ic_ingredient_butter",
            "dairy", false, false, false
        ));
        ingredients.put("flour", new Ingredient(
            "flour", "Flour", "Bột mì", "ic_ingredient_flour",
            "baking", false, false, false
        ));
        
        // Protein
        ingredients.put("fish", new Ingredient(
            "fish", "Fish", "Cá", "ic_ingredient_fish",
            "protein", false, false, false
        ));
        ingredients.put("dumpling", new Ingredient(
            "dumpling", "Dumpling", "Bánh bao", "ic_ingredient_dumpling",
            "prepared", false, false, false
        ));
        
        // Herbs and spices
        ingredients.put("pepper", new Ingredient(
            "pepper", "Pepper", "Tiêu", "ic_ingredient_pepper",
            "seasoning", false, false, false
        ));
        ingredients.put("herbs", new Ingredient(
            "herbs", "Herbs", "Rau thơm", "ic_ingredient_herbs",
            "garnish", true, false, false
        ));
    }
    
    // ========================
    // TOOLS (20+ items)
    // ========================
    private void initTools() {
        tools = new HashMap<>();
        
        // === BASIC TOOLS ===
        tools.put("cutting_board", new Tool(
            "cutting_board", "Cutting Board", "Thớt", "ic_tool_cutting_board",
            ActionType.TAP_TO_CUT, ZoneType.BOARD_ZONE
        ));
        tools.put("knife", new Tool(
            "knife", "Knife", "Dao", "ic_tool_knife",
            ActionType.TAP_TO_CUT, ZoneType.BOARD_ZONE
        ));
        tools.put("meat_mallet", new Tool(
            "meat_mallet", "Meat Mallet", "Búa đập thịt", "ic_tool_mallet",
            ActionType.TAP_TO_POUND, ZoneType.BOARD_ZONE
        ));
        tools.put("pan", new Tool(
            "pan", "Pan", "Chảo", "ic_tool_pan",
            ActionType.SHAKE_PAN, ZoneType.PAN_ZONE
        ));
        tools.put("pot", new Tool(
            "pot", "Pot", "Nồi", "ic_tool_pot",
            ActionType.SWIPE_TO_STIR, ZoneType.POT_ZONE
        ));
        tools.put("ladle", new Tool(
            "ladle", "Ladle", "Muôi", "ic_tool_ladle",
            ActionType.SWIPE_TO_STIR, ZoneType.POT_ZONE
        ));
        tools.put("spatula", new Tool(
            "spatula", "Spatula", "Xẻng", "ic_tool_spatula",
            ActionType.SHAKE_PAN, ZoneType.PAN_ZONE
        ));
        tools.put("plate", new Tool(
            "plate", "Plate", "Đĩa", "ic_tool_plate",
            ActionType.SERVE, ZoneType.SERVE_ZONE
        ));
        
        // === OVEN TOOLS ===
        tools.put("baking_tray", new Tool(
            "baking_tray", "Baking Tray", "Khay nướng", "ic_tool_baking_tray",
            ActionType.DRAG_TO_ZONE, ZoneType.OVEN_ZONE
        ));
        tools.put("oven_mitts", new Tool(
            "oven_mitts", "Oven Mitts", "Găng tay lò", "ic_tool_oven_mitts",
            ActionType.DRAG_TO_ZONE, ZoneType.OVEN_ZONE
        ));
        tools.put("oven_knob_temp", new Tool(
            "oven_knob_temp", "Temperature Knob", "Núm nhiệt độ", "ic_tool_oven_knob",
            ActionType.ROTATE_KNOB_TEMP, ZoneType.OVEN_ZONE
        ));
        tools.put("oven_knob_timer", new Tool(
            "oven_knob_timer", "Timer Knob", "Núm hẹn giờ", "ic_tool_timer_knob",
            ActionType.ROTATE_KNOB_TIMER, ZoneType.OVEN_ZONE
        ));
        
        // === STEAMER TOOLS ===
        tools.put("steamer_basket", new Tool(
            "steamer_basket", "Steamer Basket", "Giỏ hấp", "ic_tool_steamer_basket",
            ActionType.DRAG_TO_ZONE, ZoneType.STEAMER_ZONE
        ));
        tools.put("lid", new Tool(
            "lid", "Lid", "Nắp nồi", "ic_tool_lid",
            ActionType.DRAG_TO_ZONE, ZoneType.STEAMER_ZONE
        ));
        
        // === GRILL TOOLS ===
        tools.put("tongs", new Tool(
            "tongs", "Tongs", "Kẹp gắp", "ic_tool_tongs",
            ActionType.DRAG_TO_ZONE, ZoneType.GRILL_ZONE
        ));
        tools.put("grill_brush", new Tool(
            "grill_brush", "Grill Brush", "Bàn chải nướng", "ic_tool_grill_brush",
            ActionType.SWIPE_TO_STIR, ZoneType.GRILL_ZONE
        ));
        
        // === FRYER TOOLS ===
        tools.put("fryer_basket", new Tool(
            "fryer_basket", "Fryer Basket", "Giỏ chiên", "ic_tool_fryer_basket",
            ActionType.SHAKE_BASKET, ZoneType.FRYER_ZONE
        ));
        tools.put("thermometer", new Tool(
            "thermometer", "Thermometer", "Nhiệt kế", "ic_tool_thermometer",
            ActionType.DRAG_TO_ZONE, ZoneType.FRYER_ZONE
        ));
        
        // === MIXING TOOLS ===
        tools.put("whisk", new Tool(
            "whisk", "Whisk", "Phới đánh trứng", "ic_tool_whisk",
            ActionType.WHISK_CIRCLES, ZoneType.MIXING_ZONE
        ));
        tools.put("rolling_pin", new Tool(
            "rolling_pin", "Rolling Pin", "Cây cán bột", "ic_tool_rolling_pin",
            ActionType.KNEAD_DRAG, ZoneType.COUNTER_ZONE
        ));
        tools.put("mixing_bowl", new Tool(
            "mixing_bowl", "Mixing Bowl", "Tô trộn", "ic_tool_mixing_bowl",
            ActionType.DRAG_TO_ZONE, ZoneType.MIXING_ZONE
        ));
        
        // === GARNISH TOOLS ===
        tools.put("spice_shaker", new Tool(
            "spice_shaker", "Spice Shaker", "Lọ gia vị", "ic_tool_spice_shaker",
            ActionType.SEASON_SHAKE, ZoneType.GARNISH_ZONE
        ));
        tools.put("salt_shaker", new Tool(
            "salt_shaker", "Salt Shaker", "Lọ muối", "ic_tool_salt_shaker",
            ActionType.SEASON_SHAKE, ZoneType.SERVE_ZONE
        ));
        tools.put("pepper_shaker", new Tool(
            "pepper_shaker", "Pepper Shaker", "Lọ tiêu", "ic_tool_pepper_shaker",
            ActionType.SEASON_SHAKE, ZoneType.SERVE_ZONE
        ));
        
        // === DRAINING TOOLS ===
        tools.put("drain_rack", new Tool(
            "drain_rack", "Drain Rack", "Vỉ để ráo", "ic_tool_drain_rack",
            ActionType.DRAG_TO_ZONE, ZoneType.DRAIN_ZONE
        ));
        tools.put("paper_towel", new Tool(
            "paper_towel", "Paper Towel", "Giấy thấm dầu", "ic_tool_paper_towel",
            ActionType.DRAG_TO_ZONE, ZoneType.DRAIN_ZONE
        ));
    }
    
    // ========================
    // RECIPES (12 recipes)
    // ========================
    private void initRecipes() {
        recipes = new HashMap<>();
        
        // Existing 6 recipes
        recipes.put("carrot_soup", createCarrotSoupRecipe());
        recipes.put("stir_fried_veggies", createStirFriedVeggiesRecipe());
        recipes.put("fried_egg", createFriedEggRecipe());
        recipes.put("beef_steak", createBeefSteakRecipe());
        recipes.put("chicken_noodle_soup", createChickenNoodleSoupRecipe());
        recipes.put("hotpot", createHotpotRecipe());
        
        // NEW: 6 advanced recipes
        recipes.put("baked_cookies", createBakedCookiesRecipe());
        recipes.put("steamed_dumplings", createSteamedDumplingsRecipe());
        recipes.put("grilled_fish", createGrilledFishRecipe());
        recipes.put("french_fries", createFrenchFriesRecipe());
        recipes.put("noodle_soup", createNoodleSoupRecipe());
        recipes.put("omelette", createOmeletteRecipe());
        
        // NEW: 4 additional recipes
        recipes.put("baked_pizza", createBakedPizzaRecipe());
        recipes.put("steamed_vegetables", createSteamedVegetablesRecipe());
        recipes.put("grilled_steak", createGrilledSteakRecipe());
        recipes.put("fried_chicken", createFriedChickenRecipe());
        
        // Fried Rice - User's favorite recipe
        recipes.put("fried_rice", createFriedRiceRecipe());
    }
    
    // === RECIPE 1: Carrot Soup ===
    // Streamlined: each step shows only needed items (max 3-4)
    private Recipe createCarrotSoupRecipe() {
        List<RecipeStep> steps = new ArrayList<>();
        
        // Step 1: Get carrot
        steps.add(new RecipeStep.Builder()
            .stepNumber(1)
            .chefText("First, let's get a carrot from the fridge!")
            .requiredItems(Arrays.asList("carrot"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.COUNTER_ZONE)
            .inventoryForStep("carrot")
            .successText("Great job! You got the carrot!")
            .speakingPhrase("get the carrot")
            .build());
        
        // Step 2: Put carrot on cutting board
        steps.add(new RecipeStep.Builder()
            .stepNumber(2)
            .chefText("Now put the carrot on the cutting board!")
            .requiredItems(Arrays.asList("carrot"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.BOARD_ZONE)
            .inventoryForStep("carrot", "cutting_board")
            .successText("Perfect! The carrot is on the board!")
            .speakingPhrase("put the carrot on the board")
            .build());
        
        // Step 3: Cut carrot
        steps.add(new RecipeStep.Builder()
            .stepNumber(3)
            .chefText("Tap the knife to cut the carrot!")
            .requiredItems(Arrays.asList("carrot"))
            .requiredTool("knife")
            .action(ActionType.TAP_TO_CUT)
            .targetZone(ZoneType.BOARD_ZONE)
            .tapCount(5)
            .inventoryForStep("knife")
            .successText("Excellent! The carrot is sliced!")
            .speakingPhrase("cut the carrot")
            .build());
        
        // Step 4: Add water to pot
        steps.add(new RecipeStep.Builder()
            .stepNumber(4)
            .chefText("Add water to the pot!")
            .requiredItems(Arrays.asList("water"))
            .action(ActionType.POUR)
            .targetZone(ZoneType.POT_ZONE)
            .pourTime(1500)
            .inventoryForStep("pot", "water")
            .successText("The pot has water now!")
            .speakingPhrase("add water")
            .build());
        
        // Step 5: Add carrot to pot
        steps.add(new RecipeStep.Builder()
            .stepNumber(5)
            .chefText("Put the sliced carrot into the pot!")
            .requiredItems(Arrays.asList("carrot"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.POT_ZONE)
            .inventoryForStep("carrot")
            .successText("Carrot is in the pot!")
            .speakingPhrase("put the carrot in the pot")
            .build());
        
        // Step 6: Stir with ladle
        steps.add(new RecipeStep.Builder()
            .stepNumber(6)
            .chefText("Use the ladle to stir the soup!")
            .requiredTool("ladle")
            .action(ActionType.SWIPE_TO_STIR)
            .targetZone(ZoneType.POT_ZONE)
            .stirCount(6)
            .inventoryForStep("ladle")
            .successText("Yummy! The soup smells delicious!")
            .speakingPhrase("stir the soup")
            .build());
        
        // Step 7: Wait for cooking (no items)
        steps.add(new RecipeStep.Builder()
            .stepNumber(7)
            .chefText("Wait for the soup to cook!")
            .action(ActionType.WAIT_COOK)
            .targetZone(ZoneType.POT_ZONE)
            .cookTime(3000)
            .inventoryForStep() // No items - just wait
            .successText("The soup is ready!")
            .speakingPhrase("wait for the soup")
            .requiresSpeaking(false)
            .build());
        
        // Step 8: Serve
        steps.add(new RecipeStep.Builder()
            .stepNumber(8)
            .chefText("Serve the soup on a plate!")
            .action(ActionType.SERVE)
            .targetZone(ZoneType.SERVE_ZONE)
            .inventoryForStep("plate")
            .successText("Amazing! Carrot soup is served!")
            .speakingPhrase("serve the soup")
            .build());
        
        return new Recipe(
            "carrot_soup",
            "Carrot Soup",
            "Súp cà rốt",
            "A warm and healthy carrot soup",
            "ic_recipe_carrot_soup",
            1,
            steps,
            Arrays.asList("carrot", "water"),
            Arrays.asList("cutting_board", "knife", "pot", "ladle", "plate")
        );
    }
    
    // === RECIPE 2: Stir-fried Vegetables ===
    private Recipe createStirFriedVeggiesRecipe() {
        List<RecipeStep> steps = new ArrayList<>();
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(1)
            .chefText("Get a carrot and broccoli from the fridge!")
            .requiredItems(Arrays.asList("carrot", "broccoli"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.COUNTER_ZONE)
            .successText("Great! You got the vegetables!")
            .speakingPhrase("get the vegetables")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(2)
            .chefText("Cut the carrot on the cutting board!")
            .requiredItems(Arrays.asList("carrot"))
            .requiredTool("knife")
            .action(ActionType.TAP_TO_CUT)
            .targetZone(ZoneType.BOARD_ZONE)
            .tapCount(5)
            .successText("The carrot is sliced!")
            .speakingPhrase("cut the carrot")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(3)
            .chefText("Cut the broccoli too!")
            .requiredItems(Arrays.asList("broccoli"))
            .requiredTool("knife")
            .action(ActionType.TAP_TO_CUT)
            .targetZone(ZoneType.BOARD_ZONE)
            .tapCount(4)
            .successText("Broccoli is ready!")
            .speakingPhrase("cut the broccoli")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(4)
            .chefText("Pour oil into the pan!")
            .requiredItems(Arrays.asList("oil"))
            .action(ActionType.POUR)
            .targetZone(ZoneType.PAN_ZONE)
            .pourTime(1000)
            .successText("Oil is in the pan!")
            .speakingPhrase("add oil")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(5)
            .chefText("Put the vegetables in the pan!")
            .requiredItems(Arrays.asList("carrot", "broccoli"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.PAN_ZONE)
            .successText("Vegetables are in the pan!")
            .speakingPhrase("add the vegetables")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(6)
            .chefText("Shake the pan to stir-fry!")
            .action(ActionType.SHAKE_PAN)
            .targetZone(ZoneType.PAN_ZONE)
            .shakeCount(5)
            .successText("Sizzle! The vegetables are cooking!")
            .speakingPhrase("stir fry the vegetables")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(7)
            .chefText("Serve the stir-fried vegetables!")
            .action(ActionType.SERVE)
            .targetZone(ZoneType.SERVE_ZONE)
            .successText("Delicious! Stir-fried veggies are ready!")
            .speakingPhrase("serve the vegetables")
            .build());
        
        return new Recipe(
            "stir_fried_veggies",
            "Stir-fried Vegetables",
            "Rau xào",
            "Healthy and colorful stir-fried veggies",
            "ic_recipe_stir_fry",
            1,
            steps,
            Arrays.asList("carrot", "broccoli", "oil"),
            Arrays.asList("knife", "pan", "spatula", "plate")
        );
    }
    
    // === RECIPE 3: Fried Egg ===
    private Recipe createFriedEggRecipe() {
        List<RecipeStep> steps = new ArrayList<>();
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(1)
            .chefText("Get an egg from the fridge!")
            .requiredItems(Arrays.asList("egg"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.COUNTER_ZONE)
            .successText("You have an egg!")
            .speakingPhrase("get an egg")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(2)
            .chefText("Pour oil into the pan!")
            .requiredItems(Arrays.asList("oil"))
            .action(ActionType.POUR)
            .targetZone(ZoneType.PAN_ZONE)
            .pourTime(1000)
            .successText("Oil is sizzling!")
            .speakingPhrase("add oil to the pan")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(3)
            .chefText("Crack the egg into the pan!")
            .requiredItems(Arrays.asList("egg"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.PAN_ZONE)
            .successText("The egg is frying!")
            .speakingPhrase("crack the egg")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(4)
            .chefText("Wait for the egg to cook!")
            .action(ActionType.WAIT_COOK)
            .targetZone(ZoneType.PAN_ZONE)
            .cookTime(2500)
            .successText("The egg looks perfect!")
            .speakingPhrase("cook the egg")
            .requiresSpeaking(false)
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(5)
            .chefText("Add a little salt!")
            .requiredItems(Arrays.asList("salt"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.PAN_ZONE)
            .successText("Salt makes it tasty!")
            .speakingPhrase("add salt")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(6)
            .chefText("Serve the fried egg!")
            .action(ActionType.SERVE)
            .targetZone(ZoneType.SERVE_ZONE)
            .successText("Yummy! Fried egg is ready!")
            .speakingPhrase("serve the egg")
            .build());
        
        return new Recipe(
            "fried_egg",
            "Fried Egg",
            "Trứng chiên",
            "A simple and delicious fried egg",
            "ic_recipe_fried_egg",
            1,
            steps,
            Arrays.asList("egg", "oil", "salt"),
            Arrays.asList("pan", "spatula", "plate")
        );
    }
    
    // === RECIPE 4: Beef Steak ===
    private Recipe createBeefSteakRecipe() {
        List<RecipeStep> steps = new ArrayList<>();
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(1)
            .chefText("Get the beef from the fridge!")
            .requiredItems(Arrays.asList("beef"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.COUNTER_ZONE)
            .successText("Nice beef!")
            .speakingPhrase("get the beef")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(2)
            .chefText("Put the beef on the cutting board!")
            .requiredItems(Arrays.asList("beef"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.BOARD_ZONE)
            .successText("Beef is on the board!")
            .speakingPhrase("put the beef on the board")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(3)
            .chefText("Use the mallet to pound the beef!")
            .requiredItems(Arrays.asList("beef"))
            .requiredTool("meat_mallet")
            .action(ActionType.TAP_TO_POUND)
            .targetZone(ZoneType.BOARD_ZONE)
            .tapCount(6)
            .successText("The beef is tender now!")
            .speakingPhrase("pound the beef")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(4)
            .chefText("Add salt to the beef!")
            .requiredItems(Arrays.asList("salt"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.BOARD_ZONE)
            .successText("Seasoned!")
            .speakingPhrase("add salt")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(5)
            .chefText("Pour oil into the pan!")
            .requiredItems(Arrays.asList("oil"))
            .action(ActionType.POUR)
            .targetZone(ZoneType.PAN_ZONE)
            .pourTime(1000)
            .successText("Pan is hot!")
            .speakingPhrase("add oil")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(6)
            .chefText("Put the beef in the pan!")
            .requiredItems(Arrays.asList("beef"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.PAN_ZONE)
            .successText("The steak is sizzling!")
            .speakingPhrase("put the beef in the pan")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(7)
            .chefText("Shake the pan to cook evenly!")
            .action(ActionType.SHAKE_PAN)
            .targetZone(ZoneType.PAN_ZONE)
            .shakeCount(4)
            .successText("Perfect sear!")
            .speakingPhrase("shake the pan")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(8)
            .chefText("Wait for the steak to cook!")
            .action(ActionType.WAIT_COOK)
            .targetZone(ZoneType.PAN_ZONE)
            .cookTime(3500)
            .successText("Medium rare, perfect!")
            .speakingPhrase("wait for the steak")
            .requiresSpeaking(false)
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(9)
            .chefText("Serve the beef steak!")
            .action(ActionType.SERVE)
            .targetZone(ZoneType.SERVE_ZONE)
            .successText("Wow! A beautiful steak!")
            .speakingPhrase("serve the steak")
            .build());
        
        return new Recipe(
            "beef_steak",
            "Beef Steak",
            "Bít tết bò",
            "Juicy and tender beef steak",
            "ic_recipe_steak",
            2,
            steps,
            Arrays.asList("beef", "oil", "salt"),
            Arrays.asList("meat_mallet", "pan", "spatula", "plate")
        );
    }
    
    // === RECIPE 5: Chicken Noodle Soup ===
    private Recipe createChickenNoodleSoupRecipe() {
        List<RecipeStep> steps = new ArrayList<>();
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(1)
            .chefText("Get chicken and noodles!")
            .requiredItems(Arrays.asList("chicken", "noodle"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.COUNTER_ZONE)
            .successText("Ingredients ready!")
            .speakingPhrase("get chicken and noodles")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(2)
            .chefText("Cut the chicken on the board!")
            .requiredItems(Arrays.asList("chicken"))
            .requiredTool("knife")
            .action(ActionType.TAP_TO_CUT)
            .targetZone(ZoneType.BOARD_ZONE)
            .tapCount(5)
            .successText("Chicken is sliced!")
            .speakingPhrase("cut the chicken")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(3)
            .chefText("Add water to the pot!")
            .requiredItems(Arrays.asList("water"))
            .action(ActionType.POUR)
            .targetZone(ZoneType.POT_ZONE)
            .pourTime(1500)
            .successText("Water is in the pot!")
            .speakingPhrase("add water")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(4)
            .chefText("Put the chicken in the pot!")
            .requiredItems(Arrays.asList("chicken"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.POT_ZONE)
            .successText("Chicken is cooking!")
            .speakingPhrase("add the chicken")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(5)
            .chefText("Stir the soup with the ladle!")
            .requiredTool("ladle")
            .action(ActionType.SWIPE_TO_STIR)
            .targetZone(ZoneType.POT_ZONE)
            .stirCount(5)
            .successText("Smells good!")
            .speakingPhrase("stir the soup")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(6)
            .chefText("Add the noodles!")
            .requiredItems(Arrays.asList("noodle"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.POT_ZONE)
            .successText("Noodles are in!")
            .speakingPhrase("add noodles")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(7)
            .chefText("Stir again!")
            .requiredTool("ladle")
            .action(ActionType.SWIPE_TO_STIR)
            .targetZone(ZoneType.POT_ZONE)
            .stirCount(4)
            .successText("Almost ready!")
            .speakingPhrase("stir again")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(8)
            .chefText("Wait for everything to cook!")
            .action(ActionType.WAIT_COOK)
            .targetZone(ZoneType.POT_ZONE)
            .cookTime(3000)
            .successText("The soup is ready!")
            .speakingPhrase("wait for the soup")
            .requiresSpeaking(false)
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(9)
            .chefText("Serve the chicken noodle soup!")
            .action(ActionType.SERVE)
            .targetZone(ZoneType.SERVE_ZONE)
            .successText("Delicious soup is served!")
            .speakingPhrase("serve the soup")
            .build());
        
        return new Recipe(
            "chicken_noodle_soup",
            "Chicken Noodle Soup",
            "Mì gà",
            "Warm and comforting chicken noodle soup",
            "ic_recipe_noodle_soup",
            2,
            steps,
            Arrays.asList("chicken", "noodle", "water"),
            Arrays.asList("knife", "pot", "ladle", "plate")
        );
    }
    
    // === RECIPE 6: Hotpot ===
    private Recipe createHotpotRecipe() {
        List<RecipeStep> steps = new ArrayList<>();
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(1)
            .chefText("Get vegetables: mushroom, potato, tomato!")
            .requiredItems(Arrays.asList("mushroom", "potato", "tomato"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.COUNTER_ZONE)
            .successText("Lots of veggies!")
            .speakingPhrase("get the vegetables")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(2)
            .chefText("Get some shrimp and beef!")
            .requiredItems(Arrays.asList("shrimp", "beef"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.COUNTER_ZONE)
            .successText("Protein ready!")
            .speakingPhrase("get shrimp and beef")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(3)
            .chefText("Cut the potato!")
            .requiredItems(Arrays.asList("potato"))
            .requiredTool("knife")
            .action(ActionType.TAP_TO_CUT)
            .targetZone(ZoneType.BOARD_ZONE)
            .tapCount(5)
            .successText("Potato sliced!")
            .speakingPhrase("cut the potato")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(4)
            .chefText("Cut the beef into thin slices!")
            .requiredItems(Arrays.asList("beef"))
            .requiredTool("knife")
            .action(ActionType.TAP_TO_CUT)
            .targetZone(ZoneType.BOARD_ZONE)
            .tapCount(6)
            .successText("Beef is sliced thin!")
            .speakingPhrase("slice the beef")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(5)
            .chefText("Fill the pot with water!")
            .requiredItems(Arrays.asList("water"))
            .action(ActionType.POUR)
            .targetZone(ZoneType.POT_ZONE)
            .pourTime(2000)
            .successText("Pot is full!")
            .speakingPhrase("add water to the pot")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(6)
            .chefText("Add the tomato to make the broth!")
            .requiredItems(Arrays.asList("tomato"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.POT_ZONE)
            .successText("Tomato flavor!")
            .speakingPhrase("add tomato")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(7)
            .chefText("Stir the broth!")
            .requiredTool("ladle")
            .action(ActionType.SWIPE_TO_STIR)
            .targetZone(ZoneType.POT_ZONE)
            .stirCount(5)
            .successText("Broth is bubbling!")
            .speakingPhrase("stir the broth")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(8)
            .chefText("Add mushroom, potato, shrimp, and beef!")
            .requiredItems(Arrays.asList("mushroom", "potato", "shrimp", "beef"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.POT_ZONE)
            .successText("Everything is in the pot!")
            .speakingPhrase("add all ingredients")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(9)
            .chefText("Stir everything together!")
            .requiredTool("ladle")
            .action(ActionType.SWIPE_TO_STIR)
            .targetZone(ZoneType.POT_ZONE)
            .stirCount(6)
            .successText("Looking delicious!")
            .speakingPhrase("stir everything")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(10)
            .chefText("Let it cook!")
            .action(ActionType.WAIT_COOK)
            .targetZone(ZoneType.POT_ZONE)
            .cookTime(4000)
            .successText("Hotpot is ready!")
            .speakingPhrase("let it cook")
            .requiresSpeaking(false)
            .build());
        
        return new Recipe(
            "hotpot",
            "Hotpot",
            "Lẩu",
            "A fun hotpot with lots of ingredients",
            "ic_recipe_hotpot",
            3,
            steps,
            Arrays.asList("mushroom", "potato", "tomato", "shrimp", "beef", "water"),
            Arrays.asList("knife", "pot", "ladle")
        );
    }
    
    // ========================
    // GETTERS
    // ========================
    
    public Ingredient getIngredient(String id) {
        return ingredients.get(id);
    }
    
    public Tool getTool(String id) {
        return tools.get(id);
    }
    
    public Recipe getRecipe(String id) {
        return recipes.get(id);
    }
    
    public List<Ingredient> getAllIngredients() {
        return new ArrayList<>(ingredients.values());
    }
    
    public List<Tool> getAllTools() {
        return new ArrayList<>(tools.values());
    }
    
    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>(recipes.values());
        // Sort by difficulty
        Collections.sort(list, (r1, r2) -> r1.getDifficulty() - r2.getDifficulty());
        return list;
    }
    
    public List<Ingredient> getIngredientsForRecipe(Recipe recipe) {
        List<Ingredient> result = new ArrayList<>();
        if (recipe.getAllIngredientIds() != null) {
            for (String id : recipe.getAllIngredientIds()) {
                Ingredient ing = ingredients.get(id);
                if (ing != null) result.add(ing);
            }
        }
        return result;
    }
    
    public List<Tool> getToolsForRecipe(Recipe recipe) {
        List<Tool> result = new ArrayList<>();
        if (recipe.getAllToolIds() != null) {
            for (String id : recipe.getAllToolIds()) {
                Tool tool = tools.get(id);
                if (tool != null) result.add(tool);
            }
        }
        return result;
    }
    
    // ========================
    // CHEF GETTERS
    // ========================
    
    public ChefCharacter getChef(String id) {
        return chefs.get(id);
    }
    
    public List<ChefCharacter> getAllChefs() {
        return new ArrayList<>(chefs.values());
    }
    
    public List<ChefCharacter> getUnlockedChefs() {
        List<ChefCharacter> unlocked = new ArrayList<>();
        for (ChefCharacter chef : chefs.values()) {
            if (chef.isUnlocked()) {
                unlocked.add(chef);
            }
        }
        return unlocked;
    }
    
    public ChefCharacter getDefaultChef() {
        return chefs.get("chef_bunny");
    }
    
    // ========================
    // CUSTOMER ORDER GETTERS
    // ========================
    
    public CustomerOrder getOrder(String id) {
        return orders.get(id);
    }
    
    public List<CustomerOrder> getAllOrders() {
        return new ArrayList<>(orders.values());
    }
    
    /**
     * Get a random order, avoiding the last one.
     */
    public CustomerOrder getRandomOrder() {
        List<String> orderIds = new ArrayList<>(orders.keySet());
        
        // Remove last order to avoid repetition
        if (lastOrderId != null && orderIds.size() > 1) {
            orderIds.remove(lastOrderId);
        }
        
        // Pick random
        String selectedId = orderIds.get(random.nextInt(orderIds.size()));
        lastOrderId = selectedId;
        
        return orders.get(selectedId);
    }
    
    /**
     * Get order for a specific recipe.
     */
    public CustomerOrder getOrderForRecipe(String recipeId) {
        for (CustomerOrder order : orders.values()) {
            if (order.getRecipeId().equals(recipeId)) {
                return order;
            }
        }
        return null;
    }
    
    // ========================================
    // NEW RECIPES (Advanced Cooking Methods)
    // ========================================
    
    // === BAKED COOKIES (Oven) ===
    // Streamlined: realistic flow for kids
    // UI controls (knobs, buttons) are NOT in inventory - they're rendered as UI elements
    private Recipe createBakedCookiesRecipe() {
        List<RecipeStep> steps = new ArrayList<>();
        
        // Step 1: Put dough on baking tray (at counter)
        steps.add(new RecipeStep.Builder()
            .stepNumber(1)
            .chefText("Put the cookie dough on the baking tray!")
            .requiredItems(Arrays.asList("dough"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.COUNTER_ZONE)
            .inventoryForStep("dough", "baking_tray")
            .successText("Dough is on the tray!")
            .speakingPhrase("put the dough on the tray")
            .build());
        
        // Step 2: Put tray with dough into oven
        // NOTE: Tray is re-provided in inventory for user to drag
        steps.add(new RecipeStep.Builder()
            .stepNumber(2)
            .chefText("Now put the tray into the oven!")
            .requiredItems(Arrays.asList("baking_tray"))  // Changed to requiredItems so it validates correctly
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.OVEN_ZONE)
            .inventoryForStep("baking_tray")
            .successText("Tray is in the oven!")
            .speakingPhrase("put the tray in the oven")
            .build());
        
        // Step 3: Preheat oven (Hold action - no inventory items)
        steps.add(new RecipeStep.Builder()
            .stepNumber(3)
            .chefText("Hold the button to preheat the oven!")
            .action(ActionType.OVEN_PREHEAT_HOLD)
            .targetZone(ZoneType.OVEN_ZONE)
            .holdMs(1500)
            .inventoryForStep() // Empty - this is a UI gesture, not drag-drop
            .successText("Oven is preheated!")
            .speakingPhrase("preheat the oven")
            .build());
        
        // Step 4: Set temperature (UI control - no inventory items)
        steps.add(new RecipeStep.Builder()
            .stepNumber(4)
            .chefText("Turn the knob to set temperature to 180 degrees!")
            .action(ActionType.ROTATE_KNOB_TEMP)
            .targetZone(ZoneType.OVEN_ZONE)
            .rotaryParams(100, 250, 180, 10)
            .inventoryForStep() // Empty - knob is UI control
            .successText("Temperature set to 180°C!")
            .speakingPhrase("set the temperature to 180 degrees")
            .build());
        
        // Step 5: Set timer (UI control - no inventory items)
        steps.add(new RecipeStep.Builder()
            .stepNumber(5)
            .chefText("Set the timer to 10 minutes!")
            .action(ActionType.ROTATE_KNOB_TIMER)
            .targetZone(ZoneType.OVEN_ZONE)
            .timerSeconds(600)
            .inventoryForStep() // Empty - timer is UI control
            .successText("Timer set!")
            .speakingPhrase("set the timer")
            .build());
        
        // Step 6: Wait for baking (timing game - no inventory)
        steps.add(new RecipeStep.Builder()
            .stepNumber(6)
            .chefText("Watch the cookies bake! Stop at the right time!")
            .action(ActionType.TIMING_STOP)
            .targetZone(ZoneType.OVEN_ZONE)
            .timingParams(8000, 6000, 7500)
            .inventoryForStep() // Empty - just watch and tap
            .successText("Perfect timing!")
            .speakingPhrase("bake it")
            .requiresSpeaking(false)
            .build());
        
        // Step 7: Take tray out with oven mitts
        steps.add(new RecipeStep.Builder()
            .stepNumber(7)
            .chefText("Use oven mitts to take out the tray!")
            .requiredTool("oven_mitts")
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.COUNTER_ZONE)
            .inventoryForStep("oven_mitts")
            .successText("Cookies are out safely!")
            .speakingPhrase("take out the cookies")
            .build());
        
        // Step 8: Plate the cookies
        steps.add(new RecipeStep.Builder()
            .stepNumber(8)
            .chefText("Place cookies on the plate!")
            .action(ActionType.GARNISH_DRAG_SNAP)
            .targetZone(ZoneType.SERVE_ZONE)
            .snapPoints(4, 4)
            .inventoryForStep("plate")
            .successText("Beautiful cookies! Yummy!")
            .speakingPhrase("garnish the dish")
            .build());
        
        return new Recipe(
            "baked_cookies", "Baked Cookies", "Bánh quy nướng",
            "Delicious homemade cookies from the oven",
            "ic_recipe_cookies", 2, steps,
            Arrays.asList("dough"),
            Arrays.asList("baking_tray", "oven_mitts", "plate") // Removed knobs - they're UI controls
        );
    }
    
    // === STEAMED DUMPLINGS (Steamer) ===
    private Recipe createSteamedDumplingsRecipe() {
        List<RecipeStep> steps = new ArrayList<>();
        
        // Step 1: Pour water into steamer
        steps.add(new RecipeStep.Builder()
            .stepNumber(1)
            .chefText("Pour water into the steamer!")
            .requiredItems(Arrays.asList("water"))
            .action(ActionType.POUR)
            .targetZone(ZoneType.STEAMER_ZONE)
            .pourTime(1500)
            .inventoryForStep("water")
            .successText("Water added!")
            .speakingPhrase("add water")
            .build());
        
        // Step 2: Put steamer basket
        steps.add(new RecipeStep.Builder()
            .stepNumber(2)
            .chefText("Put the steamer basket on top!")
            .requiredItems(Arrays.asList("steamer_basket"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.STEAMER_ZONE)
            .inventoryForStep("steamer_basket")
            .successText("Basket is in place!")
            .speakingPhrase("put the basket")
            .build());
        
        // Step 3: Put dumplings in basket
        steps.add(new RecipeStep.Builder()
            .stepNumber(3)
            .chefText("Put dumplings in the basket!")
            .requiredItems(Arrays.asList("dumpling"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.STEAMER_ZONE)
            .inventoryForStep("dumpling")
            .successText("Dumplings are ready!")
            .speakingPhrase("put it in the steamer")
            .build());
        
        // Step 4: Cover with lid
        steps.add(new RecipeStep.Builder()
            .stepNumber(4)
            .chefText("Cover with the lid!")
            .requiredItems(Arrays.asList("lid"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.STEAMER_ZONE)
            .inventoryForStep("lid")
            .successText("Lid is on!")
            .speakingPhrase("cover the lid")
            .build());
        
        // Step 5: Wait for steam (auto timer)
        steps.add(new RecipeStep.Builder()
            .stepNumber(5)
            .chefText("Wait for the steam to start!")
            .action(ActionType.STEAM_START)
            .targetZone(ZoneType.STEAMER_ZONE)
            .warmUpMs(3000)
            .inventoryForStep() // Empty - just wait
            .successText("Steaming started!")
            .speakingPhrase("steam it")
            .requiresSpeaking(false)
            .build());
        
        // Step 6: Wipe fog (swipe gesture)
        steps.add(new RecipeStep.Builder()
            .stepNumber(6)
            .chefText("Wipe the fog from the lid! Swipe 6 times!")
            .action(ActionType.WIPE_FOG_SWIPE)
            .targetZone(ZoneType.STEAMER_ZONE)
            .swipeCount(6)
            .inventoryForStep() // Empty - gesture only
            .successText("Clear view now!")
            .speakingPhrase("wipe the fog")
            .build());
        
        // Step 7: Serve dumplings
        steps.add(new RecipeStep.Builder()
            .stepNumber(7)
            .chefText("Serve the dumplings!")
            .requiredItems(Arrays.asList("plate"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.SERVE_ZONE)
            .inventoryForStep("plate")
            .successText("Yummy steamed dumplings!")
            .speakingPhrase("serve it")
            .build());
        
        return new Recipe(
            "steamed_dumplings", "Steamed Dumplings", "Bánh bao hấp",
            "Fluffy steamed dumplings with delicious filling",
            "ic_recipe_dumplings", 2, steps,
            Arrays.asList("dumpling", "water"),
            Arrays.asList("steamer_basket", "lid", "plate")
        );
    }
    
    // === GRILLED FISH (Grill) ===
    private Recipe createGrilledFishRecipe() {
        List<RecipeStep> steps = new ArrayList<>();
        
        // Step 1: Turn on grill (hold action - no items to drag)
        steps.add(new RecipeStep.Builder()
            .stepNumber(1)
            .chefText("Hold the button to turn on the grill!")
            .action(ActionType.GRILL_ON_HOLD)
            .targetZone(ZoneType.GRILL_ZONE)
            .holdMs(1500)
            .inventoryForStep() // Empty - gesture only
            .successText("Grill is on and hot!")
            .speakingPhrase("turn on the grill")
            .build());
        
        // Step 2: Place fish on grill with tongs
        steps.add(new RecipeStep.Builder()
            .stepNumber(2)
            .chefText("Use tongs to place the fish on the grill!")
            .requiredItems(Arrays.asList("fish"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.GRILL_ZONE)
            .inventoryForStep("fish", "tongs")
            .successText("Fish is on the grill!")
            .speakingPhrase("place the fish")
            .build());
        
        // Step 3: Season with spice shaker
        steps.add(new RecipeStep.Builder()
            .stepNumber(3)
            .chefText("Shake the spice shaker to season!")
            .action(ActionType.SEASON_SHAKE)
            .targetZone(ZoneType.GRILL_ZONE)
            .shakeCount(4)
            .inventoryForStep("spice_shaker")
            .successText("Nicely seasoned!")
            .speakingPhrase("shake to season")
            .build());
        
        // Step 4: Wait for first side
        steps.add(new RecipeStep.Builder()
            .stepNumber(4)
            .chefText("Wait for the first side to cook!")
            .action(ActionType.WAIT_SEAR)
            .targetZone(ZoneType.GRILL_ZONE)
            .cookTime(3000)
            .inventoryForStep() // Empty - just wait
            .successText("First side is done!")
            .speakingPhrase("wait for it")
            .requiresSpeaking(false)
            .build());
        
        // Step 5: Flip the fish
        steps.add(new RecipeStep.Builder()
            .stepNumber(5)
            .chefText("Swipe to flip the fish!")
            .action(ActionType.GRILL_FLIP_SWIPE)
            .targetZone(ZoneType.GRILL_ZONE)
            .flipParams(1, 0, 1500)
            .inventoryForStep("tongs") // Show tongs for visual
            .successText("Nice flip!")
            .speakingPhrase("flip it")
            .build());
        
        // Step 6: Wait for other side
        steps.add(new RecipeStep.Builder()
            .stepNumber(6)
            .chefText("Wait for the other side!")
            .action(ActionType.WAIT_SEAR)
            .targetZone(ZoneType.GRILL_ZONE)
            .cookTime(3000)
            .inventoryForStep() // Empty - just wait
            .successText("Perfectly grilled!")
            .speakingPhrase("grill it")
            .requiresSpeaking(false)
            .build());
        
        // Step 7: Plate the fish
        steps.add(new RecipeStep.Builder()
            .stepNumber(7)
            .chefText("Plate the fish beautifully!")
            .action(ActionType.GARNISH_DRAG_SNAP)
            .targetZone(ZoneType.SERVE_ZONE)
            .snapPoints(3, 3)
            .inventoryForStep("plate")
            .successText("Beautiful grilled fish!")
            .speakingPhrase("garnish the dish")
            .build());
        
        return new Recipe(
            "grilled_fish", "Grilled Fish", "Cá nướng",
            "Perfectly grilled fish with seasoning",
            "ic_recipe_grilled_fish", 2, steps,
            Arrays.asList("fish", "salt"),
            Arrays.asList("tongs", "spice_shaker", "plate")
        );
    }
    
    // === FRENCH FRIES (Deep Fry) ===
    private Recipe createFrenchFriesRecipe() {
        List<RecipeStep> steps = new ArrayList<>();
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(1)
            .chefText("Pour oil to the line!")
            .requiredItems(Arrays.asList("oil"))
            .action(ActionType.OIL_LEVEL_SLIDER)
            .targetZone(ZoneType.FRYER_ZONE)
            .sliderParams(0.7f, 0.1f)
            .successText("Oil level is perfect!")
            .speakingPhrase("pour the oil")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(2)
            .chefText("Wait for the oil to heat up!")
            .action(ActionType.WAIT_OIL_HEAT)
            .targetZone(ZoneType.FRYER_ZONE)
            .temperatureParams(180, 3000)
            .successText("Oil is hot and ready!")
            .speakingPhrase("heat the oil")
            .requiresSpeaking(false)
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(3)
            .chefText("Put the fries in the fryer basket!")
            .requiredItems(Arrays.asList("potato"))
            .requiredTool("fryer_basket")
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.FRYER_ZONE)
            .successText("Fries in the basket!")
            .speakingPhrase("put the fries")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(4)
            .chefText("Let the fries cook!")
            .action(ActionType.WAIT_COOK)
            .targetZone(ZoneType.FRYER_ZONE)
            .cookTime(4000)
            .successText("Fries are getting golden!")
            .speakingPhrase("fry it")
            .requiresSpeaking(false)
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(5)
            .chefText("Shake the basket!")
            .requiredTool("fryer_basket")
            .action(ActionType.SHAKE_BASKET)
            .targetZone(ZoneType.FRYER_ZONE)
            .shakeCount(4)
            .successText("Evenly fried!")
            .speakingPhrase("shake the basket")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(6)
            .chefText("Take out and let them drain!")
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.DRAIN_ZONE)
            .successText("Draining excess oil!")
            .speakingPhrase("drain the oil")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(7)
            .chefText("Add salt to the fries!")
            .requiredTool("salt_shaker")
            .action(ActionType.SEASON_SHAKE)
            .targetZone(ZoneType.SERVE_ZONE)
            .shakeCount(3)
            .successText("Perfectly salted!")
            .speakingPhrase("add salt")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(8)
            .chefText("Serve the fries!")
            .action(ActionType.SERVE)
            .targetZone(ZoneType.SERVE_ZONE)
            .successText("Crispy French fries!")
            .speakingPhrase("serve it")
            .build());
        
        return new Recipe(
            "french_fries", "French Fries", "Khoai tây chiên",
            "Crispy golden French fries",
            "ic_recipe_fries", 2, steps,
            Arrays.asList("potato", "oil", "salt"),
            Arrays.asList("fryer_basket", "salt_shaker", "drain_rack", "plate")
        );
    }
    
    // === NOODLE SOUP (Boil/Simmer) ===
    // === NOODLE SOUP (Pot) ===
    // Streamlined: each step shows only needed items (max 4)
    private Recipe createNoodleSoupRecipe() {
        List<RecipeStep> steps = new ArrayList<>();
        
        // Step 1: Put pot on stove and add water
        steps.add(new RecipeStep.Builder()
            .stepNumber(1)
            .chefText("Pour water into the pot!")
            .requiredItems(Arrays.asList("water"))
            .action(ActionType.POUR)
            .targetZone(ZoneType.POT_ZONE)
            .pourTime(1500)
            .inventoryForStep("pot", "water")
            .successText("Water added!")
            .speakingPhrase("add water")
            .build());
        
        // Step 2: Wait for boiling (no items - just wait)
        steps.add(new RecipeStep.Builder()
            .stepNumber(2)
            .chefText("Wait for the water to boil!")
            .action(ActionType.WAIT_BOIL)
            .targetZone(ZoneType.POT_ZONE)
            .temperatureParams(100, 4000)
            .inventoryForStep() // No items - just wait
            .successText("Water is boiling!")
            .speakingPhrase("boil the water")
            .requiresSpeaking(false)
            .build());
        
        // Step 3: Add noodles and veggies
        steps.add(new RecipeStep.Builder()
            .stepNumber(3)
            .chefText("Add noodles and vegetables!")
            .requiredItems(Arrays.asList("noodle", "carrot", "broccoli"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.POT_ZONE)
            .inventoryForStep("noodle", "carrot", "broccoli") // Max 3 items
            .successText("Ingredients added!")
            .speakingPhrase("add the noodles")
            .build());
        
        // Step 4: Reduce heat (UI control - slider)
        steps.add(new RecipeStep.Builder()
            .stepNumber(4)
            .chefText("Reduce the heat!")
            .action(ActionType.HEAT_LEVEL_SLIDER)
            .targetZone(ZoneType.POT_ZONE)
            .sliderParams(0.4f, 0.1f)
            .inventoryForStep() // UI control - no items
            .successText("Heat reduced to simmer!")
            .speakingPhrase("reduce the heat")
            .build());
        
        // Step 5: Stir with ladle
        steps.add(new RecipeStep.Builder()
            .stepNumber(5)
            .chefText("Stir the soup!")
            .requiredTool("ladle")
            .action(ActionType.SWIPE_TO_STIR)
            .targetZone(ZoneType.POT_ZONE)
            .stirCount(6)
            .inventoryForStep("ladle")
            .successText("Nicely stirred!")
            .speakingPhrase("stir the soup")
            .build());
        
        // Step 6: Skim foam (gesture - no items)
        steps.add(new RecipeStep.Builder()
            .stepNumber(6)
            .chefText("Skim the foam from the soup!")
            .action(ActionType.SKIM_FOAM_SWIPE)
            .targetZone(ZoneType.POT_ZONE)
            .swipeCount(4)
            .inventoryForStep() // Gesture only
            .successText("Foam removed!")
            .speakingPhrase("skim the foam")
            .build());
        
        // Step 7: Serve
        steps.add(new RecipeStep.Builder()
            .stepNumber(7)
            .chefText("Serve the soup!")
            .requiredTool("ladle")
            .action(ActionType.SERVE)
            .targetZone(ZoneType.SERVE_ZONE)
            .inventoryForStep("ladle", "plate")
            .successText("Delicious noodle soup!")
            .speakingPhrase("serve it")
            .build());
        
        return new Recipe(
            "noodle_soup", "Noodle Soup", "Mì nước",
            "Warm and comforting noodle soup",
            "ic_recipe_noodle", 2, steps,
            Arrays.asList("noodle", "water", "carrot", "broccoli"),
            Arrays.asList("pot", "ladle", "plate")
        );
    }

    // === OMELETTE (Pan) ===
    // Streamlined: each step shows only needed items
    private Recipe createOmeletteRecipe() {
        List<RecipeStep> steps = new ArrayList<>();
        
        // Step 1: Get eggs (show 3 eggs)
        steps.add(new RecipeStep.Builder()
            .stepNumber(1)
            .chefText("Get 3 eggs from the fridge!")
            .requiredItems(Arrays.asList("egg"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.COUNTER_ZONE)
            .inventoryForStep("egg:3") // Show 3 eggs with stack count
            .successText("Eggs ready!")
            .speakingPhrase("get the eggs")
            .build());
        
        // Step 2: Crack eggs (tap action - eggs shown for visual, bowl shown)
        steps.add(new RecipeStep.Builder()
            .stepNumber(2)
            .chefText("Tap to crack the eggs!")
            .requiredItems(Arrays.asList("egg"))
            .action(ActionType.TAP_TO_CRACK)
            .targetZone(ZoneType.MIXING_ZONE)
            .tapCount(3)
            .inventoryForStep("mixing_bowl") // Bowl to crack into
            .successText("Eggs cracked!")
            .speakingPhrase("crack the eggs")
            .build());
        
        // Step 3: Whisk eggs
        steps.add(new RecipeStep.Builder()
            .stepNumber(3)
            .chefText("Whisk the eggs in circles!")
            .requiredTool("whisk")
            .action(ActionType.WHISK_CIRCLES)
            .targetZone(ZoneType.MIXING_ZONE)
            .circleCount(8)
            .inventoryForStep("whisk")
            .successText("Eggs are fluffy!")
            .speakingPhrase("whisk the eggs")
            .build());
        
        // Step 4: Add oil to pan
        steps.add(new RecipeStep.Builder()
            .stepNumber(4)
            .chefText("Add oil to the pan!")
            .requiredItems(Arrays.asList("oil"))
            .action(ActionType.POUR)
            .targetZone(ZoneType.PAN_ZONE)
            .pourTime(1000)
            .inventoryForStep("pan", "oil")
            .successText("Pan is oiled!")
            .speakingPhrase("add oil")
            .build());
        
        // Step 5: Pour eggs (gesture action - no items needed)
        steps.add(new RecipeStep.Builder()
            .stepNumber(5)
            .chefText("Pour the eggs into the pan!")
            .action(ActionType.POUR)
            .targetZone(ZoneType.PAN_ZONE)
            .pourTime(1500)
            .inventoryForStep() // Gesture only
            .successText("Eggs in the pan!")
            .speakingPhrase("pour the eggs")
            .build());
        
        // Step 6: Wait for cooking
        steps.add(new RecipeStep.Builder()
            .stepNumber(6)
            .chefText("Let it cook a bit!")
            .action(ActionType.WAIT_COOK)
            .targetZone(ZoneType.PAN_ZONE)
            .cookTime(2500)
            .inventoryForStep() // Just wait - no items
            .successText("Almost ready!")
            .speakingPhrase("cook it")
            .requiresSpeaking(false)
            .build());
        
        // Step 7: Flip (can show spatula for visual guidance)
        steps.add(new RecipeStep.Builder()
            .stepNumber(7)
            .chefText("Swipe to flip the omelette!")
            .action(ActionType.GRILL_FLIP_SWIPE)
            .targetZone(ZoneType.PAN_ZONE)
            .flipParams(1, 0, 2000)
            .inventoryForStep("spatula") // Visual hint
            .successText("Perfect flip!")
            .speakingPhrase("flip it")
            .build());
        
        // Step 8: Serve
        steps.add(new RecipeStep.Builder()
            .stepNumber(8)
            .chefText("Plate and garnish!")
            .action(ActionType.GARNISH_DRAG_SNAP)
            .targetZone(ZoneType.SERVE_ZONE)
            .snapPoints(3, 2)
            .inventoryForStep("plate")
            .successText("Beautiful omelette!")
            .speakingPhrase("garnish the dish")
            .build());
        
        return new Recipe(
            "omelette", "Omelette", "Trứng ốp la",
            "Fluffy pan-fried omelette",
            "ic_recipe_omelette", 1, steps,
            Arrays.asList("egg", "oil"),
            Arrays.asList("mixing_bowl", "whisk", "pan", "spatula", "plate")
        );
    }
    
    // === BAKED PIZZA (Oven) ===
    private Recipe createBakedPizzaRecipe() {
        List<RecipeStep> steps = new ArrayList<>();
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(1)
            .chefText("Knead the pizza dough!")
            .requiredItems(Arrays.asList("dough"))
            .requiredTool("rolling_pin")
            .action(ActionType.KNEAD_DRAG)
            .targetZone(ZoneType.COUNTER_ZONE)
            .kneadRepeats(6)
            .successText("Dough is ready!")
            .speakingPhrase("knead the dough")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(2)
            .chefText("Add tomato sauce!")
            .requiredItems(Arrays.asList("tomato"))
            .action(ActionType.POUR)
            .targetZone(ZoneType.COUNTER_ZONE)
            .pourTime(1000)
            .successText("Sauce spread!")
            .speakingPhrase("add the sauce")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(3)
            .chefText("Add toppings!")
            .requiredItems(Arrays.asList("mushroom", "onion"))
            .action(ActionType.GARNISH_DRAG_SNAP)
            .targetZone(ZoneType.COUNTER_ZONE)
            .snapPoints(5, 4)
            .successText("Toppings added!")
            .speakingPhrase("add the toppings")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(4)
            .chefText("Put pizza in the oven!")
            .requiredTool("baking_tray")
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.OVEN_ZONE)
            .successText("Pizza is in!")
            .speakingPhrase("put it in the oven")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(5)
            .chefText("Preheat the oven!")
            .action(ActionType.OVEN_PREHEAT_HOLD)
            .targetZone(ZoneType.OVEN_ZONE)
            .holdMs(1500)
            .successText("Oven ready!")
            .speakingPhrase("preheat the oven")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(6)
            .chefText("Set temperature to 200 degrees!")
            .action(ActionType.ROTATE_KNOB_TEMP)
            .targetZone(ZoneType.OVEN_ZONE)
            .rotaryParams(150, 250, 200, 10)
            .successText("200°C set!")
            .speakingPhrase("set the temperature")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(7)
            .chefText("Bake until perfect!")
            .action(ActionType.TIMING_STOP)
            .targetZone(ZoneType.OVEN_ZONE)
            .timingParams(7000, 5000, 6500)
            .successText("Perfect timing!")
            .speakingPhrase("bake it")
            .requiresSpeaking(false)
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(8)
            .chefText("Take out and serve!")
            .requiredTool("oven_mitts")
            .action(ActionType.SERVE)
            .targetZone(ZoneType.SERVE_ZONE)
            .successText("Delicious pizza!")
            .speakingPhrase("serve it")
            .build());
        
        return new Recipe(
            "baked_pizza", "Baked Pizza", "Pizza nướng",
            "Homemade pizza with fresh toppings",
            "ic_recipe_pizza", 3, steps,
            Arrays.asList("dough", "tomato", "mushroom", "onion"),
            Arrays.asList("rolling_pin", "baking_tray", "oven_mitts", "plate")
        );
    }
    
    // === STEAMED VEGETABLES (Steamer) ===
    private Recipe createSteamedVegetablesRecipe() {
        List<RecipeStep> steps = new ArrayList<>();
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(1)
            .chefText("Get vegetables from the fridge!")
            .requiredItems(Arrays.asList("broccoli", "carrot"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.COUNTER_ZONE)
            .successText("Vegetables ready!")
            .speakingPhrase("get the vegetables")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(2)
            .chefText("Cut the vegetables!")
            .requiredTool("knife")
            .action(ActionType.TAP_TO_CUT)
            .targetZone(ZoneType.BOARD_ZONE)
            .tapCount(5)
            .successText("Vegetables cut!")
            .speakingPhrase("cut the vegetables")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(3)
            .chefText("Add water to steamer!")
            .requiredItems(Arrays.asList("water"))
            .action(ActionType.POUR)
            .targetZone(ZoneType.STEAMER_ZONE)
            .pourTime(1500)
            .successText("Water added!")
            .speakingPhrase("add water")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(4)
            .chefText("Put vegetables in the steamer!")
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.STEAMER_ZONE)
            .successText("In the steamer!")
            .speakingPhrase("put it in the steamer")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(5)
            .chefText("Cover with lid!")
            .requiredTool("lid")
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.STEAMER_ZONE)
            .successText("Lid on!")
            .speakingPhrase("cover the lid")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(6)
            .chefText("Steam the vegetables!")
            .action(ActionType.STEAM_START)
            .targetZone(ZoneType.STEAMER_ZONE)
            .warmUpMs(4000)
            .successText("Vegetables steamed!")
            .speakingPhrase("steam it")
            .requiresSpeaking(false)
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(7)
            .chefText("Tap to vent the steam!")
            .action(ActionType.VENT_STEAM_TAP)
            .targetZone(ZoneType.STEAMER_ZONE)
            .tapCount(4)
            .successText("Steam released!")
            .speakingPhrase("vent the steam")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(8)
            .chefText("Serve with sauce!")
            .action(ActionType.SERVE)
            .targetZone(ZoneType.SERVE_ZONE)
            .successText("Healthy steamed veggies!")
            .speakingPhrase("serve it")
            .build());
        
        return new Recipe(
            "steamed_vegetables", "Steamed Vegetables", "Rau hấp",
            "Healthy steamed vegetables",
            "ic_recipe_steamed_veg", 1, steps,
            Arrays.asList("broccoli", "carrot", "water"),
            Arrays.asList("knife", "steamer_basket", "lid", "plate")
        );
    }
    
    // === GRILLED STEAK (Grill) ===
    private Recipe createGrilledSteakRecipe() {
        List<RecipeStep> steps = new ArrayList<>();
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(1)
            .chefText("Get the beef from fridge!")
            .requiredItems(Arrays.asList("beef"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.COUNTER_ZONE)
            .successText("Beef ready!")
            .speakingPhrase("get the beef")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(2)
            .chefText("Pound the meat to tenderize!")
            .requiredTool("meat_mallet")
            .action(ActionType.TAP_TO_POUND)
            .targetZone(ZoneType.BOARD_ZONE)
            .tapCount(6)
            .successText("Meat is tender!")
            .speakingPhrase("pound the meat")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(3)
            .chefText("Season the steak!")
            .requiredTool("spice_shaker")
            .action(ActionType.SEASON_SHAKE)
            .targetZone(ZoneType.COUNTER_ZONE)
            .shakeCount(5)
            .successText("Well seasoned!")
            .speakingPhrase("season it")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(4)
            .chefText("Turn on the grill!")
            .action(ActionType.GRILL_ON_HOLD)
            .targetZone(ZoneType.GRILL_ZONE)
            .holdMs(1500)
            .successText("Grill is hot!")
            .speakingPhrase("turn on the grill")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(5)
            .chefText("Place steak on the grill!")
            .requiredTool("tongs")
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.GRILL_ZONE)
            .successText("Steak sizzling!")
            .speakingPhrase("place the steak")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(6)
            .chefText("Sear the first side!")
            .action(ActionType.WAIT_SEAR)
            .targetZone(ZoneType.GRILL_ZONE)
            .cookTime(4000)
            .successText("Nice sear marks!")
            .speakingPhrase("sear it")
            .requiresSpeaking(false)
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(7)
            .chefText("Flip the steak!")
            .action(ActionType.GRILL_FLIP_SWIPE)
            .targetZone(ZoneType.GRILL_ZONE)
            .flipParams(1, 0, 2000)
            .successText("Perfect flip!")
            .speakingPhrase("flip it")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(8)
            .chefText("Sear the other side!")
            .action(ActionType.WAIT_SEAR)
            .targetZone(ZoneType.GRILL_ZONE)
            .cookTime(3500)
            .successText("Done to perfection!")
            .speakingPhrase("grill it")
            .requiresSpeaking(false)
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(9)
            .chefText("Plate and garnish!")
            .action(ActionType.GARNISH_DRAG_SNAP)
            .targetZone(ZoneType.SERVE_ZONE)
            .snapPoints(3, 3)
            .successText("Restaurant quality!")
            .speakingPhrase("garnish the dish")
            .build());
        
        return new Recipe(
            "grilled_steak", "Grilled Steak", "Bò bít tết nướng",
            "Juicy grilled steak with perfect sear",
            "ic_recipe_steak", 3, steps,
            Arrays.asList("beef", "salt"),
            Arrays.asList("meat_mallet", "spice_shaker", "tongs", "plate")
        );
    }
    
    // === FRIED CHICKEN (Deep Fry) ===
    private Recipe createFriedChickenRecipe() {
        List<RecipeStep> steps = new ArrayList<>();
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(1)
            .chefText("Get chicken from the fridge!")
            .requiredItems(Arrays.asList("chicken"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.COUNTER_ZONE)
            .successText("Chicken ready!")
            .speakingPhrase("get the chicken")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(2)
            .chefText("Season the chicken!")
            .requiredTool("spice_shaker")
            .action(ActionType.SEASON_SHAKE)
            .targetZone(ZoneType.COUNTER_ZONE)
            .shakeCount(5)
            .successText("Well seasoned!")
            .speakingPhrase("season it")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(3)
            .chefText("Pour oil to the level!")
            .requiredItems(Arrays.asList("oil"))
            .action(ActionType.OIL_LEVEL_SLIDER)
            .targetZone(ZoneType.FRYER_ZONE)
            .sliderParams(0.75f, 0.1f)
            .successText("Oil level perfect!")
            .speakingPhrase("pour the oil")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(4)
            .chefText("Wait for oil to heat!")
            .action(ActionType.WAIT_OIL_HEAT)
            .targetZone(ZoneType.FRYER_ZONE)
            .temperatureParams(170, 3000)
            .successText("Oil is hot!")
            .speakingPhrase("heat the oil")
            .requiresSpeaking(false)
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(5)
            .chefText("Put chicken in the fryer!")
            .requiredTool("fryer_basket")
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.FRYER_ZONE)
            .successText("Chicken frying!")
            .speakingPhrase("fry it")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(6)
            .chefText("Let it fry!")
            .action(ActionType.WAIT_COOK)
            .targetZone(ZoneType.FRYER_ZONE)
            .cookTime(5000)
            .successText("Getting golden!")
            .speakingPhrase("cook it")
            .requiresSpeaking(false)
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(7)
            .chefText("Shake the basket!")
            .requiredTool("fryer_basket")
            .action(ActionType.SHAKE_BASKET)
            .targetZone(ZoneType.FRYER_ZONE)
            .shakeCount(4)
            .successText("Even frying!")
            .speakingPhrase("shake the basket")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(8)
            .chefText("Take out and drain!")
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.DRAIN_ZONE)
            .successText("Draining oil!")
            .speakingPhrase("drain the oil")
            .build());
        
        steps.add(new RecipeStep.Builder()
            .stepNumber(9)
            .chefText("Serve the crispy chicken!")
            .action(ActionType.SERVE)
            .targetZone(ZoneType.SERVE_ZONE)
            .successText("Crispy fried chicken!")
            .speakingPhrase("serve it")
            .build());
        
        return new Recipe(
            "fried_chicken", "Fried Chicken", "Gà rán",
            "Crispy golden fried chicken",
            "ic_recipe_fried_chicken", 2, steps,
            Arrays.asList("chicken", "oil", "salt"),
            Arrays.asList("spice_shaker", "fryer_basket", "drain_rack", "plate")
        );
    }
    
    // === RECIPE: Fried Rice ===
    // Steps: Get carrot -> Put on board -> Cut -> Add oil -> Add rice -> Add carrot -> Stir-fry -> Season -> Serve
    private Recipe createFriedRiceRecipe() {
        List<RecipeStep> steps = new ArrayList<>();
        
        // Step 1: Get the carrot from fridge (drag from fridge)
        steps.add(new RecipeStep.Builder()
            .stepNumber(1)
            .chefText("Get the carrot from the fridge!")
            .requiredItems(Arrays.asList("carrot"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.FRIDGE_ZONE)
            .successText("Carrot selected!")
            .speakingPhrase("get the carrot")
            .build());
        
        // Step 2: Put carrot on cutting board
        steps.add(new RecipeStep.Builder()
            .stepNumber(2)
            .chefText("Put the carrot on the cutting board!")
            .requiredItems(Arrays.asList("carrot"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.BOARD_ZONE)
            .successText("Carrot on the board!")
            .speakingPhrase("put it on the cutting board")
            .build());
        
        // Step 3: Cut the carrot (tap 6 times)
        steps.add(new RecipeStep.Builder()
            .stepNumber(3)
            .chefText("Cut the carrot! Tap 6 times!")
            .requiredItems(Arrays.asList("carrot"))
            .requiredTool("knife")
            .action(ActionType.TAP_TO_CUT)
            .targetZone(ZoneType.BOARD_ZONE)
            .tapCount(6)
            .successText("Carrot chopped!")
            .speakingPhrase("cut the carrot")
            .build());
        
        // Step 4: Add oil to the pan (pour hold)
        steps.add(new RecipeStep.Builder()
            .stepNumber(4)
            .chefText("Add oil to the pan! Hold to pour!")
            .requiredItems(Arrays.asList("oil"))
            .action(ActionType.POUR)
            .targetZone(ZoneType.PAN_ZONE)
            .pourTime(2000)
            .successText("Oil in the pan!")
            .speakingPhrase("add oil")
            .build());
        
        // Step 5: Add rice to the pan
        steps.add(new RecipeStep.Builder()
            .stepNumber(5)
            .chefText("Add the rice to the pan!")
            .requiredItems(Arrays.asList("rice"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.PAN_ZONE)
            .successText("Rice in the pan!")
            .speakingPhrase("add rice")
            .build());
        
        // Step 6: Add chopped carrot to the pan
        steps.add(new RecipeStep.Builder()
            .stepNumber(6)
            .chefText("Add the chopped carrot!")
            .requiredItems(Arrays.asList("carrot"))
            .action(ActionType.DRAG_TO_ZONE)
            .targetZone(ZoneType.PAN_ZONE)
            .successText("Carrot added!")
            .speakingPhrase("add the carrot")
            .build());
        
        // Step 7: Stir-fry (swipe to stir + shake pan)
        steps.add(new RecipeStep.Builder()
            .stepNumber(7)
            .chefText("Stir-fry! Swipe 8 times and shake the pan!")
            .requiredTool("spatula")
            .action(ActionType.SWIPE_TO_STIR)
            .targetZone(ZoneType.PAN_ZONE)
            .stirCount(8)
            .shakeCount(6)
            .successText("Stir-fried perfectly!")
            .speakingPhrase("stir fry")
            .build());
        
        // Step 8: Season the fried rice (shake seasoning)
        steps.add(new RecipeStep.Builder()
            .stepNumber(8)
            .chefText("Season the fried rice! Shake the salt!")
            .requiredItems(Arrays.asList("salt"))
            .requiredTool("spice_shaker")
            .action(ActionType.SEASON_SHAKE)
            .targetZone(ZoneType.PAN_ZONE)
            .shakeCount(4)
            .successText("Seasoned!")
            .speakingPhrase("season it")
            .build());
        
        // Step 9: Serve the fried rice
        steps.add(new RecipeStep.Builder()
            .stepNumber(9)
            .chefText("Serve the delicious fried rice!")
            .action(ActionType.SERVE)
            .targetZone(ZoneType.SERVE_ZONE)
            .successText("Yummy fried rice!")
            .speakingPhrase("serve it")
            .build());
        
        return new Recipe(
            "fried_rice", "Fried Rice", "Cơm chiên",
            "Classic fried rice with carrot",
            "ic_recipe_fried_rice", 1, steps,
            Arrays.asList("rice", "carrot", "oil", "salt"),
            Arrays.asList("knife", "spatula", "spice_shaker", "plate")
        );
    }
}
