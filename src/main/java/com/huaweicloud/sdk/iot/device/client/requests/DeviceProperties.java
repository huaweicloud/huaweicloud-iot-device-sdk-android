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

import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.util.List;

/**
 * Provides APIs related to device properties.
 */
public class DeviceProperties {

    /**
     * Indicates service properties.
     */
    List<ServiceProperty> services;

    public List<ServiceProperty> getServices() {
        return services;
    }

    public void setServices(List<ServiceProperty> services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return JsonUtil.convertObject2String(this);
    }
}
