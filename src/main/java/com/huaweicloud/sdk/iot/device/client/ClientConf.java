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

package com.huaweicloud.sdk.iot.device.client;


import java.security.KeyStore;

/**
 * Provides APIs for client configurations.
 */

public class ClientConf {


    /**
     * Indicates a device ID, which is obtained when the device is registered on the platform. It is in the format of productId_nodeId.
     */
    private String deviceId;


    /**
     * Indicates a secret. This parameter is mandatory when secret authentication is used.
     */
    private String secret;

    /**
     * Indicates a device access address, for example, tcp://localhost:1883 or ssl://localhost:8883.
     */
    private String serverUri;

    /**
     * Indicates a protocol. Currently, only MQTT is supported.
     */
    private String protocol;

    /**
     * Indicates the size of a message queue, which caches messages for offline devices. The default value is 100. This parameter is valid only for MQTT devices.
     */
    private Integer offlineBufferSize;

    /**
     * Indicates a certificate keystore. The keyStore and keyPassword parameters must be passed during certificate authentication.
     */
    private KeyStore keyStore;

    /**
     * Indicates a private key password.
     */
    private String keyPassword;

    /**
     * Indicates a QoS level. The value can be 0 or 1. The default value is 1. This parameter is valid only for MQTT devices.
     */
    private int qos = 1;

    /**
     * Indicates a scope ID, which is used in the self-registration scenario during device provisioning.
     */
    private String scopeId;

    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets a device ID.
     *
     * @param deviceId Indicates the device ID to set.
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSecret() {
        return secret;
    }

    /**
     * Sets a secret.
     *
     * @param secret Indicates the secret to set.
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getServerUri() {
        return serverUri;
    }

    /**
     * Sets a server URI.
     *
     * @param serverUri Indicates the server URI to set.
     */
    public void setServerUri(String serverUri) {
        this.serverUri = serverUri;
    }

    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets a protocol for device access.
     *
     * @param protocol Indicates the protocol to set.
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getOfflineBufferSize() {
        return offlineBufferSize;
    }

    /**
     * Sets the size of a message queue, which caches messages for offline devices.
     *
     * @param offlineBufferSize Indicates the size to set.
     */
    public void setOfflineBufferSize(Integer offlineBufferSize) {
        this.offlineBufferSize = offlineBufferSize;
    }

    public int getQos() {
        return qos;
    }

    /**
     * Sets a client QoS level.
     *
     * @param qos Indicates the QoS level to set.
     */
    public void setQos(int qos) {
        this.qos = qos;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    /**
     * Sets a private key password.
     *
     * @param keyPassword Indicates the password to set.
     */
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    /**
     * Sets a certificate keystore.
     *
     * @param keyStore Indicates the keystore to set.
     */
    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }
}
