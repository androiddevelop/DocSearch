package me.codeboy.doc.db.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

@TableName("doc")
public class Doc implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String title;
    private String url;
    @TableId(value = "es_id")
    private String esId;
    private long times;
    @TableId(value = "updated_time")
    private long updatedTime;

    public static Doc create(String title, String url, String esId) {
        Doc doc = new Doc();
        doc.title = title;
        doc.url = url;
        doc.esId = esId;
        doc.updatedTime = System.currentTimeMillis();
        return doc;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public long getTimes() {
        return times;
    }

    public void setTimes(long times) {
        this.times = times;
    }

    public String getEsId() {
        return esId;
    }

    public void setEsId(String esId) {
        this.esId = esId;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }
}
