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

import net.rim.device.api.system.Application;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.component.StandardTitleBar;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.util.StringProvider;

public class ResultsScreen extends MainScreen {
    
    public ResultsScreen() {
        super();
        StandardTitleBar myTitleBar = new StandardTitleBar()
        .addIcon("camera_over.png")
        .addTitle("BBGoggles")
        .addClock()
        .addNotifications()
        .addSignalIndicator();
		myTitleBar.setPropertyValue(StandardTitleBar.PROPERTY_BATTERY_VISIBILITY,
        StandardTitleBar.BATTERY_VISIBLE_LOW_OR_CHARGING);
    	setTitleBar(myTitleBar); 
        final Screen camScreen = new CameraScreen();
        StringProvider takePictureText = new StringProvider("Capture Image");
        final MenuItem takePicture = new MenuItem(takePictureText,110,11)
        {
        	public void run()
        	{
	        	deleteAll();
	        	synchronized(Application.getEventLock()){
	        		UiApplication.getUiApplication().pushScreen(camScreen);
	        	}
        	}
        };
        addMenuItem(takePicture);
    }
     
    //this method will be called from the connection thread if unsuccessful
    public void errorCallBackMethod(String responseData){
    	deleteAll();
    	getMainManager().setBackground(BackgroundFactory.createSolidBackground(Color.WHITE));
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
