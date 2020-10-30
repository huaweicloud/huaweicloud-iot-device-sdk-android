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

import java.util.Map;

/**
 * 服务的属性
 */
public class ServiceProperty {

    /**
     * 服务id，和设备模型里一致
     */
    @SerializedName("service_id")
    String serviceId;

    /**
     * 属性值，具体字段由设备模型定义
     */
    Map<String, Object> properties;

    /**
     * 属性变化的时间，格式：yyyyMMddTHHmmssZ，可选，不带以平台收到的时间为准
     */
    @SerializedName("event_time")
    String eventTime;


    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    @Override
    public String toString() {
        return "ServiceProperty{"
                + "serviceId='" + serviceId + '\''
                + ", properties=" + properties
                + ", eventTime='" + eventTime + '\''
                + '}';
    }
}
