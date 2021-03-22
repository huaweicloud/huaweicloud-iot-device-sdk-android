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

import com.huaweicloud.sdk.iot.device.client.requests.Command;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.PropsGet;
import com.huaweicloud.sdk.iot.device.client.requests.PropsSet;

public interface SubDevDownlinkListener {

    /**
     * Called when a child device command is received.
     *
     * @param requestId Indicates a request ID.
     * @param command Indicates the command.
     */
    void onSubdevCommand(String requestId, Command command);

    /**
     * Called when a child device property setting request is received.
     *
     * @param requestId Indicates a request ID.
     * @param propsSet Indicates the properties to set.
     */
    void onSubdevPropertiesSet(String requestId, PropsSet propsSet);

    /**
     * Called when a child device property query request is received.
     *
     * @param requestId Indicates a request ID.
     * @param propsGet Indicates the properties to query.
     */
    void onSubdevPropertiesGet(String requestId, PropsGet propsGet);

    /**
     *  Called when a child device message is received.
     *
     * @param message Indicates the message.
     */
    void onSubdevMessage(DeviceMessage message);
}
