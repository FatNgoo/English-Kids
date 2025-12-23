package com.edu.english.masterchef.data;

/**
 * Kitchen zones where items can be placed or actions performed.
 * Extended with Oven, Steamer, Grill, Drain, and Garnish zones.
 */
public enum ZoneType {
    /** Fridge/Pantry - source of ingredients */
    FRIDGE_ZONE("Fridge", "Get ingredients from here", "🧊"),
    
    /** Counter - general working surface */
    COUNTER_ZONE("Counter", "Place items here", "📦"),
    
    /** Cutting board - for cutting vegetables */
    BOARD_ZONE("Cutting Board", "Cut ingredients here", "🔪"),
    
    /** Pan - for frying, stir-frying */
    PAN_ZONE("Pan", "Fry food here", "🍳"),
    
    /** Pot - for boiling, making soup */
    POT_ZONE("Pot", "Cook soup here", "🍲"),
    
    /** Serving area - final plating */
    SERVE_ZONE("Plate", "Serve the dish here", "🍽️"),
    
    /** Tool rack - where tools are stored */
    TOOL_ZONE("Tools", "Get cooking tools", "🔧"),
    
    // ===== NEW ZONES =====
    
    /** Oven - for baking */
    OVEN_ZONE("Oven", "Bake food here", "🔥"),
    
    /** Steamer - for steaming */
    STEAMER_ZONE("Steamer", "Steam food here", "♨️"),
    
    /** Grill - for grilling */
    GRILL_ZONE("Grill", "Grill food here", "🥩"),
    
    /** Deep fryer - for deep frying */
    FRYER_ZONE("Fryer", "Deep fry here", "🍟"),
    
    /** Drain zone - for draining oil/water */
    DRAIN_ZONE("Drain Rack", "Let food drain here", "📥"),
    
    /** Garnish/Plating zone - for decoration */
    GARNISH_ZONE("Garnish Station", "Decorate and plate here", "🎨"),
    
    /** Mixing bowl - for whisking, mixing */
    MIXING_ZONE("Mixing Bowl", "Mix ingredients here", "🥣");
    
    private final String displayName;
    private final String hint;
    private final String emoji;
    
    ZoneType(String displayName, String hint, String emoji) {
        this.displayName = displayName;
        this.hint = hint;
        this.emoji = emoji;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getHint() {
        return hint;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    public String getLabelWithEmoji() {
        return emoji + " " + displayName;
    }
}
