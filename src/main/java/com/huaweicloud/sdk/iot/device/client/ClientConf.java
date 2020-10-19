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
 * 客户端配置
 */

public class ClientConf {


    /**
     * 设备id，在平台注册设备获得，生成规则：productId_nodeId
     */
    private String deviceId;


    /**
     * 设备密码，使用密码认证时填写
     */
    private String secret;

    /**
     * 设备接入平台地址，比如tcp://localhost:1883 或者 ssl://localhost:8883
     */
    private String serverUri;

    /**
     * 协议类型，当前仅支持mqtt
     */
    private String protocol;

    /**
     * 离线消息缓存队列大小，默认100，仅MQTT协议支持
     */
    private Integer offlineBufferSize;

    /**
     * keystore格式的证书，使用证书认证时传入keyStore和keyPassword
     */
    private KeyStore keyStore;

    /**
     * 私钥密码
     */
    private String keyPassword;

    /**
     * 客户端qos，0或1，默认1，仅MQTT协议支持
     */
    private int qos = 1;

    /**
     * scopeId,在设备发放的自注册场景下使用
     */
    private String scopeId;

    public String getDeviceId() {
        return deviceId;
    }

    /**
     * 设置设备id
     *
     * @param deviceId 设备id
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSecret() {
        return secret;
    }

    /**
     * 设置设备密码
     *
     * @param secret 设备密码
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getServerUri() {
        return serverUri;
    }

    /**
     * 设置服务端地址
     *
     * @param serverUri 服务端地址
     */
    public void setServerUri(String serverUri) {
        this.serverUri = serverUri;
    }

    public String getProtocol() {
        return protocol;
    }

    /**
     * 设置设备接入协议
     *
     * @param protocol 设备接入协议
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getOfflineBufferSize() {
        return offlineBufferSize;
    }

    /**
     * 设置离线缓存大小
     *
     * @param offlineBufferSize 离线缓存大小
     */
    public void setOfflineBufferSize(Integer offlineBufferSize) {
        this.offlineBufferSize = offlineBufferSize;
    }

    public int getQos() {
        return qos;
    }

    /**
     * 客户端qos
     *
     * @param qos 客户端qos
     */
    public void setQos(int qos) {
        this.qos = qos;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    /**
     * 设置私钥密码
     *
     * @param keyPassword 私钥密码
     */
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    /**
     * 设置证书仓库
     *
     * @param keyStore 证书仓库
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
