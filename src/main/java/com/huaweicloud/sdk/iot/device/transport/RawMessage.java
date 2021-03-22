/*Copyright 2020 Huawei Technologies Co., Ltd
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 *
 * */

package com.huaweicloud.sdk.iot.device.transport;

import android.os.Parcel;
import android.os.Parcelable;

import java.nio.charset.StandardCharsets;

/**
 * Provides APIs related to raw messages.
 */
public class RawMessage implements Parcelable {

    /**
     * Indicates a message topic.
     */
    private String topic;

    /**
     * Indicates a message body.
     */
    private byte[] payload;

    /**
     * Indicates a QoS level. The value can be 0 or 1. The default value is 1.
     */
    private int qos;

    /**
     * Constructor used to create a RawMessage object.
     *
     * @param topic Indicates the topic of the message to create.
     * @param payload Indicates the message body.
     */
    public RawMessage(String topic, String payload) {
        this.topic = topic;
        this.payload = payload.getBytes(StandardCharsets.UTF_8);
        this.qos = 1;
    }

    /**
     * Constructor used to create a RawMessage object.
     *
     * @param topic Indicates the topic of the message to create.
     * @param payload Indicates the message body.
     * @param qos Indicates a QoS level. The value can be 0 or 1. 
     */
    public RawMessage(String topic, String payload, int qos) {
        this.qos = qos;
        this.topic = topic;
        this.payload = payload.getBytes(StandardCharsets.UTF_8);
    }

    protected RawMessage(Parcel in) {
        topic = in.readString();
        payload = in.createByteArray();
        qos = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(topic);
        dest.writeByteArray(payload);
        dest.writeInt(qos);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RawMessage> CREATOR = new Creator<RawMessage>() {
        @Override
        public RawMessage createFromParcel(Parcel in) {
            return new RawMessage(in);
        }

        @Override
        public RawMessage[] newArray(int size) {
            return new RawMessage[size];
        }
    };

    /**
     * Obtains the topic of this message.
     *
     * @return Returns the topic.
     */
    public String getTopic() {
        return topic;
    }


    /**
     * Sets a topic for this message.
     *
     * @param topic Indicates the topic to set.
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * Obtains the body of this message.
     *
     * @return Returns the message body.
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Sets a body for this message.
     *
     * @param payload Indicates the message body to set.
     */
    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    /**
     * Obtains the QoS level.
     *
     * @return Returns the QoS level.
     */
    public int getQos() {
        return qos;
    }

    /**
     * Sets a QoS level.
     *
     * @param qos Indicates the QoS level to set. The value can be 0 or 1.
     */
    public void setQos(int qos) {
        this.qos = qos;
    }

    @Override
    public String toString() {
        return new String(payload);
    }
}
