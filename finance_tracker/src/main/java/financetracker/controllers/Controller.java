package financetracker.controllers;

import javax.swing.WindowConstants;

import financetracker.datatypes.Model;
import financetracker.exceptions.modelserailizer.SerializerWasNotCreated;
import financetracker.views.base.FrameView;
import financetracker.windowing.MainFrame;

public abstract class Controller<T extends Model> {
    protected MainFrame mainFrame;
    protected ModelSerailizer<T> modelSerializer;

    protected Controller(String saveFilePath, MainFrame mainFrame) throws SerializerWasNotCreated {
        this.mainFrame = mainFrame;
        this.modelSerializer = new ModelSerailizer<>(saveFilePath);
    }

    public void closeFrameView(FrameView fv) {
        fv.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        fv.dispose();
    }
}
