package com.teaching.jelus.myweatherview;

public class DataEvent {
    private MessageType mType;
    private String mMessage;

    public DataEvent(MessageType type, String message) {
        mType = type;
        mMessage = message;
    }

    public MessageType getType() {
        return mType;
    }

    public String getMessage() {
        return mMessage;
    }
}
