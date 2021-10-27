package me.connect.sdk.java.samplekt.homepage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import me.connect.sdk.java.*
import me.connect.sdk.java.message.Message
import me.connect.sdk.java.message.MessageType
import me.connect.sdk.java.message.StructuredMessageHolder
import me.connect.sdk.java.samplekt.SingleLiveData
import me.connect.sdk.java.samplekt.db.ActionStatus.HISTORIZED
import me.connect.sdk.java.samplekt.db.ActionStatus.PENDING
import me.connect.sdk.java.samplekt.db.Database
import me.connect.sdk.java.samplekt.db.entity.*
import me.connect.sdk.java.samplekt.homepage.Results.*
import me.connect.sdk.java.samplekt.messages.CredDataHolder
import me.connect.sdk.java.samplekt.messages.ProofDataHolder
import me.connect.sdk.java.samplekt.wrap
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class HomePageViewModel(application: Application) : AndroidViewModel(application) {
    private val db: Database = Database.getInstance(application)
    private val actionsLiveData by lazy {
        db.actionDao().getActionsByStatus(PENDING.toString())
    }

    fun getActions(): LiveData<List<Action>> = actionsLiveData

    fun accept(actionId: Int): SingleLiveData<Results> {
        val data = SingleLiveData<Results>()
        acceptProcess(actionId, data)
        return data
    }

    fun reject(actionId: Int): SingleLiveData<Results> {
        val data = SingleLiveData<Results>()
        rejectProcess(actionId, data)
        return data
    }

    fun newAction(invite: String): SingleLiveData<Results> {
        val data = SingleLiveData<Results>()
        createAction(invite, data)
        return data
    }

    fun checkMessages(): SingleLiveData<Results> {
        val data = SingleLiveData<Results>()
        checkAllMessages(data)
        return data
    }

    fun answerMessage(actionId: Int, answer: JSONObject): SingleLiveData<Results> {
        val data = SingleLiveData<Results>()
        answerStructMessage(actionId, answer, data)
        return data
    }

    private fun answerStructMessage(actionId: Int, answer: JSONObject, liveData: SingleLiveData<Results>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val action = db.actionDao().getActionsById(actionId)
            val con = db.connectionDao().getByPwDid(action.pwDid!!)
            val sm = db.structuredMessageDao().getByEntryIdAndPwDid(action.entryId, action.pwDid)

            StructuredMessages.answer(con.serialized, sm!!.serialized, answer).wrap().await()
            sm.selectedAnswer = answer.getString("text")
            db.structuredMessageDao().update(sm)
            liveData.postValue(QUESTION_SUCCESS)
            HistoryActions.addToHistory(actionId, "Ask to question", db, liveData)
        } catch (e: Exception) {
            e.printStackTrace()
            liveData.postValue(QUESTION_FAILURE)
        }
    }

    private fun checkAllMessages(liveData: SingleLiveData<Results>) = viewModelScope.launch(Dispatchers.IO) {
        val messages = Messages.getAllPendingMessages().wrap().await()
        liveData.postValue(SUCCESS)
        for (message in messages) {
            if (MessageType.CREDENTIAL_OFFER.matches(message.type)) {
                credentialOffersProcess(message, liveData)
            }
            if (MessageType.PROOF_REQUEST.matches(message.type)) {
                proofRequestProcess(message, liveData)
            }
            if (MessageType.QUESTION.matches(message.type)) {
                questionsProcess(message, liveData)
            }
        }
    }

    private suspend fun credentialOffersProcess(message: Message, liveData: SingleLiveData<Results>) {
        try {
            val holder = CredDataHolder.extractDataFromCredentialsOfferMessage(message)
            val pwDid: String = message.pwDid
            val connection: Connection = db.connectionDao().getByPwDid(pwDid)
            val co = Credentials.createWithOffer(UUID.randomUUID().toString(), holder!!.offer).wrap().await()
            val offer = CredentialOffer(
                claimId = holder.id,
                pwDid = pwDid,
                serialized = co,
                attachConnectionLogo = connection.icon,
                threadId = holder.threadId
            )
            db.credentialOffersDao().insertAll(offer)

            Messages.updateMessageStatus(pwDid, message.uid)

            createActionWithOffer(
                MessageType.CREDENTIAL_OFFER.toString(),
                holder.name,
                connection.icon!!,
                holder.attributes,
                holder.id,
                pwDid,
                liveData
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun proofRequestProcess(message: Message, liveData: SingleLiveData<Results>) {
        try {
            val holder = ProofDataHolder.extractRequestedFieldsFromProofMessage(message)!!
            val pwDid: String = message.pwDid
            val connection: Connection = db.connectionDao().getByPwDid(pwDid)
            val pr = Proofs.createWithRequest(UUID.randomUUID().toString(), holder.proofReq).wrap().await()
            val proof = ProofRequest(
                serialized = pr,
                pwDid = pwDid,
                threadId = holder.threadId,
                attachConnectionLogo = connection.icon
            )
            db.proofRequestDao().insertAll(proof)

            Messages.updateMessageStatus(pwDid, message.uid)

            createActionWithProof(
                MessageType.CREDENTIAL_OFFER.toString(),
                holder.name,
                connection.icon!!,
                holder.threadId,
                liveData
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun questionsProcess(
        message: Message,
        liveData: SingleLiveData<Results>
    ) {
        val holder = StructuredMessages.extract(message)
        val pwDid = message.pwDid
        try {
            val sm = StructuredMessage(
                pwDid = pwDid,
                entryId = holder.id,
                type = holder.type,
                serialized = message.payload,
                answers = holder.responses
            )

            db.structuredMessageDao().insertAll(sm)
            if ("question" == holder.type) {
                Messages.updateMessageStatus(pwDid, message.uid)
            }
            createActionWithQuestion(
                MessageType.CREDENTIAL_OFFER.toString(),
                holder.questionText,
                holder.questionDetail,
                pwDid,
                holder.id,
                holder.responses,
                liveData
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun acceptProcess(
        actionId: Int,
        liveData: SingleLiveData<Results>
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val action = db.actionDao().getActionsById(actionId)
            if (action.type == null) {
                StateConnections.handleConnectionInvitation(action, db, liveData)
                return@launch
            }
            if (action.type == MessageType.CREDENTIAL_OFFER.toString()) {
                val offer = db.credentialOffersDao().getByPwDidAndClaimId(action.claimId, action.pwDid)
                StateCredentialOffers.processCredentialOffer(offer!!, db, liveData, action)
                return@launch
            }
            if (action.type == MessageType.PROOF_REQUEST.toString()) {
                val proof = db.proofRequestDao().getByThreadId(action.threadId)
                StateProofRequests.processProofRequest(proof!!, db, liveData, action)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            liveData.postValue(FAILURE)
        }
    }

    private fun rejectProcess(
        actionId: Int,
        liveData: SingleLiveData<Results>
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val action = db.actionDao().getActionsById(actionId)
            if (action.type == null) {
                HistoryActions.addToHistory(actionId, "Rejected", db, liveData)
                return@launch
            }
            if (action.type == MessageType.CREDENTIAL_OFFER.toString()) {
                val offer = db.credentialOffersDao().getByPwDidAndClaimId(action.claimId, action.pwDid)
                StateCredentialOffers.rejectCredentialOffer(offer!!, db, liveData)
                return@launch
            }
            if (action.type == MessageType.PROOF_REQUEST.toString()) {
                val proof = db.proofRequestDao().getByThreadId(action.threadId)
                StateProofRequests.rejectProofReq(proof!!, db, liveData)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            liveData.postValue(FAILURE)
        }

    }

    private fun createAction(
        invite: String,
        liveData: SingleLiveData<Results>
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val parsedInvite = ConnectionInvitations.getConnectionInvitationFromData(invite)
            val inviteObject = JSONObject(parsedInvite)
            val invitationType = ConnectionInvitations.getInvitationType(parsedInvite)
            val attachment = OutOfBandHelper.extractRequestAttach(parsedInvite)

            if (ConnectionInvitations.isAriesOutOfBandConnectionInvitation(invitationType) && attachment != null) {
                val attachmentType = attachment.getString("@type")
                if (ConnectionInvitations.isCredentialAttachment(attachmentType)) {
                    val preview = attachment.getJSONObject("credential_preview")
                    val action = Action(
                        invite = invite,
                        name = attachment.getString("comment"),
                        description = inviteObject.getString("goal"),
                        details = preview.getJSONArray("attributes").getString(0)!!,
                        icon = inviteObject.getString("profileUrl"),
                        status = PENDING.toString()
                    )
                    db.actionDao().insertAll(action)
                    liveData.postValue(ACTION_SUCCESS)
                    return@launch
                }
                if (ConnectionInvitations.isProofAttachment(attachmentType)) {
                    val decodedProofAttach = ProofRequests.decodeProofRequestAttach(attachment)
                    val action = Action(
                        invite = invite,
                        name = ProofRequests.extractRequestedNameFromProofRequest(decodedProofAttach),
                        description = inviteObject.getString("goal"),
                        details = ProofRequests.extractRequestedAttributesFromProofRequest(decodedProofAttach),
                        icon = inviteObject.getString("profileUrl"),
                        status = PENDING.toString()
                    )

                    db.actionDao().insertAll(action)
                    liveData.postValue(ACTION_SUCCESS)
                    return@launch
                }
            } else {
                val action = Action(
                    invite = invite,
                    name = inviteObject.getString("label"),
                    description = inviteObject.getString("goal"),
                    icon = inviteObject.getString("profileUrl"),
                    status = PENDING.toString()
                )

                db.actionDao().insertAll(action)
                liveData.postValue(ACTION_SUCCESS)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            liveData.postValue(ACTION_FAILURE)
        }
    }

    private suspend fun createActionWithOffer(
        type: String,
        name: String,
        icon: String,
        details: String,
        offerId: String,
        pwDid: String,
        liveData: SingleLiveData<Results>
    ) {
        try {
            val action = Action(
                type = type,
                name = name,
                description = "To issue the credential",
                icon = icon,
                details = details,
                claimId = offerId,
                pwDid = pwDid,
                status = PENDING.toString()
            )

            db.actionDao().insertAll(action)
            liveData.postValue(SUCCESS)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            liveData.postValue(FAILURE)
        }
    }

    private suspend fun createActionWithProof(
        type: String,
        name: String,
        icon: String,
        threadId: String,
        liveData: SingleLiveData<Results>
    ) {
        try {
            val action = Action(
                type = type,
                name = name,
                description = "Share the proof",
                icon = icon,
                threadId = threadId,
                status = PENDING.toString()
            )

            db.actionDao().insertAll(action)
            liveData.postValue(SUCCESS)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            liveData.postValue(FAILURE)
        }
    }

    private suspend fun createActionWithQuestion(
        type: String,
        name: String,
        details: String,
        pwDid: String,
        entryId: String,
        messageAnswers: List<StructuredMessageHolder.Response>,
        liveData: SingleLiveData<Results>
    ) {
        try {
            val action = Action(
                invite = null,
                type = type,
                name = name,
                description = "Answer the questions",
                details = details,
                pwDid = pwDid,
                entryId = entryId,
                messageAnswers = messageAnswers,
                status = PENDING.toString()
            )

            db.actionDao().insertAll(action)
            liveData.postValue(SUCCESS)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            liveData.postValue(FAILURE)
        }
    }

    object HistoryActions {
        suspend fun addHistoryAction(
            db: Database,
            name: String,
            description: String,
            icon: String,
            liveData: SingleLiveData<Results>
        ) {
            try {
                val action = Action(
                    invite = null,
                    name = name,
                    description = description,
                    icon = icon,
                    status = HISTORIZED.toString()
                )

                db.actionDao().insertAll(action)
                liveData.postValue(ACTION_SUCCESS)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                liveData.postValue(ACTION_FAILURE)
            }
        }

        suspend fun addToHistory(
            actionId: Int,
            description: String,
            db: Database,
            liveData: SingleLiveData<Results>
        ) {
            try {
                val action = db.actionDao().getActionsById(actionId)
                action.status = HISTORIZED.toString()
                action.description = description
                db.actionDao().update(action)
                liveData.postValue(REJECT)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                liveData.postValue(FAILURE)
            }
        }
    }
}
