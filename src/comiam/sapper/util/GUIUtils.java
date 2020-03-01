package comiam.sapper.util;

import comiam.sapper.game.Minesweeper;

import javax.swing.*;

public class GUIUtils
{
    public static void invokeInGUI(Runnable runnable)
    {
        try
        {
            if(!Minesweeper.isMainControllerIsGUI())
                SwingUtilities.invokeAndWait(runnable);
            else
                runnable.run();
        } catch(Throwable ignored) {}
    }
}
