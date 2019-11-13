package at.ac.fhcampuswien;

public class Player implements Comparable {
    private String name;
    private int highscore;

    public Player() {

    }

    public Player(String name, int highscore) {
        this.name = name;
        this.highscore = highscore;
    }

    public String getName() {
        return this.name;
    }

    public int getHighscore() {
        return this.highscore;
    }

    public void setHighscore(int highscore) {
        this.highscore = highscore;
    }
    //overrided the toString method for the save method in mapper
    @Override
    public String toString() {
        return name + ":" + highscore + ";";
    }
    //overrided compareTo to sort the Players descending by their highscores
    @Override
    public int compareTo(Object o) {
        int compareInt = ((Player) o).getHighscore();
        return compareInt - this.highscore;
    }


}