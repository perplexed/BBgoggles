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

