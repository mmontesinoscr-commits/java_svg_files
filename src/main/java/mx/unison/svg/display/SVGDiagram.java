package mx.unison.svg.display;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import mx.unison.svg.util.PolyLine;
import mx.unison.svg.util.elipse;
import mx.unison.svg.util.svgText;
import mx.unison.svg.util.svgRect;
import java.awt.geom.Path2D;

import javax.swing.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

public class SVGDiagram extends JComponent {

    public static final Logger LOG = Logger.getLogger(SVGDiagram.class.getName());

    private SAXParser parser = null;

    private int svgW; // ancho del dibujo
    private int svgH;  // alto del dibujo

    private Properties webColors;

    // parametros iniciales par dibujar las figuras
    private Color strokeColor;
    private Color fillColor;
    private BasicStroke currentStroke;

    // propiedades de las figuras leidas del archivo SVG
    private ArrayList<Properties> shapes = null;
    private Properties currentShape = null;

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(svgW, svgH);
    }


    public SVGDiagram(File file) {
        super();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);

        try {
            parser = factory.newSAXParser();
        } catch (Exception e) {
            LOG.severe("Error creating SAX parser: " + e.getMessage());
        }
        try {
            // Parsear el archivo SVG usando un manejador de eventos anónimo
            // Este manejador solo captura el elemento <svg> para obtener sus atributos de ancho y alto
            LOG.info("Parsing SVG file: " + file.getAbsolutePath());
            parser.parse(file, new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if (localName.equals("svg")) {
                        String width = attributes.getValue("width");
                        String height = attributes.getValue("height");
                        svgW = Integer.parseInt(width);
                        svgH = Integer.parseInt(height);
                        LOG.info("SVG Document\n\tWidth: " + width + " , Height: " + height);
                    }
                }
            });
        } catch (Exception e) {
            LOG.severe("Error getting SVG dimensions: " + e.getMessage());
        }

        shapes = new ArrayList<>();
        strokeColor = Color.BLACK;
        fillColor = Color.LIGHT_GRAY;
        currentStroke = new BasicStroke(1.0f);

        TagsHandler tagsHandler = new TagsHandler();

        try {
            parser.reset();
            parser.parse(file, tagsHandler);
        } catch (Exception e) {
            LOG.severe("Error in SVG file: " + e.getMessage());
            e.printStackTrace();
        }
        shapes = tagsHandler.getShapes();
        // Cargar los colores web desde el archivo colors.properties
        loadColors();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        LOG.info("Painting SVG Diagram");
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(strokeColor);

        for (Properties shape : shapes) {
            String type = shape.getProperty("type");

            if (type == null)
                continue;
            if (type.equals("line")) {
                String x1Str = shape.getProperty("x1");
                String y1Str = shape.getProperty("y1");
                String x2Str = shape.getProperty("x2");
                String y2Str = shape.getProperty("y2");
                String stroke = shape.getProperty("stroke");
                String strokeWidthStr = shape.getProperty("stroke-width");

                if (strokeWidthStr != null) {
                    try {
                        float strokeWidth = Float.parseFloat(strokeWidthStr);
                        currentStroke = new BasicStroke(strokeWidth);
                        g2d.setStroke(currentStroke);
                    } catch (NumberFormatException e) {
                        LOG.warning("Invalid stroke width: " + strokeWidthStr);
                        currentStroke = new BasicStroke(1.0f);
                        g2d.setStroke(currentStroke);
                    }
                }

                if (x1Str != null && y1Str != null && x2Str != null && y2Str != null) {
                    int x1 = Integer.parseInt(x1Str);
                    int y1 = Integer.parseInt(y1Str);
                    int x2 = Integer.parseInt(x2Str);
                    int y2 = Integer.parseInt(y2Str);

                    if (stroke != null) {
                        strokeColor = null;

                        try {
                            strokeColor = Color.decode(stroke);
                        } catch (NumberFormatException e) {
                            LOG.warning("Invalid stroke color: " + stroke);
                            //strokeColor = parseColor(stroke);
                            g2d.setColor( parseColor(stroke, 1.0f) );
                        }
                        g2d.setColor(strokeColor);
                    } else {
                        g2d.setColor(strokeColor);
                    }
                    g2d.drawLine(x1, y1, x2, y2);
                }

            }
            if (type.equals("circle")) {
                String cx = shape.getProperty("cx");
                String cy = shape.getProperty("cy");
                String radio = shape.getProperty("r");
                String fill = shape.getProperty("fill");
                String stroke = shape.getProperty("stroke");
                String strokeWidthStr = shape.getProperty("stroke-width");

                if (cx != null && cy != null && radio != null) {
                    int centroX = Integer.parseInt(cx);
                    int centroY = Integer.parseInt(cy);
                    int radius = Integer.parseInt(radio);

                    if (fill != null) {
                        try {
                            fillColor = Color.decode(fill);
                        } catch (NumberFormatException e) {
                            LOG.warning("Invalid stroke color: " + fill);
                            fillColor = parseColor(fill,1.0f);
                        }
                        g2d.setColor(fillColor);
                        g2d.fillOval(centroX - radius, centroY - radius
                                , radius * 2, radius * 2);
                    }
                    if (stroke != null) {
                        strokeColor = null;

                        try {
                            strokeColor = Color.decode(stroke);
                        } catch (NumberFormatException e) {
                            LOG.warning("Invalid stroke color: " + stroke);
                            strokeColor = parseColor(stroke);
                        }
                        g2d.setColor(strokeColor);
                    } else {
                        g2d.setColor(fillColor);
                    }

                    if (strokeWidthStr != null) {
                        try {
                            float strokeWidth = Float.parseFloat(strokeWidthStr);
                            currentStroke = new BasicStroke(strokeWidth);
                            g2d.setStroke(currentStroke);
                        } catch (NumberFormatException e) {
                            LOG.warning("Invalid stroke width: " + strokeWidthStr);
                            currentStroke = new BasicStroke(1.0f);
                            g2d.setStroke(currentStroke);
                        }
                    }
                    g2d.drawOval(centroX - radius,
                            centroY - radius,
                            radius * 2, radius * 2);

                }
            }   // <-- aquí termina el bloque de circle

            if (type.equals("polyline")) {
                PolyLine polyline = new PolyLine(shape.getProperty("points"));
                if (!polyline.isValid()) {
                    LOG.warning("Polyline ignorada: points inválido o con menos de 2 vértices");
                    continue;
                }
                Path2D.Double path = polyline.toPath();

                // En SVG, si fill no se indica, el relleno por defecto es negro
                String fillAttr = shape.getProperty("fill");
                Color fill = (fillAttr == null) ? Color.BLACK : safeColor(fillAttr);
                if (fill != null) {                  // null = fill="none"
                    g2d.setColor(fill);
                    g2d.fill(path);
                }

                Color stroke = safeColor(shape.getProperty("stroke"));
                if (stroke != null) {                // sin stroke no se dibuja contorno
                    g2d.setColor(stroke);
                    g2d.setStroke(new BasicStroke(parseStrokeWidth(shape.getProperty("stroke-width"))));
                    g2d.draw(path);
                }
            }

            if (type.equals("rect")) {
                svgRect rect = new svgRect(shape.getProperty("x"), shape.getProperty("y"),
                        shape.getProperty("width"), shape.getProperty("height"),
                        shape.getProperty("rx"), shape.getProperty("ry"));
                if (!rect.isValid()) {
                    LOG.warning("Rect ignorado: width/height inválidos");
                    continue;
                }
                Shape area = rect.toShape();

                String fillAttr = shape.getProperty("fill");
                Color fill = (fillAttr == null) ? Color.BLACK : safeColor(fillAttr);
                if (fill != null) {
                    g2d.setColor(fill);
                    g2d.fill(area);
                }

                Color stroke = safeColor(shape.getProperty("stroke"));
                if (stroke != null) {
                    g2d.setColor(stroke);
                    g2d.setStroke(new BasicStroke(parseStrokeWidth(shape.getProperty("stroke-width"))));
                    g2d.draw(area);
                }
            }

            if (type.equals("ellipse")) {
                elipse ellipse = new elipse(shape.getProperty("cx"), shape.getProperty("cy"),
                        shape.getProperty("rx"), shape.getProperty("ry"));
                if (!ellipse.isValid()) {
                    LOG.warning("Ellipse ignorada: radios inválidos");
                    continue;
                }
                Shape area = ellipse.toShape();

                String fillAttr = shape.getProperty("fill");
                Color fill = (fillAttr == null) ? Color.BLACK : safeColor(fillAttr);
                if (fill != null) {
                    g2d.setColor(fill);
                    g2d.fill(area);
                }

                Color stroke = safeColor(shape.getProperty("stroke"));
                if (stroke != null) {
                    g2d.setColor(stroke);
                    g2d.setStroke(new BasicStroke(parseStrokeWidth(shape.getProperty("stroke-width"))));
                    g2d.draw(area);
                }
            }

            if (type.equals("text")) {
                svgText text = new svgText(shape.getProperty("x"), shape.getProperty("y"),
                        shape.getProperty("content"), shape.getProperty("font-family"),
                        shape.getProperty("font-size"));
                if (!text.isValid()) {
                    continue;
                }
                String fillAttr = shape.getProperty("fill");
                Color fill = (fillAttr == null) ? Color.BLACK : safeColor(fillAttr);
                if (fill == null) {
                    continue;               // fill="none": el texto es invisible
                }
                g2d.setColor(fill);
                g2d.setFont(text.toFont());
                // x, y es la línea base, igual que en SVG
                g2d.drawString(text.getContent(), (float) text.getX(), (float) text.getY());
            }

        }

    }

    /** Devuelve el color, o null si es "none", nulo o no se puede interpretar. */
    private Color safeColor(String value) {
        if (value == null || value.equalsIgnoreCase("none")) return null;
        try {
            return Color.decode(value);                 // "#0000ff", "0xff0000"
        } catch (NumberFormatException e) {
            try {
                return parseColor(value, 1.0f);         // "blue", "lightblue"...
            } catch (RuntimeException ex) {             // nombre desconocido u otro error
                LOG.warning("Unknown color: " + value);
                return null;
            }
        }
    }

    /** Interpreta stroke-width; devuelve 1.0 si falta, es inválido o no es positivo. */
    private float parseStrokeWidth(String value) {
        if (value == null) return 1.0f;
        try {
            float w = Float.parseFloat(value.trim());
            return w > 0 ? w : 1.0f;
        } catch (NumberFormatException e) {
            LOG.warning("Invalid stroke width: " + value);
            return 1.0f;
        }
    }

    // https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/Values/named-color
    private void loadColors() {
        try {
            String userDir = System.getProperty("user.dir");
            FileReader reader = new FileReader(userDir + "/colors.properties");
            webColors = new Properties();
            webColors.load(reader);
            //System.out.println(webColors.size() + " " + webColors.getProperty("maroon"));
        } catch (IOException ex) {
            LOG.severe(ex.getMessage());
        }
    }

    // Metodo para parsear un color desde un string, considerando el formato hexadecimal, rgb o nombre de color
    private Color parseColor(String colorString, float opacity) {
        String colorCode = colorString.toLowerCase();
        Color newColor = null;

        //System.out.println("param " + colorCode);
        if (colorCode.startsWith("#")) {
            // Codigo de color "#00ffff"
            colorCode = colorCode.substring(1);
        } else if (colorCode.startsWith("0x")) {
            colorCode = colorCode.substring(2);
        } else if (colorCode.startsWith("rgb")) {
            // Codigo de color "rgb(00,255,0)"
            if (colorCode.startsWith("(", 3)) {
                return Color.BLACK;
            } else if (colorCode.startsWith("a(", 3)) {
                return Color.BLACK;
            }
        } else {
            // nombre de color e.g. "red
            //Color namedColor = Color.getColor(colorString);
            colorCode = webColors.getProperty(colorCode).substring(1).trim();
        }
        try {
            int r;
            int g;
            int b;
            int a;
            int len = colorCode.length();

            if (len == 6) {
                //System.out.println(colorCode);
                r = Integer.parseInt(colorCode.substring(0, 2), 16);
                g = Integer.parseInt(colorCode.substring(2, 4), 16);
                b = Integer.parseInt(colorCode.substring(4, 6), 16);
                //System.out.println(r + "," + g + "," + b);
                // newColor = new Color(r, g, b, opacity);
                newColor = new Color(r, g, b);
            }

        } catch (NumberFormatException nfe) {
            LOG.warning("Invalid color format: " + nfe.getMessage() );
            newColor = Color.BLACK;
        }
        return newColor;
    }



    private Color parseColor(String colorStr) {
        Color color = null;
        switch (colorStr) {
            case "black":
                color = Color.BLACK;
                break;
            case "white":
                color = Color.WHITE;
                break;
            case "red":
                color = Color.RED;
                break;
            case "green":
                color = Color.GREEN;
                break;
            case "blue":
                color = Color.BLUE;
                break;
            case "yellow":
                color = Color.YELLOW;
                break;
            case "cyan":
                color = Color.CYAN;
                break;
            case "magenta":
                color = Color.MAGENTA;
                break;
            case "gray":
                color = Color.GRAY;
                break;
            default:
                LOG.warning("Unknown color: " + colorStr);
        }
        return color;
    }

}