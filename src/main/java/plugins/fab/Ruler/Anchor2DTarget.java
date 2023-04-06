package plugins.fab.Ruler;

import icy.canvas.IcyCanvas;
import icy.painter.Anchor2D;
import icy.sequence.Sequence;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

/**
 * @author Fabrice de Chaumont
 */
public class Anchor2DTarget extends Anchor2D {

    Line2D line1 = new Line2D.Double();
    Line2D line2 = new Line2D.Double();

    public Anchor2DTarget(double x, double y) {
        super(x, y);
    }

    @Override
    public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas) {

        if (!visible)
            return;

        updateGeometry(canvas);

        BasicStroke strokeMainLine = new BasicStroke((float) canvas.canvasToImageLogDeltaX(2));
        BasicStroke strokeSubLine = new BasicStroke((float) canvas.canvasToImageLogDeltaX(1));

        g.setStroke(strokeMainLine);
        if (selected) {
            g.setColor(Color.yellow);
        }
        else {
            g.setColor(Color.black);
        }
        g.draw(line1);
        g.draw(line2);

        g.setStroke(strokeSubLine);
        if (selected) {
            g.setColor(Color.yellow);
        }
        else {
            g.setColor(Color.white);
        }
        g.draw(line1);
        g.draw(line2);

    }

    void updateGeometry(IcyCanvas canvas) {
        final double adjRayX = canvas.canvasToImageLogDeltaX(ray);
        final double adjRayY = canvas.canvasToImageLogDeltaY(ray);

        line1.setLine(position.x - adjRayX, position.y, position.x + adjRayX, position.y);

        line2.setLine(position.x, position.y - adjRayY, position.x, position.y + adjRayY);

    }

}
