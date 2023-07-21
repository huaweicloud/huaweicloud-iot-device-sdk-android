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

import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.nio.charset.StandardCharsets;


public class RawDeviceMessage implements Parcelable {
    private byte[] payload;

    public RawDeviceMessage(Parcel in) {
        payload = in.createByteArray();
    }

    public RawDeviceMessage() {

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(payload);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RawDeviceMessage> CREATOR = new Creator<RawDeviceMessage>() {
        @Override
        public RawDeviceMessage createFromParcel(Parcel in) {
            return new RawDeviceMessage(in);
        }

        @Override
        public RawDeviceMessage[] newArray(int size) {
            return new RawDeviceMessage[size];
        }
    };

    public String toUTF8String() {
        return new String(payload, StandardCharsets.UTF_8);
    }

    public DeviceMessage toDeviceMessage() {
        return JsonUtil.convertJsonStringToObject(new String(payload, StandardCharsets.UTF_8),
                DeviceMessage.class);
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
}
