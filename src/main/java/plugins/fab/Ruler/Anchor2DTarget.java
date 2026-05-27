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

import fr.icy.gui.canvas.IcyCanvas;
import fr.icy.model.overlay.anchor.Anchor2D;
import fr.icy.model.sequence.Sequence;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.Line2D;

/**
 * @author Fabrice de Chaumont
 */
public class Anchor2DTarget extends Anchor2D {
    Line2D line1 = new Line2D.Double();
    Line2D line2 = new Line2D.Double();

    public Anchor2DTarget(final double x, final double y) {
        super(x, y);
    }

    @Override
    public void paint(final Graphics2D g, final Sequence sequence, final IcyCanvas canvas) {
        if (!visible)
            return;

        updateGeometry(canvas);

        final BasicStroke strokeMainLine = new BasicStroke((float) canvas.canvasToImageLogDeltaX(2));
        final BasicStroke strokeSubLine = new BasicStroke((float) canvas.canvasToImageLogDeltaX(1));

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

    void updateGeometry(final @NotNull IcyCanvas canvas) {
        final double adjRayX = canvas.canvasToImageLogDeltaX(ray);
        final double adjRayY = canvas.canvasToImageLogDeltaY(ray);

        line1.setLine(position.x - adjRayX, position.y, position.x + adjRayX, position.y);

        line2.setLine(position.x, position.y - adjRayY, position.x, position.y + adjRayY);
    }
}
