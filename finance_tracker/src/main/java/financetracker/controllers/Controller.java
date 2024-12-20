package financetracker.controllers;

import javax.swing.WindowConstants;

import financetracker.datatypes.Model;
import financetracker.exceptions.controller.ControllerWasNotCreated;
import financetracker.exceptions.modelserailizer.SerializerWasNotCreated;
import financetracker.views.base.FrameView;
import financetracker.windowing.MainFrame;

public abstract class Controller<T extends Model> {
    protected MainFrame mainFrame;
    protected ModelSerailizer<T> modelSerializer;

    protected Controller(String saveFilePath, MainFrame mainFrame) throws ControllerWasNotCreated {
        this.mainFrame = mainFrame;
        try {
            this.modelSerializer = new ModelSerailizer<>(saveFilePath);
        } catch (SerializerWasNotCreated e) {
            throw new ControllerWasNotCreated("Model Serializer could not be initialized - " + this.getClass().toString() , this.getClass());
        }
    }

    /**
     * Closes the frame view in the paramteres
     * @param frameView the frame view you want to close
     */
    public void closeFrameView(FrameView frameView) {
        frameView.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frameView.dispose();
    }
}
