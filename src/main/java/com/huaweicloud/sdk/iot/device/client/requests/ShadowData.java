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
