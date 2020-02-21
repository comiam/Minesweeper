package comiam.sapper.game;

import comiam.sapper.game.records.Pair;
import comiam.sapper.game.records.ScoreRecords;
import comiam.sapper.time.Timer;
import comiam.sapper.ui.MainMenu;
import comiam.sapper.ui.ScoresFrame;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

import static comiam.sapper.time.Timer.makeTimeString;
import static comiam.sapper.util.BitOperations.getBit;
import static comiam.sapper.util.BitOperations.setBit;
import static comiam.sapper.util.CoordinateUtils.getDotsNear;
import static comiam.sapper.util.CoordinateUtils.isAvailable;

public class Sapper
{
    private static int flagCount;
    private static int maxFlagCount;
    private static boolean gameStarted = false;
    private static boolean gameEnded = false;
    public static byte[][] map;
    private static int row, col;

    public static void newGame()
    {
        flagCount = 0;
        gameStarted = false;
        gameEnded = false;
        map = new byte[col][row];
    }

    public static void newGame(FieldDimension dim)
    {
        flagCount = 0;
        maxFlagCount = 0;
        switch(dim)
        {
            case x16:
                row = 16;
                col = 16;
                maxFlagCount = 50;
                break;
            case x32:
                row = 32;
                col = 32;
                maxFlagCount = 100;
                break;
            case nothing:
                return;
            default:
                throw new IllegalStateException("Unexpected value: " + dim);
        }

        newGame();
    }

    public static void initField(int _x, int _y)
    {
        int mines = maxFlagCount;
        int x, y;
        while(mines > 0)
        {
            x = ThreadLocalRandom.current().nextInt(col);
            y = ThreadLocalRandom.current().nextInt(row);

            if(Math.abs(x - _x) <= 1 && Math.abs(y - _y) <= 1)
                continue;

            if(!isMined(x,y))//TODO 000 - flags of cell, where first bit - mine flag, second - mark flag, third - maybe mark flag
            {
                map[x][y] = setBit(map[x][y], 1, 0);//TODO mine cell
                mines--;
            }
        }
    }

    public static void openCell(int x, int y)
    {
        if(isMarked(x, y) || isMaybeMarked(x, y))
        {
            if(isMarked(x, y))
                flagCount--;
            removeMark(x, y);
            removeMaybeMark(x, y);

            SwingUtilities.invokeLater(MainMenu.getGameFrame()::repaintFlag);
            SwingUtilities.invokeLater(() -> MainMenu.getGameFrame().offMarkOnCell(x, y));
        }

        if(isMined(x, y))
            gameOver();
        else
        {
            if(minedNearCell(x, y) > 0)
                SwingUtilities.invokeLater(() -> MainMenu.getGameFrame().setNumCell(x, y, minedNearCell(x, y)));
            else
            {
                SwingUtilities.invokeLater(() -> MainMenu.getGameFrame().freeCell(x, y));
                tagCell(x, y);

                for(var point : getDotsNear(x,y))
                    if(isAvailable(point.x, point.y) && minedNearCell(point.x, point.y) == 0 && isNotTagged(point.x, point.y))
                        openCell(point.x, point.y);
                    else if(isAvailable(point.x, point.y) && minedNearCell(point.x, point.y) != 0)
                    {
                        if(isMarked(point.x, point.y) || isMaybeMarked(point.x, point.y))
                        {
                            SwingUtilities.invokeLater(() -> MainMenu.getGameFrame().offMarkOnCell(point.x, point.y));
                            if(isMarked(point.x, point.y))
                            {
                                flagCount--;
                                SwingUtilities.invokeLater(MainMenu.getGameFrame()::repaintFlag);
                            }

                            removeMaybeMark(point.x, point.y);
                            removeMark(point.x,point.y);
                        }
                        SwingUtilities.invokeLater(() -> MainMenu.getGameFrame().setNumCell(point.x, point.y, minedNearCell(point.x, point.y)));
                    }
            }
        }
        if(checkForWin())
            gameWin();
    }

    public static void markCell(int x, int y)
    {
        if(isMarked(x,y))
        {
            removeMark(x,y);
            maybeMark(x,y);

            flagCount--;
            SwingUtilities.invokeLater(MainMenu.getGameFrame()::repaintFlag);
            SwingUtilities.invokeLater(() -> MainMenu.getGameFrame().markMaybeCell(x, y));
        } else if(isMaybeMarked(x,y))
        {
            removeMaybeMark(x, y);
            SwingUtilities.invokeLater(() -> MainMenu.getGameFrame().offMarkOnCell(x, y));
        } else
        {
            if(flagCount >= maxFlagCount)
                return;
            mark(x, y);

            flagCount++;
            SwingUtilities.invokeLater(MainMenu.getGameFrame()::repaintFlag);
            SwingUtilities.invokeLater(() -> MainMenu.getGameFrame().markCell(x, y));
        }
        if(checkForWin())
            gameWin();
    }

    private static boolean checkForWin()
    {
        for(int x = 0; x < map.length; x++)
            for(int y = 0; y < map[x].length; y++)
                if(isMined(x, y) && !isMarked(x, y))
                    return false;
        return true;
    }

    private static int minedNearCell(int x, int y)
    {
        int count = 0;

        for(var p : getDotsNear(x,y))
            if(isAvailable(p.x, p.y) && isMined(p.x, p.y))
                count++;

        return count;
    }

    public static boolean isMined(int x, int y)
    {
        return getBit(map[x][y], 0) == 1;
    }

    public static boolean isMarked(int x, int y)
    {
        return getBit(map[x][y], 1) == 1;
    }

    public static boolean isMaybeMarked(int x, int y)
    {
        return getBit(map[x][y], 2) == 1;
    }

    private static void maybeMark(int x, int y)
    {
        map[x][y] = setBit(map[x][y], 1, 2);
    }

    private static void mark(int x, int y)
    {
        map[x][y] = setBit(map[x][y], 1, 1);
    }

    private static void removeMark(int x, int y)
    {
        map[x][y] = setBit(map[x][y], 0, 1);
    }

    private static void removeMaybeMark(int x, int y)
    {
        map[x][y] = setBit(map[x][y], 0, 2);
    }

    private static void tagCell(int x, int y)
    {
        map[x][y] = setBit(map[x][y], 1, 3);
    }

    private static boolean isNotTagged(int x, int y)
    {
        return getBit(map[x][y], 3) != 1;
    }

    private static void gameOver()
    {
        gameEnded = true;
        gameStarted = false;
        SwingUtilities.invokeLater(() -> MainMenu.getGameFrame().disableGame(map));
        JOptionPane.showMessageDialog(MainMenu.getGameFrame(), "<html><h2>Sorry, you was died :c</h2><i>play again!</i>");
    }

    private static void gameWin()
    {
        gameEnded = true;
        SwingUtilities.invokeLater(() -> MainMenu.getGameFrame().disableGame(map));
        JOptionPane.showMessageDialog(MainMenu.getGameFrame(), "<html><h2>You win! с:</h2><i>Your score " +
                makeTimeString(comiam.sapper.time.Timer.getSeconds() / 3600) + ":" +
                makeTimeString((comiam.sapper.time.Timer.getSeconds() % 3600) / 60) + ":" +
                makeTimeString(Timer.getSeconds() % 60) + "!</i>");

        String name = (String) JOptionPane.showInputDialog(MainMenu.getGameFrame(),"Pls, enter your name :)", "Message", JOptionPane.QUESTION_MESSAGE, null, null, System.getProperty("user.name"));
        if(name == null)
            name = System.getProperty("user.name");

        Pair p = new Pair(name + "_" + makeTimeString(comiam.sapper.time.Timer.getSeconds() / 3600) + ":" +
                makeTimeString((comiam.sapper.time.Timer.getSeconds() % 3600) / 60) + ":" +
                makeTimeString(Timer.getSeconds() % 60));
        ScoreRecords.saveRecord(p);
        ScoresFrame.showRecords();
    }

    public static int getFlagCount()
    {
        return flagCount;
    }

    public static int getMaxFlagCount()
    {
        return maxFlagCount;
    }

    public static boolean isGameEnded()
    {
        return gameEnded;
    }

    public static boolean isGameStarted()
    {
        return gameStarted;
    }

    public static void startGame()
    {
        gameStarted = true;
    }

    public static Dimension getFieldSize()
    {
        return new Dimension(col, row);
    }

    public enum FieldDimension
    {
        x16,
        x32,
        nothing
    }
}
