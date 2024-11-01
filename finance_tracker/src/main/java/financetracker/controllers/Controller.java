package financetracker.controllers;

import financetracker.models.Model;

public abstract class Controller<T extends Model> {
    private String saveFile; 
    
    protected void appendNewData(T t) {
        
    }

    protected void removeData(long id) {

    }

    protected void createSaveFile() {
        
    }
} 
