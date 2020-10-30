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
 * 服务的事件
 */
public class DeviceEvent {

    /**
     * 事件所属服务id
     */
    @SerializedName("service_id")
    String serviceId;

    /**
     * 事件类型
     */
    @SerializedName("event_type")
    String eventType;

    /**
     * 事件发生的事件
     */
    @SerializedName("event_time")
    String eventTime;

    /**
     * 事件具体的参数
     */
    Map<String, Object> paras;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public Map<String, Object> getParas() {
        return paras;
    }

    public void setParas(Map<String, Object> paras) {
        this.paras = paras;
    }
}
