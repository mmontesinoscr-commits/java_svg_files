package mx.unison.svg.util;

import java.awt.Font;
import java.util.logging.Logger;
import java.awt.Font;
import java.util.logging.Logger;

public class svgText {
    private static final Logger LOG = Logger.getLogger(svgText.class.getName());

    private static final String DEFAULT_FAMILY = "SansSerif";
    private static final float DEFAULT_SIZE = 16f;

    private double x;
    private double y;
    private final String content;
    private final String fontFamily;
    private final float fontSize;

    public svgText(String x, String y, String content, String fontFamily, String fontSize) {
        this.x = parseNumber(x, 0);
        this.y = parseNumber(y, 0);
        this.content = (content == null) ? "" : content;
        this.fontFamily = parseFamily(fontFamily);
        this.fontSize = (float) parseNumber(fontSize == null ? null : fontSize.replace("px", ""), DEFAULT_SIZE);
    }

    private static double parseNumber(String value, double defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            LOG.warning("Valor numérico inválido en text: " + value);
            return defaultValue;
        }
    }

    private static String parseFamily(String value) {
        if (value == null || value.isBlank()) return DEFAULT_FAMILY;
        String first = value.split(",")[0].trim().replace("'", "").replace("\"", "");
        return first.isEmpty() ? DEFAULT_FAMILY : first;
    }

    public boolean isValid() {
        return !content.isBlank() && fontSize > 0;
    }

    public Font toFont() {
        return new Font(fontFamily, Font.PLAIN, 1).deriveFont(fontSize);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public String getContent() { return content; }
}