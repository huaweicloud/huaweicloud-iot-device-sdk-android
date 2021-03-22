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
 * Provides listeners to listen to connections between the platform and devices.
 */
public interface ConnectListener {

    /**
     * Called when the connection is lost.
     *
     * @param cause Indicates the failure cause.
     */
    void connectionLost(Throwable cause);

    /**
     * Called when the connection is complete.
     *
     * @param reconnect Indicates whether it is a reconnection.
     * @param serverURI Indicates the server URI.
     */
    void connectComplete(boolean reconnect, String serverURI);
}
