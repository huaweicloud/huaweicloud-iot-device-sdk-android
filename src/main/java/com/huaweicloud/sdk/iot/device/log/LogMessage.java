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

package com.huaweicloud.sdk.iot.device.log;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class LogMessage implements Parcelable {

    /**
     * 设备侧日志收集开关
     * on  开启设备侧日志收集功能
     * off 关闭设备侧日志收集开关
     */
    @SerializedName("switch")
    private String switchFlag;

    /**
     * 日志收集结束时间
     */
    @SerializedName("end_time")
    private String endTime;

    public LogMessage() {
    }

    protected LogMessage(Parcel in) {
        switchFlag = in.readString();
        endTime = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(switchFlag);
        dest.writeString(endTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LogMessage> CREATOR = new Creator<LogMessage>() {
        @Override
        public LogMessage createFromParcel(Parcel in) {
            return new LogMessage(in);
        }

        @Override
        public LogMessage[] newArray(int size) {
            return new LogMessage[size];
        }
    };

    public String getSwitchFlag() {
        return switchFlag;
    }

    public void setSwitchFlag(String switchFlag) {
        this.switchFlag = switchFlag;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "LogMessage{"
                + "switchFlag='" + switchFlag + '\''
                + ", endTime='" + endTime + '\''
                + '}';
    }
}
