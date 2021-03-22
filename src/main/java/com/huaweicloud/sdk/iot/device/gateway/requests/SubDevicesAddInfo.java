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

import java.util.List;

public class SubDevicesAddInfo implements Parcelable {

    /**
     * Indicates details about new child devices.
     */
    @SerializedName("successful_devices")
    private List<DeviceInfo> successfulDevices;

    public SubDevicesAddInfo() {
    }

    /**
     * Indicates the cause of a child device addition failure.
     */
    @SerializedName("failed_devices")
    private List<FailedReason> failedDevices;

    protected SubDevicesAddInfo(Parcel in) {
        successfulDevices = in.createTypedArrayList(DeviceInfo.CREATOR);
        failedDevices = in.createTypedArrayList(FailedReason.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(successfulDevices);
        dest.writeTypedList(failedDevices);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SubDevicesAddInfo> CREATOR = new Creator<SubDevicesAddInfo>() {
        @Override
        public SubDevicesAddInfo createFromParcel(Parcel in) {
            return new SubDevicesAddInfo(in);
        }

        @Override
        public SubDevicesAddInfo[] newArray(int size) {
            return new SubDevicesAddInfo[size];
        }
    };

    public List<DeviceInfo> getSuccessfulDevices() {
        return successfulDevices;
    }

    public void setSuccessfulDevices(List<DeviceInfo> successfulDevices) {
        this.successfulDevices = successfulDevices;
    }

    public List<FailedReason> getFailedDevices() {
        return failedDevices;
    }

    public void setFailedDevices(List<FailedReason> failedDevices) {
        this.failedDevices = failedDevices;
    }

    @Override
    public String toString() {
        return "SubDevicesAddInfo{"
                + "successfulDevices=" + successfulDevices
                + ", failedDevices=" + failedDevices
                + '}';
    }
}
