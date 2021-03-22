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

package com.huaweicloud.sdk.iot.device.bootstrap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.constant.BaseConstant;
import com.huaweicloud.sdk.iot.device.constant.IotDeviceIntent;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.transport.Connection;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.transport.RawMessageListener;
import com.huaweicloud.sdk.iot.device.transport.mqtt.MqttConnection;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.security.KeyStore;


/**
 * 引导客户端，用于设备引导来获取服务端地址
 */
public class BootstrapClient implements RawMessageListener {

    private static final String TAG = "BootstrapClient";

    private String deviceId;
    private Connection connection;
    private Context mContext;
    private BootstrapConnectReceiver bootstrapConnectReceiver = new BootstrapConnectReceiver();

    class BootstrapConnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (IotDeviceIntent.ACTION_IOT_DEVICE_BOOTSTRAP_CONNECT.equals(intent.getAction())) {
                int status = intent.getIntExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
                switch (status) {
                    case BaseConstant.STATUS_SUCCESS:
                        //引导获取设备
                        handleBootstrap();
                        break;
                    case BaseConstant.STATUS_FAIL:
                        String errMsg = intent.getStringExtra(BaseConstant.COMMON_ERROR);
                        Intent sendIntent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_BOOTSTRAP);
                        intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
                        intent.putExtra(BaseConstant.COMMON_ERROR, errMsg);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(sendIntent);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void handleBootstrap() {

        final String bsTopic = "$oc/devices/" + this.deviceId + "/sys/bootstrap/down";
        connection.subscribeTopic(bsTopic, new ActionListener() {
            @Override
            public void onSuccess(Object context) {

            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                Log.e(TAG, "subscribeTopic failed:" + bsTopic);
                Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_BOOTSTRAP);
                intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
                intent.putExtra(BaseConstant.COMMON_ERROR, var2.getMessage());
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        }, 0);

        final String topic = "$oc/devices/" + this.deviceId + "/sys/bootstrap/up";
        RawMessage rawMessage = new RawMessage(topic, "");

        connection.publishMessage(rawMessage, new ActionListener() {
            @Override
            public void onSuccess(Object context) {

            }

            @Override
            public void onFailure(Object context, Throwable var2) {

                Log.e(TAG, "publishMessage failed:" + topic);
                Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_BOOTSTRAP);
                intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
                intent.putExtra(BaseConstant.COMMON_ERROR, var2.getMessage());
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        });
    }

    /**
     * 构造函数，使用密码创建
     *
     * @param context      上下文
     * @param bootstrapUri bootstrap server地址，比如ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId     设备id
     * @param deviceSecret 设备密码
     */
    public BootstrapClient(Context context, String bootstrapUri, String deviceId, String deviceSecret) {

        ClientConf clientConf = new ClientConf();
        clientConf.setServerUri(bootstrapUri);
        clientConf.setDeviceId(deviceId);
        clientConf.setSecret(deviceSecret);
        this.deviceId = deviceId;
        this.mContext = context;
        this.connection = new MqttConnection(context, clientConf, this);
        Log.i(TAG, "create BootstrapClient: " + clientConf.getDeviceId());

    }

    /**
     * 构造函数，使用证书创建
     *
     * @param context      上下文
     * @param bootstrapUri bootstrap server地址，比如ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId     设备id
     * @param keyStore     证书容器
     * @param keyPassword  证书密码
     */
    public BootstrapClient(Context context, String bootstrapUri, String deviceId, KeyStore keyStore, String keyPassword) {

        ClientConf clientConf = new ClientConf();
        clientConf.setServerUri(bootstrapUri);
        clientConf.setDeviceId(deviceId);
        clientConf.setKeyPassword(keyPassword);
        clientConf.setKeyStore(keyStore);
        this.deviceId = deviceId;
        this.mContext = context;
        this.connection = new MqttConnection(context, clientConf, this);
        Log.i(TAG, "create BootstrapClient: " + clientConf.getDeviceId());

    }

    /**
     * 构造函数，自注册场景下证书创建
     *
     * @param context      上下文
     * @param bootstrapUri bootstrap server地址，比如ssl://iot-bs.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId     设备id
     * @param keyStore     证书容器
     * @param keyPassword  证书密码
     * @param scopeId      scopeId, 自注册场景可从物联网平台获取
     */
    public BootstrapClient(Context context, String bootstrapUri, String deviceId, KeyStore keyStore, String keyPassword, String scopeId) {
        ClientConf clientConf = new ClientConf();
        clientConf.setServerUri(bootstrapUri);
        clientConf.setDeviceId(deviceId);
        clientConf.setKeyStore(keyStore);
        clientConf.setKeyPassword(keyPassword);
        clientConf.setScopeId(scopeId);
        this.deviceId = deviceId;
        this.mContext = context;
        this.connection = new MqttConnection(context, clientConf, this);
        Log.i(TAG, "create BootstrapClient: " + clientConf.getDeviceId());
    }

    @Override
    public void onMessageReceived(RawMessage message) {

        if (message.getTopic().contains("/sys/bootstrap/down")) {
            BootstrapMessage bootstrapMessage = JsonUtil.convertJsonStringToObject(message.toString(), BootstrapMessage.class);
            Log.i(TAG, "bootstrap ok address:" + bootstrapMessage);

            Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_BOOTSTRAP);
            intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_SUCCESS);
            intent.putExtra(BaseConstant.BOOTSTRAP_MESSAGE, bootstrapMessage);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

        }
    }

    /**
     * 发起设备引导
     */
    public void bootstrap() {
        connection.setConnectType(1);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(bootstrapConnectReceiver, new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_BOOTSTRAP_CONNECT));
        connection.connect();
    }

    /**
     * 关闭客户端
     */
    public void close() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(bootstrapConnectReceiver);
        connection.close();
    }

    /**
     * 设置是否支持退避重连， 默认支持。
     * 退避重连指设备初始化连接过程中，遇到连接失败的情况下，采用指数级算法重连。
     * 账号密码错误不会重连。
     *
     * @param flag true表示支持退避重连，false表示不支持退避重连
     */
    public void setAutoConnect(boolean flag) {
        if (connection != null) {
            connection.setAutoConnect(flag);
        }
    }
}

