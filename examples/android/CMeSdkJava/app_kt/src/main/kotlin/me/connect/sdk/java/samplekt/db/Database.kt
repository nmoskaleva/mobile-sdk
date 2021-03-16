package me.connect.sdk.java.samplekt.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.connect.sdk.java.samplekt.db.dao.*
import me.connect.sdk.java.samplekt.db.entity.*


@androidx.room.Database(entities = [
    Backup::class,
    Connection::class,
    CredentialOffer::class,
    ProofRequest::class,
    StructuredMessage::class
], version = 1)
@TypeConverters(ResponseConverter::class)
abstract class Database : RoomDatabase() {
    abstract fun backupDao(): BackupDao
    abstract fun connectionDao(): ConnectionDao
    abstract fun credentialOffersDao(): CredentialOfferDao
    abstract fun proofRequestDao(): ProofRequestDao
    abstract fun structuredMessageDao(): StructuredMessageDao

    companion object {
        private const val DB_NAME = "db"
        private var instance: Database? = null
        fun getInstance(context: Context): Database {
            return instance ?: Room
                    .databaseBuilder(context.applicationContext, Database::class.java, DB_NAME)
                    .build()
                    .also { instance = it }
        }
    }
}