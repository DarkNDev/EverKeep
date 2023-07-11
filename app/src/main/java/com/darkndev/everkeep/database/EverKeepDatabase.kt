package com.darkndev.everkeep.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.darkndev.everkeep.di.EverKeepScope
import com.darkndev.everkeep.models.Label
import com.darkndev.everkeep.models.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Note::class, Label::class], version = 1)
@TypeConverters(Converters::class)
abstract class EverKeepDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    abstract fun labelDao(): LabelDao

    class Callback @Inject constructor(
        private val database: Provider<EverKeepDatabase>,
        @EverKeepScope private val everKeepScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val labelDao = database.get().labelDao()
            everKeepScope.launch {
                labelDao.insertLabel(Label(label = "Normal"))
                labelDao.insertLabel(Label(label = "Shopping"))
                labelDao.insertLabel(Label(label = "Groceries"))
                labelDao.insertLabel(Label(label = "Gardening"))
                labelDao.insertLabel(Label(label = "Events"))
                labelDao.insertLabel(Label(label = "Projects"))
                labelDao.insertLabel(Label(label = "Ideas"))
            }
        }
    }
}