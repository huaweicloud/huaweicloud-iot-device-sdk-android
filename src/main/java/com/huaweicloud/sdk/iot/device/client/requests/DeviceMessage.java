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
 * Provides APIs related to device messages.
 */
public class DeviceMessage implements Parcelable {

    /**
     * Indicates a device ID. It is optional. The default value is the device ID of the client.
     */
    @SerializedName("object_device_id")
    String deviceId;

    /**
     * Indicates a message name. It is optional.
     */
    String name;


    /**
     * Indicates a message ID. It is optional.
     */
    String id;


    /**
     * Indicates the message content.
     */
    String content;

    /**
     * Default constructor used to create a DeviceMessage object.
     */
    public DeviceMessage() {

    }

    /**
     * Constructor used to create a DeviceMessage object.
     *
     * @param message Indicates the message content.
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
     * Obtains the device ID.
     *
     * @return Returns the device ID.
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets a device ID.
     *
     * @param deviceId Indicates the device ID to set. If this paramter is set to NULL, the default device ID of the client is used.
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Obtains the message name.
     *
     * @return Returns the message name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets a message name.
     *
     * @param name Indicates the message name to set. The default value is NULL.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Obtains the message ID.
     *
     * @return Returns the message ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets a message ID.
     *
     * @param id Indicates the message ID to set. The default value is NULL.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Obtains the message content.
     *
     * @return Returns the message content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the message content.
     *
     * @param content Indicates the message content to set.
     */
    public void setContent(String content) {
        this.content = content;
    }

}
