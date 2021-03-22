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

/**
 * Provides APIs related to device details.
 */
public class DeviceInfo implements Parcelable {

    @SerializedName("parent_device_id")
    String parent;

    @SerializedName("node_id")
    String nodeId;

    @SerializedName("device_id")
    String deviceId;

    String name;
    String description;

    @SerializedName("manufacturer_id")
    String manufacturerId;

    String model;

    @SerializedName("product_id")
    String productId;

    @SerializedName("fw_version")
    String fwVersion;

    @SerializedName("sw_version")
    String swVersion;

    String status;

    public DeviceInfo() {
    }

    protected DeviceInfo(Parcel in) {
        parent = in.readString();
        nodeId = in.readString();
        deviceId = in.readString();
        name = in.readString();
        description = in.readString();
        manufacturerId = in.readString();
        model = in.readString();
        productId = in.readString();
        fwVersion = in.readString();
        swVersion = in.readString();
        status = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(parent);
        dest.writeString(nodeId);
        dest.writeString(deviceId);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(manufacturerId);
        dest.writeString(model);
        dest.writeString(productId);
        dest.writeString(fwVersion);
        dest.writeString(swVersion);
        dest.writeString(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DeviceInfo> CREATOR = new Creator<DeviceInfo>() {
        @Override
        public DeviceInfo createFromParcel(Parcel in) {
            return new DeviceInfo(in);
        }

        @Override
        public DeviceInfo[] newArray(int size) {
            return new DeviceInfo[size];
        }
    };

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getManufacturerId() {
        return manufacturerId;
    }

    public void setManufacturerId(String manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getFwVersion() {
        return fwVersion;
    }

    public void setFwVersion(String fwVersion) {
        this.fwVersion = fwVersion;
    }

    public String getSwVersion() {
        return swVersion;
    }

    public void setSwVersion(String swVersion) {
        this.swVersion = swVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "DeviceInfo{"
                + "parent='" + parent + '\''
                + ", nodeId='" + nodeId + '\''
                + ", deviceId='" + deviceId + '\''
                + ", name='" + name + '\''
                + ", description='" + description + '\''
                + ", manufacturerId='" + manufacturerId + '\''
                + ", model='" + model + '\''
                + ", productId='" + productId + '\''
                + ", fwVersion='" + fwVersion + '\''
                + ", swVersion='" + swVersion + '\''
                + ", status='" + status + '\''
                + '}';
    }
}
