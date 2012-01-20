package bbgoggles;

import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.container.MainScreen;

public class CameraScreen extends MainScreen {
    /** The camera's video controller */
	private VideoControl _videoControl;

    /** The field containing the feed from the camera */
    private Field _videoField;
    
    public CameraScreen(){
    	 // Initialise the camera object and video field
        initializeCamera();    
        
        // If the field was constructed successfully, create the UI
        if(_videoField != null){
            createUI();
        }
        // If not, display an error message to the user
        else {
        	UiApplication.getUiApplication().popScreen(this);
        	((ResultsScreen)UiApplication.getUiApplication().getActiveScreen()).errorCallBackMethod("Error connecting to camera.");
        }
    }
    
    public void takePicture()
    {
        try
        {
            // A null encoding indicates that the camera should
            // use the default snapshot encoding.
            String encoding = "encoding=jpeg&width=640&height=480";
            
            // Retrieve the raw image from the VideoControl and
            // close this screen and send the image for encoding.
            byte[] image = _videoControl.getSnapshot( encoding );
            UiApplication.getUiApplication().popScreen(this);
            new ConnectionThread(image).start();
        }
        catch(Exception e)
        {
        	UiApplication.getUiApplication().popScreen(this);
        	((ResultsScreen)UiApplication.getUiApplication().getActiveScreen()).errorCallBackMethod("Error taking picture (" + e.getMessage() + ").");
        }  
    }
    
    /**
     * Initialises the Player, VideoControl and VideoField
     */
    private void initializeCamera()
    {
        try
        {
            // Create a player for the Blackberry's camera
            Player player = Manager.createPlayer( "capture://video" );
            
            // Set the player to the REALIZED state (see Player javadoc)
            player.realize();

            // Grab the video control and set it to the current display
            _videoControl = (VideoControl)player.getControl( "VideoControl" );
            //_cameraControl = (CameraControl)player.getControl( "CameraControl" );

            if (_videoControl != null){
                // Create the video field as a GUI primitive (as opposed to a
                // direct video, which can only be used on platforms with
                // LCDUI support.)
            	//_cameraControl.enableShutterFeedback(false);
                _videoField = (Field) _videoControl.initDisplayMode (VideoControl.USE_GUI_PRIMITIVE, "net.rim.device.api.ui.Field");
                _videoControl.setDisplayFullScreen(true);
                _videoControl.setVisible(true);
            }

            // Set the player to the STARTED state (see Player javadoc)
            player.start();
        }
        catch(Exception e){
        	UiApplication.getUiApplication().popScreen(this);
        	((ResultsScreen)UiApplication.getUiApplication().getActiveScreen()).errorCallBackMethod("Error taking picture (" + e.getMessage() + ").");
        }
    }
    
    /**
     * Adds the VideoField to the screen
     */
    private void createUI(){
        // Add the video field to the screen  
        add(_videoField);
    }
    
    /**
     * @see net.rim.device.api.ui.Screen#invokeAction(int)
     */   
    protected boolean invokeAction(int action){
        boolean handled = super.invokeAction(action); 
        
        if(!handled){
            if(action == ACTION_INVOKE){                         
                takePicture();
                return true;                
            }
        }        
        return handled;                
    }
}
