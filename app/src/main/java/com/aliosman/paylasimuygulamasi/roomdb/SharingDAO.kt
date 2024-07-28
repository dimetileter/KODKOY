package com.aliosman.paylasimuygulamasi.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aliosman.paylasimuygulamasi.model.SharingModel

@Dao
interface SharingDAO {

    @Query("DELETE FROM SharingModel")
    fun deleteAllPosts()

    @Query("SELECT * FROM SharingModel")
    fun getAll(): List<SharingModel>

    @Query("SELECT * FROM SharingModel WHERE id = :id")
    suspend fun findPostById(id: Int): SharingModel

    @Insert
    suspend fun insertData(posts: List<SharingModel>)

    @Insert
    suspend fun insertAllData(vararg posts: SharingModel): List<Long>

}