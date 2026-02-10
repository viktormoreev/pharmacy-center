package com.inf.cscb869_pharmacy.recipe.entity;

/**
 * Status of a prescription/recipe
 */
public enum RecipeStatus {
    /**
     * Prescription is active and can be filled
     */
    ACTIVE,
    
    /**
     * Prescription has been fulfilled/dispensed
     */
    FULFILLED,
    
    /**
     * Prescription has expired
     */
    EXPIRED,
    
    /**
     * Prescription was cancelled by doctor
     */
    CANCELLED
}
