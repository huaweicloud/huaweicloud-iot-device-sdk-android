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

public class FailedReason implements Parcelable {

    /**
     * 失败错误原因码
     */
    @SerializedName("error_code")
    private String errorCode;

    /**
     * 失败原因描述
     */
    @SerializedName("error_msg")
    private String errorMsg;

    /**
     * 子设备ID
     */
    @SerializedName("device_id")
    private String deviceId;

    /**
     * 设备标识
     */
    @SerializedName("node_id")
    private String nodeId;

    /**
     * 产品ID
     */
    @SerializedName("product_id")
    private String productId;

    public FailedReason() {
    }

    protected FailedReason(Parcel in) {
        errorCode = in.readString();
        errorMsg = in.readString();
        deviceId = in.readString();
        nodeId = in.readString();
        productId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(errorCode);
        dest.writeString(errorMsg);
        dest.writeString(deviceId);
        dest.writeString(nodeId);
        dest.writeString(productId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FailedReason> CREATOR = new Creator<FailedReason>() {
        @Override
        public FailedReason createFromParcel(Parcel in) {
            return new FailedReason(in);
        }

        @Override
        public FailedReason[] newArray(int size) {
            return new FailedReason[size];
        }
    };

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "FailedReason{"
                + "errorCode='" + errorCode + '\''
                + ", errorMsg='" + errorMsg + '\''
                + ", deviceId='" + deviceId + '\''
                + ", nodeId='" + nodeId + '\''
                + ", productId='" + productId + '\''
                + '}';
    }
}
