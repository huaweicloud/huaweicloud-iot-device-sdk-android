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

package com.huaweicloud.sdk.iot.device.transport.mqtt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.constant.BaseConstant;
import com.huaweicloud.sdk.iot.device.constant.IotDeviceIntent;
import com.huaweicloud.sdk.iot.device.log.LogService;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.transport.ConnectListener;
import com.huaweicloud.sdk.iot.device.transport.Connection;
import com.huaweicloud.sdk.iot.device.transport.IoTSSLSocketFactory;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.transport.RawMessageListener;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;

import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.net.ssl.SSLContext;


/**
 * Provides APIs related to MQTT connections.
 */
public class MqttConnection implements Connection {

    private static final int DEFAULT_QOS = 1;
    private static final int DEFAULT_SUBSCRIBE_QOS = 0;
    private static final int DEFAULT_CONNECT_TIMEOUT = 60;
    private static final int DEFAULT_KEEPLIVE = 120;
    private static final String CONNECT_TYPE = "0";
    private static final String CHECK_TIME_STAMP = "0";
    private ClientConf clientConf;
    private MqttAsyncClient mqttAsyncClient;
    private String timeStamp;
    private ConnectListener connectListener;
    private RawMessageListener rawMessageListener;
    private Context mContext;
    private static int retryTimes = 0;
    private int minBackoff = 1000;
    private int maxBackoff = 4 * 60 * 1000; //4 minutes
    private int defaultBackoff = 1000;
    private SecureRandom secureRandom = new SecureRandom();
    private boolean autoConnect = true;
    private int connectType = 0; //设备连接类型，默认0为设备接入，1为设备引导
    private static final int CONNECT_TYPE_DEVICE = 0;     //设备接入
    private static final int CONNECT_TYPE_BOOTSTRAP = 1;  //设备引导

    private static final String TAG = "MqttConnection";

    public MqttConnection(Context mContext, ClientConf clientConf, RawMessageListener rawMessageListener) {
        this.mContext = mContext;
        this.clientConf = clientConf;
        this.rawMessageListener = rawMessageListener;
    }

    private MqttCallback mqttCallback = new MqttCallbackExtended() {

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            Log.i(TAG, "Mqtt client connected. address :" + serverURI + " ,reconnect=" + reconnect);
            saveLog(LogService.MQTT_CONNECTION_COMPLETE, "mqtt connectComplete reconnect:" + reconnect);
            if (reconnect) {
                subscribeTopic();
            }

            if (reconnect && connectListener != null) {
                connectListener.connectComplete(true, serverURI);
            }
        }

        @Override
        public void connectionLost(Throwable throwable) {
            Log.e(TAG, "Connection lost." + throwable);
            saveLog(LogService.MQTT_CONNECTION_LOST, ExceptionUtil.getBriefStackTrace(throwable));
            if (connectListener != null) {
                connectListener.connectionLost(throwable);
            }
        }

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
            Log.i(TAG, "messageArrived topic =  " + topic + ", msg = " + mqttMessage.toString());
            RawMessage rawMessage = new RawMessage(topic, mqttMessage.toString());
            try {
                if (rawMessageListener != null) {
                    rawMessageListener.onMessageReceived(rawMessage);
                }

            } catch (Exception e) {
                Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

        }
    };

    private IMqttActionListener iMqttActionListener;

    private void subscribeTopic() {
        subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/messages/down", null, DEFAULT_SUBSCRIBE_QOS);
        subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/commands/#", null, DEFAULT_SUBSCRIBE_QOS);
        subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/properties/set/#", null, DEFAULT_SUBSCRIBE_QOS);
        subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/properties/get/#", null, DEFAULT_SUBSCRIBE_QOS);
        subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/shadow/get/response/#", null, DEFAULT_SUBSCRIBE_QOS);
        subscribeTopic("$oc/devices/" + clientConf.getDeviceId() + "/sys/events/down", null, DEFAULT_SUBSCRIBE_QOS);
    }

    @Override
    public void connect() {
        try {
            SimpleDateFormat formart = new SimpleDateFormat("yyyyMMddHH");
            formart.setTimeZone(TimeZone.getTimeZone("UTC"));
            timeStamp = formart.format(new Date());

            String clientId = null;
            if (clientConf.getScopeId() == null) {
                clientId = clientConf.getDeviceId() + "_" + CONNECT_TYPE + "_" + CHECK_TIME_STAMP + "_" + timeStamp;
            } else {
                clientId = clientConf.getDeviceId() + "_" + CONNECT_TYPE + "_" + clientConf.getScopeId();
            }

            mqttAsyncClient = new MqttAsyncClient(clientConf.getServerUri(), clientId, new MemoryPersistence());
            iMqttActionListener = new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {

                    Log.i(TAG, "connect success " + clientConf.getServerUri());
                    saveLog(LogService.MQTT_CONNECTION_SUCCESS, "connect success");
                    retryTimes = 0;
                    DisconnectedBufferOptions bufferOptions = new DisconnectedBufferOptions();
                    bufferOptions.setBufferEnabled(true);
                    if (clientConf.getOfflineBufferSize() != null) {
                        bufferOptions.setBufferSize(clientConf.getOfflineBufferSize());
                    }
                    bufferOptions.setPersistBuffer(false);
                    bufferOptions.setDeleteOldestMessages(false);
                    mqttAsyncClient.setBufferOpts(bufferOptions);
                    if (connectType == CONNECT_TYPE_BOOTSTRAP) {
                        Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_BOOTSTRAP_CONNECT);
                        intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_SUCCESS);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    } else {
                        subscribeTopic();
                        Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_CONNECT);
                        intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_SUCCESS);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    }
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {

                    Log.i(TAG, "connect failed " + throwable.toString());
                    saveLog(LogService.MQTT_CONNECTION_FAILURE, ExceptionUtil.getBriefStackTrace(throwable));
                    MqttException mqttException = iMqttToken.getException();
                    int reasonCode = mqttException.getReasonCode();
                    boolean needToRetry = isNeedToRetry(reasonCode);
                    if (autoConnect && needToRetry) {
                        reconnect();
                    } else if (connectType == CONNECT_TYPE_BOOTSTRAP) {
                        Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_BOOTSTRAP_CONNECT);
                        intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
                        intent.putExtra(BaseConstant.COMMON_ERROR, throwable.getMessage());
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    } else {
                        Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_CONNECT);
                        intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
                        intent.putExtra(BaseConstant.COMMON_ERROR, throwable.getMessage());
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    }
                }
            };

            innerConnect();

        } catch (MqttException e) {
            Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
        }

    }

    private boolean isNeedToRetry(int reasonCode) {
        if (reasonCode == MqttException.REASON_CODE_FAILED_AUTHENTICATION) {
            return false;
        }

        return true;
    }

    private void innerConnect() {
        mqttAsyncClient.setCallback(mqttCallback);

        MqttConnectOptions options = new MqttConnectOptions();
        if (clientConf.getServerUri().contains("ssl:")) {

            try {
                SSLContext sslContext = IotUtil.getSSLContext(mContext, clientConf);
                options.setSocketFactory(new IoTSSLSocketFactory(sslContext.getSocketFactory()));
                options.setHttpsHostnameVerificationEnabled(false);
            } catch (Exception e) {
                Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
                //todo
                return;
            }
        }

        options.setCleanSession(true);
        options.setUserName(clientConf.getDeviceId());

        if (clientConf.getSecret() != null && !clientConf.getSecret().isEmpty()) {
            String passWord = IotUtil.sha256_mac(clientConf.getSecret(), timeStamp);
            options.setPassword(passWord.toCharArray());
        }

        options.setConnectionTimeout(DEFAULT_CONNECT_TIMEOUT);
        options.setKeepAliveInterval(DEFAULT_KEEPLIVE);
        options.setAutomaticReconnect(true);

        Log.i(TAG, "try to connect to " + clientConf.getServerUri());

        try {
            mqttAsyncClient.connect(options, null, iMqttActionListener);
        } catch (MqttException e) {
            Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
        }
    }

    /**
     * Backoff reconnect.
     */
    private void reconnect() {
        int lowBound = (int) (defaultBackoff * 0.8);
        int highBound = (int) (defaultBackoff * 1.2);
        int randomBackoff = secureRandom.nextInt(highBound - lowBound);
        int backOffWithJitter = (int) (Math.pow(2.0, retryTimes)) * (randomBackoff + lowBound);
        //解决超过整型最大值被截断的问题
        int expectwaitTImeUntilNextRetry = minBackoff + backOffWithJitter;
        int waitTImeUntilNextRetry;
        if (expectwaitTImeUntilNextRetry > maxBackoff || expectwaitTImeUntilNextRetry <= minBackoff) {
            waitTImeUntilNextRetry = maxBackoff;
        } else {
            waitTImeUntilNextRetry = expectwaitTImeUntilNextRetry;
        }

        Log.d(TAG, "run: try to reconnect:" + retryTimes + ",waitTImeUntilNextRetry:" + waitTImeUntilNextRetry);

        final HandlerThread handlerThread = new HandlerThread("mqtt reconnect");
        handlerThread.start();
        Looper mqttLooper = handlerThread.getLooper();
        Handler mqttHandler = new Handler(mqttLooper);
        mqttHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                innerConnect();
                handlerThread.quit();
            }
        }, waitTImeUntilNextRetry);

        retryTimes++;
    }

    @Override
    public void publishMessage(RawMessage message, ActionListener listener) {
        if (null == mqttAsyncClient) {
            Log.e(TAG, "subscribeTopic: mqttAsyncClient not initialized!");
            return;
        }

        try {
            MqttMessage mqttMessage = new MqttMessage(message.getPayload());
            mqttMessage.setQos(message.getQos() == 0 ? 0 : DEFAULT_QOS);

            mqttAsyncClient.publish(message.getTopic(), mqttMessage, message.getTopic(),
                    new PublishMessageMqttActionListener(message, listener));
            Log.i(TAG, "publish message topic =  " + message.getTopic() + ", msg = " + message.toString());
        } catch (MqttException e) {
            Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
            if (listener != null) {
                listener.onFailure(null, e);
            }
        }
    }

    static class PublishMessageMqttActionListener implements IMqttActionListener {
        private RawMessage message;
        private ActionListener listener;

        public PublishMessageMqttActionListener(RawMessage message, ActionListener listener) {
            this.message = message;
            this.listener = listener;
        }

        @Override
        public void onSuccess(IMqttToken iMqttToken) {
            if (listener != null) {
                listener.onSuccess(null);
            }
        }

        @Override
        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            Log.e(TAG, "publish message failed   " + message);
            if (listener != null) {
                listener.onFailure(null, throwable);
            }
        }
    }

    @Override
    public void close() {
        //清空连接回调，解决退避重连的时候不能断开连接的问题。
        iMqttActionListener = null;
        retryTimes = 0;

        if (mqttAsyncClient != null && mqttAsyncClient.isConnected()) {
            try {
                mqttAsyncClient.disconnect();
                mqttAsyncClient.close();
            } catch (MqttException e) {
                Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
            }
        }
    }

    @Override
    public boolean isConnected() {
        if (mqttAsyncClient == null) {
            return false;
        }
        return mqttAsyncClient.isConnected();
    }

    @Override
    public void setConnectListener(ConnectListener connectListener) {
        this.connectListener = connectListener;
    }

    public void setRawMessageListener(RawMessageListener rawMessageListener) {
        this.rawMessageListener = rawMessageListener;
    }

    /**
     * Subscribes to a topic.
     *
     * @param topic Indicates the topic to subscribe.
     */
    @Override
    public void subscribeTopic(String topic, ActionListener actionListener, int qos) {
        if (null == mqttAsyncClient) {
            Log.e(TAG, "subscribeTopic: mqttAsyncClient not initialized!");
            return;
        }
        try {
            mqttAsyncClient.subscribe(topic, qos, null,
                    new SubscribeTopicMqttActionListener(topic, actionListener));
        } catch (MqttException e) {
            Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
            if (actionListener != null) {
                actionListener.onFailure(topic, e);
            }
        }
    }

    @Override
    public void setAutoConnect(boolean flag) {
        this.autoConnect = false;
    }

    @Override
    public void setConnectType(int type) {
        connectType = type;
    }

    static class SubscribeTopicMqttActionListener implements IMqttActionListener {
        private String topic;
        private ActionListener actionListener;

        public SubscribeTopicMqttActionListener(String topic, ActionListener actionListener) {
            this.topic = topic;
            this.actionListener = actionListener;
        }

        @Override
        public void onSuccess(IMqttToken iMqttToken) {
            if (actionListener != null) {
                actionListener.onSuccess(topic);
            }
        }

        @Override
        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            Log.e(TAG, "subscribe topic failed:" + topic);
            if (actionListener != null) {
                actionListener.onFailure(topic, throwable);
            }
        }
    }

    /**
     * 保存最新连接失败和断开连接的错误信息
     *
     * @param logType 日志类型
     * @param content 日志内容
     */
    public void saveLog(String logType, String content) {
        SharedPreferences sp = mContext.getSharedPreferences(LogService.IOT_ERROR_LOG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(IotUtil.getTimeStamp());
        stringBuilder.append(" ");
        stringBuilder.append(logType);
        stringBuilder.append(":");
        stringBuilder.append(content);

        editor.putString(logType, stringBuilder.toString());
        editor.commit();
    }
}
