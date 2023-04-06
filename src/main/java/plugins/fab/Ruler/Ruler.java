package plugins.fab.Ruler;

import icy.gui.dialog.MessageDialog;
import icy.plugin.abstract_.PluginActionable;
import icy.sequence.Sequence;

/**
 * @author Fabrice de Chaumont
 */
public class Ruler extends PluginActionable {

    @Override
    public void run() {

        Sequence sequence = getActiveSequence();

        if (sequence != null) {
            sequence.addOverlay(new RulerPainter());
        }
        else {
            MessageDialog.showDialog("Please open an image first.", MessageDialog.INFORMATION_MESSAGE);
        }

    }

}
