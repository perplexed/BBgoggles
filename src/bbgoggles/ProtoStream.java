package bbgoggles;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;

public class ProtoStream extends Thread{
	byte[]responseData = null;
	
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
	
	
	public ProtoStream(DataInputStream dis) throws IOException {
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
		parseStream(this.responseData);
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
		int i = 0;
		while( elements.hasMoreElements() ) {
			Object[] element = (Object[])elements.nextElement();
			int field = ((Integer) element[0]).intValue();
			switch(field){
				case 1:
					i++;
					parseSuccess((byte[]) element[1]);
					final String type = this.TYPE;
					final String static_image = this.STATIC_IMAGE;
					synchronized(Application.getEventLock()){
						System.out.println("Got event lock...");
						((ResultsScreen)UiApplication.getUiApplication().getActiveScreen()).add(new LabelField(type + " " + i,LabelField.FOCUSABLE){
			    	        public boolean navigationClick(int status , int time){
			    	            BrowserSession bSession = Browser.getDefaultSession();
			    	            bSession.displayPage(static_image);
			    	            return true;
			    	        }
			    	    });
					}
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
	
	public void parseResult(byte[] data){
		Vector result = getData(data);
		Enumeration elements = result.elements();
		System.out.println("Got the hashtable");
		while( elements.hasMoreElements() ) {
			Object[] element = (Object[])elements.nextElement();
			int field = ((Integer) element[0]).intValue();
			switch(field){
				case 1:
					System.out.println("Parsing a successful result...");
					parseSuccess((byte[]) element[1]);
					final String title = this.TITLE;
					final String type = this.TYPE;
					final String search_url = this.SEARCH_URL;
					synchronized(Application.getEventLock()){
						System.out.println("Got event lock, printing successful result...");
						((ResultsScreen)UiApplication.getUiApplication().getActiveScreen()).add(new LabelField(title + " (" + type + ")",LabelField.FOCUSABLE){
			    	        public boolean navigationClick(int status , int time){
			    	            BrowserSession bSession = Browser.getDefaultSession();
			    	            bSession.displayPage(search_url);
			    	            return true;
			    	        }
			    	    });
					}
					break;
				case 15705729:
					System.out.println("Parsing an unsuccessful result...");
					parseFail((byte[]) element[1]);
					final String response_time = this.RESPONSE_TIME;
					synchronized(Application.getEventLock()){
						System.out.println("Got event lock, printing response time...");
						((ResultsScreen)UiApplication.getUiApplication().getActiveScreen()).add(new LabelField(response_time));
					}
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
}