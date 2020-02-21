package comiam.sapper.ui;

import comiam.sapper.game.Sapper;
import comiam.sapper.time.Timer;
import comiam.sapper.ui.components.CustomButton;
import comiam.sapper.ui.components.CustomDialog;
import comiam.sapper.ui.components.CustomPanel;
import comiam.sapper.ui.components.UIDesigner;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static comiam.sapper.time.Timer.makeTimeString;
import static comiam.sapper.ui.MainMenu.*;
import static comiam.sapper.ui.components.UIDesigner.DEFAULT_BACKGROUND;

public class GameFrame extends JPanel
{
    private static final int SIZE = 30;
    private final HashMap<CustomButton, Image> imageMap = new HashMap<>();
    private Image bomb;
    private Image flag;
    private Image goodFlag;
    private Image badFlag;
    private Image flagMaybe;
    private Image goodFlagMaybe;
    private Image badFlagMaybe;
    private CustomPanel minePanel;
    private CustomPanel pausePanel;
    private JPanel statPanel;
    private JLabel flagCountL;
    private JLabel timerCountL;
    private CustomButton pause;
    private CustomButton replay;
    private CustomButton[] cells;

    private int row, col;
    private boolean timerOn = false;
    private boolean isButtonStop = false;
    private boolean mayPause = true;

    public GameFrame(Sapper.FieldDimension dim)
    {
        setSizes(dim);
        setLayout(new GridBagLayout());

        fillContent();
        loadContent();
        setAutoResizer();
    }

    @Override
    public Dimension getMinimumSize()
    {
        int width = minePanel.getMinimumSize().width + statPanel.getMinimumSize().width;
        int height = minePanel.getMinimumSize().height;
        return new Dimension(width, height);
    }

    @Override
    public Dimension getPreferredSize()
    {
        int width = minePanel.getMinimumSize().width + statPanel.getMinimumSize().width;
        int height = minePanel.getMinimumSize().height;
        return new Dimension(width, height);
    }

    private void setAutoResizer()
    {
        for(JButton b : cells)
        {
            Font label12Font = UIDesigner.getFont(22, b.getFont(), true);
            if(label12Font != null) b.setFont(label12Font);
            b.addComponentListener(new ComponentAdapter()
            {
                protected void decreaseFontSize(JButton comp)
                {
                    Font font = comp.getFont();
                    FontMetrics fm = comp.getFontMetrics(font);
                    int width = comp.getWidth();
                    int height = comp.getHeight();
                    int textWidth = fm.stringWidth(comp.getText());
                    int textHeight = fm.getHeight();

                    int size = font.getSize();
                    while(size > 0 && (textHeight - 10 > height || textWidth - 10 > width))
                    {
                        size -= 2;
                        font = font.deriveFont(font.getStyle(), size);
                        fm = comp.getFontMetrics(font);
                        textWidth = fm.stringWidth(comp.getText());
                        textHeight = fm.getHeight();
                    }

                    comp.setFont(font);
                }

                protected void increaseFontSize(JButton comp)
                {
                    Font font = comp.getFont();
                    FontMetrics fm = comp.getFontMetrics(font);
                    int width = comp.getWidth();
                    int height = comp.getHeight();
                    int textWidth = fm.stringWidth(comp.getText());
                    int textHeight = fm.getHeight();

                    int size = font.getSize();
                    while(textHeight - 25 < height && textWidth - 25 < width)
                    {
                        size += 2;
                        font = font.deriveFont(font.getStyle(), size);
                        fm = comp.getFontMetrics(font);
                        textWidth = fm.stringWidth(comp.getText());
                        textHeight = fm.getHeight();
                    }

                    comp.setFont(font);
                    decreaseFontSize(comp);
                }

                @Override
                public void componentResized(ComponentEvent e)
                {
                    JButton comp = (JButton) e.getComponent();
                    Font font = comp.getFont();
                    FontMetrics fm = comp.getFontMetrics(font);
                    int width = comp.getWidth();
                    int height = comp.getHeight();
                    int textWidth = fm.stringWidth(comp.getText());
                    int textHeight = fm.getHeight();

                    int offset;
                    if(textHeight > height || textWidth > width)
                    {
                        decreaseFontSize(comp);
                        offset = (int) (width * 0.15);
                    } else
                    {
                        increaseFontSize(comp);
                        offset = (int) (width * 0.2);
                    }

                    if(imageMap.containsKey(comp))
                    {
                        comp.setIcon(new ImageIcon(imageMap.get(comp).getScaledInstance(width - offset, height - offset, Image.SCALE_AREA_AVERAGING)));
                        if(!imageMap.get(comp).equals(bomb))
                            comp.setDisabledIcon(comp.getIcon());
                    }
                }
            });
        }
    }

    private void setSizes(Sapper.FieldDimension dim)
    {
        switch(dim)
        {
            case x16:
                row = 16;
                col = 16;
                break;
            case x32:
                row = 32;
                col = 32;
                break;
            case nothing:
                return;
            default:
                throw new IllegalStateException("Unexpected value: " + dim);
        }
        setMinimumSize(new Dimension(col * SIZE, row * SIZE));
        setPreferredSize(new Dimension(col * SIZE, row * SIZE));
        setSize(new Dimension(col * SIZE, row * SIZE));
        setToMinimum();
    }

    private void loadContent()
    {
        try
        {
            bomb = ImageIO.read(new File("res/mine.png"));
            flag = ImageIO.read(new File("res/flag.png"));
            flagMaybe = ImageIO.read(new File("res/flagmaybe.png"));
            goodFlag = ImageIO.read(new File("res/goodflag.png"));
            badFlag = ImageIO.read(new File("res/badflag.png"));
            goodFlagMaybe = ImageIO.read(new File("res/goodflagmaybe.png"));
            badFlagMaybe = ImageIO.read(new File("res/badflagmaybe.png"));
        } catch(IOException e)
        {
            JOptionPane.showMessageDialog(null, "I cant load images. Game is кряк :c", "Error!", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void fillContent()
    {
        this.removeAll();
        minePanel = new CustomPanel();
        isButtonStop = false;
        timerOn = false;
        mayPause = true;

        minePanel.setPreferredSize(new Dimension(col * SIZE, row * SIZE));
        minePanel.setMinimumSize(new Dimension(col * SIZE, row * SIZE));
        minePanel.setLayout(new GridLayout(row, col, 0, 0));
        minePanel.setBackground(DEFAULT_BACKGROUND);

        cells = new CustomButton[row * col];

        for(int i = 0; i < row * col; i++)
        {
            CustomButton btn = new CustomButton();
            final int finalI = i;

            btn.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    if(!e.getComponent().isEnabled())
                        return;
                    if(!Sapper.isGameStarted())
                    {
                        Sapper.initField(finalI % col, finalI / col);
                        timerOn = true;
                        Sapper.startGame();
                        pause.setVisible(true);
                        replay.setVisible(true);
                        turnOnTimer();
                    }

                    if(SwingUtilities.isLeftMouseButton(e))
                        Sapper.openCell(finalI % col, finalI / col);
                    else if(SwingUtilities.isRightMouseButton(e))
                        Sapper.markCell(finalI % col, finalI / col);
                }
            });
            minePanel.add(btn);
            cells[i] = btn;
        }
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;

        add(minePanel, c);

        statPanel = new JPanel();
        statPanel.setLayout(new BorderLayout());
        statPanel.setBackground(DEFAULT_BACKGROUND);
        statPanel.setMinimumSize(new Dimension(120, row * SIZE));
        statPanel.setPreferredSize(new Dimension(120, row * SIZE));

        JPanel stat0Panel = new JPanel();
        stat0Panel.setLayout(new BoxLayout(stat0Panel, BoxLayout.Y_AXIS));
        stat0Panel.setBackground(DEFAULT_BACKGROUND);
        stat0Panel.setPreferredSize(new Dimension(125, 170));

        flagCountL = new JLabel();
        try
        {
            flagCountL.setIcon(new ImageIcon(ImageIO.read(new File("res/flag.png")).getScaledInstance(40, 50, Image.SCALE_AREA_AVERAGING)));
        } catch(IOException ignored)
        {
        }

        flagCountL.setText("");
        flagCountL.setForeground(Color.LIGHT_GRAY);
        flagCountL.setAlignmentX(Component.CENTER_ALIGNMENT);
        flagCountL.setHorizontalTextPosition(JLabel.CENTER);
        flagCountL.setVerticalTextPosition(JLabel.BOTTOM);
        flagCountL.setMaximumSize(new Dimension(flagCountL.getMaximumSize().width + 10, 80));
        stat0Panel.add(flagCountL);

        timerCountL = new JLabel();
        try
        {
            timerCountL.setIcon(new ImageIcon(ImageIO.read(new File("res/timer.png")).getScaledInstance(50, 50, Image.SCALE_AREA_AVERAGING)));
        } catch(IOException ignored)
        {
        }

        timerCountL.setText("00:00:00");
        timerCountL.setForeground(Color.LIGHT_GRAY);
        timerCountL.setHorizontalTextPosition(JLabel.CENTER);
        timerCountL.setVerticalTextPosition(JLabel.BOTTOM);
        timerCountL.setAlignmentX(Component.CENTER_ALIGNMENT);
        stat0Panel.add(timerCountL);

        statPanel.add(stat0Panel, BorderLayout.NORTH);

        JPanel stat1Panel = new JPanel();
        stat1Panel.setLayout(new BoxLayout(stat1Panel, BoxLayout.Y_AXIS));
        stat1Panel.setBackground(DEFAULT_BACKGROUND);
        stat1Panel.setPreferredSize(new Dimension(120, 90));

        CustomButton newGame = new CustomButton("New game?");
        newGame.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        newGame.addActionListener(e ->
        {
            Timer.stop();
            if(!Sapper.isGameEnded() && Sapper.isGameStarted())
            {
                int a = JOptionPane.showConfirmDialog(getGameFrame(),
                        "<html><h2>You are sure?</h2><i>Do you want start a new game?</i>",
                        "Message",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if(a != JOptionPane.YES_OPTION)
                {
                    Timer.on();
                    return;
                }
            }
            Sapper.FieldDimension dim = CustomDialog.getDimension(getGameFrame());
            if(dim == Sapper.FieldDimension.nothing)
                return;

            setSizes(dim);
            fillContent();
            setAutoResizer();
            updatePanel();
            timerOn = false;
            mayPause = true;
            isButtonStop = false;

            Sapper.newGame(dim);
            pause.setVisible(false);
            timerCountL.setText("00:00:00");
            repaintFlag();
        });

        Font label12Font = UIDesigner.getFont(15, newGame.getFont(), false);
        if(label12Font != null) newGame.setFont(label12Font);
        stat1Panel.add(newGame);

        replay = new CustomButton("Replay?");
        replay.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        replay.addActionListener(e ->
        {
            Timer.stop();
            if(!Sapper.isGameEnded() && Sapper.isGameStarted())
            {

                int a = JOptionPane.showConfirmDialog(getGameFrame(),
                        "<html><h2>You are sure?</h2><i>Do you want start a new game?</i>",
                        "Message",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if(a != JOptionPane.YES_OPTION)
                {
                    Timer.on();
                    return;
                }
            }
            if(!timerOn && pausePanel != null)
            {
                GridBagConstraints c0 = new GridBagConstraints();
                c0.gridx = 0;
                c0.gridy = 0;
                c0.fill = GridBagConstraints.BOTH;
                c0.anchor = GridBagConstraints.CENTER;

                pause.setText("Pause?");
                remove(pausePanel);
                pausePanel = null;
                add(minePanel, c0);
            }

            for(var b : cells)
            {
                b.setBackground(UIDesigner.BUTTON_BACKGROUND);
                b.setText("");
                b.setIcon(null);
                b.setDisabledIcon(null);
                b.setEnabled(true);
                imageMap.remove(b);
            }

            Sapper.newGame();
            pause.setVisible(false);
            timerCountL.setText("00:00:00");
            timerOn = false;
            mayPause = true;
            isButtonStop = false;

            revalidate();
            repaint();
            repaintFlag();
        });

        label12Font = UIDesigner.getFont(15, replay.getFont(), false);
        if(label12Font != null) replay.setFont(label12Font);
        stat1Panel.add(replay);

        pause = new CustomButton("Pause?");
        pause.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        pause.addActionListener(e ->
        {
            if(Sapper.isGameEnded())
                return;

            setPause();

        });

        label12Font = UIDesigner.getFont(15, pause.getFont(), false);
        if(label12Font != null) pause.setFont(label12Font);
        stat1Panel.add(pause);

        statPanel.add(stat1Panel, BorderLayout.SOUTH);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.EAST;

        add(statPanel, c);
        repaintFlag();

        pause.setVisible(false);
        replay.setVisible(false);
    }

    public void repaintFlag()
    {
        flagCountL.setText(Sapper.getFlagCount() + "/" + Sapper.getMaxFlagCount());
    }

    private CustomPanel createPausePanel()
    {
        CustomPanel blockPanel = new CustomPanel();
        blockPanel.setBackground(Color.GRAY);
        blockPanel.setPreferredSize(minePanel.getSize());
        blockPanel.setMinimumSize(minePanel.getMinimumSize());
        blockPanel.setLayout(new BorderLayout());

        JLabel pause = new JLabel("Pause");
        pause.setForeground(Color.LIGHT_GRAY);
        pause.setHorizontalTextPosition(JLabel.CENTER);
        pause.setHorizontalAlignment(JLabel.CENTER);
        pause.setFont(UIDesigner.getFont(28, pause.getFont(), false));
        pause.addComponentListener(new ComponentAdapter()
        {
            protected void decreaseFontSize(JLabel comp)
            {
                Font font = comp.getFont();
                FontMetrics fm = comp.getFontMetrics(font);
                int width = comp.getWidth();
                int height = comp.getHeight();
                int textWidth = fm.stringWidth(comp.getText());
                int textHeight = fm.getHeight();

                int size = font.getSize();
                while(size > 0 && (textHeight >= height * 0.2 || textWidth > width * 0.2))
                {
                    size -= 2;
                    font = font.deriveFont(font.getStyle(), size);
                    fm = comp.getFontMetrics(font);
                    textWidth = fm.stringWidth(comp.getText());
                    textHeight = fm.getHeight();
                }

                comp.setFont(font);
            }

            protected void increaseFontSize(JLabel comp)
            {
                Font font = comp.getFont();
                FontMetrics fm = comp.getFontMetrics(font);
                int width = comp.getWidth();
                int height = comp.getHeight();
                int textWidth = fm.stringWidth(comp.getText());
                int textHeight = fm.getHeight();

                int size = font.getSize();
                while(textHeight <= height * 0.2 || textWidth <= width * 0.2)
                {
                    size += 2;
                    font = font.deriveFont(font.getStyle(), size);
                    fm = comp.getFontMetrics(font);
                    textWidth = fm.stringWidth(comp.getText());
                    textHeight = fm.getHeight();
                }

                comp.setFont(font);
                decreaseFontSize(comp);
            }

            @Override
            public void componentResized(ComponentEvent e)
            {
                JLabel comp = (JLabel) e.getComponent();
                Font font = comp.getFont();
                FontMetrics fm = comp.getFontMetrics(font);
                int width = comp.getWidth();
                int height = comp.getHeight();
                int textWidth = fm.stringWidth(comp.getText());
                int textHeight = fm.getHeight();

                if(textHeight > height || textWidth > width)
                    decreaseFontSize(comp);
                else
                    increaseFontSize(comp);
            }
        });
        blockPanel.add(pause, BorderLayout.CENTER);

        return blockPanel;
    }

    private void turnOnTimer()
    {
        timerOn = true;
        Timer.start(this::repaintTimer);
    }

    private void repaintTimer()
    {
        timerCountL.setText(makeTimeString(Timer.getSeconds() / 3600) + ":" + makeTimeString((Timer.getSeconds() % 3600) / 60) + ":" + makeTimeString(Timer.getSeconds() % 60));
    }

    public void setNumCell(int x, int y, int num)
    {
        CustomButton btn = cells[y * col + x];
        Color col = switch(num)
                {
                    case 1 -> new Color(129, 255, 135, 255);
                    case 2 -> new Color(255, 240, 151, 255);
                    case 3 -> new Color(255, 184, 113, 255);
                    case 4 -> new Color(255, 134, 86, 255);
                    case 5 -> new Color(200, 92, 91, 255);
                    case 6 -> new Color(178, 75, 94, 255);
                    case 7 -> new Color(195, 44, 57, 255);
                    case 8 -> new Color(195, 2, 0, 255);
                    default -> Color.WHITE;
                };
        btn.setBackground(col);
        btn.setText("" + num);
        btn.setEnabled(false);
    }

    public void markCell(int x, int y)
    {
        CustomButton btn = cells[y * col + x];
        btn.setIcon(new ImageIcon(flag.getScaledInstance((int) (btn.getWidth() - btn.getWidth() * 0.15), (int) (btn.getHeight() - btn.getHeight() * 0.15), Image.SCALE_AREA_AVERAGING)));
        imageMap.put(btn, flag);
    }

    public void markMaybeCell(int x, int y)
    {
        CustomButton btn = cells[y * col + x];
        btn.setIcon(new ImageIcon(flagMaybe.getScaledInstance((int) (btn.getWidth() - btn.getWidth() * 0.15), (int) (btn.getHeight() - btn.getHeight() * 0.15), Image.SCALE_AREA_AVERAGING)));
        imageMap.put(btn, flagMaybe);
    }

    public void offMarkOnCell(int x, int y)
    {
        CustomButton btn = cells[y * col + x];
        btn.setIcon(null);
        imageMap.remove(btn);
    }

    public void freeCell(int x, int y)
    {
        CustomButton btn = cells[y * col + x];
        btn.setBackground(Color.WHITE);
        btn.setEnabled(false);
    }

    public void onPause()
    {
        if(!timerOn)
            return;
        GridBagConstraints c0 = new GridBagConstraints();
        c0.gridx = 0;
        c0.gridy = 0;
        c0.fill = GridBagConstraints.BOTH;
        c0.anchor = GridBagConstraints.CENTER;

        remove(minePanel);
        pausePanel = createPausePanel();
        add(pausePanel, c0);
        revalidate();
        repaint();
        pause.setText("Run?");
        timerOn = false;
        Timer.stop();
        for(var a : cells)
            a.setEnabled(false);
    }

    public void offPause()
    {
        if(timerOn || isButtonStop)
            return;

        GridBagConstraints c0 = new GridBagConstraints();
        c0.gridx = 0;
        c0.gridy = 0;
        c0.fill = GridBagConstraints.BOTH;
        c0.anchor = GridBagConstraints.CENTER;

        pause.setText("Pause?");
        remove(pausePanel);
        pausePanel = null;
        add(minePanel, c0);
        revalidate();
        repaint();

        timerOn = true;
        Timer.on();
        for(var a : cells)
            if(a.getBackground() == UIDesigner.BUTTON_BACKGROUND)
            {
                a.setEnabled(true);
            }
    }

    private void setPause()
    {
        GridBagConstraints c0 = new GridBagConstraints();
        c0.gridx = 0;
        c0.gridy = 0;
        c0.fill = GridBagConstraints.BOTH;
        c0.anchor = GridBagConstraints.CENTER;

        if(timerOn)
        {
            remove(minePanel);
            pausePanel = createPausePanel();
            add(pausePanel, c0);
            revalidate();
            repaint();
            pause.setText("Run?");
            isButtonStop = true;
            timerOn = false;
            Timer.stop();
            for(var a : cells)
                a.setEnabled(false);
        } else
        {
            pause.setText("Pause?");
            remove(pausePanel);
            pausePanel = null;
            add(minePanel, c0);
            revalidate();
            repaint();

            timerOn = true;
            isButtonStop = false;
            Timer.on();
            for(var a : cells)
                if(a.getBackground() == UIDesigner.BUTTON_BACKGROUND)
                    a.setEnabled(true);
        }
    }

    public void disableGame(byte[][] map)
    {
        Timer.stop();
        mayPause = false;

        for(int x = 0; x < map.length; x++)
            for(int y = 0; y < map[x].length; y++)
            {
                CustomButton btn = cells[y * col + x];
                if(Sapper.isMaybeMarked(x, y))
                {
                    if(Sapper.isMined(x, y))
                    {
                        btn.setIcon(new ImageIcon(goodFlagMaybe.getScaledInstance((int) (btn.getWidth() - btn.getWidth() * 0.15), (int) (btn.getHeight() - btn.getHeight() * 0.15), Image.SCALE_AREA_AVERAGING)));
                        imageMap.put(btn, goodFlagMaybe);
                    } else
                    {
                        btn.setIcon(new ImageIcon(badFlagMaybe.getScaledInstance((int) (btn.getWidth() - btn.getWidth() * 0.15), (int) (btn.getHeight() - btn.getHeight() * 0.15), Image.SCALE_AREA_AVERAGING)));
                        imageMap.put(btn, badFlagMaybe);
                    }
                    btn.setDisabledIcon(btn.getIcon());
                } else if(Sapper.isMarked(x, y))
                {
                    if(Sapper.isMined(x, y))
                    {
                        btn.setIcon(new ImageIcon(goodFlag.getScaledInstance((int) (btn.getWidth() - btn.getWidth() * 0.15), (int) (btn.getHeight() - btn.getHeight() * 0.15), Image.SCALE_AREA_AVERAGING)));
                        imageMap.put(btn, goodFlag);
                    } else
                    {
                        btn.setIcon(new ImageIcon(badFlag.getScaledInstance((int) (btn.getWidth() - btn.getWidth() * 0.15), (int) (btn.getHeight() - btn.getHeight() * 0.15), Image.SCALE_AREA_AVERAGING)));
                        imageMap.put(btn, badFlag);
                    }
                    btn.setDisabledIcon(btn.getIcon());
                } else if(Sapper.isMined(x, y))
                {
                    btn.setIcon(new ImageIcon(bomb.getScaledInstance((int) (btn.getWidth() - btn.getWidth() * 0.15), (int) (btn.getHeight() - btn.getHeight() * 0.15), Image.SCALE_AREA_AVERAGING)));
                    imageMap.put(btn, bomb);
                }
            }

        for(var a : cells)
            a.setEnabled(false);
        pause.setVisible(false);
    }

    public boolean canMakePause()
    {
        return mayPause;
    }
}