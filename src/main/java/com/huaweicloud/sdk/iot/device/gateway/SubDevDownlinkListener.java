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
     * 子设备命令下发通知
     *
     * @param requestId 请求id
     * @param command   命令
     */
    void onSubdevCommand(String requestId, Command command);

    /**
     * 子设备属性设置通知
     *
     * @param requestId 请求id
     * @param propsSet  属性设置
     */
    void onSubdevPropertiesSet(String requestId, PropsSet propsSet);

    /**
     * 子设备读属性通知
     *
     * @param requestId 请求id
     * @param propsGet  属性查询
     */
    void onSubdevPropertiesGet(String requestId, PropsGet propsGet);

    /**
     * 子设备消息下发
     *
     * @param message 设备消息
     */
    void onSubdevMessage(DeviceMessage message);
}
