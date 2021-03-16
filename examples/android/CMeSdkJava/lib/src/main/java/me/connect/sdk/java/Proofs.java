package me.connect.sdk.java;

import android.util.Log;

import androidx.annotation.NonNull;

import com.evernym.sdk.vcx.VcxException;
import com.evernym.sdk.vcx.connection.ConnectionApi;
import com.evernym.sdk.vcx.proof.DisclosedProofApi;
import com.evernym.sdk.vcx.utils.UtilsApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java9.util.concurrent.CompletableFuture;
import me.connect.sdk.java.message.MessageState;
import me.connect.sdk.java.message.MessageStatusType;
import me.connect.sdk.java.message.MessageType;

/**
 * Class containing methods to work with proofs.
 */
public class Proofs {
    public static final String TAG = "ConnectMeVcx";

    private Proofs() {
    }

    /**
     * Get proof requests
     * Deprecated. Use {@link Messages#getPendingMessages(String, MessageType)} instead.
     *
     * @param connection serialized connection
     * @return {@link CompletableFuture} containing list of proof requests as JSON strings.
     */
    @Deprecated
    public static @NonNull
    CompletableFuture<List<String>> getRequests(@NonNull String connection) {
        Logger.getInstance().i("Getting proof requests");
        CompletableFuture<List<String>> result = new CompletableFuture<>();
        try {
            ConnectionApi.connectionDeserialize(connection).whenComplete((conHandle, err) -> {
                if (err != null) {
                    Logger.getInstance().e("Failed to deserialize connection", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    DisclosedProofApi.proofGetRequests(conHandle).whenComplete((offersJson, er) -> {
                        if (er != null) {
                            Logger.getInstance().e("Failed to get proof requests", er);
                            result.completeExceptionally(er);
                            return;
                        }
                        Logger.getInstance().i("Received proof requests");
                        try {
                            JSONArray offerArray = new JSONArray(offersJson);
                            List<String> offers = new ArrayList<>();
                            for (int i = 0; i < offerArray.length(); i++) {
                                offers.add(offerArray.getString(i));
                            }
                            result.complete(offers);
                        } catch (JSONException ex) {
                            result.completeExceptionally(ex);
                        }

                    });
                } catch (VcxException ex) {
                    Logger.getInstance().e("Failed to get proof requests", ex);
                    result.completeExceptionally(ex);
                }
            });
        } catch (Exception ex) {
            Logger.getInstance().e("Failed to deserialize connection", ex);
            result.completeExceptionally(ex);
        }
        return result;
    }

    /**
     * Loops indefinitely until proof request status is not changed
     *
     * @param serializedProof string containing serialized proof request
     * @param messageState    desired message state
     * @return string containing serialized proof request
     */
    public static String awaitStatusChange(String serializedProof, MessageState messageState) {
        Logger.getInstance().i("Awaiting proof state change");
        int count = 1;
        try {
            Integer handle = DisclosedProofApi.proofDeserialize(serializedProof).get();
            while (true) {
                Logger.getInstance().i("Awaiting proof state change: attempt #" + count);
                Integer state0 = DisclosedProofApi.proofUpdateState(handle).get();
                Logger.getInstance().i("Awaiting proof state change: update state=" + state0);
                Integer state = DisclosedProofApi.proofGetState(handle).get();
                Logger.getInstance().i("Awaiting proof state change: got state=" + state);
                if (messageState.matches(state)) {
                    return DisclosedProofApi.proofSerialize(handle).get();
                }
                count++;
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            Logger.getInstance().e("Failed to await proof state", e);
            e.printStackTrace();
        }
        return serializedProof;
    }

    /**
     * Creates proof request
     *
     * @param sourceId custom string for this cred offer
     * @param message  proof request string
     * @return {@link CompletableFuture} containing serialized proof request
     */
    public static @NonNull
    CompletableFuture<String> createWithRequest(@NonNull String sourceId,
                                                @NonNull String message) {
        Logger.getInstance().i("Retrieving proof request");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            DisclosedProofApi.proofCreateWithRequest(sourceId, message).whenComplete((proofHandle, err) -> {
                if (err != null) {
                    Logger.getInstance().e("Failed create proof with request: ", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    DisclosedProofApi.proofSerialize(proofHandle).whenComplete((sp, e) -> {
                        if (e != null) {
                            Logger.getInstance().e("Failed to serialize proof request: ", e);
                            result.completeExceptionally(e);
                        } else {
                            result.complete(sp);
                        }
                    });
                } catch (VcxException ex) {
                    result.completeExceptionally(ex);
                }
            });
        } catch (Exception ex) {
            result.completeExceptionally(ex);
        }
        return result;
    }

    /**
     * Retrieves available credentials for proof request
     *
     * @param serializedProof proof request string
     * @return {@link CompletableFuture} containing string with available credentials
     */
    public static @NonNull
    CompletableFuture<String> retrieveAvailableCredentials(@NonNull String serializedProof) {
        Logger.getInstance().i("Retrieving credentials for proof request");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            DisclosedProofApi.proofDeserialize(serializedProof).whenComplete((proofHandle, err) -> {
                if (err != null) {
                    Logger.getInstance().e("Failed deserialize proof request: ", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    DisclosedProofApi.proofRetrieveCredentials(proofHandle).whenComplete((retrievedCreds, e) -> {
                        if (e != null) {
                            Logger.getInstance().e("Failed to retrieve proof credentials: ", e);
                            result.completeExceptionally(e);
                        } else {
                            result.complete(retrievedCreds);
                        }
                    });
                } catch (VcxException ex) {
                    result.completeExceptionally(ex);
                }
            });
        } catch (Exception ex) {
            result.completeExceptionally(ex);
        }
        return result;
    }

    /**
     * Respond to proof request with credentials
     *
     * @param serializedConnection   string containing serialized connection
     * @param serializedProof        string containing serialized proof request
     * @param selectedCreds          selected credentials to provide proof
     * @param selfAttestedAttributes user-defined attributes to provide proof
     * @param messageId              messsage ID
     * @return CompletableFuture containing serialized proof
     */
    public static
    @NonNull
    CompletableFuture<String> send(@NonNull String serializedConnection, @NonNull String serializedProof,
                                   @NonNull String selectedCreds, @NonNull String selfAttestedAttributes,
                                   @NonNull String messageId) {
        Logger.getInstance().i("Sending proof request response");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            ConnectionApi.connectionDeserialize(serializedConnection).whenComplete((conHandle, err) -> {
                if (err != null) {
                    Logger.getInstance().e("Failed to deserialize connection: ", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    DisclosedProofApi.proofDeserialize(serializedProof).whenComplete((pHandle, er) -> {
                        if (er != null) {
                            Logger.getInstance().e("Failed to deserialize proof: ", er);
                            result.completeExceptionally(er);
                            return;
                        }
                        try {
                            DisclosedProofApi.proofGenerate(pHandle, selectedCreds, selfAttestedAttributes).whenComplete((v, e) -> {
                                if (e != null) {
                                    Logger.getInstance().e("Failed to generate proof: ", e);
                                    result.completeExceptionally(e);
                                    return;
                                }
                                try {
                                    DisclosedProofApi.proofSend(pHandle, conHandle).whenComplete((r, error) -> {
                                        if (error != null) {
                                            Logger.getInstance().e("Failed to send proof: ", error);
                                            result.completeExceptionally(error);
                                            return;
                                        }
                                        try {
                                            ConnectionApi.connectionGetPwDid(conHandle).whenComplete((pwDid, t) -> {
                                                if (t != null) {
                                                    Logger.getInstance().e("Failed to get pwDid: ", t);
                                                    result.completeExceptionally(t);
                                                    return;
                                                }
                                                try {
                                                    String jsonMsg = Messages.prepareUpdateMessage(pwDid, messageId);
                                                    UtilsApi.vcxUpdateMessages(MessageStatusType.ANSWERED, jsonMsg).whenComplete((v1, thr) -> {
                                                        if (thr != null) {
                                                            Logger.getInstance().e("Failed to update messages", thr);
                                                            result.completeExceptionally(thr);
                                                            return;
                                                        }
                                                        try {
                                                            DisclosedProofApi.proofSerialize(pHandle).whenComplete((sp, th) -> {
                                                                if (th != null) {
                                                                    Logger.getInstance().e("Failed to serialize proof: ", th);
                                                                    result.completeExceptionally(th);
                                                                } else {
                                                                    result.complete(sp);
                                                                }
                                                            });
                                                        } catch (VcxException ex) {
                                                            result.completeExceptionally(ex);
                                                        }
                                                    });
                                                } catch (Exception ex) {
                                                    result.completeExceptionally(ex);
                                                }
                                            });
                                        } catch (Exception ex) {
                                            result.completeExceptionally(ex);
                                        }
                                    });
                                } catch (VcxException ex) {
                                    result.completeExceptionally(ex);
                                }
                            });
                        } catch (VcxException ex) {
                            result.completeExceptionally(ex);
                        }
                    });
                } catch (Exception ex) {
                    result.completeExceptionally(ex);
                }
            });
        } catch (Exception ex) {
            result.completeExceptionally(ex);
        }
        return result;
    }

    /**
     * Reject proof request
     *
     * @param serializedConnection string containing serialized connection
     * @param serializedProof      string containing serialized proof request
     * @param messageId            message ID
     * @return CompletableFuture containing serialized proof
     */
    public static
    @NonNull
    CompletableFuture<String> reject(@NonNull String serializedConnection, @NonNull String serializedProof,
                                     @NonNull String messageId) {
        Logger.getInstance().i("Sending proof request response");
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            ConnectionApi.connectionDeserialize(serializedConnection).whenComplete((conHandle, err) -> {
                if (err != null) {
                    Logger.getInstance().e("Failed to deserialize connection: ", err);
                    result.completeExceptionally(err);
                    return;
                }
                try {
                    DisclosedProofApi.proofDeserialize(serializedProof).whenComplete((pHandle, e) -> {
                        if (e != null) {
                            Logger.getInstance().e("Failed to deserialize proof: ", e);
                            result.completeExceptionally(e);
                            return;
                        }
                        try {
                            DisclosedProofApi.proofReject(pHandle, conHandle).whenComplete((v, er) -> {
                                if (er != null) {
                                    Logger.getInstance().e("Failed to reject proof: ", er);
                                    result.completeExceptionally(er);
                                    return;
                                }
                                try {
                                    ConnectionApi.connectionGetPwDid(conHandle).whenComplete((pwDid, t) -> {
                                        if (t != null) {
                                            Logger.getInstance().e("Failed to get pwDid: ", t);
                                            result.completeExceptionally(t);
                                            return;
                                        }
                                        try {
                                            String jsonMsg = Messages.prepareUpdateMessage(pwDid, messageId);
                                            UtilsApi.vcxUpdateMessages(MessageStatusType.ANSWERED, jsonMsg).whenComplete((v1, thr) -> {
                                                if (thr != null) {
                                                    Logger.getInstance().e("Failed to update messages", thr);
                                                    result.completeExceptionally(thr);
                                                    return;
                                                }
                                                try {
                                                    DisclosedProofApi.proofSerialize(pHandle).whenComplete((sp, th) -> {
                                                        if (th != null) {
                                                            Logger.getInstance().e("Failed to serialize proof: ", th);
                                                            result.completeExceptionally(th);
                                                        } else {
                                                            result.complete(sp);
                                                        }
                                                    });
                                                } catch (VcxException ex) {
                                                    result.completeExceptionally(ex);
                                                }
                                            });
                                        } catch (Exception ex) {
                                            result.completeExceptionally(ex);
                                        }
                                    });
                                } catch (Exception ex) {
                                    result.completeExceptionally(ex);
                                }
                            });
                        } catch (VcxException ex) {
                            result.completeExceptionally(ex);
                        }
                    });
                } catch (Exception ex) {
                    result.completeExceptionally(ex);
                }
            });
        } catch (Exception ex) {
            result.completeExceptionally(ex);
        }
        return result;
    }

    /**
     * Temporary method to extract credentials for proof request and prepare map that will be used later in proof request confirmation
     *
     * @param proofRequestCreds JSON string containing list of available credentials
     * @return JSON string containing prepared payload for proof request
     */
    public static String mapCredentials(String proofRequestCreds) {
        try {
            JSONObject result = new JSONObject();
            JSONObject resultAttrs = new JSONObject();
            result.put("attrs", resultAttrs);
            JSONObject data = new JSONObject(proofRequestCreds);
            JSONObject attrs = data.getJSONObject("attrs");
            for (Iterator<String> it = attrs.keys(); it.hasNext(); ) {
                String key = it.next();
                JSONArray credArray = attrs.getJSONArray(key);
                JSONObject cred = credArray.getJSONObject(0);
                JSONObject credHolder = new JSONObject();
                credHolder.put("credential", cred);
                resultAttrs.put(key, credHolder);
            }
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}