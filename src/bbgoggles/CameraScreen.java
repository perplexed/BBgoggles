/*This file is part of BBgoggles.

BBgoggles is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

BBgoggles is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with BBgoggles.  If not, see <http://www.gnu.org/licenses/>.*/

package bbgoggles;

import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.StandardTitleBar;
import net.rim.device.api.ui.container.MainScreen;


public class CameraScreen extends MainScreen {
	//PERSISTENT_KEY String is "imageencoding"
	private static final long PERSISTENT_KEY = 0x438e53a4e668d986L;
	static PersistentObject store;
    static {
    store = PersistentStore.getPersistentObject( PERSISTENT_KEY );
    }
    
    /** The camera's video controller */
	private VideoControl _videoControl;

    /** The field containing the feed from the camera */
    private Field _videoField;
    
    public CameraScreen(){
    	StandardTitleBar myTitleBar = new StandardTitleBar()
        .addIcon("camera_over.png")
        .addTitle("BBGoggles")
        .addClock()
        .addNotifications()
        .addSignalIndicator();
		myTitleBar.setPropertyValue(StandardTitleBar.PROPERTY_BATTERY_VISIBILITY,
        StandardTitleBar.BATTERY_VISIBLE_LOW_OR_CHARGING);
    	setTitleBar(myTitleBar);
    	
    	 // Initialise the camera object and video field
        initializeCamera();    
        
        // If the field was constructed successfully, create the UI
        if(_videoField != null){
            createUI();
        }
        // If not, display an error message to the user
        else {
        	UiApplication.getUiApplication().popScreen(this);
        	Dialog.alert("Error connecting to camera.");
        }
    }
    
    public void takePicture()
    {
        try
        {
            // A null encoding indicates that the camera should
            // use the default snapshot encoding.
        	String _storedEncoding = (String) store.getContents();
        	
        	if( _storedEncoding == null )
        	{
        		_storedEncoding = "encoding=jpeg&width=640&height=480";
	        	store.setContents( _storedEncoding );
	        	store.commit();
        	}
 
            // Retrieve the raw image from the VideoControl and
            // close this screen and send the image for encoding.
            byte[] image = _videoControl.getSnapshot( _storedEncoding );
            FrozenImage frozenScreen = new FrozenImage(image, this);
            UiApplication.getUiApplication().pushScreen(frozenScreen);
        }
        catch(Exception e)
        {
        	UiApplication.getUiApplication().popScreen(this);
        	Dialog.alert("Error taking picture (" + e.getMessage() + ").");
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
                
              //Retrieve the list of valid encodings.
                /*String encodingString = System.getProperty("video.snapshot.encodings");

                //Extract the properties as an array of words.
                String[] properties = StringUtilities.stringToKeywords(encodingString);

                //The list of encodings;
                Vector encodingList = new Vector();

                //Strings representing the four properties of an encoding as
                //returned by System.getProperty().
                String encoding = "encoding";
                String width = "width";
                String height = "height";
                String quality = "quality";*/
            }

            // Set the player to the STARTED state (see Player javadoc)
            player.start();
        }
        catch(Exception e){
        	UiApplication.getUiApplication().popScreen(this);
        	Dialog.alert("Error taking picture (" + e.getMessage() + ").");
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
