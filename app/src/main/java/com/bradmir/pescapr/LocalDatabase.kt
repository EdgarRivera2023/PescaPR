package com.bradmir.pescapr

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bradmir.pescapr.data.ApprovedSpotPhoto
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
    val fotosUrls: List<String> = emptyList(),
    val userId: String = "",
    val firestoreId: String = "",
    val approvedPhotos: List<ApprovedSpotPhoto> = emptyList()
)

internal fun SpotEntity.withLocalIdentityFrom(existing: SpotEntity): SpotEntity =
    copy(id = existing.id)

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
        return try {
            gson.fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromApprovedPhotoList(value: List<ApprovedSpotPhoto>): String = gson.toJson(value)

    @TypeConverter
    fun toApprovedPhotoList(value: String): List<ApprovedSpotPhoto> {
        val listType = object : TypeToken<List<ApprovedSpotPhoto>>() {}.type
        return try {
            gson.fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
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

    @Query("SELECT * FROM spots WHERE firestoreId = :firestoreId ORDER BY id LIMIT 1")
    suspend fun getSpotByFirestoreId(firestoreId: String): SpotEntity?

    @Transaction
    suspend fun upsertFirestoreSpot(spot: SpotEntity): Long {
        if (spot.firestoreId.isBlank()) return insertSpot(spot)

        val existing = getSpotByFirestoreId(spot.firestoreId)
        return if (existing == null) {
            insertSpot(spot)
        } else {
            updateSpot(spot.withLocalIdentityFrom(existing))
            existing.id.toLong()
        }
    }
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

@Database(entities = [SpotEntity::class, RecordEntity::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun spotDao(): SpotDao
    abstract fun recordDao(): RecordDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE spots ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE spots ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE spots ADD COLUMN approvedPhotos TEXT NOT NULL DEFAULT '[]'")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Keep the oldest local row as the stable identity for each Firestore spot.
                // Repoint captures before removing only the redundant spot rows.
                db.execSQL(
                    """
                    UPDATE records
                    SET spotId = (
                        SELECT MIN(canonical.id)
                        FROM spots AS canonical
                        WHERE canonical.firestoreId = (
                            SELECT duplicate.firestoreId
                            FROM spots AS duplicate
                            WHERE duplicate.id = records.spotId
                        )
                    )
                    WHERE spotId IN (
                        SELECT duplicate.id
                        FROM spots AS duplicate
                        WHERE TRIM(duplicate.firestoreId) <> ''
                          AND duplicate.id <> (
                              SELECT MIN(canonical.id)
                              FROM spots AS canonical
                              WHERE canonical.firestoreId = duplicate.firestoreId
                          )
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    DELETE FROM spots
                    WHERE TRIM(firestoreId) <> ''
                      AND id <> (
                          SELECT MIN(canonical.id)
                          FROM spots AS canonical
                          WHERE canonical.firestoreId = spots.firestoreId
                      )
                    """.trimIndent()
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pescapr_local_db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
