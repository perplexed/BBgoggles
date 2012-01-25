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
import java.io.DataOutputStream;
import java.util.Random;
import javax.microedition.io.HttpConnection;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

public class ConnectionThread extends Thread {
    byte[] IMAGE;
    String CSSID;
    String URL = "http://www.google.com/goggles/container_proto?cssid=";
    final WaitPopupScreen dialogScreen = new WaitPopupScreen();
 
    public ConnectionThread(byte[] IMAGE) {
        this.IMAGE = IMAGE; //IMAGE URL
        genCSSID();
    }
    
    public void genCSSID(){
    	String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    	Random randomness = new Random();
    	StringBuffer sb = new StringBuffer(16);
    	for( int i = 0; i < 16; i++ ) 
    		sb.append( AB.charAt( randomness.nextInt(AB.length()) ) );
    	this.CSSID = sb.toString();
    }
    
    public boolean initialise(){
    	HttpConnection connection = null;
        DataOutputStream out = null;
        try { 
    		connection = (HttpConnection) new ConnectionFactory().getConnection(URL + CSSID).getConnection();
    		connection.setRequestMethod(HttpConnection.POST);
    		connection.setRequestProperty("Content-Type", "application/x-protobuffer");
    		connection.setRequestProperty("Pragma", "no-cache");
    		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_1_3 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7E18 Safari/528.16 GoogleMobileApp/0.7.3.5675 GoogleGoggles-iPhone/1.0; gzip");
        
    		out = new DataOutputStream(connection.openOutputStream());
    		out.write(new byte[]{(byte) 0x22, (byte) 0x00, (byte) 0x62, (byte) 0x3C, (byte) 0x0A, (byte) 0x13, (byte) 0x22, (byte) 0x02, (byte) 0x65, (byte) 0x6E, (byte) 0xBA, (byte) 0xD3, (byte) 0xF0, (byte) 0x3B, (byte) 0x0A, (byte) 0x08, (byte) 0x01, (byte) 0x10, (byte) 0x01, (byte) 0x28, (byte) 0x01, (byte) 0x30, (byte) 0x00, (byte) 0x38, (byte) 0x01, (byte) 0x12, (byte) 0x1D, (byte) 0x0A, (byte) 0x09, (byte) 0x69, (byte) 0x50, (byte) 0x68, (byte) 0x6F, (byte) 0x6E, (byte) 0x65, (byte) 0x20, (byte) 0x4F, (byte) 0x53, (byte) 0x12, (byte) 0x03, (byte) 0x34, (byte) 0x2E, (byte) 0x31, (byte) 0x1A, (byte) 0x00, (byte) 0x22, (byte) 0x09, (byte) 0x69, (byte) 0x50, (byte) 0x68, (byte) 0x6F, (byte) 0x6E, (byte) 0x65, (byte) 0x33, (byte) 0x47, (byte) 0x53, (byte) 0x1A, (byte) 0x02, (byte) 0x08, (byte) 0x02, (byte) 0x22, (byte) 0x02, (byte) 0x08, (byte) 0x01});
            out.close();
            out = null;
    		
    		int rc = connection.getResponseCode();
    		if(rc == HttpConnection.HTTP_OK) {
    			connection.close();
    			return true;
    		} else {
    	    	genCSSID();
    	    	connection.close();
    			return false;
    		}
        
    	} catch (Exception e) {
    		sendError ("Error: Could not connect to server (" + e.getMessage() + ").");
    		return false;
    	}
    }
    
    public void sendData(){
    	HttpConnection connection = null;
        DataOutputStream out = null;
		DataInputStream dis = null;
    	try { 
            connection = (HttpConnection) new ConnectionFactory().getConnection(URL + CSSID).getConnection();
            connection.setRequestMethod(HttpConnection.POST);
            connection.setRequestProperty("Content-Type", "application/x-protobuffer");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_1_3 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7E18 Safari/528.16 GoogleMobileApp/0.7.3.5675 GoogleGoggles-iPhone/1.0; gzip");
            
            out = new DataOutputStream(connection.openOutputStream());
            ImageEncode.sendImage(IMAGE, out);
            
            int rc = connection.getResponseCode();
            if(rc == HttpConnection.HTTP_OK) {
            	try {
                    dis = new DataInputStream(connection.openInputStream());
                    System.out.println("Connection to response open.");
                    new ProtoStream(dis, dialogScreen).start();
            	}
            	catch (Exception e) {
            		sendError ("Error: Could not read result (" + e.toString() + ").");
          	    	}
            } else {
            	sendError ("Error: HTTP error code " + Integer.toString(rc) + ".");
            }
            
        } catch (Exception e) {
        	sendError ("Error: " + e.toString());
        }
    }
    
    public void run() {
        UiApplication.getUiApplication().invokeLater(new Runnable() {
            public void run() {
                UiApplication.getUiApplication().pushModalScreen(dialogScreen);
            }
        });
        int counter = 1;
        while (initialise() != true && counter < 4){
            counter++;
        }
        if(counter<4){
        	sendData();
        } else {
        	sendError("Error: Tried to initialise connection 3 times without success.");
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
