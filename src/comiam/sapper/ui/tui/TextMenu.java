package comiam.sapper.ui.tui;

import comiam.sapper.game.records.Pair;
import comiam.sapper.game.records.ScoreRecords;
import comiam.sapper.ui.gui.GUIGame;
import comiam.sapper.util.TextUtils;

import java.io.Console;
import java.util.ArrayList;

import static comiam.sapper.game.Minesweeper.*;
import static comiam.sapper.util.GUIUtils.invokeInGUI;
import static comiam.sapper.util.IOUtils.print;
import static comiam.sapper.util.IOUtils.println;

public class TextMenu
{
    private final boolean twoInterfacesNeed;
    private Console console;

    public TextMenu(boolean twoInterfacesNeed)
    {
        this.twoInterfacesNeed = twoInterfacesNeed;
        println();
        println("MINESWEEPER V" + GAME_VERSION);
        println("Hello! What u want to do?)");
        println("""
                        play   - play a new game
                        scores - get scoreboard
                        about  - look at developers of this sh// game
                        exit   - exit from game
                    """);
        runMenu();
    }

    private void runMenu()
    {
        console = System.console();
        String[] line;
        menu: do
        {
            print("> ");
            line = console.readLine().split(" ");
            if(line.length == 0)
                continue;
            if(line.length > 1)
            {
                println("Unknown command: " + TextUtils.getStringFromArray(line));
                continue;
            }

            switch(TextUtils.getStringFromArray(line))
            {
                case "play" -> {
                    playGame();
                    break menu;
                }
                case "scores" -> getScores();
                case "about" -> getAbout();
                case "exit" -> exit();
                default -> println("Unknown command: " + TextUtils.getStringFromArray(line));
            }
        }while(true);
    }

    private void playGame()
    {
        println("Pls, select field size: x16 or x32");
        String[] line;
        FieldDimension dim;

        menu: do
        {
            print("> ");
            line = console.readLine().split(" ");
            if(line.length == 0)
                continue;
            if(line.length > 1)
            {
                println("Unknown size: " + TextUtils.getStringFromArray(line));
                continue;
            }

            switch(TextUtils.getStringFromArray(line))
            {
                case "x16" -> {
                    dim = FieldDimension.x16;
                    break menu;
                }
                case "x32" -> {
                    dim = FieldDimension.x32;
                    break menu;
                }
                case "exit", "nothing" -> exit();
                default -> println("Unknown size: " + TextUtils.getStringFromArray(line));
            }
        }while(true);

        newGame(dim);

        TextGame tg = new TextGame();
        setMainController(tg);

        if(twoInterfacesNeed)
        {
            invokeInGUI(() -> {
                GUIGame cont = new GUIGame();
                addController(cont);
                cont.init(null);
            });
        }
        tg.init(console);
    }

    private void getScores()
    {
        ArrayList<Pair> pairs = ScoreRecords.getRecords();
        if(pairs == null)
        {
            println("Records list is empty!");
            return;
        }
        for(var p : pairs)
            println(p.getName() + ": " + p.getTime());
    }

    private void getAbout()
    {
        println(String.format("""
                              Minesweeper v%s
                              Designer: comiam.
                              Developer: comiam.
                              Date: 29.02.20.
                              """, GAME_VERSION));
    }

    private void exit()
    {
        System.exit(0);
    }
}
