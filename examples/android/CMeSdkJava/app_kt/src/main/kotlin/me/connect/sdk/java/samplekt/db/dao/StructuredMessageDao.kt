package me.connect.sdk.java.samplekt.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import me.connect.sdk.java.samplekt.db.entity.StructuredMessage


@Dao
interface StructuredMessageDao {
    @Query("SELECT * FROM structuredmessage")
    fun getAll(): LiveData<List<StructuredMessage>>

    @Query("SELECT * FROM structuredmessage WHERE id = :id")
    suspend fun getById(id: Int): StructuredMessage

    @Insert
    suspend fun insertAll(vararg messages: StructuredMessage)

    @Query("SELECT EXISTS(SELECT * FROM structuredmessage WHERE (entry_id = :entryId AND connection_id = :connectionId))")
    suspend fun checkMessageExists(entryId: String, connectionId: Int): Boolean

    @Update
    suspend fun update(message: StructuredMessage)

    @Delete
    suspend fun delete(message: StructuredMessage)
}