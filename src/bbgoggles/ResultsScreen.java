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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.StandardTitleBar;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.extension.component.PictureScrollField;
import net.rim.device.api.ui.extension.component.PictureScrollField.HighlightStyle;
import net.rim.device.api.ui.extension.component.PictureScrollField.ScrollEntry;
import net.rim.device.api.util.StringProvider;

public class ResultsScreen extends MainScreen {
	ScrollEntry[] entries;
	Vector scrollImages = new Vector();
    
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
	        	synchronized(Application.getEventLock()){
	        		UiApplication.getUiApplication().popScreen(UiApplication.getUiApplication().getActiveScreen());
	        		UiApplication.getUiApplication().pushScreen(camScreen);
	        	}
        	}
        };
        addMenuItem(takePicture);
    }
    
    public void addImage(String url, String image_site, String image){
    	String imageEntry[] = new String[3];
    	imageEntry[0] = url;
    	imageEntry[1] = image_site;
    	imageEntry[2] = image;
    	scrollImages.addElement(imageEntry);
    }
    
    public void loadImages(){
    	entries = new ScrollEntry[scrollImages.size()];
    	Enumeration elements = scrollImages.elements();
    	int i = 0;
		while( elements.hasMoreElements() ) {
			String element[] = (String[]) elements.nextElement();
			entries[i] = new ScrollEntry(connectServerForImage(element[0]), element[1], element[2]);
			i++;	
		}
    	PictureScrollField pictureScrollField = new PictureScrollField((Display.getWidth()/4)*3,(Display.getHeight()/5)*3);
        pictureScrollField.setData(entries, 0);
    	pictureScrollField.setHighlightStyle(HighlightStyle.ILLUMINATE_WITH_SHRINK_LENS);
        pictureScrollField.setHighlightBorderColor(Color.BLACK);
        pictureScrollField.setBackground(BackgroundFactory.createSolidBackground(Color.BLACK));
        pictureScrollField.setLabelsVisible(true);
        this.getMainManager().setBackground(BackgroundFactory.createSolidBackground(Color.BLACK));
        add(pictureScrollField);
    }
     
    public static Bitmap connectServerForImage(String url) {

	      HttpConnection httpConnection = null;
	      DataOutputStream httpDataOutput = null;
	      InputStream httpInput = null;
	      int rc;

	      Bitmap bitmp = null;
	      try {
	       httpConnection = (HttpConnection) Connector.open(url);
	       rc = httpConnection.getResponseCode();
	       if (rc != HttpConnection.HTTP_OK) {
	        throw new IOException("HTTP response code: " + rc);
	       }
	       httpInput = httpConnection.openInputStream();
	       InputStream inp = httpInput;
	       byte[] b = IOUtilities.streamToBytes(inp);
	       EncodedImage hai = EncodedImage.createEncodedImage(b, 0, b.length);
	       return hai.getBitmap();

	      } catch (Exception ex) {
	       System.out.println("URL Bitmap Error........" + ex.getMessage());
	      } finally {
	       try {
	        if (httpInput != null)
	         httpInput.close();
	        if (httpDataOutput != null)
	         httpDataOutput.close();
	        if (httpConnection != null)
	         httpConnection.close();
	       } catch (Exception e) {
	        e.printStackTrace();

	       }
	      }
	      return bitmp;
	     }
    
    protected boolean onSavePrompt()
    {
        // Prevent the save dialog from being displayed
        return true;
    }
}
