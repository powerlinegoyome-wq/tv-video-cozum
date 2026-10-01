package com.videocozum.tv;

public class SourceItem {
    public String id;
    public String name;
    public String pid;
    public boolean isParent;
    public String solvedType;
    public String swf;
    public String video;
    public String audio;
    public String url;

    public SourceItem(String id, String name, String pid, boolean isParent) {
        this.id = id;
        this.name = name;
        this.pid = pid;
        this.isParent = isParent;
    }

    public SourceItem(String id, String name, String solvedType, String swf, String video, String audio, String url) {
        this.id = id;
        this.name = name;
        this.isParent = false;
        this.solvedType = solvedType;
        this.swf = swf;
        this.video = video;
        this.audio = audio;
        this.url = url;
    }
}
