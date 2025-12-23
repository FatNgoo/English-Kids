package com.edu.english.masterchef.data;

import java.io.Serializable;

/**
 * Model representing a cooking ingredient.
 */
public class Ingredient implements Serializable {
    
    private String id;
    private String nameEn;
    private String nameVi;
    private String drawableName;
    private String category; // vegetable, meat, liquid, seasoning
    private boolean canCut;
    private boolean canPound;
    private boolean isLiquid;
    
    public Ingredient() {}
    
    public Ingredient(String id, String nameEn, String nameVi, String drawableName, 
                      String category, boolean canCut, boolean canPound, boolean isLiquid) {
        this.id = id;
        this.nameEn = nameEn;
        this.nameVi = nameVi;
        this.drawableName = drawableName;
        this.category = category;
        this.canCut = canCut;
        this.canPound = canPound;
        this.isLiquid = isLiquid;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    
    public String getNameVi() { return nameVi; }
    public void setNameVi(String nameVi) { this.nameVi = nameVi; }
    
    public String getDrawableName() { return drawableName; }
    public void setDrawableName(String drawableName) { this.drawableName = drawableName; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public boolean isCanCut() { return canCut; }
    public void setCanCut(boolean canCut) { this.canCut = canCut; }
    
    public boolean isCanPound() { return canPound; }
    public void setCanPound(boolean canPound) { this.canPound = canPound; }
    
    public boolean isLiquid() { return isLiquid; }
    public void setLiquid(boolean liquid) { isLiquid = liquid; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
