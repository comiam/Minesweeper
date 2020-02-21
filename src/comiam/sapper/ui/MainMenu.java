package comiam.sapper.ui;

import comiam.sapper.game.Sapper;
import comiam.sapper.ui.components.CustomButton;
import comiam.sapper.ui.components.CustomDialog;
import comiam.sapper.ui.components.UIDesigner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import static comiam.sapper.ui.components.UIDesigner.DEFAULT_BACKGROUND;

public class MainMenu extends JFrame
{
    private static JFrame mainFrame;
    private JPanel mainPanel;
    private static GameFrame gamePanel;

    public MainMenu()
    {
        if(mainFrame != null)
            return;

        setupUI();
        setContentPane(getRootComponent());
        setSize(180, 240);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame = this;
    }

    private void setupUI()
    {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(DEFAULT_BACKGROUND);

        CustomButton newGameButton = new CustomButton();
        newGameButton.setText("New game");
        newGameButton.addActionListener(e -> {
            Sapper.FieldDimension dim = CustomDialog.getDimension(mainFrame);
            if(dim == Sapper.FieldDimension.nothing)
                return;

            openGameFrame(dim);
        });

        Font label12Font = UIDesigner.getFont(15, newGameButton.getFont(), false);
        if(label12Font != null) newGameButton.setFont(label12Font);

        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(newGameButton, gbc);

        CustomButton highScoresButton = new CustomButton();
        highScoresButton.setText("High Scores");
        label12Font = UIDesigner.getFont(15, newGameButton.getFont(), false);
        if(label12Font != null) highScoresButton.setFont(label12Font);
        highScoresButton.addActionListener((e) -> ScoresFrame.showRecords());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(highScoresButton, gbc);

        CustomButton aboutButton = new CustomButton();
        aboutButton.setText("About");
        aboutButton.addActionListener((e) -> AboutFrame.showAbout());
        label12Font = UIDesigner.getFont(15, aboutButton.getFont(), false);
        if(label12Font != null) aboutButton.setFont(label12Font);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(aboutButton, gbc);

        CustomButton exitButton = new CustomButton();
        exitButton.setText("Exit");
        label12Font = UIDesigner.getFont(15, exitButton.getFont(), false);
        if(label12Font != null) exitButton.setFont(label12Font);
        exitButton.addActionListener(e -> System.exit(0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(exitButton, gbc);

        final JLabel label1 = new JLabel();
        Font label1Font = UIDesigner.getFont(22, label1.getFont(), false);
        if(label1Font != null) label1.setFont(label1Font);
        label1.setHorizontalAlignment(0);
        label1.setHorizontalTextPosition(0);
        label1.setText("Minesweeper");
        label1.setVerticalAlignment(1);
        label1.setVerticalTextPosition(1);
        label1.setForeground(Color.LIGHT_GRAY);

        mainPanel.add(label1);

        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.01;
        spacer1.setBackground(DEFAULT_BACKGROUND);
        mainPanel.add(spacer1, gbc);
    }

    private void openGameFrame(Sapper.FieldDimension dim)
    {
        Sapper.newGame(dim);

        gamePanel = new GameFrame(dim);
        mainFrame.setContentPane(gamePanel);
        mainFrame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowFocusListener(new WindowFocusListener()
        {
            @Override
            public void windowGainedFocus(WindowEvent e)
            {
                if(Sapper.isGameStarted() && getGameFrame().canMakePause())
                    getGameFrame().offPause();
            }

            @Override
            public void windowLostFocus(WindowEvent e)
            {
                if(Sapper.isGameStarted() && getGameFrame().canMakePause())
                    getGameFrame().onPause();
            }
        });
        
        mainFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                if(Sapper.isGameStarted())
                {
                    int a = JOptionPane.showConfirmDialog(MainMenu.getGameFrame(),
                            "<html><h2>You are sure?</h2><i>Do you want close the game?</i>",
                            "Message",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if(a != JOptionPane.YES_OPTION)
                        return;
                }
                System.exit(0);
            }
        });
        updatePanel();
    }

    public static void setToMinimum()
    {
        mainFrame.setMinimumSize(new Dimension(100, 100));
    }

    public static void updatePanel()
    {
        mainFrame.revalidate();
        mainFrame.pack();
        mainFrame.setMinimumSize(mainFrame.getSize());
        mainFrame.setResizable(true);
        mainFrame.setLocationRelativeTo(null);
    }

    public static GameFrame getGameFrame()
    {
        return gamePanel;
    }

    private JComponent getRootComponent()
    {
        return mainPanel;
    }
}
