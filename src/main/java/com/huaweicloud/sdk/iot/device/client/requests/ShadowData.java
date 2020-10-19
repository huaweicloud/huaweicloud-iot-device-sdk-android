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

/**
 * 服务影子数据
 */
public class ShadowData implements Parcelable {

    /**
     * 服务id，和设备模型里一致
     */
    @SerializedName("service_id")
    private String serviceId;

    /**
     * 设备影子desired区的属性列表
     */
    @SerializedName("desired")
    private PropertiesData desiredData;

    /**
     * 设备影子reported区的属性列表
     */
    @SerializedName("reported")
    private PropertiesData reportedData;

    /**
     * 设备影子版本信息
     */
    private Integer version;

    public ShadowData() {
    }

    protected ShadowData(Parcel in) {
        serviceId = in.readString();
        desiredData = in.readParcelable(PropertiesData.class.getClassLoader());
        reportedData = in.readParcelable(PropertiesData.class.getClassLoader());
        if (in.readByte() == 0) {
            version = null;
        } else {
            version = in.readInt();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(serviceId);
        dest.writeParcelable(desiredData, flags);
        dest.writeParcelable(reportedData, flags);
        if (version == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(version);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ShadowData> CREATOR = new Creator<ShadowData>() {
        @Override
        public ShadowData createFromParcel(Parcel in) {
            return new ShadowData(in);
        }

        @Override
        public ShadowData[] newArray(int size) {
            return new ShadowData[size];
        }
    };

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public PropertiesData getDesiredData() {
        return desiredData;
    }

    public void setDesiredData(PropertiesData desiredData) {
        this.desiredData = desiredData;
    }

    public PropertiesData getReportedData() {
        return reportedData;
    }

    public void setReportedData(PropertiesData reportedData) {
        this.reportedData = reportedData;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ShadowData{"
                + "serviceId='" + serviceId + '\''
                + ", desiredData=" + desiredData
                + ", reportedData=" + reportedData
                + ", version=" + version
                + '}';
    }
}
