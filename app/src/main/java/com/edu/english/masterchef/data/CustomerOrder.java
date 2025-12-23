package com.edu.english.masterchef.data;

/**
 * Represents a customer order in the Master Chef game.
 * Each order has a customer, requested recipe, and intro text.
 */
public class CustomerOrder {
    
    private String orderId;
    private String recipeId;
    private String customerName;
    private String customerNameVi;
    private String customerAvatarDrawable;
    private String introTextEn;       // "Hi! I'd like some Fried Rice, please!"
    private String introTextVi;
    private String thankYouTextEn;    // "Thank you! It looks delicious!"
    private String thankYouTextVi;
    
    // Order metadata
    private int difficulty;           // 1-3 stars
    private int bonusStars;           // Extra stars for perfect completion
    private long estimatedTimeMs;     // Estimated cooking time
    
    public CustomerOrder() {}
    
    public CustomerOrder(String orderId, String recipeId, 
                         String customerName, String customerNameVi,
                         String customerAvatarDrawable,
                         String introTextEn, String introTextVi,
                         String thankYouTextEn, String thankYouTextVi,
                         int difficulty, int bonusStars, long estimatedTimeMs) {
        this.orderId = orderId;
        this.recipeId = recipeId;
        this.customerName = customerName;
        this.customerNameVi = customerNameVi;
        this.customerAvatarDrawable = customerAvatarDrawable;
        this.introTextEn = introTextEn;
        this.introTextVi = introTextVi;
        this.thankYouTextEn = thankYouTextEn;
        this.thankYouTextVi = thankYouTextVi;
        this.difficulty = difficulty;
        this.bonusStars = bonusStars;
        this.estimatedTimeMs = estimatedTimeMs;
    }
    
    // === Builder Pattern ===
    
    public static class Builder {
        private CustomerOrder order = new CustomerOrder();
        
        public Builder orderId(String id) { order.orderId = id; return this; }
        public Builder recipeId(String id) { order.recipeId = id; return this; }
        public Builder customerName(String name) { order.customerName = name; return this; }
        public Builder customerNameVi(String name) { order.customerNameVi = name; return this; }
        public Builder customerAvatar(String drawable) { order.customerAvatarDrawable = drawable; return this; }
        public Builder introTextEn(String text) { order.introTextEn = text; return this; }
        public Builder introTextVi(String text) { order.introTextVi = text; return this; }
        public Builder thankYouEn(String text) { order.thankYouTextEn = text; return this; }
        public Builder thankYouVi(String text) { order.thankYouTextVi = text; return this; }
        public Builder difficulty(int stars) { order.difficulty = stars; return this; }
        public Builder bonusStars(int stars) { order.bonusStars = stars; return this; }
        public Builder estimatedTime(long ms) { order.estimatedTimeMs = ms; return this; }
        
        public CustomerOrder build() { return order; }
    }
    
    // === Getters ===
    
    public String getOrderId() { return orderId; }
    public String getRecipeId() { return recipeId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerNameVi() { return customerNameVi; }
    public String getCustomerAvatarDrawable() { return customerAvatarDrawable; }
    public String getIntroTextEn() { return introTextEn; }
    public String getIntroTextVi() { return introTextVi; }
    public String getThankYouTextEn() { return thankYouTextEn; }
    public String getThankYouTextVi() { return thankYouTextVi; }
    public int getDifficulty() { return difficulty; }
    public int getBonusStars() { return bonusStars; }
    public long getEstimatedTimeMs() { return estimatedTimeMs; }
    
    // === Setters ===
    
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setRecipeId(String recipeId) { this.recipeId = recipeId; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setCustomerNameVi(String customerNameVi) { this.customerNameVi = customerNameVi; }
    public void setCustomerAvatarDrawable(String customerAvatarDrawable) { this.customerAvatarDrawable = customerAvatarDrawable; }
    public void setIntroTextEn(String introTextEn) { this.introTextEn = introTextEn; }
    public void setIntroTextVi(String introTextVi) { this.introTextVi = introTextVi; }
    public void setThankYouTextEn(String thankYouTextEn) { this.thankYouTextEn = thankYouTextEn; }
    public void setThankYouTextVi(String thankYouTextVi) { this.thankYouTextVi = thankYouTextVi; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
    public void setBonusStars(int bonusStars) { this.bonusStars = bonusStars; }
    public void setEstimatedTimeMs(long estimatedTimeMs) { this.estimatedTimeMs = estimatedTimeMs; }
}
