package me.connect.sdk.java.message;

import java.util.Arrays;
import java.util.List;

import me.connect.sdk.java.Logger;

public enum MessageType {

    CREDENTIAL,
    CONNECTION_RESPONSE,
    ACK,

    CREDENTIAL_OFFER,
    PROOF_REQUEST,
    QUESTION;

    private static final List<String> CREDENTIAL_OFFER_VALUES = Arrays.asList("credOffer", "CRED_OFFER", "credential-offer");
    private static final List<String> PROOF_REQUEST_VALUES = Arrays.asList("proofReq", "PROOF_REQUEST", "presentation-request");
    private static final List<String> QUESTION_VALUES = Arrays.asList("Question", "QUESTION", "committed-question", "question");

    private static final String CREDENTIAL_VALUES = "credential";
    private static final String CONNECTION_RESPONSE_VALUES = "connections/1.0/response";
    private static final String ACK_VALUES = "ack";


    public boolean matches(String type) {
        List<String> listToCheck;
        switch (this) {
            case QUESTION:
                listToCheck = QUESTION_VALUES;
                break;
            case PROOF_REQUEST:
                listToCheck = PROOF_REQUEST_VALUES;
                break;
            case CREDENTIAL_OFFER:
                listToCheck = CREDENTIAL_OFFER_VALUES;
                break;
            default:
                Logger.getInstance().w("Message type " + this + "matching was not implemented");
                return false;
        }
        return listToCheck.contains(type);
    }

    public boolean matchesValue(String type) {
        String valueCheck;
        switch (this) {
            case CREDENTIAL:
                valueCheck = CREDENTIAL_VALUES;
                break;
            case CONNECTION_RESPONSE:
                valueCheck = CONNECTION_RESPONSE_VALUES;
                break;
            case ACK:
                valueCheck = ACK_VALUES;
                break;
            default:
                Logger.getInstance().w("Message type " + this + "matching was not implemented");
                return false;
        }
        return type.contains(valueCheck);
    }
}
