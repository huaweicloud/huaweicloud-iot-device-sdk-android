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

package com.huaweicloud.sdk.iot.device.service;

import com.huaweicloud.sdk.iot.device.client.IotResult;
import com.huaweicloud.sdk.iot.device.client.requests.Command;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;

import java.util.Map;

/**
 * Provides APIs related to services.
 */
public interface IService {


    /**
     * Called when a property read request is received.
     *
     * @param fields Indicates the names of fields to read. If it is set to NULL, all fields are read.
     * @return Returns the property values.
     */
    Map<String, Object> onRead(String... fields);

    /**
     * Called when a property write request is received.
     *
     * @param properties Indicates the desired properties.
     * @return Returns the operation result, which is a JSON object.
     */
    IotResult onWrite(Map<String, Object> properties);


    /**
     * Called when a command delivered by the platform is received.
     *
     * @param command Indicates a command request.
     * @return Returns a command response.
     */
    CommandRsp onCommand(Command command);

    /**
     * Called when an event delivered by the platform is received.
     *
     * @param deviceEvent Indicates the event.
     */
    void onEvent(DeviceEvent deviceEvent);

}
