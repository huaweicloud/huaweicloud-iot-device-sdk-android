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

package com.huaweicloud.sdk.iot.device.gateway;


import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceInfo;
import com.huaweicloud.sdk.iot.device.gateway.requests.SubDevicesInfo;

/**
 * Persistently stores child device details.
 */
public interface SubDevicesPersistence {

    DeviceInfo getSubDevice(String nodeId);

    int addSubDevices(SubDevicesInfo subDevicesInfo);

    int deleteSubDevices(SubDevicesInfo subDevicesInfo);

    long getVersion();

}
