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
 * 原始消息类
 */
public class RawMessage implements Parcelable {

    /**
     * 消息主题
     */
    private String topic;

    /**
     * 消息体
     */
    private byte[] payload;

    /**
     * qos,0或1，默认为1
     */
    private int qos;

    /**
     * 构造函数
     *
     * @param topic   消息topic
     * @param payload 消息体
     */
    public RawMessage(String topic, String payload) {
        this.topic = topic;
        this.payload = payload.getBytes(StandardCharsets.UTF_8);
        this.qos = 1;
    }

    /**
     * 构造函数
     *
     * @param topic   消息topic
     * @param payload 消息体
     * @param qos     qos,0或1
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
     * 查询topic
     *
     * @return 消息topic
     */
    public String getTopic() {
        return topic;
    }


    /**
     * 设置topic
     *
     * @param topic 消息topic
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * 查询消息体
     *
     * @return 消息体
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * 设置消息体
     *
     * @param payload 消息体
     */
    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    /**
     * 查询qos
     *
     * @return qos
     */
    public int getQos() {
        return qos;
    }

    /**
     * 设置qos，0或1
     *
     * @param qos qos
     */
    public void setQos(int qos) {
        this.qos = qos;
    }

    @Override
    public String toString() {
        return new String(payload);
    }
}
