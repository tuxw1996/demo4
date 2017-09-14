package com.gx.me.demo4.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by TO on 2017/9/12.
 */
@Entity
public class Record {
    @Id
    public Long id;
    public String nj_id;
    public String nj_name;
    public String name;
    public String url;
    public String updated;
    public int novel_id;

    @Generated(hash = 433984120)
    public Record(Long id, String nj_id, String nj_name, String name, String url,
            String updated, int novel_id) {
        this.id = id;
        this.nj_id = nj_id;
        this.nj_name = nj_name;
        this.name = name;
        this.url = url;
        this.updated = updated;
        this.novel_id = novel_id;
    }

    @Generated(hash = 477726293)
    public Record() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNj_id() {
        return this.nj_id;
    }

    public void setNj_id(String nj_id) {
        this.nj_id = nj_id;
    }

    public String getNj_name() {
        return this.nj_name;
    }

    public void setNj_name(String nj_name) {
        this.nj_name = nj_name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUpdated() {
        return this.updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public int getNovel_id() {
        return this.novel_id;
    }

    public void setNovel_id(int novel_id) {
        this.novel_id = novel_id;
    }

    public static Record json2record(JSONObject json) {

        try {
            return new Record(json.getLong("id"),
                    json.getString("nj_id"),
                    json.getString("nj_name"),
                    json.getString("name"),
                    json.getString("url"),
                    json.getString("updated"),
                    json.getInt("novel_id"));
        } catch (JSONException e) {
        }
        return null;
    }

    public String getDownloadKey() {
        return "dl_r_" + novel_id + "_" + id;
    }

    public String getDownloadfilename() {
        return "dl_r_" + novel_id + "_" + id + ".mp3";
    }

}
