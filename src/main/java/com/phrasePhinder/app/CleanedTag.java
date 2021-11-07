package com.phrasePhinder.app;

import org.w3c.dom.Node;

public class CleanedTag {
    long startTime;
    long endTime;
    String[] words;
    public CleanedTag(Node tag){
        String textContent = tag.getTextContent();
        textContent = textContent.toLowerCase().replaceAll("\\[.*?\\]"," ").
                replaceAll("[^a-z A-Z 0-9]","").replaceAll(" +", " ").trim();
        if(!textContent.equals(""))
            words = textContent.split(" ");

        startTime = Long.parseLong(tag.getAttributes().
                getNamedItem("begin").getNodeValue().replace("t",""));

        endTime = Long.parseLong(tag.getAttributes().
                getNamedItem("end").getNodeValue().replace("t",""));
    }

    @Override
    public String toString() {
        return String.format("Start time: %d End time: %d Words length: %s", startTime, endTime, words[0]);
    }
}
