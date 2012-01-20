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

import net.rim.device.api.ui.UiApplication;


public class BBgoggles extends UiApplication
{
    public static void main(String[] args)
    {
        // Create a new instance of the application and make the currently
        // running thread the application's event dispatch thread.
        BBgoggles theApp = new BBgoggles();       
        theApp.enterEventDispatcher();
    }
    
    public BBgoggles()
    {        
        // Push a screen onto the UI stack for rendering.
    	ResultsScreen thescreen = new ResultsScreen();
        pushScreen(thescreen);
    }
}

