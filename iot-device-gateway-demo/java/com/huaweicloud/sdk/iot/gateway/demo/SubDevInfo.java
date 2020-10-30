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

package com.huaweicloud.sdk.iot.gateway.demo;


import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceInfo;

import java.util.Map;

public class SubDevInfo {

    long version;

    Map<String, DeviceInfo> subdevices;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Map<String, DeviceInfo> getSubdevices() {
        return subdevices;
    }

    public void setSubdevices(Map<String, DeviceInfo> subdevices) {
        this.subdevices = subdevices;
    }

    @Override
    public String toString() {
        return "SubDevInfo{"
                + "version=" + version
                + ", subdevices=" + subdevices
                + '}';
    }
}
