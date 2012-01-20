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

public class ImageEncode {
	public static void sendImage(byte[] image, DataOutputStream output) throws IOException {
		try {
			byte breaker = (byte) 0x0A;
	        int length = image.length;   
	        output.write(breaker);
	        output.write(toVarint(length + 32));
	        output.write(breaker);
	        output.write(toVarint(length + 14));
	        output.write(breaker);
	        output.write(toVarint(length + 10));
	        output.write(breaker);
	        output.write(toVarint(length));
	        output.write(image);
	        output.write(new byte[]{(byte) 0x18, (byte) 0x4B, (byte) 0x20, (byte) 0x01, (byte) 0x30, (byte) 0x00, (byte) 0x92, (byte) 0xEC, (byte) 0xF4, (byte) 0x3B, (byte) 0x09, (byte) 0x18, (byte) 0x00, (byte) 0x38, (byte) 0xC6, (byte) 0x97, (byte) 0xDC, (byte) 0xDF, (byte) 0xF7, (byte) 0x25, (byte) 0x22, (byte) 0x00});
		} catch(Exception e){
	    	System.out.println("Image encoding error: " + e.getMessage());
	    }
    }
	
	public static byte[] toVarint(int value){
		byte[] b = null;
		try {
			int len = 0;
		    int valCop = value;
			int index = 0;
		    
		    while ((0x7F & valCop) != 0){
		        valCop = valCop >> 7;
		        len++;
		    }
		    
		    b = new byte[len];
		    
		    while ((0x7F & value) != 0){
		        int i = (0x7F & value);
		        if ((0x7F & (value >> 7)) != 0){
		            i += 128;
		        }
		        b[index] = (byte)(i);
		        value = value >> 7;
		        index++;
		    }
	    } catch(Exception e){
	    	System.out.println("Varint encode error: " + e.getMessage());
	    }
	    return b;
	}	
}
