package at.ac.fhcampuswien;

import javax.swing.*;

import java.util.ArrayList;

import static at.ac.fhcampuswien.Main.*;

public class Highscore extends JInternalFrame {

    private static final int IFRAMEWIDTH = FRAMEWIDTH / 2 - 80;
    private static final int IFRAMEHEIGHT = FRAMEHEIGHT - 40;

    private JFrame frame;
    ArrayList<Player> listOfPlayer;
    Mapper mapper;


    public Highscore(JFrame frame, Player player) {
        this.frame = frame;
        Player player1 = player;
        mapper = new Mapper();
        close();
    }

    //sets the settings for the highscoreboard and loads the list of players of the file
    public void run() {
        setSize(IFRAMEWIDTH, IFRAMEHEIGHT);
        setLocation(FRAMEWIDTH / 2 + ((FRAMEWIDTH / 2 - IFRAMEHEIGHT) / 2), 20);
        setVisible(true);
        frame.add(this);
        setContentPane(new JLabel(new ImageIcon(this.getClass().getClassLoader().getResource("images/planke5.jpg"))));
        listOfPlayer = mapper.load();
        setFrameIcon(null);
        setBorder(null);
    }

    public void close() {
        try {
            setClosed(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void open() {

        try {
            setClosed(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}