package at.ac.fhcampuswien;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Mapper {
    ArrayList<Player> listOfPlayers;
    public Mapper(){

    }
    //Checks and saves the new highscore of the player
    public void save(Player player){
        listOfPlayers = load();
        File file = null;
        FileOutputStream fileout = null;
        ArrayList<Player> tmpListOfPlayers;

        try {
            Scanner s = new Scanner(new FileInputStream("highscore.txt"));
            tmpListOfPlayers = new ArrayList<Player>();
            file = new File("highscore.txt");

            if (!file.exists()) {
                file.createNewFile();
            }
            fileout = new FileOutputStream(file);

            //checks Duplicates and sorts entries
            tmpListOfPlayers = checkDuplicates(player,tmpListOfPlayers);
            Collections.sort(tmpListOfPlayers);

            //saves the new Arraylist of Players
            fileout.write("".getBytes());
            for (int i = 0; i < tmpListOfPlayers.size() ; i++) {
                String textToSave = tmpListOfPlayers.get(i).toString().toLowerCase();
                byte[] textToSaveBytes = textToSave.getBytes();
                fileout.write(textToSaveBytes);
            }
            fileout.close();


        } catch (FileNotFoundException e) {
            System.out.println("Datei wurde nicht gefunden");
            e.getMessage();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Loads and splits the input in a Array List of Players
    public ArrayList<Player> load(){
        ArrayList<Player> readList = new ArrayList<Player>();
        try {
            Scanner s = new Scanner(new FileInputStream("highscore.txt"));
            s.useDelimiter(";");
            String tmp= "";
            while (s.hasNext()) {
                tmp += s.next()+":";
                String[] tmpArray= tmp.split(":");
                readList.add(new Player(tmpArray[0],Integer.valueOf(tmpArray[1])));
                tmp = "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readList;
    }
    //Checks for duplicates and adds the new player
    public ArrayList<Player> checkDuplicates(Player player, ArrayList<Player> tmpListOfPlayers){
        boolean isPlayerInList = false;
        for(int i = 0; i<listOfPlayers.size(); i++)
        {
            //the current Player is been updated
            if(listOfPlayers.get(i).getName().contains(player.getName().toLowerCase())&& !isPlayerInList){
                if(listOfPlayers.get(i).getHighscore()<player.getHighscore())
                {
                    tmpListOfPlayers.add(player);
                    isPlayerInList = true;
                }
                else {
                    tmpListOfPlayers.add(listOfPlayers.get(i));
                    isPlayerInList = true;
                }
            }
            //The players that are not the same as the current player is added to the tmplist
            else if(!listOfPlayers.get(i).getName().contains(player.getName().toLowerCase()))
            {
                tmpListOfPlayers.add(listOfPlayers.get(i));
            }
        }
        //If the current player isnt in the new tmplist at all the new player will be added
        for(int j= 0; j<tmpListOfPlayers.size();j++)
        {
            if(!tmpListOfPlayers.get(j).getName().contains(player.getName().toLowerCase())&& !isPlayerInList)
            {
                tmpListOfPlayers.add(player);
                isPlayerInList = true;
            }
        }
        return tmpListOfPlayers;
    }
}
