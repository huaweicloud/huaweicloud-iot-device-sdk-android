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
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.util.List;

/**
 * 写属性操作
 */
public class PropsSet implements Parcelable {

    @SerializedName("object_device_id")
    String deviceId;

    List<ServiceProperty> services;

    public PropsSet() {
    }

    protected PropsSet(Parcel in) {
        deviceId = in.readString();
        services = in.readArrayList(getClass().getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deviceId);
        dest.writeList(services);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PropsSet> CREATOR = new Creator<PropsSet>() {
        @Override
        public PropsSet createFromParcel(Parcel in) {
            return new PropsSet(in);
        }

        @Override
        public PropsSet[] newArray(int size) {
            return new PropsSet[size];
        }
    };

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<ServiceProperty> getServices() {
        return services;
    }

    public void setServices(List<ServiceProperty> services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return JsonUtil.convertObject2String(this);
    }
}
