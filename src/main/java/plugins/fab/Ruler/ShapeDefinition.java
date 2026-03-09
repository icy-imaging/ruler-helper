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

import org.jetbrains.annotations.Contract;

import java.awt.*;

/**
 * @author Fabrice de Chaumont
 */
public class ShapeDefinition {
    public int stroke;
    public Shape shape;
    public float alpha = 1;

    @Contract(pure = true)
    public ShapeDefinition(final int stroke, final Shape shape) {
        this.stroke = stroke;
        this.shape = shape;
    }

    @Contract(pure = true)
    public ShapeDefinition(final int stroke, final Shape shape, final float alpha) {
        this(stroke, shape);
        this.alpha = alpha;
    }
}
