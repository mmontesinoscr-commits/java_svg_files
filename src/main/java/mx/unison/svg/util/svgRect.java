package mx.unison.svg.util;


import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.logging.Logger;

public class svgRect {
    private static final Logger LOG = Logger.getLogger(svgText.class.getName());

    private double x;
    private double y;
    private double width;
    private double height;
    private double rx;
    private double ry;
    private boolean valid;

    public svgRect(String x, String y, String width, String height, String rx, String ry) {
        try {
            this.x = (x == null) ? 0 : Double.parseDouble(x.trim());
            this.y = (y == null) ? 0 : Double.parseDouble(y.trim());
            this.width = (width == null) ? 0 : Double.parseDouble(width.trim());
            this.height = (height == null) ? 0 : Double.parseDouble(height.trim());
            this.valid = this.width > 0 && this.height > 0;
        } catch (NumberFormatException e) {
            LOG.warning("Atributos de rect inválidos");
            this.valid = false;
            return;
        }
        resolveRadii(rx, ry);
    }

    private void resolveRadii(String rxStr, String ryStr) {
        double rxVal = parseRadius(rxStr);
        double ryVal = parseRadius(ryStr);

        if (rxVal < 0 && ryVal < 0) {
            rxVal = 0;
            ryVal = 0;
        } else if (rxVal < 0) {
            rxVal = ryVal;
        } else if (ryVal < 0) {
            ryVal = rxVal;
        }
        this.rx = Math.min(rxVal, width / 2);
        this.ry = Math.min(ryVal, height / 2);
    }

    private double parseRadius(String value) {
        if (value == null) return -1;
        try {
            double r = Double.parseDouble(value.trim());
            return r >= 0 ? r : -1;
        } catch (NumberFormatException e) {
            LOG.warning("Radio de esquina inválido en rect: " + value);
            return -1;
        }
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isRounded() {
        return rx > 0 && ry > 0;
    }



    public Shape toShape() {
        if (isRounded()) {
            return new RoundRectangle2D.Double(x, y, width, height, 2 * rx, 2 * ry);
        }
        return new Rectangle2D.Double(x, y, width, height);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getRx() { return rx; }
    public double getRy() { return ry; }
}
