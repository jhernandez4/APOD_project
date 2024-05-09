package edu.fullerton.csu.astronomypictureoftheday

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date
import java.util.UUID

@Entity
data class Favorite (@PrimaryKey val id: UUID,
                     val title: String,
                     val date: Calendar){}