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
 * Provides APIs related to connections between the platform and the device.
 */
public interface Connection {

    /**
     * Creates a connection.
     */
    void connect();

    /**
     * Publishes a message.
     *
     * @param message Indicates the message to publish.
     * @param listener Indicates a listener to listen to message publish.
     */
    void publishMessage(RawMessage message, ActionListener listener);

    /**
     * Closes the connection.
     */
    void close();

    /**
     * Checks whether the device is connected to the platform.
     *
     * @return Returns true if the device is connected to the platform; returns false otherwise.
     */
    boolean isConnected();

    /**
     * Sets a connection listener.
     *
     * @param connectListener Indicates the listener to set.
     */
    void setConnectListener(ConnectListener connectListener);

    /**
     * @param topic Indicates the custom topic to subscribe. The SDK automatically subscribes to system topics.
     * @param actionListener Indicates a listener to listen to whether the subscription is successful.
     * @param qos Indicates a QoS level.
     */
    void subscribeTopic(String topic, ActionListener actionListener, int qos);

    /**
     * 设置是否支持退避重连， 默认支持。
     * 退避重连指设备初始化连接过程中，遇到连接失败的情况下，采用指数级算法重连。
     * 账号密码错误不会重连。
     *
     * @param flag true表示支持退避重连，false表示不支持退避重连
     */
    void setAutoConnect(boolean flag);

    /**
     * 设置MQTT连接的类型， 默认为设备接入
     *
     * @param type 0设备接入，1设备引导
     */
    void setConnectType(int type);
}
