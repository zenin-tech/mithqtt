package com.github.longkerdandy.mithqtt.api.internal;

import java.io.Serializable;

/**
 * Represent MQTT PUBLISH Message's VariableHeader and Payload
 */
@SuppressWarnings("unused")
public class Publish implements Serializable {

    private String topicName;
    private int packetId;
    private byte[] payload;
    private long timestamp;

    protected Publish() {
    }

    public Publish(String topicName, int packetId, byte[] payload) {
        this.topicName = topicName;
        this.packetId = packetId;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }

    public Publish(String topicName, int packetId, byte[] payload,long timestamp) {
        this.topicName = topicName;
        this.packetId = packetId;
        this.payload = payload;
        this.timestamp = timestamp;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getPacketId() {
        return packetId;
    }

    public void setPacketId(int packetId) {
        this.packetId = packetId;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
