
package mx.unison.svg.util;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class PolyLine {
    private static final Logger LOG = Logger.getLogger(PolyLine.class.getName());

    private final List<Point2D.Double> vertices = new ArrayList<>();

    public PolyLine(String points) {
        if (points == null || points.isBlank()) {
            return;
        }
        String[] nums = points.trim().split("[\\s,]+");
        if (nums.length % 2 != 0) {
            LOG.warning("points con cantidad impar de números; se ignora el último: " + points);
        }
        try {
            for (int i = 0; i + 1 < nums.length; i += 2) {
                vertices.add(new Point2D.Double(
                        Double.parseDouble(nums[i]),
                        Double.parseDouble(nums[i + 1])));
            }
        } catch (NumberFormatException e) {
            LOG.warning("Atributo points inválido: " + points);
            vertices.clear();
        }
    }

    public List<Point2D.Double> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    public boolean isValid() {
        return vertices.size() >= 2;
    }

    public Path2D.Double toPath() {
        Path2D.Double path = new Path2D.Double();
        if (!isValid()) {
            return path;
        }
        Point2D.Double first = vertices.get(0);
        path.moveTo(first.x, first.y);
        for (int i = 1; i < vertices.size(); i++) {
            path.lineTo(vertices.get(i).x, vertices.get(i).y);
        }
        return path;
    }
}
