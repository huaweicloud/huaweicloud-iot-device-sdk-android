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

package com.huaweicloud.sdk.iot.device.gateway.requests;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Provides APIs related to device statuses.
 */
public class DeviceStatus implements Parcelable {

    @SerializedName("device_id")
    String deviceId;

    String status;

    public DeviceStatus() {
    }

    protected DeviceStatus(Parcel in) {
        deviceId = in.readString();
        status = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deviceId);
        dest.writeString(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DeviceStatus> CREATOR = new Creator<DeviceStatus>() {
        @Override
        public DeviceStatus createFromParcel(Parcel in) {
            return new DeviceStatus(in);
        }

        @Override
        public DeviceStatus[] newArray(int size) {
            return new DeviceStatus[size];
        }
    };

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
