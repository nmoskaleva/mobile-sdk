package me.connect.sdk.java.samplekt.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import me.connect.sdk.java.samplekt.db.entity.CredentialOffer

@Dao
interface CredentialOfferDao {
    @Query("SELECT * FROM credentialoffer")
    fun getAll(): LiveData<List<CredentialOffer>>

    @Query("SELECT * FROM credentialoffer WHERE id = :id")
    suspend fun getById(id: Int): CredentialOffer

    @Insert
    suspend fun insertAll(vararg connections: CredentialOffer)

    @Query("SELECT EXISTS(SELECT * FROM credentialoffer WHERE (claim_id = :claimId AND pwDid = :pwDid))")
    fun checkOfferExists(claimId: String?, pwDid: String?): Boolean

    @Query("SELECT * FROM credentialoffer WHERE (claim_id = :claimId AND pwDid = :pwDid)")
    fun getByPwDidAndClaimId(
        claimId: String?,
        pwDid: String?
    ): CredentialOffer?

    @Update
    suspend fun update(connection: CredentialOffer)

    @Delete
    suspend fun delete(connection: CredentialOffer)
}