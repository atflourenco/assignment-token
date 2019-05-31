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

    @ColumnInfo(name = "password")
    private String password;

    public Data(@NonNull String key, String label, String password) {
        this.key = key;
        this.label = label;
        this.password = password;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
