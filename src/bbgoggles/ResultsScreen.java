package bbgoggles;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;

public class ResultsScreen extends MainScreen {
    
    public ResultsScreen() {
        super();
        setTitle("BBGoggles");
        //getMainManager().setBackground(BackgroundFactory.createLinearGradientBackground(0x0099CCFF, 0x0099CCFF, 0x00336699,0x00336699));  
        final Screen camScreen = new CameraScreen(); //wait pop-up screen extends RIM's PopupScreen class
        ButtonField requestButton = new ButtonField("Capture Image");
        requestButton.setChangeListener(new FieldChangeListener() {
            public void fieldChanged(Field field, int context) {
                //push the CameraScreen
            	deleteAll();
            	UiApplication.getUiApplication().pushModalScreen(camScreen);
            }
        });
        setStatus(requestButton);
    }
     
    //this method will be called from the connection thread if unsuccessful
    public void errorCallBackMethod(String responseData){
    	deleteAll();
    	add(new LabelField("Unsuccessful Request"));
    	//add(new SeparatorField());
    	add(new RichTextField(responseData));
    }
    
    protected boolean onSavePrompt()
    {
        // Prevent the save dialog from being displayed
        return true;
    }
}
