package bbgoggles;

import net.rim.device.api.ui.Graphics;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.component.ButtonField;

/**
 * Button field with a bitmap as its label.
 */
public class CustomButton extends ButtonField {
        private Bitmap bitmap;
        private Bitmap bitmapHighlight;
        private boolean highlighted = false;

        /**
         * Instantiates a new bitmap button field.
         * 
         * @param bitmap the bitmap to use as a label
         */
        public CustomButton(String pathtobitmap, String pathtobitmapHighlighted) {
            this(Bitmap.getBitmapResource(pathtobitmapHighlighted), Bitmap.getBitmapResource(pathtobitmapHighlighted), ButtonField.CONSUME_CLICK|ButtonField.FIELD_HCENTER|ButtonField.FIELD_VCENTER);
        }

        public CustomButton(Bitmap bitmap, Bitmap bitmapHighlight, long style) {
            super(style);
            this.bitmap = bitmap;
            this.bitmapHighlight = bitmapHighlight;
        }

        /* (non-Javadoc)
         * @see net.rim.device.api.ui.component.ButtonField#layout(int, int)
         */
        protected void layout(int width, int height) {
                setExtent(getPreferredWidth(), getPreferredHeight());
        }

        /* (non-Javadoc)
         * @see net.rim.device.api.ui.component.ButtonField#getPreferredWidth()
         */
        public int getPreferredWidth() {
                return bitmap.getWidth();
        }

        /* (non-Javadoc)
         * @see net.rim.device.api.ui.component.ButtonField#getPreferredHeight()
         */
        public int getPreferredHeight() {
                return bitmap.getHeight();
        }

        /* (non-Javadoc)
         * @see net.rim.device.api.ui.component.ButtonField#paint(net.rim.device.api.ui.Graphics)
         */
        protected void paint(Graphics graphics) {
                super.paint(graphics);
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                Bitmap b = bitmap;
                if (highlighted)
                    b = bitmapHighlight;
                graphics.drawBitmap(0, 0, width, height, b, 0, 0);
        }

        protected void onFocus(int direction) 
        {
        	this.highlighted = true;
        	super.onFocus(direction); 
        }
        
        protected void onUnfocus() 
        {
        	this.highlighted = false;
        	super.onUnfocus();
        }
}
