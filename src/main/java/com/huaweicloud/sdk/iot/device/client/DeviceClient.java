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


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.huaweicloud.sdk.iot.device.client.requests.Command;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRspV3;
import com.huaweicloud.sdk.iot.device.client.requests.CommandV3;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvents;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceProperties;
import com.huaweicloud.sdk.iot.device.client.requests.DevicePropertiesV3;
import com.huaweicloud.sdk.iot.device.client.requests.PropsGet;
import com.huaweicloud.sdk.iot.device.client.requests.PropsSet;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.client.requests.ShadowGet;
import com.huaweicloud.sdk.iot.device.client.requests.ShadowMessage;
import com.huaweicloud.sdk.iot.device.service.AbstractDevice;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.transport.ConnectListener;
import com.huaweicloud.sdk.iot.device.transport.Connection;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.transport.RawMessageListener;
import com.huaweicloud.sdk.iot.device.transport.mqtt.MqttConnection;
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.huaweicloud.sdk.iot.device.constant.BaseConstant;
import com.huaweicloud.sdk.iot.device.constant.IotDeviceIntent;


/**
 * 设备客户端，提供和平台的通讯能力，包括：
 * 消息：双向，异步，不需要定义模型
 * 属性：双向，设备可以上报属性，平台可以向设备读写属性，属性需要在模型定义
 * 命令：单向，同步，平台向设备调用设备的命令
 * 事件：双向、异步，需要在模型定义
 * 用户不能直接创建DeviceClient实例，只能先创建IoTDevice实例，然后通过IoTDevice的getClient接口获取DeviceClient实例
 */
public class DeviceClient implements RawMessageListener {

    private static final String TAG = "DeviceClient";

    private Context mContext;
    private ClientConf clientConf;
    private Connection connection;
    private RequestManager requestManager;
    private String deviceId;
    private Map<String, RawMessageListener> rawMessageListenerMap;
    private AbstractDevice device;

    private ScheduledExecutorService executorService;
    private int clientThreadCount = 1;
    private LocalBroadcastManager localBroadcastManager;

    // 设备消息上报监听器
    private ActionListener messagesActionListener = new ActionListener() {
        @Override
        public void onSuccess(Object context) {
            Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_MESSAGES_UP);
            intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_SUCCESS);
            localBroadcastManager.sendBroadcast(intent);
        }

        @Override
        public void onFailure(Object context, Throwable var2) {
            Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_MESSAGES_UP);
            intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
            intent.putExtra(BaseConstant.COMMON_ERROR, var2.getMessage());
            localBroadcastManager.sendBroadcast(intent);
        }
    };

    private ActionListener propertyActionListener = new ActionListener() {
        @Override
        public void onSuccess(Object context) {
            Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_PROPERTIES_REPORT);
            intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_SUCCESS);
            localBroadcastManager.sendBroadcast(intent);
        }

        @Override
        public void onFailure(Object context, Throwable e) {
            Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_PROPERTIES_REPORT);
            intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
            intent.putExtra(BaseConstant.PROPERTIES_REPORT_ERROR, e.getMessage());
            localBroadcastManager.sendBroadcast(intent);
        }
    };

    private ActionListener propertyV3ActionListener = new ActionListener() {
        @Override
        public void onSuccess(Object context) {
            Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_PROPERTIES_REPORT_V3);
            intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_SUCCESS);
            localBroadcastManager.sendBroadcast(intent);
        }

        @Override
        public void onFailure(Object context, Throwable e) {
            Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_PROPERTIES_REPORT_V3);
            intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
            intent.putExtra(BaseConstant.PROPERTIES_REPORT_ERROR, e.getMessage());
            localBroadcastManager.sendBroadcast(intent);
        }
    };

    private ActionListener binaryV3ActionListener = new ActionListener() {
        @Override
        public void onSuccess(Object context) {
            Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_PROPERTIES_BINARY_V3);
            intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_SUCCESS);
            localBroadcastManager.sendBroadcast(intent);
        }

        @Override
        public void onFailure(Object context, Throwable e) {
            Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_PROPERTIES_BINARY_V3);
            intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
            intent.putExtra(BaseConstant.PROPERTIES_REPORT_ERROR, e.getMessage());
            localBroadcastManager.sendBroadcast(intent);
        }
    };

    private ConnectListener connectListener = new ConnectListener() {
        @Override
        public void connectionLost(Throwable cause) {
            Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_CONNECT);
            intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_LOST);
            intent.putExtra(BaseConstant.COMMON_ERROR, cause.getMessage());
            localBroadcastManager.sendBroadcast(intent);
        }

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_CONNECT);
            intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_RECONNECT);
            intent.putExtra(BaseConstant.SERVERURI, serverURI);
            localBroadcastManager.sendBroadcast(intent);
        }
    };

    public DeviceClient(Context mContext, ClientConf clientConf, AbstractDevice device) {
        checkClientConf(clientConf);
        this.clientConf = clientConf;
        this.deviceId = clientConf.getDeviceId();
        this.requestManager = new RequestManager(this);
        this.connection = new MqttConnection(mContext, clientConf, this);
        this.device = device;
        this.rawMessageListenerMap = new ConcurrentHashMap<String, RawMessageListener>();
        this.mContext = mContext;
        localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        connection.setConnectListener(connectListener);
    }

    public ClientConf getClientConf() {
        return clientConf;
    }

    private void checkClientConf(ClientConf clientConf) throws IllegalArgumentException {
        if (clientConf == null) {
            throw new IllegalArgumentException("clientConf is null");
        }
        if (clientConf.getDeviceId() == null) {
            throw new IllegalArgumentException("clientConf.getDeviceId() is null");
        }
        if (clientConf.getSecret() == null && clientConf.getKeyStore() == null) {
            throw new IllegalArgumentException("secret and keystore is null");
        }
        if (clientConf.getServerUri() == null) {
            throw new IllegalArgumentException("clientConf.getSecret() is null");
        }
        if (!clientConf.getServerUri().startsWith("tcp://") && (!clientConf.getServerUri().startsWith("ssl://"))) {
            throw new IllegalArgumentException("invalid serverUri");
        }
    }

    /**
     * 和平台建立连接，连接成功时，SDK会自动向平台订阅系统定义的topic。
     */
    public void connect() {

        if (executorService == null) {
            executorService = Executors.newScheduledThreadPool(clientThreadCount);
        }

        connection.connect();
    }

    /**
     * 上报设备消息
     * 如果需要上报子设备消息，需要调用DeviceMessage的setDeviceId接口设置为子设备的设备id
     *
     * @param deviceMessage 设备消息
     */
    public void reportDeviceMessage(DeviceMessage deviceMessage) {
        String topic = "$oc/devices/" + this.deviceId + "/sys/messages/up";
        this.publishRawMessage(new RawMessage(topic, JsonUtil.convertObject2String(deviceMessage)), messagesActionListener);
    }

    /**
     * 获取平台的设备影子数据
     */
    public void getShadowMessage(ShadowGet shadowGet) {
        UUID uuid = UUID.randomUUID();
        String requestId = uuid.toString();
        String topic = "$oc/devices/" + this.deviceId + "/sys/shadow/get/request_id=" + requestId;
        this.publishRawMessage(new RawMessage(topic, JsonUtil.convertObject2String(shadowGet)), null);
    }

    /**
     * 上报设备消息，支持指定qos
     *
     * @param deviceMessage 设备消息
     * @param qos           消息qos，0或1
     */
    public void reportDeviceMessage(DeviceMessage deviceMessage, int qos) {
        String topic = "$oc/devices/" + this.deviceId + "/sys/messages/up";
        if (qos != 0) {
            qos = 1;
        }
        this.publishRawMessage(new RawMessage(topic, JsonUtil.convertObject2String(deviceMessage), qos), messagesActionListener);
    }

    /**
     * 发布原始消息，原始消息和设备消息（DeviceMessage）的区别是：
     * 1、可以自定义topic，该topic需要在平台侧配置
     * 2、不限制payload的格式
     *
     * @param rawMessage 原始消息
     * @param listener   监听器
     */
    public void publishRawMessage(RawMessage rawMessage, ActionListener listener) {
        connection.publishMessage(rawMessage, listener);
    }

    /**
     * 发布自定义topic
     *
     * @param topicName 自定义的topic去除固定前缀后名称
     * @param message   上报消息内容
     * @param qos       消息qos，0或1
     */
    public void publishTopic(final String topicName, String message, int qos) {
        String topic = "$oc/devices/" + this.deviceId + "/user/" + topicName;
        if (qos != 0) {
            qos = 1;
        }

        this.publishRawMessage(new RawMessage(topic, message, qos), new ActionListener() {
            @Override
            public void onSuccess(Object context) {
                Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_CUSTOMIZED_TOPIC_REPORT);
                intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_SUCCESS);
                intent.putExtra(BaseConstant.CUSTOMIZED_TOPIC_NAME, topicName);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_CUSTOMIZED_TOPIC_REPORT);
                intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
                intent.putExtra(BaseConstant.CUSTOMIZED_TOPIC_NAME, topicName);
                intent.putExtra(BaseConstant.COMMON_ERROR, var2.getMessage());
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        });
    }

    /**
     * 上报设备属性
     *
     * @param properties 设备属性列表
     */
    public void reportProperties(List<ServiceProperty> properties) {
        reportPropertiesForInner(properties, propertyActionListener);
    }

    /**
     * 向平台上报设备属性（V3接口）
     *
     * @param devicePropertiesV3 设备上报的属性
     */
    public void reportPropertiesV3(DevicePropertiesV3 devicePropertiesV3) {
        String topic = "/huawei/v1/devices/" + this.deviceId + "/data/json";

        RawMessage rawMessage = new RawMessage(topic, devicePropertiesV3.toString());
        connection.publishMessage(rawMessage, propertyV3ActionListener);
    }

    /**
     * 向平台上报设备属性（V3接口）
     *
     * @param bytes 设备上报的码流
     */
    public void reportBinaryV3(Byte[] bytes) {

        String deviceId = clientConf.getDeviceId();
        String topic = "/huawei/v1/devices/" + deviceId + "/data/binary";

        RawMessage rawMessage = new RawMessage(topic, Arrays.toString(bytes));
        connection.publishMessage(rawMessage, binaryV3ActionListener);
    }

    /**
     * 向平台上报V3命令响应
     *
     * @param commandRspV3 命令响应结果
     */
    public void responseCommandV3(CommandRspV3 commandRspV3) {

        String topic = "/huawei/v1/devices/" + deviceId + "/data/json";
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(commandRspV3));
        connection.publishMessage(rawMessage, null);
    }

    /**
     * 向平台上报V3命令响应（码流）
     *
     * @param bytes 响应码流
     */
    public void responseCommandBinaryV3(Byte[] bytes) {

        String topic = "/huawei/v1/devices/" + deviceId + "/data/binary";
        RawMessage rawMessage = new RawMessage(topic, Arrays.toString(bytes));
        connection.publishMessage(rawMessage, null);
    }

    /**
     * 内部上报设备属性
     *
     * @param properties 设备属性列表
     */
    public void reportPropertiesForInner(List<ServiceProperty> properties, ActionListener actionListener) {

        String topic = "$oc/devices/" + this.deviceId + "/sys/properties/report";
        Map<String, List<ServiceProperty>> services = new HashMap<String, List<ServiceProperty>>(4);
        services.put("services", properties);

        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(services));
        connection.publishMessage(rawMessage, actionListener);
    }

    private void onPropertiesSet(RawMessage message) {

        String requestId = IotUtil.getRequestId(message.getTopic());

        PropsSet propsSet = JsonUtil.convertJsonStringToObject(message.toString(), PropsSet.class);
        if (propsSet == null) {
            return;
        }

        device.onPropertiesSet(requestId, propsSet);

        if (propsSet.getDeviceId() == null || propsSet.getDeviceId().equals(getDeviceId())) {
            handlePropertiesSet(requestId, propsSet);
            return;
        }

    }

    private void handlePropertiesSet(String requestId, PropsSet propsSet) {
        Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_PROPERTIES_SET);
        intent.putExtra(BaseConstant.REQUEST_ID, requestId);
        intent.putExtra(BaseConstant.SYS_PROPERTIES_SET, propsSet);
        localBroadcastManager.sendBroadcast(intent);
    }


    private void onPropertiesGet(RawMessage message) {

        String requestId = IotUtil.getRequestId(message.getTopic());

        PropsGet propsGet = JsonUtil.convertJsonStringToObject(message.toString(), PropsGet.class);
        if (propsGet == null) {
            return;
        }

        device.onPropertiesGet(requestId, propsGet);

        if (propsGet.getDeviceId() == null || propsGet.getDeviceId().equals(getDeviceId())) {
            handlePropertiesGet(requestId, propsGet.getServiceId());
            return;
        }

    }

    private void handlePropertiesGet(String requestId, String serviceId) {
        Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_PROPERTIES_GET);
        intent.putExtra(BaseConstant.REQUEST_ID, requestId);
        intent.putExtra(BaseConstant.SERVICE_ID, serviceId);
        localBroadcastManager.sendBroadcast(intent);
    }


    private void onCommand(RawMessage message) {

        String requestId = IotUtil.getRequestId(message.getTopic());

        Command command = JsonUtil.convertJsonStringToObject(message.toString(), Command.class);
        if (command == null) {
            Log.e(TAG, "invalid command");
            return;
        }

        device.onCommand(requestId, command);

        if (command.getDeviceId() == null || command.getDeviceId().equals(getDeviceId())) {
            handleCommand(requestId, command);
            return;
        }

    }

    /**
     * 处理平台下发的设备命令
     */
    private void handleCommand(String requestId, Command command) {
        Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_COMMANDS);
        intent.putExtra(BaseConstant.REQUEST_ID, requestId);
        intent.putExtra(BaseConstant.SYS_COMMANDS, command);
        localBroadcastManager.sendBroadcast(intent);
    }


    private void onDeviceMessage(RawMessage message) {
        DeviceMessage deviceMessage = JsonUtil.convertJsonStringToObject(message.toString(),
                DeviceMessage.class);
        if (deviceMessage == null) {
            Log.e(TAG, "invalid deviceMessage: " + message.toString());
            return;
        }

        device.onDeviceMessage(deviceMessage);

        if (deviceMessage.getDeviceId() == null || deviceMessage.getDeviceId().equals(getDeviceId())) {
            handleDeviceMessage(deviceMessage);
            return;
        }

    }

    /**
     * 处理平台下发的消息
     *
     * @param deviceMessage
     */
    private void handleDeviceMessage(DeviceMessage deviceMessage) {
        Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_MESSAGES_DOWN);
        intent.putExtra(BaseConstant.SYS_DOWN_MESSAGES, deviceMessage);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void onEvent(RawMessage message) {

        DeviceEvents deviceEvents = JsonUtil.convertJsonStringToObject(message.toString(), DeviceEvents.class);
        if (deviceEvents == null) {
            Log.e(TAG, "invalid events");
            return;
        }
        device.onEvent(deviceEvents);
    }

    private void onResponse(RawMessage message) {
        requestManager.onRequestResponse(message);
    }


    @Override
    public void onMessageReceived(final RawMessage message) {

        if (executorService == null) {
            Log.e(TAG, "executionService is null");
            return;
        }

        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    String topic = message.getTopic();

                    RawMessageListener listener = rawMessageListenerMap.get(topic);
                    if (listener != null) {
                        listener.onMessageReceived(message);
                        return;
                    }

                    if (topic.contains("/messages/down")) {
                        onDeviceMessage(message);
                    } else if (topic.contains("sys/commands/request_id")) {
                        onCommand(message);

                    } else if (topic.contains("/sys/properties/set/request_id")) {
                        onPropertiesSet(message);

                    } else if (topic.contains("/sys/properties/get/request_id")) {
                        onPropertiesGet(message);

                    } else if (topic.contains("/desired/properties/get/response")) {
                        onResponse(message);
                    } else if (topic.contains("/sys/shadow/get/response")) {
                        onShadowResponse(message);
                    } else if (topic.contains("/sys/events/down")) {
                        onEvent(message);
                    } else if (topic.contains("/huawei/v1/devices") && topic.contains("/command/")) {
                        onCommandV3(message);
                    } else {
                        Log.e(TAG, "unknown topic: " + topic);
                    }

                } catch (Exception e) {
                    Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
                }
            }
        }, 0, TimeUnit.MILLISECONDS);

    }

    private void onCommandV3(RawMessage message) {
        CommandV3 commandV3 = JsonUtil.convertJsonStringToObject(message.toString(), CommandV3.class);
        if (commandV3 == null) {
            Log.e(TAG, "onCommandV3: invalid commandV3");
            return;
        }

        Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_COMMANDS_V3);
        intent.putExtra(BaseConstant.SYS_COMMANDS, commandV3);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void onShadowResponse(RawMessage message) {
        String requestId = IotUtil.getRequestId(message.getTopic());

        ShadowMessage shadowMessage = JsonUtil.convertJsonStringToObject(message.toString(), ShadowMessage.class);
        if (shadowMessage == null) {
            return;
        }

        //只处理直连设备的，子设备的由AbstractGateway处理
        if (shadowMessage.getDeviceId() == null || shadowMessage.getDeviceId().equals(getDeviceId())) {
            handleShadow(requestId, shadowMessage);
            return;
        }
    }

    private void handleShadow(String requestId, ShadowMessage shadowMessage) {
        Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SHADOW_GET);
        intent.putExtra(BaseConstant.REQUEST_ID, requestId);
        intent.putExtra(BaseConstant.SHADOW_DATA, shadowMessage);
        localBroadcastManager.sendBroadcast(intent);
    }


    public void close() {
        connection.close();
    }


    /**
     * 上报命令响应
     *
     * @param requestId  请求id，响应的请求id必须和请求的一致
     * @param commandRsp 命令响应
     */
    public void respondCommand(String requestId, CommandRsp commandRsp) {

        String topic = "$oc/devices/" + deviceId + "/sys/commands/response/request_id=" + requestId;
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(commandRsp));
        connection.publishMessage(rawMessage, null);
    }

    /**
     * 上报读属性响应
     *
     * @param requestId 请求id，响应的请求id必须和请求的一致
     * @param services  服务属性
     */
    public void respondPropsGet(String requestId, List<ServiceProperty> services) {

        DeviceProperties deviceProperties = new DeviceProperties();
        deviceProperties.setServices(services);

        String topic = "$oc/devices/" + deviceId + "/sys/properties/get/response/request_id=" + requestId;
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(deviceProperties));
        connection.publishMessage(rawMessage, null);
    }

    /**
     * 上报写属性响应
     *
     * @param requestId 请求id，响应的请求id必须和请求的一致
     * @param iotResult 写属性结果
     */
    public void respondPropsSet(String requestId, IotResult iotResult) {

        String topic = "$oc/devices/" + deviceId + "/sys/properties/set/response/request_id=" + requestId;
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(iotResult));
        connection.publishMessage(rawMessage, null);
    }

    /**
     * 获取设备id
     *
     * @return 设备id
     */
    public String getDeviceId() {
        return deviceId;
    }


    /**
     * 订阅自定义topic。系统topic由SDK自动订阅，此接口只能用于订阅自定义topic
     *
     * @param topicName 自定义的topic去除固定前缀后名称
     * @param qos       消息qos，0或1
     */
    public void subscribeTopic(final String topicName, int qos) {
        String topic = "$oc/devices/" + this.deviceId + "/user/" + topicName;

        ActionListener actionListener = new ActionListener() {
            @Override
            public void onSuccess(Object context) {
                Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_CUSTOMIZED_TOPIC_CONNECT);
                intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_SUCCESS);
                intent.putExtra(BaseConstant.CUSTOMIZED_TOPIC_NAME, topicName);
                localBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onFailure(Object context, Throwable throwable) {
                Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_CUSTOMIZED_TOPIC_CONNECT);
                intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
                intent.putExtra(BaseConstant.CUSTOMIZED_TOPIC_NAME, topicName);
                intent.putExtra(BaseConstant.COMMON_ERROR, throwable.getMessage());
                localBroadcastManager.sendBroadcast(intent);
            }
        };

        RawMessageListener rawMessageListener = new RawMessageListener() {
            @Override
            public void onMessageReceived(RawMessage message) {
                Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_CUSTOMIZED_TOPIC_MESSAGE);
                intent.putExtra(BaseConstant.CUSTOMIZED_TOPIC_MESSAGE, message);
                intent.putExtra(BaseConstant.CUSTOMIZED_TOPIC_NAME, topicName);
                localBroadcastManager.sendBroadcast(intent);
            }
        };

        connection.subscribeTopic(topic, actionListener, qos);
        rawMessageListenerMap.put(topic, rawMessageListener);
    }

    /**
     * 订阅V3Topic
     *
     * @param topic 订阅topic
     * @param qos   qos
     */
    public void subscribeTopicV3(String topic, int qos) {
        connection.subscribeTopic(topic, null, qos);
    }

    public void setDevice(AbstractDevice device) {
        this.device = device;
    }

    /**
     * 事件上报
     *
     * @param event    事件
     * @param listener 监听器
     */
    public void reportEvent(DeviceEvent event, ActionListener listener) {

        DeviceEvents events = new DeviceEvents();
        events.setDeviceId(getDeviceId());
        events.setServices(Arrays.asList(event));
        String deviceId = clientConf.getDeviceId();
        String topic = "$oc/devices/" + deviceId + "/sys/events/up";

        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(events));
        connection.publishMessage(rawMessage, listener);

    }

    public Future<?> scheduleTask(Runnable runnable) {
        return executorService.schedule(runnable, 0, TimeUnit.MILLISECONDS);
    }

    public Future<?> scheduleTask(Runnable runnable, long delay) {
        return executorService.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    public Future<?> scheduleRoutineTask(Runnable runnable, long period) {
        return executorService.scheduleAtFixedRate(runnable, period, period, TimeUnit.MILLISECONDS);
    }

    public int getClientThreadCount() {
        return clientThreadCount;
    }

    public void setClientThreadCount(int clientThreadCount) {
        clientThreadCount = clientThreadCount;
    }
}
