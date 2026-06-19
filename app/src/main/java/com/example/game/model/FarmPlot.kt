package com.example.game.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * A single farm plot tracked in Room.
 * [gridX],[gridY] form a compound unique index so the UI can lay them out in a grid.
 */
@Entity(
    tableName = "farm_plots",
    indices = [Index(value = ["gridX", "gridY"], unique = true)]
)
data class FarmPlot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gridX: Int,
    val gridY: Int,
    val status: PlotStatus = PlotStatus.EMPTY,
    val crop: CropType? = null,
    val daysPlanted: Int = 0,
    val isWatered: Boolean = false,
    val quality: Float = 1.0f,  // 0.5–2.0, affects sell price
)
