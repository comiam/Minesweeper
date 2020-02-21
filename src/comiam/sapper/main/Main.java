package comiam.sapper.main;

import comiam.sapper.ui.MainMenu;
import comiam.sapper.ui.components.UIDesigner;

public class Main
{
    public static void main(String[] args)
    {
        UIDesigner.init();
        new MainMenu();
    }
}
