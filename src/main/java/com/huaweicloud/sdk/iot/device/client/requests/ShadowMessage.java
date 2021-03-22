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

import java.util.List;

/**
 * Sets device shadow data.
 */
public class ShadowMessage implements Parcelable {

    /**
     * Indicates the ID of the device for which the device shadow data is to be set.
     */
    @SerializedName("object_device_id")
    private String deviceId;

    /**
     * Indicates the device shadow data.
     */
    private List<ShadowData> shadow;

    public ShadowMessage() {
    }

    protected ShadowMessage(Parcel in) {
        deviceId = in.readString();
        shadow = in.createTypedArrayList(ShadowData.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deviceId);
        dest.writeTypedList(shadow);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ShadowMessage> CREATOR = new Creator<ShadowMessage>() {
        @Override
        public ShadowMessage createFromParcel(Parcel in) {
            return new ShadowMessage(in);
        }

        @Override
        public ShadowMessage[] newArray(int size) {
            return new ShadowMessage[size];
        }
    };

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<ShadowData> getShadow() {
        return shadow;
    }

    public void setShadow(List<ShadowData> shadow) {
        this.shadow = shadow;
    }

    @Override
    public String toString() {
        return "ShadowMessage{"
                + "deviceId='" + deviceId + '\''
                + ", shadow=" + shadow
                + '}';
    }
}
