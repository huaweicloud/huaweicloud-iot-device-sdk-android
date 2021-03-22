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
import com.huaweicloud.sdk.iot.device.constant.BaseConstant;
import com.huaweicloud.sdk.iot.device.constant.IotDeviceIntent;
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


/**
* Provides APIs related to device clients. A device client can exchange the following information with the platform:
* Messages: Message interactions are bidirectional and asynchronous. Messages do not need to be defined in the product model.
* Properties: Property interactions are bidirectional. Devices can report properties, and the platform can read properties from and write properties to devices. Properties must be defined in the product model.
* Commands: Command interactions are unidirectional and synchronous. The platform sends commands to devices.
* Events: Event interactions are bidirectional and asynchronous. Events must be defined in the product model.
* You cannot directly create a DeviceClient instance. Instead, create an IoTDevice instance and then call the getClient method of IoTDevice to obtain a DeviceClient instance.
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

    // Indicates listeners for device message reporting.
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
            intent.putExtra(BaseConstant.RECONNECT, reconnect);
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
     * Connects to the platform. When the connection is established, the SDK automatically subscribes to system topics.
     */
    public void connect() {

        if (executorService == null) {
            executorService = Executors.newScheduledThreadPool(clientThreadCount);
        }

        connection.connect();
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

    /**
     * Reports a device message.
     * To report a message for a child device, call the setDeviceId API of DeviceMessage to set the device ID of the child device.
     *
     * @param deviceMessage Indicates the device message to report.
     */
    public void reportDeviceMessage(DeviceMessage deviceMessage) {
        String topic = "$oc/devices/" + this.deviceId + "/sys/messages/up";
        this.publishRawMessage(new RawMessage(topic, JsonUtil.convertObject2String(deviceMessage)), messagesActionListener);
    }

    /**
     * Reports a device message with a listener specified.
     *
     * @param deviceMessage Indicates the device message to report.
     * @param listener Indicates the listener.
     */
    public void reportDeviceMessage(DeviceMessage deviceMessage, ActionListener listener) {
        String topic = "$oc/devices/" + this.deviceId + "/sys/messages/up";
        this.publishRawMessage(new RawMessage(topic, JsonUtil.convertObject2String(deviceMessage)), listener);
    }

    /**
     * Obtains the device shadow data from the platform.
     */
    public void getShadowMessage(ShadowGet shadowGet) {
        UUID uuid = UUID.randomUUID();
        String requestId = uuid.toString();
        String topic = "$oc/devices/" + this.deviceId + "/sys/shadow/get/request_id=" + requestId;
        this.publishRawMessage(new RawMessage(topic, JsonUtil.convertObject2String(shadowGet)), null);
    }

    /**
     * Reports a device message with a specified QoS level.
     *
     * @param deviceMessage Indicates the device message to report.
     * @param qos Indicates the QoS level. The value can be 0 or 1.
     */
    public void reportDeviceMessage(DeviceMessage deviceMessage, int qos) {
        String topic = "$oc/devices/" + this.deviceId + "/sys/messages/up";
        if (qos != 0) {
            qos = 1;
        }
        this.publishRawMessage(new RawMessage(topic, JsonUtil.convertObject2String(deviceMessage), qos), messagesActionListener);
    }

    /**
     * Publishes a raw message. The differences between raw messages and device messages are as follows:
     * 1. A topic can be customized. The topic must be configured on the platform.
     * 2. The payload format is not limited.
     * 
     * @param rawMessage Indicates the raw message to report.
     * @param listener Indicates a listener.
     */
    public void publishRawMessage(RawMessage rawMessage, ActionListener listener) {
        connection.publishMessage(rawMessage, listener);
    }

    /**
     * Publishes a custom topic.
     *
     * @param topicName Indicates the name of the custom topic, excluding the fixed prefix.
     * @param message Indicates the message content.
     * @param qos Indicates a QoS level. The value can be 0 or 1.
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
     * Reports device properties.
     *
     * @param properties Indicates the properties to report.
     */
    public void reportProperties(List<ServiceProperty> properties) {
        reportPropertiesForInner(properties, propertyActionListener);
    }

    /**
     * Reports device properties. This is a V3 API.
     *
     * @param devicePropertiesV3 Indicates the properties to report.
     */
    public void reportPropertiesV3(DevicePropertiesV3 devicePropertiesV3) {
        String topic = "/huawei/v1/devices/" + this.deviceId + "/data/json";

        RawMessage rawMessage = new RawMessage(topic, devicePropertiesV3.toString());
        connection.publishMessage(rawMessage, propertyV3ActionListener);
    }

    /**
     * Reports device properties in binary code streams. This is a V3 API.
     *
     * @param bytes Indicates the binary code stream to report.
     */
    public void reportBinaryV3(Byte[] bytes) {

        String deviceId = clientConf.getDeviceId();
        String topic = "/huawei/v1/devices/" + deviceId + "/data/binary";

        RawMessage rawMessage = new RawMessage(topic, Arrays.toString(bytes));
        connection.publishMessage(rawMessage, binaryV3ActionListener);
    }

    /**
     * Reports a V3 command response.
     *
     * @param commandRspV3 Indicates the command response to report.
     */
    public void responseCommandV3(CommandRspV3 commandRspV3) {

        String topic = "/huawei/v1/devices/" + deviceId + "/data/json";
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(commandRspV3));
        connection.publishMessage(rawMessage, null);
    }

    /**
     * Reports a V3 command response in binary code streams.
     *
     * @param bytes Indicates the binary code stream to report. 
     */
    public void responseCommandBinaryV3(Byte[] bytes) {

        String topic = "/huawei/v1/devices/" + deviceId + "/data/binary";
        RawMessage rawMessage = new RawMessage(topic, Arrays.toString(bytes));
        connection.publishMessage(rawMessage, null);
    }

    /**
     * Internally reports device properties.
     *
     * @param properties Indicates the properties to report.
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
     * Processes a device command delivered by the platform.
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
     * Processes a message delivered by the platform.
     *
     * @param deviceMessage Indicates the device message delivered.
     */
    private void handleDeviceMessage(DeviceMessage deviceMessage) {
        //增加设备方法重引导功能，下发的消息内容为BootstrapRequestTrigger，则需要设备发起重引导
        if ("BootstrapRequestTrigger".equals(deviceMessage.getContent())) {
            Intent triggerIntent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_BOOTSTRAP_REQUEST_TRIGGER);
            localBroadcastManager.sendBroadcast(triggerIntent);
        }

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

        // Only messages delivered to directly connected devices are processed. Messages delivered to child devices are processed by AbstractGateway.
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
     * Reports a command response.
     *
     * @param requestId Indicates the request ID, which must be the same as that in the request.
     * @param commandRsp Indicates the command response to report.
     */
    public void respondCommand(String requestId, CommandRsp commandRsp) {

        String topic = "$oc/devices/" + deviceId + "/sys/commands/response/request_id=" + requestId;
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(commandRsp));
        connection.publishMessage(rawMessage, null);
    }

    /**
     * Reports a response to a property query request.
     *
     * @param requestId Indicates the request ID, which must be the same as that in the request.
     * @param services Indicates service properties.
     */
    public void respondPropsGet(String requestId, List<ServiceProperty> services) {

        DeviceProperties deviceProperties = new DeviceProperties();
        deviceProperties.setServices(services);

        String topic = "$oc/devices/" + deviceId + "/sys/properties/get/response/request_id=" + requestId;
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(deviceProperties));
        connection.publishMessage(rawMessage, null);
    }

    /**
     * Reports a response to a property setting request.
     *
     * @param requestId Indicates the request ID, which must be the same as that in the request.
     * @param iotResult Indicates the property setting result.
     */
    public void respondPropsSet(String requestId, IotResult iotResult) {

        String topic = "$oc/devices/" + deviceId + "/sys/properties/set/response/request_id=" + requestId;
        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(iotResult));
        connection.publishMessage(rawMessage, null);
    }

    /**
     * Obtains the device ID.
     *
     * @return Returns the device ID.
     */
    public String getDeviceId() {
        return deviceId;
    }


    /**
     * Subscribes to a custom topic. System topics are automatically subscribed by the SDK. This method can be used only to subscribe to custom topics.
     *
     * @param topicName Indicates the name of the custom topic, excluding the fixed prefix.
     * @param qos Indicates a QoS level. The value can be 0 or 1.
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
     * Subscribes to a V3 topic.
     *
     * @param topic Indicates the topic to subscribe.
     * @param qos Indicates the QoS.
     */
    public void subscribeTopicV3(String topic, int qos) {
        connection.subscribeTopic(topic, null, qos);
    }

    public void setDevice(AbstractDevice device) {
        this.device = device;
    }

    /**
     * Reports an event.
     *
     * @param event Indicates the event to report.
     * @param listener Indicates a listener.
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
