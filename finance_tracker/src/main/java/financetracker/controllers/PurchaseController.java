package financetracker.controllers;

import financetracker.exceptions.controller.CannotCreateControllerException;
import financetracker.models.Purchase;

public class PurchaseController extends Controller<Purchase> {
    private static final String DEFAULT_FILE_PATH = "saves\\purchases.dat";
    
    public PurchaseController() throws CannotCreateControllerException {
        this(DEFAULT_FILE_PATH);
    }
    
    public PurchaseController(String filePath) throws CannotCreateControllerException {
        super(filePath);
    }



}
