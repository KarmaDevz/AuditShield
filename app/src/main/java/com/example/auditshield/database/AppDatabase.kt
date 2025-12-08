package com.example.auditshield.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.auditshield.database.dao.AuditDao
import com.example.auditshield.database.dao.QuestionDao
import com.example.auditshield.database.dao.AnswerDao
import com.example.auditshield.database.entity.AuditEntity
import com.example.auditshield.database.entity.QuestionEntity
import com.example.auditshield.database.entity.AnswerEntity

@Database(
    entities = [
        AuditEntity::class,
        QuestionEntity::class,
        AnswerEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun auditDao(): AuditDao
    abstract fun questionDao(): QuestionDao
    abstract fun answerDao(): AnswerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "auditshield_db"
                )
                    .fallbackToDestructiveMigration()   // ← La clave
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
