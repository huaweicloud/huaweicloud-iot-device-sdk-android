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

package com.huaweicloud.sdk.iot.device.transport;

/**
 * 连接监听器
 */
public interface ConnectListener {

    /**
     * 连接丢失通知
     *
     * @param cause 连接丢失原因
     */
    void connectionLost(Throwable cause);

    /**
     * 连接成功通知
     *
     * @param reconnect 是否为重连
     * @param serverURI 服务端地址
     */
    void connectComplete(boolean reconnect, String serverURI);
}
