package mx.unison.svg.util;

import java.awt.geom.Ellipse2D;
import java.util.logging.Logger;

public class elipse {
    private static final Logger LOG = Logger.getLogger(elipse.class.getName());

    private double cx;
    private double cy;
    private double rx;
    private double ry;
    private boolean valid;

    public elipse(String cx, String cy, String rx, String ry) {
        try {
            this.cx = (cx == null) ? 0 : Double.parseDouble(cx.trim());
            this.cy = (cy == null) ? 0 : Double.parseDouble(cy.trim());
            this.rx = (rx == null) ? 0 : Double.parseDouble(rx.trim());
            this.ry = (ry == null) ? 0 : Double.parseDouble(ry.trim());
            this.valid = this.rx > 0 && this.ry > 0;
        } catch (NumberFormatException e) {
            LOG.warning("Atributos de ellipse inválidos");
            this.valid = false;
        }
    }

    public boolean isValid() {
        return valid;
    }


    public Ellipse2D.Double toShape() {
        return new Ellipse2D.Double(cx - rx, cy - ry, 2 * rx, 2 * ry);
    }

    public double getCx() { return cx; }
    public double getCy() { return cy; }
    public double getRx() { return rx; }
    public double getRy() { return ry; }

}

