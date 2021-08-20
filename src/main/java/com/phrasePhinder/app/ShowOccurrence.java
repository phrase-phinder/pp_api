package com.phrasePhinder.app;

public class ShowOccurrence {
    public final String startTime;
    public final String endTime;
    public final String seasonNum;
    public final String episodeNum;
    public final String episodeName;

    public ShowOccurrence(String startTime, String endTime, String seasonNum, String episodeNum, String episodeName){
        this.startTime = startTime;
        this.endTime = endTime;
        this.seasonNum = seasonNum;
        this.episodeNum = episodeNum;
        this.episodeName = episodeName;
    }
}
