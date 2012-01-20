package bbgoggles;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class WaitPopupScreen extends PopupScreen {
	public WaitPopupScreen() {
        super(new VerticalFieldManager());
        LabelField labelField = new LabelField("Initialising connection...",
                Field.FIELD_HCENTER);
        add(labelField);
    }
}
