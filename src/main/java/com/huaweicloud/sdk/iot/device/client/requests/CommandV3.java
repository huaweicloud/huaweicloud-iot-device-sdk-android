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

import java.util.Map;

/**
 * 设备命令V3
 */
public class CommandV3 implements Parcelable {

    private String msgType;
    private String serviceId;
    private int mid;
    private String cmd;
    private Map<String, Object> paras;

    protected CommandV3(Parcel in) {
        msgType = in.readString();
        serviceId = in.readString();
        mid = in.readInt();
        cmd = in.readString();
        paras = in.readHashMap(getClass().getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(msgType);
        dest.writeString(serviceId);
        dest.writeInt(mid);
        dest.writeString(cmd);
        dest.writeMap(paras);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CommandV3> CREATOR = new Creator<CommandV3>() {
        @Override
        public CommandV3 createFromParcel(Parcel in) {
            return new CommandV3(in);
        }

        @Override
        public CommandV3[] newArray(int size) {
            return new CommandV3[size];
        }
    };

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public Map<String, Object> getParas() {
        return paras;
    }

    public void setParas(Map<String, Object> paras) {
        this.paras = paras;
    }
}
