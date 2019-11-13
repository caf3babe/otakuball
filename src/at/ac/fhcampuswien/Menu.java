package at.ac.fhcampuswien;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static at.ac.fhcampuswien.Main.*;


// Frame creates world, set scale, rendering is happened - we use "initiliazeworld *************************************
public class Menu extends Frame {

    public Menu() {
        super("Otakuball", 128.0);
    }

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private Playground playground;
    private Highscore highscore;
    private JButton resumeGame;
    private int highscoreClickCounter = 0;
    private JTextField nameInput;
    public static Player player;
    private JLabel name1, top1, name2, top2, name3, top3, name4, top4, name5, top5, name6, top6, name7, top7, highscoreLabel;


    // Override method from Frame **************************************************************************************
    @Override
    protected void initializeWorld() {

        JButton playgame, highscore, exitgame;
        setLocationRelativeTo(null);
        setContentPane(new JLabel(new ImageIcon(this.getClass().getClassLoader().getResource("images/background_menu_new.png"))));

        nameInput = new JTextField("your name");
        nameInput.selectAll();
        nameInput.setToolTipText("Enter your Name to play.");
        nameInput.setEditable(true);
        nameInput.setFont(font);
        nameInput.setBounds(WIDTH / 4 - 130, HEIGHT / 4 + 50, 260, 45);
        nameInput.setHorizontalAlignment(JTextField.CENTER);
        add(nameInput);


        // Buttons in Main Menu ****************************************************************************************
        int buttonWidth = 300;
        int buttonHeigth = 71;
        playgame = new JButton();
        playgame.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("images/start_transparent.png")));
        playgame.setBounds(WIDTH / 4 - buttonWidth / 2, HEIGHT / 2 - buttonHeigth / 2, buttonWidth, buttonHeigth);
        playgame.setBorderPainted(false);
        playgame.setContentAreaFilled(false);
        playgame.setFocusPainted(false);
        playgame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pressToPlay();
            }
        });
        add(playgame);

        highscore = new JButton();
        highscore.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("images/highscore_transparent2.png")));
        highscore.setBounds(WIDTH / 4 - buttonWidth / 2, HEIGHT / 2 - buttonHeigth / 2 + 100, buttonWidth, buttonHeigth);
        highscore.setContentAreaFilled(false);
        highscore.setBorderPainted(false);
        highscore.setFocusPainted(false);
        highscore.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scoretable();
            }
        });
        add(highscore);

        exitgame = new JButton();
        exitgame.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("images/exit_transparent.png")));
        exitgame.setBounds(WIDTH / 4 - buttonWidth / 2, HEIGHT / 2 - buttonHeigth / 2 + 200, buttonWidth, buttonHeigth);
        exitgame.setContentAreaFilled(false);
        exitgame.setBorderPainted(false);
        exitgame.setFocusPainted(false);
        exitgame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        add(exitgame);


        // Highscore entries, font and bounds **************************************************************************
        highscoreLabel = new JLabel("");
        name1 = new JLabel("");
        top1 = new JLabel("");
        name2 = new JLabel("");
        top2 = new JLabel("");
        name3 = new JLabel("");
        top3 = new JLabel("");
        name4 = new JLabel("");
        top4 = new JLabel("");
        name5 = new JLabel("");
        top5 = new JLabel("");
        name6 = new JLabel("");
        top6 = new JLabel("");
        name7 = new JLabel("");
        top7 = new JLabel("");


        name1.setBounds(WIDTH / 2 + 110, 200, 200, 100);
        top1.setBounds(WIDTH / 2 + WIDTH / 4 + 40, 200, 200, 100);
        name2.setBounds(WIDTH / 2 + 110, 300, 200, 100);
        top2.setBounds(WIDTH / 2 + WIDTH / 4 + 40, 300, 200, 100);
        name3.setBounds(WIDTH / 2 + 110, 400, 200, 100);
        top3.setBounds(WIDTH / 2 + WIDTH / 4 + 40, 400, 200, 100);
        name4.setBounds(WIDTH / 2 + 110, 500, 200, 100);
        top4.setBounds(WIDTH / 2 + WIDTH / 4 + 40, 500, 200, 100);
        name5.setBounds(WIDTH / 2 + 110, 600, 200, 100);
        top5.setBounds(WIDTH / 2 + WIDTH / 4 + 40, 600, 200, 100);
        name6.setBounds(WIDTH / 2 + 110, 700, 200, 100);
        top6.setBounds(WIDTH / 2 + WIDTH / 4 + 40, 700, 200, 100);
        name7.setBounds(WIDTH / 2 + 110, 800, 200, 100);
        top7.setBounds(WIDTH / 2 + WIDTH / 4 + 40, 800, 200, 100);
        highscoreLabel.setBounds(WIDTH / 2 + 260, 60, 200, 100);

        name1.setFont(font);
        name1.setFont(name1.getFont().deriveFont(32.0f));
        top1.setFont(font);
        top1.setFont(name1.getFont().deriveFont(32.0f));
        name2.setFont(font);
        name2.setFont(name1.getFont().deriveFont(32.0f));
        top2.setFont(font);
        top2.setFont(name1.getFont().deriveFont(32.0f));
        name3.setFont(font);
        name3.setFont(name1.getFont().deriveFont(32.0f));
        top3.setFont(font);
        top3.setFont(name1.getFont().deriveFont(32.0f));
        name4.setFont(font);
        name4.setFont(name1.getFont().deriveFont(32.0f));
        top4.setFont(font);
        top4.setFont(name1.getFont().deriveFont(32.0f));
        name5.setFont(font);
        name5.setFont(name1.getFont().deriveFont(32.0f));
        top5.setFont(font);
        top5.setFont(name1.getFont().deriveFont(32.0f));
        name6.setFont(font);
        name6.setFont(name1.getFont().deriveFont(32.0f));
        top6.setFont(font);
        top6.setFont(name1.getFont().deriveFont(32.0f));
        name7.setFont(font);
        name7.setFont(name1.getFont().deriveFont(32.0f));
        top7.setFont(font);
        top7.setFont(name1.getFont().deriveFont(32.0f));
        highscoreLabel.setFont(font);
        highscoreLabel.setFont(name1.getFont().deriveFont(50.0f));

        add(name1);
        add(top1);
        add(name2);
        add(top2);
        add(name3);
        add(top3);
        add(name4);
        add(top4);
        add(name5);
        add(top5);
        add(name6);
        add(top6);
        add(name7);
        add(top7);
        add(highscoreLabel);

        name1.setForeground(Color.WHITE);
        top1.setForeground(Color.WHITE);
        name2.setForeground(Color.WHITE);
        top2.setForeground(Color.WHITE);
        name3.setForeground(Color.WHITE);
        top3.setForeground(Color.WHITE);
        name4.setForeground(Color.WHITE);
        top4.setForeground(Color.WHITE);
        name5.setForeground(Color.WHITE);
        top5.setForeground(Color.WHITE);
        name6.setForeground(Color.WHITE);
        top6.setForeground(Color.WHITE);
        name7.setForeground(Color.WHITE);
        top7.setForeground(Color.WHITE);
        highscoreLabel.setForeground(Color.WHITE);

        setVisible(true);
    }


    // Action by clicking "Start"-Button, Highscore disable ************************************************************
    public void pressToPlay() {
        if (nameInput.getText().equals("") || nameInput.getText().equals("your name")) {
            JOptionPane.showMessageDialog(null, "Please enter your name to play game!");
        } else {
            player = new Player(nameInput.getText(), 0);
            playground = new Playground();
            this.dispose();
            playground.run();
        }
        if (highscore != null) {
            if (highscore.isVisible()) {
                name1.setText("");
                top1.setText("");
                name2.setText("");
                top2.setText("");
                name3.setText("");
                top3.setText("");
                name4.setText("");
                top4.setText("");
                name5.setText("");
                top5.setText("");
                name6.setText("");
                top6.setText("");
                name7.setText("");
                top7.setText("");
                highscoreLabel.setText("");
                highscore.setVisible(false);
            }
        }
    }


    // Visible and disable Highscore with Modulo ***********************************************************************
    public void scoretable() {

        if (highscoreClickCounter % 2 == 0) {
            highscore = new Highscore(this, player);
            highscore.run();
            int topPlayerCount = highscore.listOfPlayer.size();

            if (topPlayerCount >= 1) {
                name1.setText(highscore.listOfPlayer.get(0).getName());
                top1.setText(String.valueOf(highscore.listOfPlayer.get(0).getHighscore()));
            }
            if (topPlayerCount >= 2) {
                name2.setText(highscore.listOfPlayer.get(1).getName());
                top2.setText(String.valueOf(highscore.listOfPlayer.get(1).getHighscore()));
            }
            if (topPlayerCount >= 3) {
                name3.setText(highscore.listOfPlayer.get(2).getName());
                top3.setText(String.valueOf(highscore.listOfPlayer.get(2).getHighscore()));
            }
            if (topPlayerCount >= 4) {
                name4.setText(highscore.listOfPlayer.get(3).getName());
                top4.setText(String.valueOf(highscore.listOfPlayer.get(3).getHighscore()));
            }
            if (topPlayerCount >= 5) {
                name5.setText(highscore.listOfPlayer.get(4).getName());
                top5.setText(String.valueOf(highscore.listOfPlayer.get(4).getHighscore()));
            }
            if (topPlayerCount >= 6) {
                name6.setText(highscore.listOfPlayer.get(5).getName());
                top6.setText(String.valueOf(highscore.listOfPlayer.get(5).getHighscore()));
            }
            if (topPlayerCount >= 7) {
                name7.setText(highscore.listOfPlayer.get(6).getName());
                top7.setText(String.valueOf(highscore.listOfPlayer.get(6).getHighscore()));
            }
            highscoreLabel.setText("TOP 7");
            highscoreClickCounter++;

        } else if (highscoreClickCounter % 2 >= 1) {
            highscore.setVisible(false);
            name1.setText("");
            top1.setText("");
            name2.setText("");
            top2.setText("");
            name3.setText("");
            top3.setText("");
            name4.setText("");
            top4.setText("");
            name5.setText("");
            top5.setText("");
            name6.setText("");
            top6.setText("");
            name7.setText("");
            top7.setText("");
            highscoreLabel.setText("");
            highscoreClickCounter++;
        }
    }

    
    public void createResumeButton() {

        int buttonWidth = 300;
        int buttonHeigth = 71;
        resumeGame = new JButton();
        resumeGame.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("images/resume_transparent.png")));
        resumeGame.setBounds(WIDTH / 4 - buttonWidth / 2, HEIGHT / 2 - buttonHeigth / 2 - 100, buttonWidth, buttonHeigth);
        resumeGame.setBorderPainted(false);
        resumeGame.setContentAreaFilled(false);
        resumeGame.setFocusPainted(false);
        resumeGame.addActionListener(e -> pressToResume());
        add(resumeGame);
    }


    public void pressToResume() {
        if (playground instanceof Playground) {
            if (playground.isPaused()) {
                this.dispose();
                playground.resume();
                playground.setVisible(true);
                if (playground.getLevel().getThemeSong() != null && !playground.isDrawExplosionImageTrue()) {
                    playground.getLevel().getThemeSong().startPlaying();
                }
                if (playground.getLevel().getWonSound() != null && playground.isDrawExplosionImageTrue()) {
                    playground.getLevel().getWonSound().startPlaying();
                }
            }
        }
    }


    public void removeResumeButton() {
        if (resumeGame instanceof JButton) {
            remove(resumeGame); //in Playground disabled when all Level passed
        }
    }


    public Playground getPlayground() {
        return this.playground;
    }
}