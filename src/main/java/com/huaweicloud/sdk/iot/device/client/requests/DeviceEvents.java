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

import com.google.gson.annotations.SerializedName;

import java.util.List;


/**
 * Provides APIs related to device events.
 */
public class DeviceEvents {

    /**
     * Indicates a device ID.
     */
    @SerializedName("object_device_id")
    String deviceId;

    /**
     * Indicates service events.
     */
    @SerializedName("services")
    List<DeviceEvent> services;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<DeviceEvent> getServices() {
        return services;
    }

    public void setServices(List<DeviceEvent> services) {
        this.services = services;
    }
}
