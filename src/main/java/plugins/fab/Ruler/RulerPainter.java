/**
 * by Fabrice de Chaumont
 */

package plugins.fab.Ruler;

import icy.canvas.Canvas2D;
import icy.canvas.IcyCanvas;
import icy.main.Icy;
import icy.math.UnitUtil;
import icy.math.UnitUtil.UnitPrefix;
import icy.painter.Anchor2D;
import icy.painter.Anchor2D.Anchor2DListener;
import icy.painter.Overlay;
import icy.painter.PainterEvent;
import icy.roi.ROI2D;
import icy.sequence.Sequence;
import icy.type.point.Point5D;
import icy.util.GraphicsUtil;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class RulerPainter extends Overlay implements Anchor2DListener
{

	private Anchor2DTarget a1;
	private Anchor2DTarget a2;

	public RulerPainter()
	{
		super("Ruler Helper");
	}

	// Listener of Anchor

	@Override
	public void painterChanged(PainterEvent event)
	{
		for (Sequence sequence : Icy.getMainInterface().getSequencesContaining(this))
		{
			sequence.painterChanged(this);
		}
	}

	@Override
	public void positionChanged(Anchor2D source)
	{

	}

	int findBestMajTickSpace(int sliderSize, int delta)
	{

		final int values[] = { 5, 10, 20, 50, 100, 200, 250, 500, 1000, 2000, 2500, 5000 };

		int wantedMajTickSpace;
		// wanted a major tick each ~40 pixels
		try
		{
			wantedMajTickSpace = delta / (sliderSize / 40);
		} catch (ArithmeticException e)
		{
			return values[0];
		}

		int min = Integer.MAX_VALUE;
		int bestValue = 5;

		// try with our predefined values
		for (int value : values)
		{
			final int dx = Math.abs(value - wantedMajTickSpace);

			if (dx < min)
			{
				min = dx;
				bestValue = value;
			}
		}

		return bestValue;
	}

	// Painter Section:

	@Override
	public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
	{
		if (g == null)
			return;
		if (!(canvas instanceof Canvas2D))
			return;

		ArrayList<ShapeDefinition> lineDefinitionList = new ArrayList<ShapeDefinition>();
		
		if (a1 == null || a2 == null)
		{
			Rectangle bounds = g.getClipBounds();

			int w = bounds.width;
			int h = bounds.height;
			int x = bounds.x;
			int y = bounds.y;

			a1 = new Anchor2DTarget(x + w / 4, y + h / 2);
			a2 = new Anchor2DTarget(x + 3 * w / 4, y + h / 2);

			sequence.addPainter(this);
			a1.addAnchorListener(this);
			a2.addAnchorListener(this);
		}

		BasicStroke[] stroke = new BasicStroke[4];

		stroke[0] = new BasicStroke((float) canvas.canvasToImageLogDeltaX(2));
		stroke[1] = new BasicStroke((float) canvas.canvasToImageLogDeltaX(3));
		stroke[2] = new BasicStroke((float) canvas.canvasToImageLogDeltaX(4));
		stroke[3] = new BasicStroke((float) canvas.canvasToImageLogDeltaX(5));

		Line2D line = new Line2D.Double(a1.getPosition(), a2.getPosition());

		// transform and display ticks

		lineDefinitionList.clear();

		AffineTransform originalTransform = g.getTransform();

		double distance = line.getP1().distance(line.getP2());
		double vx = (line.getP2().getX() - line.getP1().getX()) / distance;
		double vy = (line.getP2().getY() - line.getP1().getY()) / distance;

		lineDefinitionList.add(new ShapeDefinition(3, new Line2D.Double(0, 0, distance, 0)));

		int tickSpace = findBestMajTickSpace((int) distance, (int) canvas.canvasToImageDeltaX((int) distance));

		for (int i = 0; i < line.getP1().distance(line.getP2()); i++)
		{
			if (i % tickSpace == 0)
			{
				lineDefinitionList.add(new ShapeDefinition(2, new Line2D.Double(i, 0, i, -convertScale(canvas, 20))));
			}

			if (i % (tickSpace / 5) == 0)
			{
				lineDefinitionList.add(new ShapeDefinition(1, new Line2D.Double(i, 0, i, -convertScale(canvas, 5))));
			}
		}

		// draw lines ( black background, then white )
		g.translate(line.getX1(), line.getY1());
		g.rotate(Math.atan2(vy, vx), 0, 0);
		if (vx < 0)
			g.rotate(Math.PI, distance / 2, 0);

		g.setColor(Color.black);
		for (ShapeDefinition ld : lineDefinitionList)
		{
			g.setStroke(stroke[ld.stroke]);
			g.draw(ld.shape);
		}

		g.setColor(Color.white);
		for (ShapeDefinition ld : lineDefinitionList)
		{
			g.setStroke(stroke[ld.stroke - 1]);
			g.draw(ld.shape);
		}

		// draw text

		int fontSize = (int) convertScale(canvas, 20);

		Font font = new Font("Arial", Font.PLAIN, fontSize);
		String pixelString;
		double realDistance = Math.sqrt((Math.pow(vx * distance * sequence.getPixelSizeX(), 2) + Math.pow(vy * distance * sequence.getPixelSizeY(), 2)));
		pixelString = " " + (int) distance + " px" + " / " + UnitUtil.getBestUnitInMeters(realDistance, 2, UnitPrefix.MICRO);
		Rectangle2D pixelBounds = GraphicsUtil.getStringBounds(g, font, pixelString);
		g.translate(distance / 2 - pixelBounds.getWidth() / 2, -convertScale(canvas, 20));
		g.setFont(font);
		g.setColor(Color.white);
		g.fill(pixelBounds);
		g.setColor(Color.black);
		g.drawString(pixelString, 0, 0);

		// get back to original transform

		g.setTransform(originalTransform);

		// display anchors

		a1.paint(g, sequence, canvas);
		a2.paint(g, sequence, canvas);
	}

	double convertScale(IcyCanvas canvas, double value)
	{
		return ROI2D.canvasToImageLogDeltaX(canvas, value);
	}

	@Override
	public void mousePressed(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
	{
		if (a1 == null || a2 == null)
			return;
		a1.mousePressed(e, imagePoint, canvas);
		a2.mousePressed(e, imagePoint, canvas);
	}

	@Override
	public void mouseReleased(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
	{
		if (a1 == null || a2 == null)
			return;
		a1.mouseReleased(e, imagePoint, canvas);
		a2.mouseReleased(e, imagePoint, canvas);
	}

	@Override
	public void mouseClick(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
	{
		if (a1 == null || a2 == null)
			return;
		a1.mouseClick(e, imagePoint, canvas);
		a2.mouseClick(e, imagePoint, canvas);
	}

	@Override
	public void mouseMove(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
	{
		if (a1 == null || a2 == null)
			return;
		a1.mouseMove(e, imagePoint, canvas);
		a2.mouseMove(e, imagePoint, canvas);
	}

	@Override
	public void mouseDrag(MouseEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
	{
		if (a1 == null || a2 == null)
			return;
		a1.mouseDrag(e, imagePoint, canvas);
		a2.mouseDrag(e, imagePoint, canvas);
	}

	@Override
	public void keyPressed(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
	{
		if (a1 == null || a2 == null)
			return;
		if (e.getKeyCode() == KeyEvent.VK_DELETE && (a1.isSelected() || a2.isSelected()))
		{
			// System.out.println(e.getKeyCode());
			// System.out.println(e);
			for (Sequence sequence : Icy.getMainInterface().getSequencesContaining(this))
			{
				sequence.removePainter(this);
			}
		}

		a1.keyPressed(e, imagePoint, canvas);
		a2.keyPressed(e, imagePoint, canvas);
	}

	@Override
	public void keyReleased(KeyEvent e, Point5D.Double imagePoint, IcyCanvas canvas)
	{
		if (a1 == null || a2 == null)
			return;
		a1.keyReleased(e, imagePoint, canvas);
		a2.keyReleased(e, imagePoint, canvas);
	}

}
