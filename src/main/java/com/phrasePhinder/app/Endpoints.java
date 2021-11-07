package com.phrasePhinder.app;

import org.postgresql.util.PSQLException;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Map;
@CrossOrigin(origins = {"http://phrasephinder.com","http://www.phrasephinder.com","http://localhost","https://phrasephinder.com","https://www.phrasephinder.com","192.168.1.10"})
@RestController
public class Endpoints {
    private static final Map<String, String> env = System.getenv();

    @GetMapping("/api/series")
    public ArrayList<String> getShows() {
        return getListFromPostgres("series");
    }

    @GetMapping("/api/movie")
    public ArrayList<String> getMovies() {
        return getListFromPostgres("movie");
    }

    @GetMapping("/api/comedian")
    public ArrayList<String> getComedian() {
        return getListFromPostgres("comedian");
    }
    @GetMapping("/api/shows")
    public ArrayList<String> getAllShows(){
        return getAllShowsFromPostgres();
    }

    @PostMapping("/api/search")
    public ArrayList<ShowOccurrence> getOccurrences(@RequestParam String show, @RequestParam String phrase) {
        if(Trie.trieShows.containsKey(show)){
            return getOccurrencesFromTrie(show, phrase);
        }
        return getOccurrencesFromPostgres(show, phrase);
    }

    @PostMapping("/api/didYouMean")
    public ArrayList<String> getDidYouMean(@RequestParam String show, @RequestParam String phrase){
        phrase = phrase.toLowerCase().replaceAll("\\[.*?\\]", " ").
                replaceAll("[^a-z A-Z 0-9]", "").replaceAll(" +", " ").trim();

        String[] phraseSplit = phrase.split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        int i = phraseSplit.length > 5 ? phraseSplit.length - 5 : 0;
        int prefixLength = i - 1;
        while(i < phraseSplit.length){
            stringBuilder.append(phraseSplit[i]);
            stringBuilder.append(" ");
            i++;
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        String shortenedPhrase = stringBuilder.toString();

        StringBuilder prefix = new StringBuilder();
        for(int j = 0; j < prefixLength; j++){
            prefix.append(phraseSplit[j]);
            prefix.append(" ");
        }
        System.out.println("show: " + show + "phrase: " + shortenedPhrase + " prefix: " + prefix.toString());
        return Trie.didYouMean(show,shortenedPhrase,prefix.toString());
    }

    private ArrayList<ShowOccurrence> getOccurrencesFromTrie(String show, String phrase){
        phrase = phrase.toLowerCase().replaceAll("\\[.*?\\]", " ").
                replaceAll("[^a-z A-Z 0-9]", "").replaceAll(" +", " ").trim();

        String[] phraseSplit = phrase.split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        int i = phraseSplit.length > 5 ? phraseSplit.length - 5 : 0;
        while(i < phraseSplit.length){
            stringBuilder.append(phraseSplit[i]);
            stringBuilder.append(" ");
            i++;
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        phrase = stringBuilder.toString();
        ArrayList<ShowOccurrenceToIndex> showOccurrenceToIndices = Trie.search(show,phrase);
        ArrayList<ShowOccurrence> showOccurrences = new ArrayList<ShowOccurrence>();

        for(ShowOccurrenceToIndex showOccurrenceToIndex: showOccurrenceToIndices){
            Map<String,String> map = Trie.episodeMap.get(showOccurrenceToIndex.episodeIndex);
            ShowOccurrence newShowOccurrence = new ShowOccurrence(String.valueOf(showOccurrenceToIndex.startTime),String.valueOf(showOccurrenceToIndex.endTime),
                    map.get("seasonNum"),map.get("episodeNum"),map.get("episodeName"));
            showOccurrences.add(newShowOccurrence);
        }
        return showOccurrences;
    }

    private ArrayList<ShowOccurrence> getOccurrencesFromPostgres(String show, String phrase) {
        show = show.toLowerCase();
        phrase = phrase.toLowerCase().replaceAll("\\[.*?\\]", " ").
                replaceAll("[^a-z A-Z 0-9]", "").replaceAll(" +", " ").trim();

        Connection connection = null;
        PreparedStatement statement = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager
                    .getConnection("jdbc:postgresql://" + env.get("POSTGRES_IP") + ":" + env.get("POSTGRES_PORT") +"/phrasephinder",
                            env.get("POSTGRES_USER"), env.get("POSTGRES_PASSWORD"));
            System.out.println("Opened database successfully");

            String sql = String.format("SELECT * FROM %s Where phrase Like ? ORDER BY seasonnum ASC, episodenum ASC;",show);
            statement = connection.prepareStatement(sql);
            statement.setString(1,"%" + phrase + "%");
            ResultSet result = null;
            ArrayList<ShowOccurrence> resultArray = new ArrayList<>();
            try {
                result = statement.executeQuery();
            } catch (PSQLException ex) {
                System.out.println(ex.getMessage());
            }
            while (result.next()) {
                ShowOccurrence occurrence = new ShowOccurrence(
                        result.getString("starttime"),
                        result.getString("endtime"),
                        result.getString("seasonnum"),
                        result.getString("episodenum"),
                        result.getString("episodename")
                );
                resultArray.add(occurrence);
            }
            statement.close();
            connection.close();
            return resultArray;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ArrayList<>();

    }

    public static ArrayList<String> getListFromPostgres(String type) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            ArrayList<String> series = new ArrayList<>();
            Class.forName("org.postgresql.Driver");
            connection = DriverManager
                    .getConnection("jdbc:postgresql://" + env.get("POSTGRES_IP") + ":" + env.get("POSTGRES_PORT") +"/phrasephinder",
                            env.get("POSTGRES_USER"), env.get("POSTGRES_PASSWORD"));
            System.out.println("Opened database successfully");

            String sql = "SELECT name FROM shows Where category = ? ORDER BY name ASC;";
            statement = connection.prepareStatement(sql);
            statement.setString(1,type);
            ResultSet result = null;
            ArrayList<String> resultArray = new ArrayList<>();
            try {
                result = statement.executeQuery();
            } catch (PSQLException ex) {
                System.out.println(ex.getMessage());
            }
            while (result.next()) {
                resultArray.add(result.getString("name"));
            }
            statement.close();
            connection.close();
            return resultArray;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ArrayList<>();
    }
    public static ArrayList<String> getAllShowsFromPostgres(){
        Connection connection = null;
        Statement statement = null;
        try {
            ArrayList<String> series = new ArrayList<>();
            Class.forName("org.postgresql.Driver");
            connection = DriverManager
                    .getConnection("jdbc:postgresql://" + env.get("POSTGRES_IP") + ":" + env.get("POSTGRES_PORT") +"/phrasephinder",
                            env.get("POSTGRES_USER"), env.get("POSTGRES_PASSWORD"));
            System.out.println("Opened database successfully");

            statement = connection.createStatement();
            ResultSet result = null;
            String sql = "SELECT * FROM shows;";
            ArrayList<String> resultArray = new ArrayList<>();
            try {
                result = statement.executeQuery(sql);
            } catch (PSQLException ex) {
                System.out.println(ex.getMessage());
            }
            while (result.next()) {
                resultArray.add(result.getString("name"));
            }
            statement.close();
            connection.close();
            return resultArray;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ArrayList<>();
    }
}
