package com.phrasePhinder.app;

public class ShowOccurrenceToIndex {
    public final long startTime;
    public final long endTime;
    public final int episodeIndex;

    public ShowOccurrenceToIndex(long startTime, long endTime, int episodeIndex){
        this.startTime = startTime;
        this.endTime = endTime;
        this.episodeIndex = episodeIndex;
    }

    @Override
    public String toString(){
        return String.format("start %s end %s episodeIndex %d",
                startTime,endTime,episodeIndex);
    }
}