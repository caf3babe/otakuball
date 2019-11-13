package at.ac.fhcampuswien;

import java.awt.*;

public class Main {

    public static Menu m;
    public static final int FRAMEWIDTH = 1920;
    public static final int FRAMEHEIGHT = 1080;
    public static Font font;
    private static final int TOTAL_LEVEL_NUMBER = 6;

    //Font mangat.ttf loaded in Fontloader, set here as default Font, create new Menu **********************************
    public static void main(String[] args) {
        FontLoader fontLoader = new FontLoader();
        font = new Font("Manga Temple", Font.PLAIN, 24);
        m = new Menu();
    }

    //needed in Class Playground to check actual Level, to restart level and more **************************************
    public static int getTotalLevelNumber(){
        return TOTAL_LEVEL_NUMBER;
    }
}