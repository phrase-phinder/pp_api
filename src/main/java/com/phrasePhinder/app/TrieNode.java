package com.phrasePhinder.app;

import java.util.*;

public class TrieNode {
    String value;
    Map<String, TrieNode> children = new HashMap<>();
    ArrayList<ShowOccurrenceToIndex> showOccurrences = new ArrayList<>();
    public TrieNode(String value){
        this.value = value;
    }

    public TrieNode(){}

}
