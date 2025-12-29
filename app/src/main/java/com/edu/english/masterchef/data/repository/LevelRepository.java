package com.edu.english.masterchef.data.repository;

import com.edu.english.masterchef.data.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for level data and game content
 * In production, this would load from JSON files or remote API
 * For now, using hardcoded mock data
 */
public class LevelRepository {
    
    private static LevelRepository instance;
    private Map<String, Food> foodMap;
    private Map<String, Ingredient> ingredientMap;
    private List<LevelConfig> levels;

    private LevelRepository() {
        initializeMockData();
    }

    public static synchronized LevelRepository getInstance() {
        if (instance == null) {
            instance = new LevelRepository();
        }
        return instance;
    }

    /**
     * Initialize mock data for Level 1: Spaghetti Bolognese
     */
    private void initializeMockData() {
        foodMap = new HashMap<>();
        ingredientMap = new HashMap<>();
        levels = new ArrayList<>();

        // === INGREDIENTS ===
        createIngredient("tomato", "Tomato", "Cà chua", "ing_tomato", "vegetable");
        createIngredient("onion", "Onion", "Hành tây", "ing_onion", "vegetable");
        createIngredient("garlic", "Garlic", "Tỏi", "ing_garlic", "spice");
        createIngredient("beef", "Ground Beef", "Thịt bò băm", "ing_beef", "meat");
        createIngredient("pasta", "Spaghetti Pasta", "Mì Ý", "ing_pasta", "grain");
        createIngredient("olive_oil", "Olive Oil", "Dầu ô liu", "ing_olive_oil", "oil");
        createIngredient("salt", "Salt", "Muối", "ing_salt", "spice");
        createIngredient("basil", "Basil", "Húng quế", "ing_basil", "spice");
        createIngredient("cheese", "Parmesan Cheese", "Phô mai Parmesan", "ing_cheese", "dairy");
        createIngredient("sauce", "Tomato Sauce", "Sốt cà chua", "ing_sauce", "sauce");

        // Distractors for Scene 2
        createIngredient("pepper_distractor", "Bell Pepper", "Ớt chuông", "ing_pepper", "vegetable");
        createIngredient("carrot_distractor", "Carrot", "Cà rốt", "ing_carrot", "vegetable");
        createIngredient("chicken_distractor", "Chicken", "Thịt gà", "ing_chicken", "meat");

        // === FOOD: Spaghetti Bolognese ===
        Food spaghetti = new Food(
            "spaghetti_bolognese",
            "Spaghetti Bolognese",
            "Mì Ý sốt thịt bò",
            "dish_spaghetti",
            "italian",
            Arrays.asList("tomato", "onion", "garlic", "beef", "pasta", "olive_oil", "salt", "basil", "cheese"),
            "Classic Italian pasta with rich meat sauce"
        );
        foodMap.put(spaghetti.getDishId(), spaghetti);

        // === LEVEL 1 CONFIG ===
        LevelConfig level1 = new LevelConfig(1, "spaghetti_bolognese", "easy", 0, 50, 80, 100);
        
        // Scene 1 Script: Ordering
        SceneScript scene1 = new SceneScript();
        scene1.addDialog(new DialogLine("npc", "Welcome to our restaurant! What would you like to order today?", "Chào mừng đến nhà hàng! Hôm nay bạn muốn gọi món gì?"))
              .addDialog(new DialogLine("player", "I want to order Spaghetti Bolognese.", "Tôi muốn gọi món Spaghetti Bolognese."))
              .addDialog(new DialogLine("npc", "Great choice! Please wait a moment while we prepare your ingredients.", "Lựa chọn tuyệt vời! Vui lòng chờ trong giây lát."));
        level1.setScene1Script(scene1);

        // Scene 2 Script: Shopping - include ALL ingredients needed for cooking
        SceneScript scene2 = new SceneScript();
        scene2.setRequiredIngredientIds(Arrays.asList("tomato", "onion", "garlic", "beef", "pasta", "olive_oil", "salt", "basil", "cheese", "sauce"));
        scene2.setDistractorIngredientIds(Arrays.asList("pepper_distractor", "carrot_distractor", "chicken_distractor"));
        level1.setScene2Script(scene2);

        // Scene 3 Script: Cooking
        SceneScript scene3 = new SceneScript();
        scene3.addCookingStep(CookingStep.dragDrop("onion", "zone_cutting", "Place onion on cutting board"))
              .addCookingStep(CookingStep.tapZone("zone_cutting", 3, "Tap to chop the onion"))
              .addCookingStep(CookingStep.dragDrop("garlic", "zone_cutting", "Place garlic on cutting board"))
              .addCookingStep(CookingStep.tapZone("zone_cutting", 3, "Tap to chop the garlic"))
              .addCookingStep(CookingStep.dragDrop("olive_oil", "zone_stove", "Pour olive oil into pan"))
              .addCookingStep(CookingStep.hold("olive_oil", "zone_stove", 500, "Hold to pour oil"))
              .addCookingStep(CookingStep.dragDrop("onion", "zone_stove", "Add chopped onion to pan"))
              .addCookingStep(CookingStep.dragDrop("garlic", "zone_stove", "Add chopped garlic to pan"))
              .addCookingStep(CookingStep.dragDrop("beef", "zone_stove", "Add ground beef to pan"))
              .addCookingStep(CookingStep.stir("zone_stove", 0.8f, "Stir the ingredients"))
              .addCookingStep(CookingStep.dragDrop("tomato", "zone_stove", "Add tomatoes to the pan"))
              .addCookingStep(CookingStep.dragDrop("salt", "zone_stove", "Add salt"))
              .addCookingStep(CookingStep.dragDrop("basil", "zone_stove", "Add basil leaves"))
              .addCookingStep(CookingStep.timerWait(2, "Let it simmer..."))
              .addCookingStep(CookingStep.dragDrop("pasta", "zone_counter", "Place pasta on counter"))
              .addCookingStep(CookingStep.dragDrop("pasta", "zone_plate", "Serve pasta on plate"))
              .addCookingStep(CookingStep.dragDrop("sauce", "zone_plate", "Pour sauce over pasta"))
              .addCookingStep(CookingStep.dragDrop("cheese", "zone_plate", "Sprinkle cheese on top"));
        level1.setScene3Script(scene3);

        levels.add(level1);
        
        // === LEVEL 2: Margherita Pizza ===
        createIngredient("flour", "Flour", "Bột mì", "ing_flour", "grain");
        createIngredient("yeast", "Yeast", "Men", "ing_yeast", "spice");
        createIngredient("mozzarella", "Mozzarella", "Phô mai Mozzarella", "ing_mozzarella", "dairy");
        Food pizza = new Food("margherita_pizza", "Margherita Pizza", "Pizza Margherita", 
            "dish_pizza", "italian", Arrays.asList("flour", "tomato", "mozzarella", "basil", "olive_oil"), 
            "Classic Italian pizza with tomato and cheese");
        foodMap.put(pizza.getDishId(), pizza);
        LevelConfig level2 = new LevelConfig(2, "margherita_pizza", "easy", 0, 50, 80, 100);
        
        // Scene 2 for Level 2
        SceneScript scene2_L2 = new SceneScript();
        scene2_L2.setRequiredIngredientIds(Arrays.asList("flour", "tomato", "mozzarella", "basil", "olive_oil"));
        scene2_L2.setDistractorIngredientIds(Arrays.asList("pepper_distractor", "chicken_distractor"));
        level2.setScene2Script(scene2_L2);
        
        // Scene 3 for Level 2: Pizza cooking
        SceneScript scene3_L2 = new SceneScript();
        scene3_L2.addCookingStep(CookingStep.dragDrop("flour", "zone_counter", "Place flour on counter"))
                 .addCookingStep(CookingStep.tapZone("zone_counter", 3, "Knead the dough"))
                 .addCookingStep(CookingStep.dragDrop("tomato", "zone_counter", "Add tomato sauce"))
                 .addCookingStep(CookingStep.dragDrop("mozzarella", "zone_counter", "Add mozzarella cheese"))
                 .addCookingStep(CookingStep.dragDrop("basil", "zone_counter", "Add fresh basil"))
                 .addCookingStep(CookingStep.dragDrop("olive_oil", "zone_counter", "Drizzle olive oil"))
                 .addCookingStep(CookingStep.timerWait(2, "Bake the pizza..."));
        level2.setScene3Script(scene3_L2);
        
        levels.add(level2);
        
        // Level 3: Caesar Salad
        createIngredient("lettuce", "Lettuce", "Xà lách", "ing_lettuce", "vegetable");
        createIngredient("croutons", "Croutons", "Bánh mì nướng", "ing_croutons", "grain");
        createIngredient("caesar_dressing", "Caesar Dressing", "Sốt Caesar", "ing_caesar", "sauce");
        Food salad = new Food("caesar_salad", "Caesar Salad", "Salad Caesar", 
            "dish_salad", "american", Arrays.asList("lettuce", "cheese", "croutons", "caesar_dressing"), 
            "Fresh salad with Caesar dressing");
        foodMap.put(salad.getDishId(), salad);
        LevelConfig level3 = new LevelConfig(3, "caesar_salad", "easy", 0, 50, 80, 100);
        
        SceneScript scene2_L3 = new SceneScript();
        scene2_L3.setRequiredIngredientIds(Arrays.asList("lettuce", "cheese", "croutons", "caesar_dressing"));
        scene2_L3.setDistractorIngredientIds(Arrays.asList("tomato", "onion"));
        level3.setScene2Script(scene2_L3);
        
        SceneScript scene3_L3 = new SceneScript();
        scene3_L3.addCookingStep(CookingStep.dragDrop("lettuce", "zone_cutting", "Chop the lettuce"))
                 .addCookingStep(CookingStep.tapZone("zone_cutting", 3, "Cut the lettuce"))
                 .addCookingStep(CookingStep.dragDrop("lettuce", "zone_plate", "Place lettuce on plate"))
                 .addCookingStep(CookingStep.dragDrop("croutons", "zone_plate", "Add croutons"))
                 .addCookingStep(CookingStep.dragDrop("cheese", "zone_plate", "Sprinkle cheese"))
                 .addCookingStep(CookingStep.dragDrop("caesar_dressing", "zone_plate", "Pour Caesar dressing"));
        level3.setScene3Script(scene3_L3);
        
        levels.add(level3);
        
        // Level 4: Beef Burger
        createIngredient("bun", "Burger Bun", "Bánh burger", "ing_bun", "grain");
        createIngredient("patty", "Beef Patty", "Miếng thịt burger", "ing_patty", "meat");
        createIngredient("lettuce_burger", "Lettuce", "Xà lách", "ing_lettuce", "vegetable");
        createIngredient("tomato_burger", "Tomato Slice", "Lát cà chua", "ing_tomato", "vegetable");
        Food burger = new Food("beef_burger", "Beef Burger", "Burger thịt bò", 
            "dish_burger", "american", Arrays.asList("bun", "patty", "lettuce_burger", "tomato_burger", "cheese"), 
            "Juicy beef burger with fresh vegetables");
        foodMap.put(burger.getDishId(), burger);
        LevelConfig level4 = new LevelConfig(4, "beef_burger", "medium", 0, 50, 80, 100);
        
        SceneScript scene2_L4 = new SceneScript();
        scene2_L4.setRequiredIngredientIds(Arrays.asList("bun", "patty", "lettuce_burger", "tomato_burger", "cheese"));
        scene2_L4.setDistractorIngredientIds(Arrays.asList("onion", "garlic"));
        level4.setScene2Script(scene2_L4);
        
        SceneScript scene3_L4 = new SceneScript();
        scene3_L4.addCookingStep(CookingStep.dragDrop("patty", "zone_stove", "Grill the patty"))
                 .addCookingStep(CookingStep.timerWait(2, "Wait for patty to cook..."))
                 .addCookingStep(CookingStep.stir("zone_stove", 0.5f, "Flip the patty"))
                 .addCookingStep(CookingStep.dragDrop("bun", "zone_plate", "Place bottom bun"))
                 .addCookingStep(CookingStep.dragDrop("lettuce_burger", "zone_plate", "Add lettuce"))
                 .addCookingStep(CookingStep.dragDrop("tomato_burger", "zone_plate", "Add tomato"))
                 .addCookingStep(CookingStep.dragDrop("patty", "zone_plate", "Add patty"))
                 .addCookingStep(CookingStep.dragDrop("cheese", "zone_plate", "Add cheese"))
                 .addCookingStep(CookingStep.dragDrop("bun", "zone_plate", "Place top bun"));
        level4.setScene3Script(scene3_L4);
        levels.add(level4);
        
        // Level 5: Tom Yum Soup
        createIngredient("shrimp", "Shrimp", "Tôm", "ing_shrimp", "seafood");
        createIngredient("lemongrass", "Lemongrass", "Sả", "ing_lemongrass", "spice");
        createIngredient("lime", "Lime", "Chanh", "ing_lime", "fruit");
        createIngredient("chili", "Chili", "Ớt", "ing_chili", "spice");
        Food soup = new Food("tom_yum", "Tom Yum Soup", "Súp Tom Yum", 
            "dish_soup", "thai", Arrays.asList("shrimp", "lemongrass", "tomato", "lime", "chili"), 
            "Spicy and sour Thai soup");
        foodMap.put(soup.getDishId(), soup);
        LevelConfig level5 = new LevelConfig(5, "tom_yum", "medium", 0, 50, 80, 100);
        
        SceneScript scene2_L5 = new SceneScript();
        scene2_L5.setRequiredIngredientIds(Arrays.asList("shrimp", "lemongrass", "tomato", "lime", "chili"));
        scene2_L5.setDistractorIngredientIds(Arrays.asList("beef", "pasta"));
        level5.setScene2Script(scene2_L5);
        
        SceneScript scene3_L5 = new SceneScript();
        scene3_L5.addCookingStep(CookingStep.dragDrop("lemongrass", "zone_stove", "Add lemongrass to pot"))
                 .addCookingStep(CookingStep.dragDrop("tomato", "zone_stove", "Add tomatoes"))
                 .addCookingStep(CookingStep.dragDrop("chili", "zone_stove", "Add chili"))
                 .addCookingStep(CookingStep.timerWait(2, "Let it boil..."))
                 .addCookingStep(CookingStep.dragDrop("shrimp", "zone_stove", "Add shrimp"))
                 .addCookingStep(CookingStep.stir("zone_stove", 0.5f, "Stir the soup"))
                 .addCookingStep(CookingStep.dragDrop("lime", "zone_stove", "Squeeze lime"))
                 .addCookingStep(CookingStep.timerWait(2, "Soup is ready!"));
        level5.setScene3Script(scene3_L5);
        
        levels.add(level5);
        
        // Level 6: Sushi Roll
        createIngredient("rice", "Sushi Rice", "Cơm sushi", "ing_rice", "grain");
        createIngredient("nori", "Nori Sheet", "Rong biển", "ing_nori", "seafood");
        createIngredient("salmon", "Salmon", "Cá hồi", "ing_salmon", "seafood");
        createIngredient("avocado", "Avocado", "Bơ", "ing_avocado", "fruit");
        Food sushi = new Food("sushi_roll", "Sushi Roll", "Sushi cuốn", 
            "dish_sushi", "japanese", Arrays.asList("rice", "nori", "salmon", "avocado"), 
            "Fresh sushi roll with salmon");
        foodMap.put(sushi.getDishId(), sushi);
        LevelConfig level6 = new LevelConfig(6, "sushi_roll", "medium", 0, 50, 80, 100);
        
        SceneScript scene2_L6 = new SceneScript();
        scene2_L6.setRequiredIngredientIds(Arrays.asList("rice", "nori", "salmon", "avocado"));
        scene2_L6.setDistractorIngredientIds(Arrays.asList("cheese", "tomato"));
        level6.setScene2Script(scene2_L6);
        
        SceneScript scene3_L6 = new SceneScript();
        scene3_L6.addCookingStep(CookingStep.dragDrop("nori", "zone_counter", "Place nori sheet"))
                 .addCookingStep(CookingStep.dragDrop("rice", "zone_counter", "Spread rice on nori"))
                 .addCookingStep(CookingStep.dragDrop("salmon", "zone_counter", "Add salmon"))
                 .addCookingStep(CookingStep.dragDrop("avocado", "zone_counter", "Add avocado"))
                 .addCookingStep(CookingStep.tapZone("zone_counter", 3, "Roll the sushi"))
                 .addCookingStep(CookingStep.tapZone("zone_cutting", 4, "Cut into pieces"));
        level6.setScene3Script(scene3_L6);
        
        levels.add(level6);
        
        // Level 7: Chicken Curry
        createIngredient("chicken_meat", "Chicken", "Thịt gà", "ing_chicken", "meat");
        createIngredient("curry_powder", "Curry Powder", "Bột cà ri", "ing_curry", "spice");
        createIngredient("coconut_milk", "Coconut Milk", "Nước cốt dừa", "ing_coconut", "dairy");
        createIngredient("potato", "Potato", "Khoai tây", "ing_potato", "vegetable");
        Food curry = new Food("chicken_curry", "Chicken Curry", "Cà ri gà", 
            "dish_curry", "indian", Arrays.asList("chicken_meat", "curry_powder", "coconut_milk", "potato", "onion"), 
            "Rich and creamy chicken curry");
        foodMap.put(curry.getDishId(), curry);
        LevelConfig level7 = new LevelConfig(7, "chicken_curry", "hard", 0, 50, 80, 100);
        
        SceneScript scene2_L7 = new SceneScript();
        scene2_L7.setRequiredIngredientIds(Arrays.asList("chicken_meat", "curry_powder", "coconut_milk", "potato", "onion"));
        scene2_L7.setDistractorIngredientIds(Arrays.asList("beef", "tomato"));
        level7.setScene2Script(scene2_L7);
        
        SceneScript scene3_L7 = new SceneScript();
        scene3_L7.addCookingStep(CookingStep.dragDrop("chicken_meat", "zone_cutting", "Cut chicken"))
                 .addCookingStep(CookingStep.tapZone("zone_cutting", 4, "Dice the chicken"))
                 .addCookingStep(CookingStep.dragDrop("onion", "zone_stove", "Fry onion"))
                 .addCookingStep(CookingStep.dragDrop("curry_powder", "zone_stove", "Add curry powder"))
                 .addCookingStep(CookingStep.dragDrop("chicken_meat", "zone_stove", "Add chicken"))
                 .addCookingStep(CookingStep.dragDrop("potato", "zone_stove", "Add potatoes"))
                 .addCookingStep(CookingStep.dragDrop("coconut_milk", "zone_stove", "Pour coconut milk"))
                 .addCookingStep(CookingStep.stir("zone_stove", 0.8f, "Stir well"))
                 .addCookingStep(CookingStep.timerWait(2, "Simmer the curry..."));
        level7.setScene3Script(scene3_L7);
        
        levels.add(level7);
        
        // Level 8: Pad Thai
        createIngredient("rice_noodles", "Rice Noodles", "Phở", "ing_noodles", "grain");
        createIngredient("tamarind", "Tamarind", "Me", "ing_tamarind", "fruit");
        createIngredient("peanuts", "Peanuts", "Đậu phộng", "ing_peanuts", "nut");
        createIngredient("bean_sprouts", "Bean Sprouts", "Giá đỗ", "ing_sprouts", "vegetable");
        Food padthai = new Food("pad_thai", "Pad Thai", "Pad Thai", 
            "dish_padthai", "thai", Arrays.asList("rice_noodles", "shrimp", "tamarind", "peanuts", "bean_sprouts"), 
            "Famous Thai stir-fried noodles");
        foodMap.put(padthai.getDishId(), padthai);
        LevelConfig level8 = new LevelConfig(8, "pad_thai", "hard", 0, 50, 80, 100);
        
        SceneScript scene2_L8 = new SceneScript();
        scene2_L8.setRequiredIngredientIds(Arrays.asList("rice_noodles", "shrimp", "tamarind", "peanuts", "bean_sprouts"));
        scene2_L8.setDistractorIngredientIds(Arrays.asList("salmon", "cheese"));
        level8.setScene2Script(scene2_L8);
        
        SceneScript scene3_L8 = new SceneScript();
        scene3_L8.addCookingStep(CookingStep.dragDrop("rice_noodles", "zone_stove", "Add noodles to wok"))
                 .addCookingStep(CookingStep.dragDrop("shrimp", "zone_stove", "Add shrimp"))
                 .addCookingStep(CookingStep.stir("zone_stove", 0.7f, "Stir-fry"))
                 .addCookingStep(CookingStep.dragDrop("tamarind", "zone_stove", "Add tamarind sauce"))
                 .addCookingStep(CookingStep.dragDrop("bean_sprouts", "zone_stove", "Add bean sprouts"))
                 .addCookingStep(CookingStep.stir("zone_stove", 0.5f, "Toss everything"))
                 .addCookingStep(CookingStep.timerWait(2, "Almost ready..."))
                 .addCookingStep(CookingStep.dragDrop("peanuts", "zone_plate", "Sprinkle peanuts"));
        level8.setScene3Script(scene3_L8);
        
        levels.add(level8);
        
        // Level 9: Beef Steak
        createIngredient("beef_steak", "Beef Steak", "Bít tết bò", "ing_steak", "meat");
        createIngredient("butter", "Butter", "Bơ", "ing_butter", "dairy");
        createIngredient("rosemary", "Rosemary", "Hương thảo", "ing_rosemary", "spice");
        createIngredient("mushroom", "Mushroom", "Nấm", "ing_mushroom", "vegetable");
        Food steak = new Food("beef_steak", "Beef Steak", "Bít tết bò", 
            "dish_steak", "american", Arrays.asList("beef_steak", "butter", "rosemary", "mushroom", "salt"), 
            "Tender grilled beef steak");
        foodMap.put(steak.getDishId(), steak);
        LevelConfig level9 = new LevelConfig(9, "beef_steak", "hard", 0, 50, 80, 100);
        
        SceneScript scene2_L9 = new SceneScript();
        scene2_L9.setRequiredIngredientIds(Arrays.asList("beef_steak", "butter", "rosemary", "mushroom", "salt"));
        scene2_L9.setDistractorIngredientIds(Arrays.asList("chicken_meat", "onion"));
        level9.setScene2Script(scene2_L9);
        
        SceneScript scene3_L9 = new SceneScript();
        scene3_L9.addCookingStep(CookingStep.dragDrop("butter", "zone_stove", "Melt butter in pan"))
                 .addCookingStep(CookingStep.dragDrop("beef_steak", "zone_stove", "Place steak in pan"))
                 .addCookingStep(CookingStep.dragDrop("rosemary", "zone_stove", "Add rosemary"))
                 .addCookingStep(CookingStep.timerWait(2, "Sear one side..."))
                 .addCookingStep(CookingStep.stir("zone_stove", 0.5f, "Flip the steak"))
                 .addCookingStep(CookingStep.timerWait(2, "Cook other side..."))
                 .addCookingStep(CookingStep.dragDrop("mushroom", "zone_stove", "Add mushrooms"))
                 .addCookingStep(CookingStep.dragDrop("salt", "zone_stove", "Season with salt"))
                 .addCookingStep(CookingStep.timerWait(2, "Steak is ready!"));
        level9.setScene3Script(scene3_L9);
        
        levels.add(level9);
        
        // Level 10: Chocolate Cake
        createIngredient("cocoa", "Cocoa Powder", "Bột ca cao", "ing_cocoa", "spice");
        createIngredient("sugar", "Sugar", "Đường", "ing_sugar", "grain");
        createIngredient("egg", "Egg", "Trứng", "ing_egg", "dairy");
        createIngredient("milk", "Milk", "Sữa", "ing_milk", "dairy");
        Food cake = new Food("chocolate_cake", "Chocolate Cake", "Bánh Socola", 
            "dish_cake", "dessert", Arrays.asList("flour", "cocoa", "sugar", "egg", "milk", "butter"), 
            "Rich chocolate layer cake");
        foodMap.put(cake.getDishId(), cake);
        LevelConfig level10 = new LevelConfig(10, "chocolate_cake", "expert", 0, 50, 80, 100);
        
        SceneScript scene2_L10 = new SceneScript();
        scene2_L10.setRequiredIngredientIds(Arrays.asList("flour", "cocoa", "sugar", "egg", "milk", "butter"));
        scene2_L10.setDistractorIngredientIds(Arrays.asList("salt", "cheese"));
        level10.setScene2Script(scene2_L10);
        
        SceneScript scene3_L10 = new SceneScript();
        scene3_L10.addCookingStep(CookingStep.dragDrop("flour", "zone_counter", "Add flour to bowl"))
                  .addCookingStep(CookingStep.dragDrop("cocoa", "zone_counter", "Add cocoa powder"))
                  .addCookingStep(CookingStep.dragDrop("sugar", "zone_counter", "Add sugar"))
                  .addCookingStep(CookingStep.stir("zone_counter", 0.5f, "Mix dry ingredients"))
                  .addCookingStep(CookingStep.dragDrop("egg", "zone_counter", "Add eggs"))
                  .addCookingStep(CookingStep.dragDrop("milk", "zone_counter", "Pour milk"))
                  .addCookingStep(CookingStep.dragDrop("butter", "zone_counter", "Add melted butter"))
                  .addCookingStep(CookingStep.stir("zone_counter", 0.8f, "Mix until smooth"))
                  .addCookingStep(CookingStep.timerWait(2, "Baking the cake..."));
        level10.setScene3Script(scene3_L10);
        
        levels.add(level10);
    }

    private void createIngredient(String id, String name, String nameVi, String icon, String category) {
        Ingredient ing = new Ingredient(id, name, nameVi, icon, category);
        ingredientMap.put(id, ing);
    }

    // === PUBLIC API ===

    /**
     * Get all available levels
     */
    public List<LevelConfig> getAllLevels() {
        List<LevelConfig> result = new ArrayList<>();
        for (LevelConfig level : levels) {
            // Populate Food object from foodMap
            Food food = foodMap.get(level.getDishId());
            level.setFood(food);
            result.add(level);
        }
        return result;
    }

    /**
     * Get level by ID
     */
    public LevelConfig getLevel(int levelId) {
        for (LevelConfig level : levels) {
            if (level.getLevelId() == levelId) {
                // Populate Food object from foodMap
                Food food = foodMap.get(level.getDishId());
                level.setFood(food);
                return level;
            }
        }
        return null;
    }

    /**
     * Get food by dish ID
     */
    public Food getFood(String dishId) {
        return foodMap.get(dishId);
    }

    /**
     * Get ingredient by ID
     */
    public Ingredient getIngredient(String ingredientId) {
        return ingredientMap.get(ingredientId);
    }

    /**
     * Get multiple ingredients by IDs
     */
    public List<Ingredient> getIngredients(List<String> ids) {
        List<Ingredient> result = new ArrayList<>();
        for (String id : ids) {
            Ingredient ing = ingredientMap.get(id);
            if (ing != null) {
                result.add(ing);
            }
        }
        return result;
    }

    /**
     * Get all ingredients for a dish
     */
    public List<Ingredient> getIngredientsForFood(String dishId) {
        Food food = foodMap.get(dishId);
        if (food != null) {
            return getIngredients(food.getIngredientIds());
        }
        return new ArrayList<>();
    }

    /**
     * Get all ingredients for a dish (alias method for cookbook)
     */
    public List<Ingredient> getIngredientsForDish(String dishId) {
        return getIngredientsForFood(dishId);
    }
}
