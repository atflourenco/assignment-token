package com.token.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.token.model.Data;

@Dao
public interface DataDao {

    @Insert
    void insert(Data data);

    @Query("DELETE FROM data_user")
    void deleteAll();

    @Query("SELECT * FROM data_user")
    Data getData();
}
