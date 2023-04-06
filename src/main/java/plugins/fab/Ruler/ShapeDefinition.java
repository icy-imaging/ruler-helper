package plugins.fab.Ruler;

import java.awt.Shape;

/**
 * @author Fabrice de Chaumont
 */
public class ShapeDefinition {

    public int stroke;
    public Shape shape;
    public float alpha = 1;

    public ShapeDefinition(int stroke, Shape shape) {
        this.stroke = stroke;
        this.shape = shape;
    }

    public ShapeDefinition(int stroke, Shape shape, float alpha) {
        this(stroke, shape);
        this.alpha = alpha;
    }

}
