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

package com.huaweicloud.sdk.iot.device.bootstrap;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 设备引导信息
 */
public class BootstrapMessage implements Parcelable {

    /**
     * 设备接入IOT平台地址
     */
    private String address;

    protected BootstrapMessage(Parcel in) {
        address = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BootstrapMessage> CREATOR = new Creator<BootstrapMessage>() {
        @Override
        public BootstrapMessage createFromParcel(Parcel in) {
            return new BootstrapMessage(in);
        }

        @Override
        public BootstrapMessage[] newArray(int size) {
            return new BootstrapMessage[size];
        }
    };

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BootstrapMessage() {
    }

    @Override
    public String toString() {
        return "BootstrapMessage{"
                + "address='" + address + '\''
                + '}';
    }
}
