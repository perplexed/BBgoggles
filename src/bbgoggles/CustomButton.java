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
