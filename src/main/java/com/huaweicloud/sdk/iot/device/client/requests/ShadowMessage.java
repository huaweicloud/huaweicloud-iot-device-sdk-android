/*Copyright (c) <2020>, <Huawei Technologies Co., Ltd>
 * All rights reserved.
 * &Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used
 * to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 *
 * */

package com.huaweicloud.sdk.iot.device.client.requests;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 设备影子下行数据
 */
public class ShadowMessage implements Parcelable {

    /**
     * 设备影子的目标设备ID
     */
    @SerializedName("object_device_id")
    private String deviceId;

    /**
     * 服务影子数据
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
