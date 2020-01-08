package me.codeboy.doc.db.domain;

import java.io.Serializable;

public class DocInfo implements Serializable {
    private String title;
    private String url;
    private String esId;
    private String content;

    public static DocInfo create(String esId, String title, String content, String url) {
        DocInfo doc = new DocInfo();
        doc.esId = esId;
        doc.title = title;
        doc.content = content;
        doc.url = url;
        return doc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEsId() {
        return esId;
    }

    public void setEsId(String esId) {
        this.esId = esId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
