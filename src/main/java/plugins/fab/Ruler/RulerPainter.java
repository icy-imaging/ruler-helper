/*
 * Copyright (c) 2010-2026. Institut Pasteur.
 *
 * This file is part of Icy.
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */

package plugins.fab.Ruler;

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.common.geom.point.Point5D;
import org.bioimageanalysis.icy.common.math.UnitUtil;
import org.bioimageanalysis.icy.gui.GraphicsUtil;
import org.bioimageanalysis.icy.gui.canvas.Canvas2D;
import org.bioimageanalysis.icy.gui.canvas.IcyCanvas;
import org.bioimageanalysis.icy.model.overlay.Overlay;
import org.bioimageanalysis.icy.model.overlay.OverlayEvent;
import org.bioimageanalysis.icy.model.overlay.OverlayListener;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * @author Fabrice de Chaumont
 */
public class RulerPainter extends Overlay implements OverlayListener {
    private Anchor2DTarget a1;
    private Anchor2DTarget a2;

    public RulerPainter() {
        super("Ruler Helper");
    }

    // Listener of Anchor

    @Override
    public void overlayChanged(final OverlayEvent overlayEvent) {
        for (final Sequence sequence : Icy.getMainInterface().getSequencesContaining(this)) {
            sequence.overlayChanged(this);
        }
    }

    int findBestMajTickSpace(final int sliderSize, final int delta) {

        final int[] values = {5, 10, 20, 50, 100, 200, 250, 500, 1000, 2000, 2500, 5000};

        final int wantedMajTickSpace;
        // wanted a major tick each ~40 pixels
        try {
            wantedMajTickSpace = delta / (sliderSize / 40);
        }
        catch (final ArithmeticException e) {
            return values[0];
        }

        int min = Integer.MAX_VALUE;
        int bestValue = 5;

        // try with our predefined values
        for (final int value : values) {
            final int dx = Math.abs(value - wantedMajTickSpace);

            if (dx < min) {
                min = dx;
                bestValue = value;
            }
        }

        return bestValue;
    }

    // Painter Section:

    @Override
    public void paint(final Graphics2D g, final Sequence sequence, final IcyCanvas canvas) {
        if (g == null)
            return;
        if (!(canvas instanceof Canvas2D))
            return;

        if (a1 == null || a2 == null) {
            final Rectangle bounds = g.getClipBounds();

            final int w = bounds.width;
            final int h = bounds.height;
            final int x = bounds.x;
            final int y = bounds.y;

            a1 = new Anchor2DTarget(x + w / 4f, y + h / 2f);
            a2 = new Anchor2DTarget(x + 3f * w / 4f, y + h / 2f);

            sequence.addOverlay(this);
            a1.addOverlayListener(this);
            a2.addOverlayListener(this);
        }

        final BasicStroke[] stroke = new BasicStroke[4];

        stroke[0] = new BasicStroke((float) canvas.canvasToImageLogDeltaX(2));
        stroke[1] = new BasicStroke((float) canvas.canvasToImageLogDeltaX(3));
        stroke[2] = new BasicStroke((float) canvas.canvasToImageLogDeltaX(4));
        stroke[3] = new BasicStroke((float) canvas.canvasToImageLogDeltaX(5));

        final Line2D line = new Line2D.Double(a1.getPosition(), a2.getPosition());

        // transform and display ticks
        final ArrayList<ShapeDefinition> lineDefinitionList = new ArrayList<>();

        final AffineTransform originalTransform = g.getTransform();

        final double distance = line.getP1().distance(line.getP2());
        final double vx = (line.getP2().getX() - line.getP1().getX()) / distance;
        final double vy = (line.getP2().getY() - line.getP1().getY()) / distance;

        lineDefinitionList.add(new ShapeDefinition(3, new Line2D.Double(0, 0, distance, 0)));

        final int tickSpace = findBestMajTickSpace((int) distance, (int) canvas.canvasToImageDeltaX((int) distance));

        for (int i = 0; i < line.getP1().distance(line.getP2()); i++) {
            if (i % tickSpace == 0) {
                lineDefinitionList.add(new ShapeDefinition(2, new Line2D.Double(i, 0, i, -convertScale(canvas, 20))));
            }

            if (i % (tickSpace / 5) == 0) {
                lineDefinitionList.add(new ShapeDefinition(1, new Line2D.Double(i, 0, i, -convertScale(canvas, 5))));
            }
        }

        // draw lines ( black background, then white )
        g.translate(line.getX1(), line.getY1());
        g.rotate(Math.atan2(vy, vx), 0, 0);
        if (vx < 0)
            g.rotate(Math.PI, distance / 2, 0);

        g.setColor(Color.black);
        for (final ShapeDefinition ld : lineDefinitionList) {
            g.setStroke(stroke[ld.stroke]);
            g.draw(ld.shape);
        }

        g.setColor(Color.white);
        for (final ShapeDefinition ld : lineDefinitionList) {
            g.setStroke(stroke[ld.stroke - 1]);
            g.draw(ld.shape);
        }

        // draw text

        final int fontSize = (int) convertScale(canvas, 20);

        final Font font = new Font("Arial", Font.PLAIN, fontSize);
        final String pixelString;
        final double realDistance = Math.sqrt((Math.pow(vx * distance * sequence.getPixelSizeX(), 2) + Math.pow(vy * distance * sequence.getPixelSizeY(), 2)));
        pixelString = " " + (int) distance + " px" + " / " + UnitUtil.getBestUnitInMeters(realDistance, 2, UnitUtil.UnitPrefix.MICRO);
        final Rectangle2D pixelBounds = GraphicsUtil.getStringBounds(g, font, pixelString);
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

    double convertScale(final @NotNull IcyCanvas canvas, final int value) {
        return canvas.canvasToImageLogDeltaX(value);
    }

    @Override
    public void mousePressed(final MouseEvent e, final Point5D.Double imagePoint, final IcyCanvas canvas) {
        if (a1 == null || a2 == null)
            return;
        a1.mousePressed(e, imagePoint, canvas);
        a2.mousePressed(e, imagePoint, canvas);
    }

    @Override
    public void mouseReleased(final MouseEvent e, final Point5D.Double imagePoint, final IcyCanvas canvas) {
        if (a1 == null || a2 == null)
            return;
        a1.mouseReleased(e, imagePoint, canvas);
        a2.mouseReleased(e, imagePoint, canvas);
    }

    @Override
    public void mouseClick(final MouseEvent e, final Point5D.Double imagePoint, final IcyCanvas canvas) {
        if (a1 == null || a2 == null)
            return;
        a1.mouseClick(e, imagePoint, canvas);
        a2.mouseClick(e, imagePoint, canvas);
    }

    @Override
    public void mouseMove(final MouseEvent e, final Point5D.Double imagePoint, final IcyCanvas canvas) {
        if (a1 == null || a2 == null)
            return;
        a1.mouseMove(e, imagePoint, canvas);
        a2.mouseMove(e, imagePoint, canvas);
    }

    @Override
    public void mouseDrag(final MouseEvent e, final Point5D.Double imagePoint, final IcyCanvas canvas) {
        if (a1 == null || a2 == null)
            return;
        a1.mouseDrag(e, imagePoint, canvas);
        a2.mouseDrag(e, imagePoint, canvas);
    }

    @Override
    public void keyPressed(final KeyEvent e, final Point5D.Double imagePoint, final IcyCanvas canvas) {
        if (a1 == null || a2 == null)
            return;
        if (e.getKeyCode() == KeyEvent.VK_DELETE && (a1.isSelected() || a2.isSelected())) {
            // System.out.println(e.getKeyCode());
            // System.out.println(e);
            for (final Sequence sequence : Icy.getMainInterface().getSequencesContaining(this)) {
                sequence.removeOverlay(this);
            }
        }

        a1.keyPressed(e, imagePoint, canvas);
        a2.keyPressed(e, imagePoint, canvas);
    }

    @Override
    public void keyReleased(final KeyEvent e, final Point5D.Double imagePoint, final IcyCanvas canvas) {
        if (a1 == null || a2 == null)
            return;
        a1.keyReleased(e, imagePoint, canvas);
        a2.keyReleased(e, imagePoint, canvas);
    }

}
