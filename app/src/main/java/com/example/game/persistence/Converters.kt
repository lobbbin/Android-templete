package com.example.game.persistence

import androidx.room.TypeConverter
import com.example.game.model.*

/** Room type converters for enums and custom types. */
class Converters {

    // ── Season ──────────────────────────────────────────────
    @TypeConverter fun fromSeason(s: Season): String = s.name
    @TypeConverter fun toSeason(v: String): Season = Season.valueOf(v)

    // ── Weather ─────────────────────────────────────────────
    @TypeConverter fun fromWeather(w: Weather): String = w.name
    @TypeConverter fun toWeather(v: String): Weather = Weather.valueOf(v)

    // ── PlotStatus ─────────────────────────────────────────
    @TypeConverter fun fromPlotStatus(p: PlotStatus): String = p.name
    @TypeConverter fun toPlotStatus(v: String): PlotStatus = PlotStatus.valueOf(v)

    // ── CropType ────────────────────────────────────────────
    @TypeConverter fun fromCropType(c: CropType?): String? = c?.name
    @TypeConverter fun toCropType(v: String?): CropType? = v?.let { CropType.valueOf(it) }

    // ── ItemType ─────────────────────────────────────────────
    @TypeConverter fun fromItemType(i: ItemType): String = i.name
    @TypeConverter fun toItemType(v: String): ItemType = ItemType.valueOf(v)

    // ── AnimalType ──────────────────────────────────────────
    @TypeConverter fun fromAnimalType(a: AnimalType): String = a.name
    @TypeConverter fun toAnimalType(v: String): AnimalType = AnimalType.valueOf(v)

    // ── NPCId ───────────────────────────────────────────────
    @TypeConverter fun fromNPCId(n: NPCId): String = n.name
    @TypeConverter fun toNPCId(v: String): NPCId = NPCId.valueOf(v)
}
