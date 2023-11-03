package com.jlu.drsmog.adapters;

public class Record {
    private final int id;
    private String time;
    private String blackness;
    private String path;

    private String name;

    public Record(int id, String time, String blackness, String path, String name) {
        this.id = id;
        this.time = time;
        this.blackness = blackness;
        this.path = path;
        this.name =name;
    }

    // Getter methods
    public String getTime() {
        return time;
    }

    public String getBlackness() {
        return blackness;
    }

    public String getPath() {
        return path;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Time: " + time + ", Blackness: " + blackness + ", Path: " + path+", Name: " + name;
    }

    // Setter methods (如果需要的话)
}
