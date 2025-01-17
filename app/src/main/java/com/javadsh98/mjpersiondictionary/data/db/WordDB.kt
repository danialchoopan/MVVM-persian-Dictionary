package com.javadsh98.mjpersiondictionary.data.db

import android.app.Application
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.javadsh98.mjpersiondictionary.data.db.convertor.Converter
import com.javadsh98.mjpersiondictionary.data.db.dao.WordDao
import com.javadsh98.mjpersiondictionary.data.db.entity.Word
import com.javadsh98.mjpersiondictionary.data.gson.Dictionary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream

@Database(entities = arrayOf(Word::class) , version = 3, exportSchema = true )
@TypeConverters(value = arrayOf(Converter::class))
abstract class WordDB : RoomDatabase() {

    abstract fun getWordDao(): WordDao

    companion object{
        val TAG = WordDB::class.java.simpleName

        var migration1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
            }
        }

        var migration1_3: Migration = object : Migration(1, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
            }
        }

        var migration2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
            }
        }

        var INSTANCE : WordDB? = null

        fun getInstance(application: Application, scope: CoroutineScope): WordDB{
            if(INSTANCE == null)
                INSTANCE = Room.databaseBuilder(application, WordDB::class.java, "w_database")
//                    .addCallback(WordCallBack(scope, application))
//                    .addMigrations(migration1_2)
//                    .addMigrations(migration1_3)
//                    .addMigrations(migration2_3)
                    .fallbackToDestructiveMigration()
                    .createFromAsset("database/my_database")
                    .build()
            return INSTANCE as WordDB
        }

    }

    private class WordCallBack(val scope: CoroutineScope, val application: Application) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)

            INSTANCE.let {

                scope.launch {
                    it?.getWordDao()?.deleteAllWords()

                    var json: String? = null
                    try {
                        val inputStream: InputStream = application.assets.open("database/data.json")
                        val size: Int = inputStream.available()
                        val buffer = ByteArray(size)
                        inputStream.read(buffer)
                        inputStream.close()
                        json = String(buffer, Charsets.UTF_8)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    var gson = Gson()
                    var word = gson.fromJson(json, Dictionary::class.java)

                    for (i in 0 until word.list.size){
                        var w = word.list[i]
                        var word = Word(persianWord = w.persian, englishWord = w.englisgh)
                        Log.d(TAG, "english word = ${word.englishWord}")
                        it?.getWordDao()?.insert(word)
                    }

                }


            }

        }
    }

}