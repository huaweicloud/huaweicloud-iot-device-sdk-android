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

import java.util.Map;

/**
 * Provides V3 APIs related to device properties.
 */
public class ServiceData {
    /**
     * Indicates a service ID, which must be the same as that defined in the product model.
     */
    private String serviceId;

    /**
     * Indicates the time when the property value was changed, in the format of yyyyMMddTHHmmssZ. It is optional. If it is set to NULL, the time when the platform received the property value is used.
     */
    private String eventTime;

    /**
     * Indicates a property value. The property field is defined in the product model.
     */
    private Map<String, Object> serviceData;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public Map<String, Object> getServiceData() {
        return serviceData;
    }

    public void setServiceData(Map<String, Object> serviceData) {
        this.serviceData = serviceData;
    }

    @Override
    public String toString() {
        return "ServiceData{"
                + "serviceId='" + serviceId + '\''
                + ", eventTime='" + eventTime + '\''
                + ", serviceData=" + serviceData
                + '}';
    }
}
