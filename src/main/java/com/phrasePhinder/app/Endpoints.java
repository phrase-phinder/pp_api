package com.phrasePhinder.app;

import org.postgresql.util.PSQLException;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Map;
@CrossOrigin(origins = {"http://phrasephinder.com","http://www.phrasephinder.com","http://localhost"})
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

    @PostMapping("/api/search")
    public ArrayList<ShowOccurrence> getOccurrences(@RequestParam String show, @RequestParam String phrase) {
        return getOccurrencesFromPostgres(show, phrase);
    }

    private ArrayList<ShowOccurrence> getOccurrencesFromPostgres(String show, String phrase) {
        show = show.toLowerCase();
        phrase = phrase.toLowerCase().replaceAll("\\[.*?\\]", " ").
                replaceAll("[^a-z A-Z 0-9]", "").replaceAll(" +", " ").trim();

        Connection connection = null;
        Statement statement = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager
                    .getConnection("jdbc:postgresql://" + env.get("POSTGRES_IP") + ":5432/phrasephinder",
                            env.get("POSTGRES_USER"), env.get("POSTGRES_PASSWORD"));
            System.out.println("Opened database successfully");

            statement = connection.createStatement();
            String sql = String.format("SELECT * FROM %s Where phrase Like '%%%s%%' ORDER BY seasonnum ASC, episodenum ASC, startime Asc;", show, phrase);

            ResultSet result = null;
            ArrayList<ShowOccurrence> resultArray = new ArrayList<>();
            try {
                result = statement.executeQuery(sql);
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
        Statement statement = null;
        try {
            ArrayList<String> series = new ArrayList<>();
            Class.forName("org.postgresql.Driver");
            connection = DriverManager
                    .getConnection("jdbc:postgresql://" + env.get("POSTGRES_IP") + ":5432/phrasephinder",
                            env.get("POSTGRES_USER"), env.get("POSTGRES_PASSWORD"));
            System.out.println("Opened database successfully");

            statement = connection.createStatement();
            String sql = String.format("SELECT name FROM shows Where category = '%s' ORDER BY name ASC;", type);
            ResultSet result = null;
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

    private static ArrayList getArrayList(Statement statement, String sql) throws SQLException {
        ResultSet result = null;
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
        return resultArray;
    }
}
