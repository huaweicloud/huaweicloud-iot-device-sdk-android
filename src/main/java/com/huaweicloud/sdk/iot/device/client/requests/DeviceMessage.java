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

package com.huaweicloud.sdk.iot.device.client.requests;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * 设备消息
 */
public class DeviceMessage implements Parcelable {

    /**
     * 设备id，可选，默认为客户端本身的设备id
     */
    @SerializedName("object_device_id")
    String deviceId;

    /**
     * 消息名，可选
     */
    String name;


    /**
     * 消息id，可选
     */
    String id;


    /**
     * 消息具体内容
     */
    String content;

    /**
     * 默认构造函数
     */
    public DeviceMessage() {

    }

    /**
     * 构造函数
     *
     * @param message 消息内容
     */
    public DeviceMessage(String message) {
        content = message;
    }

    protected DeviceMessage(Parcel in) {
        deviceId = in.readString();
        name = in.readString();
        id = in.readString();
        content = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deviceId);
        dest.writeString(name);
        dest.writeString(id);
        dest.writeString(content);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DeviceMessage> CREATOR = new Creator<DeviceMessage>() {
        @Override
        public DeviceMessage createFromParcel(Parcel in) {
            return new DeviceMessage(in);
        }

        @Override
        public DeviceMessage[] newArray(int size) {
            return new DeviceMessage[size];
        }
    };

    /**
     * 查询设备id
     *
     * @return 设备id
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * 设置设备id，不设置默认为客户端的设备id
     *
     * @param deviceId 设备id
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * 查询消息名
     *
     * @return 消息名
     */
    public String getName() {
        return name;
    }

    /**
     * 设置消息名，默认为空
     *
     * @param name 消息名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 查询消息id
     *
     * @return 消息id
     */
    public String getId() {
        return id;
    }

    /**
     * 设置消息id
     *
     * @param id 消息id，默认为空
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 查询消息内容
     *
     * @return 消息内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置消息内容
     *
     * @param content 消息内容
     */
    public void setContent(String content) {
        this.content = content;
    }

}
