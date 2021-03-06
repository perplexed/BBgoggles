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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.StandardTitleBar;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.browser.field2.*;

public class ProtoStream extends Thread{
	byte[]responseData = null;
	ResultsScreen resScreen;
	WaitPopupScreen dialogScreen;
	
	// Result data
	String TITLE = null;
	String TYPE = null;
	Integer X_COORD = null;
	Integer Y_COORD = null;
	Integer WIDTH = null;
	Integer HEIGHT = null;
	String IMAGE = null;
	String STATIC_IMAGE = null;
	String IMAGE_SITE = null;
	Float LAT = null;
	Float LNG = null;
	String LANG = null;
	String SEARCH_URL = null;
	
	// Global result data
	String RESPONSE_TIME = null;
	String CSSID = null;
	String TRAILING_BYTES = null;
	
	// ScrollImages
	boolean success = false;
	
	public ProtoStream(DataInputStream dis, WaitPopupScreen popupScreen) throws IOException {
		this.dialogScreen = popupScreen;
		byte[] lenbyte = new byte[5];
		System.out.println("Made 5 byte array.");
		dis.readFully(lenbyte, 0, 5);
		System.out.println("Got 5 bytes from server starting at pos 1... " + byteArrayToHexString(lenbyte));
		int[] varint = readVarint(lenbyte, 1);
		this.responseData = new byte[varint[1]+varint[0]];
		System.arraycopy(lenbyte, 0, this.responseData, 0, 5);
		System.out.println("Got "+varint[1]+" bytes from server.");
		dis.readFully(this.responseData, 5, varint[1]-(5-varint[0])); // dis.readFully(this.responseData, 5, varint[1]-2);
		byte[] datadump = new byte[20];
		System.arraycopy(this.responseData, 0, datadump, 0, 20);
		System.out.println("These are the first 20 bytes... " + byteArrayToHexString(datadump));
		dis.close();
	}
	
	public String byteArrayToHexString(byte[] b) {
	    StringBuffer sb = new StringBuffer(b.length * 2);
	    for (int i = 0; i < b.length; i++) {
	      int v = b[i] & 0xff;
	      if (v < 16) {
	        sb.append('0');
	      }
	      sb.append(Integer.toHexString(v));
	      sb.append(" ");
	    }
	    return sb.toString().toUpperCase();
	}
	
	public byte[] readBytes(byte[] data, int from, int len){
		byte res[] = new byte[len];
		System.arraycopy(data, from, res, 0, len);
		return res;
	}
	
	public byte readByte(byte[] data, int from){
		return data[from];
	}
	
	public int[] readVarint(byte[] data, int from){
        int res = 0;
        int len = 0;
        byte b;
        while ((0x80 & (b = readByte(data, from+len))) != 0){
                res |= ((b & 0x7f) << len*7);
                len++;
        }
        res |= (readByte(data, from+len) << len*7);
        return new int[]{(from+len+1), res};
	}
	
	public double readDouble(byte[] data, int from){
		byte[] raw = readBytes(data, from, 8);
		long res = 0;
		for (int i=0;i<8;i++){
			res |= (raw[i] << 8);
		}
		return Double.longBitsToDouble(res);
	}
	
	public double readFloat(byte[] data, int from){
		byte[] raw = readBytes(data, from, 4);
		int res = 0;
		for (int i=0;i<4;i++){
			res |= (raw[i] << 4);
		}
		return Float.intBitsToFloat(res);
	}
	
	public int[] readTag(byte[] data, int from){
		int[] varint = readVarint(data, from);
		return new int[] {varint[0], (varint[1] & 0x7), (varint[1] >> 3)};
	}
	
	public Vector getData(byte[] data){
		System.out.println("Getting a round of data.");
		Vector result = new Vector();
		Object[] element = null;
		int pos = 0;
		while(pos < data.length){
			int[] tagData = readTag(data, pos);
			System.out.println("Current position in data stream: "+pos+", wtype: "+tagData[1]+", field: "+tagData[2]);
			pos = tagData[0];
			switch(tagData[1]){
				case 0:
					// Varint
					int[] varint = readVarint(data, pos);
					pos = varint[0];
					element = new Object[]{new Integer(tagData[2]), new Integer(varint[1])};
					result.addElement(element);
					System.out.println(new Integer(varint[1]));
					break;
				case 1:
					// 64-bit;
					element = new Object[]{new Integer(tagData[2]), new Double(readDouble(data, pos))};
					result.addElement(element);
					pos += 8;
					break;
				case 2:
					// Length Delimited (there may be further messages or packed data)
					int[] varint1 = readVarint(data, pos);
					pos = varint1[0];
					byte[] databytes = readBytes(data, pos, varint1[1]);
					System.out.println(new String(databytes));
					element = new Object[]{new Integer(tagData[2]), databytes };
					result.addElement(element);
					pos += varint1[1];
					break;
				case 3:
					// Start Group (deprecated)
					break;
				case 4:
					// End Group (deprecated)
					break;
				case 5:
					// 32-bit
					element = new Object[]{new Integer(tagData[2]), new Float(readFloat(data, pos))};
					result.addElement(element);
					pos += 4;
					break;
				default:
					// New Wire Type
					break;
			}
		}
		return result;
	}
	
	public void run(){
		System.out.println("Starting parse operation...");
		synchronized(Application.getEventLock()){
			dialogScreen.changeText("Interpreting response...");
		}
		resScreen = new ResultsScreen();
		parseStream(this.responseData);
	    if (success == true){
	    	synchronized(Application.getEventLock()){
		    	UiApplication.getUiApplication().popScreen(dialogScreen.getScreenBelow());
		    	UiApplication.getUiApplication().popScreen(dialogScreen);
		    	UiApplication.getUiApplication().pushScreen(resScreen);
	    	}
	    } else {
	    	synchronized(Application.getEventLock()){
		    	UiApplication.getUiApplication().popScreen(dialogScreen);
	    	}
	    	final int[] result = new int[1];
		    UiApplication.getUiApplication().invokeAndWait(new Runnable() {
		    	  public void run() {
		    		  result[0] = Dialog.ask(Dialog.D_YES_NO, "No image matches found. Would you like to view similar images?");
		    	  }
		    });
		    if(result[0] == Dialog.YES){
		    	WaitPopupScreen waitScreen = new WaitPopupScreen();
		    	waitScreen.changeText("Loading similar images...");
		    	synchronized(Application.getEventLock()){
		    		UiApplication.getUiApplication().pushScreen(waitScreen);
		    	}
				UiApplication.getUiApplication().invokeAndWait(new Runnable() {
			    	  public void run() {
			    		  	resScreen.loadImages();
			    	  }
				});
			   		synchronized(Application.getEventLock()){
			   				UiApplication.getUiApplication().popScreen(waitScreen);
			   				UiApplication.getUiApplication().popScreen(UiApplication.getUiApplication().getActiveScreen());
			    			UiApplication.getUiApplication().pushScreen(resScreen);
			    	}
			 } else {
			   		CameraScreen camScreen = new CameraScreen();
			   		synchronized(Application.getEventLock()){
			   				UiApplication.getUiApplication().popScreen(UiApplication.getUiApplication().getActiveScreen());
				    		UiApplication.getUiApplication().pushScreen(camScreen);
			    	}
			}
	    }
		System.out.println("Finished");
	}
	
	public void parseCoords(byte[] data){
		Vector result = getData(data);
		Enumeration elements = result.elements();
		System.out.println("Got the hashtable");
		while( elements.hasMoreElements() ) {
			Object[] element = (Object[])elements.nextElement();
			int field = ((Integer) element[0]).intValue();
			switch(field){
				case 1:
					this.X_COORD = (Integer) element[1];
					break;
				case 2:
					this.WIDTH = (Integer) element[1];
					break;
				case 3:
					this.Y_COORD = (Integer) element[1];
					break;
				case 4:
					this.HEIGHT = (Integer) element[1];
					break;
				default:
			}
		}
	}
	
	public void parseLatLng(byte[] data){
		Vector result = getData(data);
		Enumeration elements = result.elements();
		System.out.println("Got the hashtable");
		while( elements.hasMoreElements() ) {
			Object[] element = (Object[])elements.nextElement();
			int field = ((Integer) element[0]).intValue();
			switch(field){
				case 1:
					this.LAT = (Float) element[1];
					break;
				case 2:
					this.LNG = (Float) element[1];
					break;
				default:
			}
		}
	}
	
	public void parseImages(byte[] data){
		Vector result = getData(data);
		Enumeration elements = result.elements();
		System.out.println("Got the hashtable");
		while( elements.hasMoreElements() ) {
			Object[] element = (Object[])elements.nextElement();
			int field = ((Integer) element[0]).intValue();
			switch(field){
				case 1:
					this.IMAGE = new String((byte[]) element[1]);
					break;
				case 2:
					this.STATIC_IMAGE = new String((byte[]) element[1]);
					break;
				case 3:
					this.IMAGE_SITE = new String((byte[]) element[1]);
					break;
				default:
			}
		}
	}
	
	public void parseData(byte[] data){
		Vector result = getData(data);
		Enumeration elements = result.elements();
		System.out.println("Got the hashtable");
		while( elements.hasMoreElements() ) {
			Object[] element = (Object[])elements.nextElement();
			int field = ((Integer) element[0]).intValue();
			switch(field){
				case 1:
					parseCoords((byte[]) element[1]);
					break;
				case 3:
					parseImages((byte[]) element[1]);
					break;
				case 5:
					parseLatLng((byte[]) element[1]);
					break;
				case 6:
					this.LANG = new String((byte[]) element[1]);
					break;
				default:
			}
		}
	}
	
	public void parseURL(byte[] data){
		Vector result = getData(data);
		Enumeration elements = result.elements();
		System.out.println("Got the hashtable");
		while( elements.hasMoreElements() ) {
			Object[] element = (Object[])elements.nextElement();
			int field = ((Integer) element[0]).intValue();
			switch(field){
				case 2:
					this.SEARCH_URL = new String((byte[]) element[1]);
					break;
				default:
			}
		}
	}
	
	public void parseDirect(byte[] data){
		Vector result = getData(data);
		Enumeration elements = result.elements();
		System.out.println("Got the hashtable");
		while( elements.hasMoreElements() ) {
			Object[] element = (Object[])elements.nextElement();
			int field = ((Integer) element[0]).intValue();
			switch(field){
				case 1:
					this.TITLE = new String((byte[]) element[1]);
					break;
				case 3:
					this.TYPE = new String((byte[]) element[1]);
					break;
				default:
			}
		}
	}
	
	public void parseSuccess(byte[] data){
		Vector result = getData(data);
		Enumeration elements = result.elements();
		System.out.println("Got the hashtable");
		while( elements.hasMoreElements() ) {
			Object[] element = (Object[])elements.nextElement();
			int field = ((Integer) element[0]).intValue();
			switch(field){
				/*case 1:
					this.TITLE = new String((byte[]) element[1]);
					break;
				case 2:
					this.TYPE = new String((byte[]) element[1]);
					break;*/
				case 15690847:
					parseData((byte[]) element[1]);
					break;
				case 15693652:
					parseURL((byte[]) element[1]);
					break;
				case 16045192:
					parseDirect((byte[]) element[1]);
					break;
				default:
			}
		}
	}
	
	public void parseFail(byte[] data){
		Vector result = getData(data);
		Enumeration elements = result.elements();
		System.out.println("Got the hashtable");
		while( elements.hasMoreElements() ) {
			Object[] element = (Object[])elements.nextElement();
			int field = ((Integer) element[0]).intValue();
			switch(field){
				case 1:
					parseSuccess((byte[]) element[1]);
					resScreen.addImage(this.STATIC_IMAGE, this.IMAGE_SITE, this.IMAGE);
					break;
				case 3:
					this.RESPONSE_TIME = new String((byte[]) element[1]);
					System.out.println("Response time: " + this.RESPONSE_TIME);
					break;
				case 6:
					this.CSSID = new String((byte[]) element[1]);
					System.out.println("Cssid: " + this.CSSID);
					break;
				default:
			}
		}	
	}
	
	public MainScreen newBrowserScreen(final String url){
		final MainScreen _browserScreen;
		BrowserFieldConfig _bfConfig;
		BrowserField _bf2;
		_browserScreen = new MainScreen();
		StandardTitleBar myTitleBar = new StandardTitleBar()
        .addIcon("camera_over.png")
        .addTitle("BBGoggles")
        .addClock()
        .addNotifications()
        .addSignalIndicator();
		myTitleBar.setPropertyValue(StandardTitleBar.PROPERTY_BATTERY_VISIBILITY,
        StandardTitleBar.BATTERY_VISIBLE_LOW_OR_CHARGING);
    	_browserScreen.setTitleBar(myTitleBar);
		_bfConfig = new BrowserFieldConfig();
	    _bfConfig.setProperty( BrowserFieldConfig.NAVIGATION_MODE, BrowserFieldConfig.NAVIGATION_MODE_POINTER );
	    _bfConfig.setProperty( BrowserFieldConfig.JAVASCRIPT_ENABLED, Boolean.TRUE );
	    _bfConfig.setProperty( BrowserFieldConfig.USER_AGENT, "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_1_3 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7E18 Safari/528.16 GoogleMobileApp/0.7.3.5675 GoogleGoggles-iPhone/1.0; gzip" );
	    _bf2 = new BrowserField(_bfConfig);
	    /*BrowserFieldListener listener = new BrowserFieldListener() {
	        public void documentLoaded(BrowserField browserField, Document document) throws Exception
	        {
	        	String newurl = document.getBaseURI();
	        	if (!url.equalsIgnoreCase(newurl)){
	        		BrowserSession bSession = Browser.getDefaultSession();
	        		synchronized(Application.getEventLock()){
	        			UiApplication.getUiApplication().popScreen(_browserScreen);
	        		}
	        		bSession.displayPage(newurl);
	        	}
	        }
	    };
	    _bf2.addListener( listener );*/
    	_bf2.requestContent(url);
	    _browserScreen.add(_bf2);
	    return _browserScreen;
	}
	
	public void parseResult(byte[] data){
		Vector result = getData(data);
		Enumeration elements = result.elements();
		System.out.println("Got the hashtable");
		int i = 0;
		while( elements.hasMoreElements() ) {
			Object[] element = (Object[])elements.nextElement();
			int field = ((Integer) element[0]).intValue();
			switch(field){
				case 1:
					try {
						i++;
						System.out.println("Parsing a successful result...");
						if (i==1){
							success = true;
							synchronized(Application.getEventLock()){
								dialogScreen.changeText("Getting image matches...");
							}
							LabelField label = new LabelField("Image Results");
							resScreen.getMainManager().setBackground(BackgroundFactory.createSolidBackground(Color.WHITE));
					        resScreen.add(label);
						}
						parseSuccess((byte[]) element[1]);
						final String title = this.TITLE;
						final String type = this.TYPE;
						final String search_url = this.SEARCH_URL;
						System.out.println("Got event lock, printing successful result...");
						resScreen.add(new LabelField(title + " (" + type + ")",LabelField.FOCUSABLE){
				    	    public boolean navigationClick(int status , int time){
				    	    	synchronized(Application.getEventLock()){
				    	        	MainScreen _thebrowserScreen = newBrowserScreen(search_url);
				    	        	UiApplication.getUiApplication().pushScreen(_thebrowserScreen);
				    	            return true;
				    	        }
				    	    }
				    	});
					}
					catch (Exception e) {
			        	sendError ("Error: " + e.toString());
			        }
					break;
				case 15705729:
					System.out.println("Parsing an unsuccessful result...");
					parseFail((byte[]) element[1]);
					//final String response_time = this.RESPONSE_TIME;
					//resScreen.add(new LabelField(response_time));
					break;
				default:
			}
		}
	}
	
	public void parseStream(byte[] data){
		Vector result = getData(data);
		Enumeration elements = result.elements();
		while( elements.hasMoreElements() ) {
			Object[] element = (Object[])elements.nextElement();
			int field = ((Integer) element[0]).intValue();
			switch(field){
				case 1:
					System.out.println("Parsing a successful result...");
					parseResult((byte[]) element[1]);
					break;
				case 4:
					System.out.println("Parsing trailing bytes...");
					this.TRAILING_BYTES = byteArrayToHexString((byte[]) element[1]);
					System.out.println(this.TRAILING_BYTES);
					break;
				default:
			}
		}
	}
	
	public void sendError(String error){
		final String errorText = error;
        //use invokeLater method to pass results back to the main thread
        UiApplication.getUiApplication().invokeLater(new Runnable() {
            public void run() {
                UiApplication.getUiApplication().popScreen(dialogScreen); //hide wait popup screen
                //pass results to the callback method of the current screen
                Dialog.alert("Error: "+errorText);
            }
        });
    }
}