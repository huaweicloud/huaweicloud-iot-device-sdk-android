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

public class SubDevicesDeleteInfo implements Parcelable {

    /**
     * Indicates a list of deleted child device IDs.
     */
    @SerializedName("successful_devices")
    private List<String> successfulDevices;

    /**
     * Indicates the cause of a child device deletion failure.
     */
    @SerializedName("failed_devices")
    private List<FailedReason> failedDevices;

    protected SubDevicesDeleteInfo(Parcel in) {
        successfulDevices = in.createStringArrayList();
        failedDevices = in.createTypedArrayList(FailedReason.CREATOR);
    }

    public SubDevicesDeleteInfo() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(successfulDevices);
        dest.writeTypedList(failedDevices);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SubDevicesDeleteInfo> CREATOR = new Creator<SubDevicesDeleteInfo>() {
        @Override
        public SubDevicesDeleteInfo createFromParcel(Parcel in) {
            return new SubDevicesDeleteInfo(in);
        }

        @Override
        public SubDevicesDeleteInfo[] newArray(int size) {
            return new SubDevicesDeleteInfo[size];
        }
    };

    public List<String> getSuccessfulDevices() {
        return successfulDevices;
    }

    public void setSuccessfulDevices(List<String> successfulDevices) {
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
        return "SubDevicesDeleteInfo{"
                + "successfulDevices=" + successfulDevices
                + ", failedDevices=" + failedDevices
                + '}';
    }
}
