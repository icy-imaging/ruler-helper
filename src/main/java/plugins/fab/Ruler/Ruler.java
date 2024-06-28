/*
 * Copyright (c) 2010-2024. Institut Pasteur.
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

import org.bioimageanalysis.icy.extension.plugin.abstract_.PluginActionable;
import org.bioimageanalysis.icy.extension.plugin.annotation_.IcyPluginIcon;
import org.bioimageanalysis.icy.extension.plugin.annotation_.IcyPluginName;
import org.bioimageanalysis.icy.gui.dialog.MessageDialog;
import org.bioimageanalysis.icy.model.sequence.Sequence;

/**
 * @author Fabrice de Chaumont
 */
@IcyPluginName("Ruler Helper")
@IcyPluginIcon(path = "/ruler-helper.png")
public class Ruler extends PluginActionable {
    @Override
    public void run() {
        final Sequence sequence = getActiveSequence();

        if (sequence != null) {
            sequence.addOverlay(new RulerPainter());
        }
        else {
            MessageDialog.showDialog("Please open an image first.", MessageDialog.INFORMATION_MESSAGE);
        }
    }
}
