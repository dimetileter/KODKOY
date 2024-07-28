package com.aliosman.paylasimuygulamasi.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SharingModel  (

    @ColumnInfo(name = "userName")
    val userName: String?,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "time")
    val time: String?,

    @ColumnInfo(name = "location")
    val location: String?,

    @ColumnInfo(name = "url")
    val url: String?
){
    @PrimaryKey (autoGenerate = true) var id: Int = 0
}