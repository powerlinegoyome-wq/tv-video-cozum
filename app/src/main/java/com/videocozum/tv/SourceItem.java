package com.videocozum.tv;

public class SourceItem {
    public static final int TYPE_CATEGORY = 0;
    public static final int TYPE_TEST = 1;
    public static final int TYPE_QUESTION = 2;

    public int type = TYPE_CATEGORY;
    public String id;
    public String name;
    public String pid;
    public boolean isParent;
    public String solvedType;
    public String swf;
    public String video;
    public String audio;
    public String url;

    // Kategori veya Test öğesi (source_list içinden gelen)
    public SourceItem(String id, String name, String pid, boolean isParent) {
        this.id = id;
        this.name = name;
        this.pid = pid;
        this.isParent = isParent;
        this.type = isParent ? TYPE_CATEGORY : TYPE_TEST;
    }

    // Soru öğesi (content_list içinden gelen)
    public SourceItem(String id, String name, String solvedType, String swf, String video, String audio, String url) {
        this.id = id;
        this.name = name;
        this.isParent = false;
        this.type = TYPE_QUESTION;
        this.solvedType = solvedType;
        this.swf = swf;
        this.video = video;
        this.audio = audio;
        this.url = url;
    }
}
