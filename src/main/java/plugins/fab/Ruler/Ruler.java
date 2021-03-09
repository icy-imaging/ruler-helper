/**
 * by Fabrice de Chaumont
 */

package plugins.fab.Ruler;

import icy.gui.dialog.MessageDialog;
import icy.plugin.abstract_.PluginActionable;
import icy.sequence.Sequence;

public class Ruler extends PluginActionable
{

    @Override
    public void run()
    {

        Sequence sequence = getFocusedSequence();

        if (sequence != null)
        {
            sequence.addPainter(new RulerPainter());
        }
        else
        {
            MessageDialog.showDialog("Please open an image first.", MessageDialog.INFORMATION_MESSAGE);
        }

    }

}
