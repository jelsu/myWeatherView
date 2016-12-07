package com.teaching.jelus.myweatherview;

public class DataEvent {
    private String mMessageType;
    private String mMessage;

    public DataEvent(String messageType, String message) {
        mMessageType = messageType;
        mMessage = message;
    }

    public String getMessageType() {
        return mMessageType;
    }

    public String getMessage() {
        return mMessage;
    }
}
