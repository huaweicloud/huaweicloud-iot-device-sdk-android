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

import java.util.Map;


/**
 * 设备命令
 */
public class Command implements Parcelable {

    @SerializedName("service_id")
    private String serviceId;

    @SerializedName("command_name")
    private String commandName;

    @SerializedName("object_device_id")
    private String deviceId;

    private Map<String, Object> paras;

    public Command() {
    }

    protected Command(Parcel in) {
        serviceId = in.readString();
        commandName = in.readString();
        deviceId = in.readString();
        paras = in.readHashMap(getClass().getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(serviceId);
        dest.writeString(commandName);
        dest.writeString(deviceId);
        dest.writeMap(paras);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Command> CREATOR = new Creator<Command>() {
        //创建普通对象
        @Override
        public Command createFromParcel(Parcel in) {
            return new Command(in);
        }

        //创建对象数组
        @Override
        public Command[] newArray(int size) {
            return new Command[size];
        }
    };

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Map<String, Object> getParas() {
        return paras;
    }

    public void setParas(Map<String, Object> paras) {
        this.paras = paras;
    }

    @Override
    public String toString() {
        return JsonUtil.convertObject2String(this);
    }
}
