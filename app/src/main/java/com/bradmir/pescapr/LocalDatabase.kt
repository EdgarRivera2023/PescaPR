package com.bradmir.pescapr

import android.content.Context
import androidx.room.*
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow

// --- ENTITIES ---

@Entity(tableName = "spots")
data class SpotEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val fotosUrls: List<String> = emptyList()
)

@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombrePez: String,
    val peso: String,
    val longitud: String,
    val lugar: String, // Para compatibilidad
    val fecha: String,
    val fotosUrls: List<String> = emptyList(),
    val spotId: Int, // ID del spot local
    val fishId: String? = null, // ID de la guía oficial (Firestore)
    val climaTemp: String = "",
    val climaWind: String = "",
    val climaPressure: String = "",
    val climaTide: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// --- TYPE CONVERTERS ---

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
}

// --- DAOS ---

@Dao
interface SpotDao {
    @Query("SELECT * FROM spots")
    fun getAllSpots(): Flow<List<SpotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpot(spot: SpotEntity): Long

    @Update
    suspend fun updateSpot(spot: SpotEntity)

    @Delete
    suspend fun deleteSpot(spot: SpotEntity)

    @Query("SELECT * FROM spots WHERE id = :id")
    suspend fun getSpotById(id: Int): SpotEntity?
}

@Dao
interface RecordDao {
    @Query("SELECT * FROM records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE spotId = :spotId ORDER BY timestamp DESC")
    fun getRecordsBySpot(spotId: Int): Flow<List<RecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RecordEntity)

    @Update
    suspend fun updateRecord(record: RecordEntity)

    @Delete
    suspend fun deleteRecord(record: RecordEntity)
}

// --- DATABASE ---

@Database(entities = [SpotEntity::class, RecordEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun spotDao(): SpotDao
    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pescapr_local_db"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
