package com.token.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity(tableName = "data_user")
public class Data implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "key")
    @NonNull
    private String key;

    @ColumnInfo(name = "label")
    private String label;


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Data(String key, String label) {
        this.key = key;
        this.label = label;
    }
}
