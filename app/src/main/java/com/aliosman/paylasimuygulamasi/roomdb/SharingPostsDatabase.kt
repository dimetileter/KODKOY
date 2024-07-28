package com.aliosman.paylasimuygulamasi.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aliosman.paylasimuygulamasi.model.SharingModel

@Database(entities = [SharingModel::class], version = 1)
abstract class SharingPostsDatabase : RoomDatabase() {
    abstract fun userDao(): SharingDAO
}