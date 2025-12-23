package com.edu.english.masterchef.data;

import java.io.Serializable;

/**
 * Model representing a cooking tool.
 */
public class Tool implements Serializable {
    
    private String id;
    private String nameEn;
    private String nameVi;
    private String drawableName;
    private ActionType associatedAction;
    private ZoneType targetZone;
    
    public Tool() {}
    
    public Tool(String id, String nameEn, String nameVi, String drawableName,
                ActionType associatedAction, ZoneType targetZone) {
        this.id = id;
        this.nameEn = nameEn;
        this.nameVi = nameVi;
        this.drawableName = drawableName;
        this.associatedAction = associatedAction;
        this.targetZone = targetZone;
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
    
    public ActionType getAssociatedAction() { return associatedAction; }
    public void setAssociatedAction(ActionType associatedAction) { this.associatedAction = associatedAction; }
    
    public ZoneType getTargetZone() { return targetZone; }
    public void setTargetZone(ZoneType targetZone) { this.targetZone = targetZone; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tool tool = (Tool) o;
        return id != null && id.equals(tool.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
