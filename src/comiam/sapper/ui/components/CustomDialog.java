package comiam.sapper.ui.components;

import comiam.sapper.game.Sapper;

import javax.swing.*;
import java.awt.*;

public class CustomDialog
{
    private static final String[] dimensions =
            {
                    "16x16",
                    "32x32"
            };

    public static Sapper.FieldDimension getDimension(Component frame)
    {
        String result = (String) JOptionPane.showInputDialog(
                frame,
                "Choose size of field:",
                "Message",
                JOptionPane.QUESTION_MESSAGE,
                null, dimensions, dimensions[0]);

        if(result == null)
            return Sapper.FieldDimension.nothing;

        switch(result)
        {
            case "16x16":
                return Sapper.FieldDimension.x16;
            case "32x32":
                return Sapper.FieldDimension.x32;
        }
        return Sapper.FieldDimension.nothing;
    }
}
