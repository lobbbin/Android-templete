package com.example.game.model

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromQuarter(quarter: Quarter): String = quarter.name

    @TypeConverter
    fun toQuarter(value: String): Quarter = Quarter.valueOf(value)

    @TypeConverter
    fun fromPolicyCategory(category: PolicyCategory): String = category.name

    @TypeConverter
    fun toPolicyCategory(value: String): PolicyCategory = PolicyCategory.valueOf(value)

    @TypeConverter
    fun fromDiplomaticStatus(status: DiplomaticStatus): String = status.name

    @TypeConverter
    fun toDiplomaticStatus(value: String): DiplomaticStatus = DiplomaticStatus.valueOf(value)

    @TypeConverter
    fun fromCrisisType(type: CrisisType): String = type.name

    @TypeConverter
    fun toCrisisType(value: String): CrisisType = CrisisType.valueOf(value)
}