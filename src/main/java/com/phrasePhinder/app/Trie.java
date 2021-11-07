package com.phrasePhinder.app;

import com.phrasePhinder.app.CleanedTag;
import com.phrasePhinder.app.ShowOccurrenceToIndex;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

public class Trie {
    public static NodeList pTags;
    public static LinkedList<CleanedTag> cleanedTags;
    public static int episodeIndex;

    public static HashMap<Integer,HashMap<String,String>> episodeMap = new HashMap<>();

    public static Map<String, TrieNode> trieShows = new HashMap<>();

    public static void addShow(String show){
        TrieNode root = new TrieNode();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        File folder = new File(Objects.requireNonNull(classloader.getResource("Scripts/" + show)).getFile());
        trieShows.put(show, root);

        File[] listOfEpisodes = folder.listFiles();
        assert listOfEpisodes != null;
        for(File episode: listOfEpisodes){
            setEpisodeDetails(episode);
            parseFile(episode,show);
        }
    }

    public static void setEpisodeDetails(File episode) {
        int fileNameStart = episode.toString().lastIndexOf('/') + 1;
        int fileNameEnd = episode.toString().lastIndexOf(".csv");
        String[] fileSplit = episode.toString().substring(fileNameStart,fileNameEnd).split(" ",2);
        int episodeIdx = fileSplit[0].indexOf('E');
        String seasonNum = fileSplit[0].substring(1,episodeIdx);
        String episodeNum = fileSplit[0].substring(episodeIdx+1);
        String episodeName = fileSplit[1];

        HashMap<String,String> episodeDetails = new HashMap<>();
        episodeDetails.put("seasonNum",seasonNum);
        episodeDetails.put("episodeNum",episodeNum);
        episodeDetails.put("episodeName",episodeName);

        episodeIndex++;
        episodeMap.put(episodeIndex,episodeDetails);
    }

    public static void parseFile(File episode,String show){
        getPTags(episode);
        cleanedTags = new LinkedList<>();
        for(int i = 0; i < pTags.getLength(); i++){
            CleanedTag cleanedTag = new CleanedTag(pTags.item(i));
            if(cleanedTag.words != null){
                cleanedTags.add(cleanedTag);
            }
        }
        loadAllPhrases(show);
    }

    public static void getPTags(File file){
        try{
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(file);
            document.normalize();
            pTags = document.getElementsByTagName("p");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void loadAllPhrases(String show){
        for(int i = 0; i < cleanedTags.size(); i++){
            String[] words = cleanedTags.get(i).words;
            for(int j = 0; j < words.length; j++){
                long startTime = cleanedTags.get(i).startTime;
                List<String> phrase = new ArrayList<>(5);
                String nextWord = words[j];
                int nextRow = i;
                int nextColumn = j;
                int count = 0;
                while(nextWord != null && count < 5){
                    phrase.add(nextWord);
                    count++;
                    nextColumn++;
                    if(nextColumn == cleanedTags.get(nextRow).words.length){
                        nextColumn = 0;
                        nextRow++;
                    }
                    if(nextRow == cleanedTags.size()){
                        nextWord = null;
                        nextRow--;
                    }
                    else{
                        nextWord = cleanedTags.get(nextRow).words[nextColumn];
                    }
                }
                long endTime = cleanedTags.get(nextRow).endTime;
                ShowOccurrenceToIndex occurrence = new ShowOccurrenceToIndex(startTime,
                        endTime, episodeIndex);
                loadPhrase(phrase,occurrence, 0, trieShows.get(show));
            }
        }
    }

    public static void loadPhrase(List<String> phrase, ShowOccurrenceToIndex occurrence, int index, TrieNode curr) {
        if (index == phrase.size())
            return;
        if(!curr.children.containsKey(phrase.get(index))){
            TrieNode next = new TrieNode(phrase.get(index));
            next.showOccurrences.add(occurrence);
            curr.children.put(phrase.get(index),next);
        }
        else{
            curr.children.get(phrase.get(index)).showOccurrences.add(occurrence);
        }
        loadPhrase(phrase,occurrence,index + 1, curr.children.get(phrase.get(index)));
    }

    public static ArrayList<String> didYouMean(String show, String phrase, String prefix){
        return didYouMean(phrase.split(" "), trieShows.get(show),0,prefix);
    }

    public static ArrayList<String> didYouMean(String[] phrase, TrieNode curr, int index, String prefix){
        if(curr.children.containsKey(phrase[index])){
            return didYouMean(phrase,curr.children.get(phrase[index]), index + 1, prefix + " " + phrase[index]);
        }
        ArrayList<String> result = new ArrayList<>();
        dfs(curr,prefix,result,phrase.length - index);
        return result;
    }

    public static void dfs(TrieNode curr, String prefix, ArrayList<String> result, int count){
        if(result.size() <= 5 && count == 0){
            result.add(prefix);
            return;
        }

        for(String next: curr.children.keySet()){
            dfs(curr.children.get(next), prefix + " " + curr.children.get(next).value, result, count - 1);
        }
    }

    public static ArrayList<ShowOccurrenceToIndex> search(String show,String phrase){
        return search(phrase.split(" "),trieShows.get(show),0);
    }

    public static ArrayList<ShowOccurrenceToIndex> search(String[] phrase, TrieNode curr, int index){
        if(index == phrase.length){
            return curr.showOccurrences;
        }
        if(curr.children.containsKey(phrase[index])){
            return search(phrase, curr.children.get(phrase[index]),index + 1);
        }
        return new ArrayList<>();
    }
}
