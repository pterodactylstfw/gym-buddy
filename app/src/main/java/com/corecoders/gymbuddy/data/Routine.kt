package com.corecoders.gymbuddy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class Routine(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String = "",
    val name: String,
    val description: String = "",
    val lastPerformed: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
