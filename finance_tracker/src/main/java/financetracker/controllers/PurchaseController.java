package financetracker.controllers;

import financetracker.exceptions.controller.CannotCreateControllerException;
import financetracker.models.Purchase;
import financetracker.windowing.MainFrame;

public class PurchaseController extends Controller<Purchase> {
    private static final String DEFAULT_FILE_PATH = "saves\\purchases.dat";
    
    public PurchaseController(MainFrame mainFrame) throws CannotCreateControllerException {
        this(DEFAULT_FILE_PATH, mainFrame);
    }
    
    public PurchaseController(String filePath, MainFrame mainFrame) throws CannotCreateControllerException {
        super(filePath, mainFrame);
    }



}
