package comiam.sapper.ui.tui;

import comiam.sapper.game.Minesweeper;
import comiam.sapper.time.Timer;
import comiam.sapper.ui.GameViewController;
import comiam.sapper.util.CoordinateUtils;
import comiam.sapper.util.TextUtils;

import java.io.Console;
import java.util.Arrays;

import static comiam.sapper.time.Timer.*;
import static comiam.sapper.util.IOUtils.print;
import static comiam.sapper.util.IOUtils.println;
import static comiam.sapper.util.TextUtils.*;
import static java.lang.Integer.parseInt;

public class TextGame implements GameViewController
{
    private static final int FREE_CELL = 0;
    private static final int NOT_OPENED_CELL = -1;
    private static final int MARKED_CELL = -2;
    private static final int MARKED_MAYBE_CELL = -3;
    private static final int MINED_CELL = -4;
    private Console console;
    private int[][] map;
    private boolean gameOverFlag = false;
    private boolean gameWinFlag = false;
    private boolean stopFlag = false;

    public void init(Console console)
    {
        map = new int[Minesweeper.getFieldSize().width][Minesweeper.getFieldSize().height];

        this.console = console;
        for(var arr : map)
            Arrays.fill(arr, NOT_OPENED_CELL);

        display();
        if(console == null)
            return;

        String line;
        do
        {
            line = console.readLine().trim();

            if(line.split(" ").length > 3)
            {
                println("Unknown command: " + line);
                if(Minesweeper.isMainController(this))
                    print("> ");
                continue;
            }

            String[] lineArr = line.split(" ");

            if(lineArr.length == 3 && isNumeric(lineArr[1]) && isNumeric(lineArr[2]) && !isNumeric(lineArr[0]))
            {
                if(!CoordinateUtils.isAvailable(parseInt(lineArr[1]), parseInt(lineArr[2])))
                {
                    println("Wrong coordinates!");
                    if(Minesweeper.isMainController(this))
                        print("> ");
                    continue;
                }
                if(lineArr[0].length() != 1 || !"mo".contains(lineArr[0]))
                {
                    println("Wrong mode!");
                    if(Minesweeper.isMainController(this))
                        print("> ");
                    continue;
                }

                if(!Minesweeper.isGameStarted())
                {
                    Minesweeper.initField(parseInt(lineArr[1]), parseInt(lineArr[2]));
                    Minesweeper.startGame();
                    start(Minesweeper::repaintControllersTimer);
                }
                if(Minesweeper.isGameEnded())
                {
                    if(Minesweeper.isMainController(this))
                        print("> ");
                    continue;
                }
                switch(lineArr[0])
                {
                    case "o" -> Minesweeper.openCell(parseInt(lineArr[1]), parseInt(lineArr[2]));
                    case "m" -> Minesweeper.markCell(parseInt(lineArr[1]), parseInt(lineArr[2]));
                }
                continue;
            }

            switch(line)
            {
                case "pause" -> {
                    if(!Minesweeper.isGameStarted())
                    {
                        if(Minesweeper.isMainController(this))
                            print("> ");
                        continue;
                    }
                    Minesweeper.pauseControllers();
                    setPause();
                }
                case "replay" -> {
                    if(!Minesweeper.isGameEnded() && !Minesweeper.isGameStarted())
                    {
                        if(Minesweeper.isMainController(this))
                            print("> ");
                        continue;
                    }
                    Minesweeper.restartControllers();
                }
                case "new" -> Minesweeper.rebuildControllers();
                case "exit" -> System.exit(0);
                default -> {
                    println("Unknown command: " + line);
                    if(Minesweeper.isMainController(this))
                        print("> ");
                }
            }
        } while(true);
    }

    private void display()
    {
        println(getTimeString(getSeconds() / 3600) + ":" + getTimeString((getSeconds() % 3600) / 60) + ":" + getTimeString(getSeconds() % 60));
        println(Minesweeper.getFlagCount() + "/" + Minesweeper.getMaxFlagCount());

        if(stopFlag)
        {
            println("""
                    (  _ \\ / _\\ / )( \\/ ___)(  __)
                     ) __//    \\) \\/ (\\___ \\ ) _)
                    (__)  \\_/\\_/\\____/(____/(____)
                    """);
            if(Minesweeper.isMainController(this))
                print("> ");
            return;
        }

        print("  ");
        for(int i = 0; i < map.length; i++)
            print(getNumericString(i));
        println();

        for(int x = 0; x < map.length; x++)
        {
            print(getNumericString(x));
            for(int y = 0; y < map[x].length; y++)
                switch(map[y][x])
                {
                    case MINED_CELL -> print(" *");
                    case NOT_OPENED_CELL -> print(" ?");
                    case MARKED_CELL -> print(" F");
                    case MARKED_MAYBE_CELL -> print(" V");
                    case FREE_CELL -> print("  ");
                    case 1, 2, 3, 4, 5, 6, 7, 8 -> print(" " + map[y][x]);
                    default -> throw new IllegalArgumentException("Wrong map encoding: " + map[y][x]);
                }
            println();
        }
        if(gameOverFlag)
            println("YOU LOSE");
        else if(gameWinFlag)
            println("YOU WIN");

        if(Minesweeper.isMainController(this))
            print("> ");
    }

    @Override
    public void markCell(int x, int y)
    {
        map[x][y] = MARKED_CELL;
        display();
    }

    @Override
    public void markMaybeCell(int x, int y)
    {
        map[x][y] = MARKED_MAYBE_CELL;
        display();
    }

    @Override
    public void offMarkOnCell(int x, int y)
    {
        map[x][y] = NOT_OPENED_CELL;
    }

    @Override
    public void freeCell(int x, int y)
    {
        map[x][y] = FREE_CELL;
    }

    @Override
    public void setPause()
    {
        if(Minesweeper.isMainController(this))
        {
            if(isRunning())
                stop();
            else
                on();
        }
        stopFlag = !stopFlag;

        display();
    }

    @Override
    public void setNumCell(int x, int y, int num)
    {
        map[x][y] = (byte) num;
    }

    @Override
    public void repaintTimer()
    {
    }

    @Override
    public void repaintFlag()
    {
    }

    @Override
    public void disableGame(byte[][] map)
    {
        if(Minesweeper.isMainController(this))
            stop();
        for(int x = 0; x < map.length; x++)
            for(int y = 0; y < map[x].length; y++)
                if(Minesweeper.isMined(map[x][y]))
                    this.map[x][y] = MINED_CELL;
    }

    @Override
    public boolean restartGame()
    {
        gameWinFlag = false;
        gameOverFlag = false;
        stopFlag = false;
        for(var arr : map)
            Arrays.fill(arr, (byte) NOT_OPENED_CELL);
        if(Minesweeper.isMainController(this))
        {
            stop();
            Minesweeper.newGame();
        }
        display();
        return true;
    }

    @Override
    public boolean rebuildField()
    {
        if(Minesweeper.isMainController(this))
        {
            Timer.stop();
            println("Pls, select field size: x16 or x32");
            String line;
            Minesweeper.FieldDimension dim;

            menu:
            do
            {
                if(Minesweeper.isMainController(this))
                    print("> ");
                line = console.readLine().trim();
                if(line.split(" ").length > 1)
                {
                    println("Unknown size: " + line);
                    continue;
                }

                switch(line)
                {
                    case "x16" -> {
                        dim = Minesweeper.FieldDimension.x16;
                        break menu;
                    }
                    case "x32" -> {
                        dim = Minesweeper.FieldDimension.x32;
                        break menu;
                    }
                    case "nothing" -> {
                        start(Minesweeper::repaintControllersTimer);
                        return false;
                    }
                    case "exit" -> System.exit(0);
                    default -> println("Unknown size: " + line);
                }
            } while(true);

            Minesweeper.newGame(dim);
        }
        map = new int[Minesweeper.getFieldSize().width][Minesweeper.getFieldSize().height];
        for(var arr : map)
            Arrays.fill(arr, NOT_OPENED_CELL);

        gameOverFlag = false;
        gameWinFlag = false;
        stopFlag = false;

        display();

        return true;
    }

    @Override
    public void noticeOverGame()
    {
        gameOverFlag = true;
        gameWinFlag = false;

        display();
    }

    @Override
    public void noticeWinGame()
    {
        gameWinFlag = true;
        gameOverFlag = false;

        display();
    }

    @Override
    public boolean isGUI()
    {
        return false;
    }

    @Override
    public void update(boolean makeOnlyOutSymbol)
    {
        if(makeOnlyOutSymbol)
            print("> ");
        else
            display();
    }

}
