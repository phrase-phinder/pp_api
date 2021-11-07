CREATE TABLE Shows(
    Name TEXT,
    Category TEXT,
    PRIMARY KEY (NAME)
);

CREATE TABLE Phrases(
    Id INTEGER,
    ShowName TEXT,
    Phrase TEXT,
    StartTime BIGINT ,
    EndTime BIGINT ,
    SeasonNum INTEGER,
    EpisodeNum INTEGER,
    EpisodeName TEXT,
    Links TEXT,
    PRIMARY KEY (Id, Phrase, StartTime, EndTime, ShowName, EpisodeName));
