package org.luba239.mom_tracker.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.luba239.mom_tracker.TimerSession

@Entity(tableName = "timer_state")
data class TimerStateEntity(
    @PrimaryKey
    val id: Int = 1,
    val isRunning: Boolean,
    val currentSessionStart: Long,
    val totalSeconds: Long,
    val sessions: List<TimerSession>
)

class Converters {
    @TypeConverter
    fun fromSessionList(value: List<TimerSession>): String {
        val gson = Gson()
        val type = object : TypeToken<List<TimerSession>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toSessionList(value: String): List<TimerSession> {
        val gson = Gson()
        val type = object : TypeToken<List<TimerSession>>() {}.type
        return gson.fromJson(value, type)
    }
}

@Dao
interface TimerStateDao {
    @Query("SELECT * FROM timer_state WHERE id = 1")
    suspend fun getTimerState(): TimerStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTimerState(state: TimerStateEntity)
}

@Database(entities = [TimerStateEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class TimerDatabase : RoomDatabase() {
    abstract fun timerStateDao(): TimerStateDao

    companion object {
        @Volatile
        private var INSTANCE: TimerDatabase? = null

        fun getDatabase(context: Context): TimerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TimerDatabase::class.java,
                    "timer_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}