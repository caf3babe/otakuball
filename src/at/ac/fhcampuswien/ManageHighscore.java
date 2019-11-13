package at.ac.fhcampuswien;

public class ManageHighscore {
    int actualHighscore;
    Mapper mapper;
    public ManageHighscore(){

        mapper = new Mapper();
    }
    //gets the highscore of the player and adds the level points to save them
    public void handleHighscore(int lives){
        int points;
        actualHighscore = Menu.player.getHighscore();
        points = lives*10;
        Menu.player.setHighscore(points+ actualHighscore);
        mapper.save(Menu.player);
    }
}
