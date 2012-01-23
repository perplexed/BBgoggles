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
import java.util.Date;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.StandardTitleBar;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.StringProvider;

public class FrozenImage extends MainScreen {
	public FrozenImage(final byte[] image){
		StandardTitleBar myTitleBar = new StandardTitleBar()
        .addIcon("camera_over.png")
        .addTitle("BBGoggles")
        .addClock()
        .addNotifications()
        .addSignalIndicator();
		myTitleBar.setPropertyValue(StandardTitleBar.PROPERTY_BATTERY_VISIBILITY,
        StandardTitleBar.BATTERY_VISIBLE_LOW_OR_CHARGING);
    	setTitleBar(myTitleBar);
		try {
			EncodedImage _encodedImage = EncodedImage.createEncodedImage(image, 0, image.length, "image/jpeg");
			_encodedImage = sizeImage(_encodedImage, Display.getWidth(), Display.getHeight());
	        add(new BitmapField(_encodedImage.getBitmap()));
	        StringProvider uploadPictureText = new StringProvider("Upload Image");
	        final MenuItem uploadPicture = new MenuItem(uploadPictureText,110,11)
	        {
	        	public void run()
	        	{
	        		UiApplication.getUiApplication().popScreen(UiApplication.getUiApplication().getActiveScreen().getScreenBelow());
	                UiApplication.getUiApplication().popScreen(UiApplication.getUiApplication().getActiveScreen());
	        		new ConnectionThread(image).start();
	            }
	        };
	        StringProvider capturePictureText = new StringProvider("Capture Image");
	        final MenuItem capturePicture = new MenuItem(capturePictureText,110,11)
	        {
	        	public void run()
	        	{
	                UiApplication.getUiApplication().popScreen(UiApplication.getUiApplication().getActiveScreen());
	            }
	        };
	        StringProvider savePictureText = new StringProvider("Save Image");
	        final MenuItem savePicture = new MenuItem(savePictureText,110,11)
	        {
	        	public void run()
	        	{
	                Date date = new Date(); 
	        		saveImage("file:///SDCard/"+String.valueOf(date.hashCode())+".jpg", image);
	            }
	        };
	        addMenuItem(uploadPicture);
	        addMenuItem(capturePicture);
	        addMenuItem(savePicture);
		}
		catch(Exception e)
        {
        	UiApplication.getUiApplication().popScreen(this);
        	UiApplication.getUiApplication().popScreen(UiApplication.getUiApplication().getActiveScreen());
        	((ResultsScreen)UiApplication.getUiApplication().getActiveScreen()).errorCallBackMethod("Error freezing image (" + e.getMessage() + ").");
        }  
	}
	
	public EncodedImage sizeImage(EncodedImage image, int width, 
			  int height) {
			  EncodedImage result = null;

			  int currentWidthFixed32 = Fixed32.toFP(image.getWidth());
			  int currentHeightFixed32 = Fixed32.toFP(image.getHeight());

			  int requiredWidthFixed32 = Fixed32.toFP(width);
			  int requiredHeightFixed32 = Fixed32.toFP(height);

			  int scaleXFixed32 = Fixed32.div(currentWidthFixed32,
			    requiredWidthFixed32);
			  int scaleYFixed32 = Fixed32.div(currentHeightFixed32,
			    requiredHeightFixed32);

			  result = image.scaleImage32(scaleXFixed32, scaleYFixed32);
			  return result;
			 }
	
	private void saveImage(String fName, byte[] image) {
		  DataOutputStream os = null;
		  FileConnection fconn = null;
		  try {
		   fconn = (FileConnection) Connector.open(fName, Connector.READ_WRITE);
		   if (!fconn.exists())
		    fconn.create();

		   os = fconn.openDataOutputStream();
		   os.write(image);
		  } catch (IOException e) {
		   System.out.println(e.getMessage());
		  } finally {
		   try {
		    if (null != os)
		     os.close();
		    if (null != fconn)
		     fconn.close();
		   } catch (IOException e) {
		    System.out.println(e.getMessage());
		   }
		  }
		 }
}
