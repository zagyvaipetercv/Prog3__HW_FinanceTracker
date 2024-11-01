package financetracker.controllers;

import financetracker.models.Model;

public abstract class Controller<T extends Model> {
    private String saveFile; 
    
    protected void appendToFile() {
        
    }
} 
