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

/**
 * 上行获取设备影子数据
 */
public class ShadowGet {

    /**
     * 需要获取设备影子的目标设备ID,可选
     */
    @SerializedName("object_device_id")
    private String deviceId;

    /**
     * 需要获取设备影子的设备服务ID,不带的话查询所有服务ID的设备影子数据,可选
     */
    @SerializedName("service_id")
    private String serviceId;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }
}
