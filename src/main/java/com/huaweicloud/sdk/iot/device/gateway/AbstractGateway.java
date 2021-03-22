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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.huaweicloud.sdk.iot.device.IoTDevice;
import com.huaweicloud.sdk.iot.device.client.requests.Command;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvents;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.PropsGet;
import com.huaweicloud.sdk.iot.device.client.requests.PropsSet;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.constant.BaseConstant;
import com.huaweicloud.sdk.iot.device.constant.IotDeviceIntent;
import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceInfo;
import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceProperty;
import com.huaweicloud.sdk.iot.device.gateway.requests.DeviceStatus;
import com.huaweicloud.sdk.iot.device.gateway.requests.SubDeviceProperties;
import com.huaweicloud.sdk.iot.device.gateway.requests.SubDevicesAddInfo;
import com.huaweicloud.sdk.iot.device.gateway.requests.SubDevicesDeleteInfo;
import com.huaweicloud.sdk.iot.device.gateway.requests.SubDevicesInfo;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An abstract gateway that implements device management and message forwarding of child devices.
 */
public abstract class AbstractGateway extends IoTDevice {

    private final static String TAG = "AbstractGateway";

    private SubDevicesPersistence subDevicesPersistence;
    private GatewayBroadcastReceiver gatewayBroadcastReceiver = new GatewayBroadcastReceiver();
    private Context mContext;

    /**
     * Constructor used to create an AbstractGateway object. In this method, secret authentication is used.
     *
     * @param mContext Indicates the context.
     * @param subDevicesPersistence Indicates the persistence of child device details.
     * @param serverUri Indicates the device access address, for example, ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883.
     * @param deviceId Indicates the device ID.
     * @param deviceSecret Indicates the secret.
     */
    public AbstractGateway(Context mContext, SubDevicesPersistence subDevicesPersistence, String serverUri, String deviceId, String deviceSecret) {
        super(mContext, serverUri, deviceId, deviceSecret);
        this.subDevicesPersistence = subDevicesPersistence;
        this.mContext = mContext;
    }

    /**
     * Constructor used to create an AbstractGateway object. In this method, certificate authentication is used.
     *
     * @param mContext Indicates the context.
     * @param subDevicesPersistence Indicates the persistence of child device details.
     * @param serverUri Indicates the device access address, for example, ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883.
     * @param deviceId Indicates the device ID.
     * @param keyStore Indicates a certificate keystore.
     * @param keyPassword Indicates the password of the certificate.
     */
    public AbstractGateway(Context mContext, SubDevicesPersistence subDevicesPersistence, String serverUri, String deviceId, KeyStore keyStore, String keyPassword) {
        super(mContext, serverUri, deviceId, keyStore, keyPassword);
        this.subDevicesPersistence = subDevicesPersistence;
        this.mContext = mContext;
    }

    class GatewayBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (IotDeviceIntent.ACTION_IOT_DEVICE_CONNECT.equals(intent.getAction())) {
                int status = intent.getIntExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
                switch (status) {
                    case BaseConstant.STATUS_SUCCESS:
                        // Synchronizes child device details to the platform during connection.
                        syncSubDevices();
                        break;
                    case BaseConstant.STATUS_RECONNECT:
                        // Synchronizes child device details to the platform during connection or reconnection.
                        boolean reconnect = intent.getBooleanExtra(BaseConstant.RECONNECT, false);
                        if (reconnect) {
                            syncSubDevices();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Creates a connection to the platform.
     */
    @Override
    public void init() {
        super.init();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(gatewayBroadcastReceiver,
                new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_CONNECT));
    }

    /**
     * Closes the connection to the platform.
     */
    @Override
    public void close() {
        super.close();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(gatewayBroadcastReceiver);
    }


    /**
     * Obtains a child device by node ID.
     *
     * @param nodeId Indicates the node ID.
     * @return Returns the child device details.
     */
    public DeviceInfo getSubDeviceByNodeId(String nodeId) {
        return subDevicesPersistence.getSubDevice(nodeId);
    }

    /**
     * Obtains a child device by device ID.
     *
     * @param deviceId Indicates the device ID.
     * @return Returns the child device details.
     */
    public DeviceInfo getSubDeviceByDeviceId(String deviceId) {
        String nodeId = IotUtil.getNodeIdFromDeviceId(deviceId);
        return subDevicesPersistence.getSubDevice(nodeId);
    }

    /**
     * Reports the result of child device discovery.
     *
     * @param deviceInfos Indicates the list of child device details.
     * @param listener Indicates a listener.
     */
    public void reportSubDevList(List<DeviceInfo> deviceInfos, ActionListener listener) {

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setServiceId("sub_device_discovery");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());
        deviceEvent.setEventType("scan_result");

        Map<String, Object> para = new HashMap<String, Object>();
        para.put("devices", deviceInfos);
        deviceEvent.setParas(para);

        getClient().reportEvent(deviceEvent, listener);

    }

    class SubDeviceMessageReportActionListener implements ActionListener {
        private String deviceId;

        public SubDeviceMessageReportActionListener(String deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public void onSuccess(Object context) {
            Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SUB_MESSAGES_UP);
            intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_SUCCESS);
            intent.putExtra(BaseConstant.SUB_DEVICE_ID, deviceId);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }

        @Override
        public void onFailure(Object context, Throwable var2) {
            Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SUB_MESSAGES_UP);
            intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
            intent.putExtra(BaseConstant.SUB_DEVICE_ID, deviceId);
            intent.putExtra(BaseConstant.COMMON_ERROR, var2.getMessage());
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }

    /**
     * Reports a child device message.
     *
     * @param deviceMessage Indicates the message to report.
     */
    public void reportSubDeviceMessage(DeviceMessage deviceMessage) {
        getClient().reportDeviceMessage(deviceMessage, new SubDeviceMessageReportActionListener(deviceMessage.getDeviceId()));
    }

    class SubDevicePropertiesReportActionListener implements ActionListener {

        private ArrayList<String> deviceIds;

        public SubDevicePropertiesReportActionListener(ArrayList<String> deviceIds) {
            this.deviceIds = deviceIds;
        }

        @Override
        public void onSuccess(Object context) {
            Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SUB_PROPERTIES_REPORT);
            intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_SUCCESS);
            intent.putStringArrayListExtra(BaseConstant.SUB_DEVICE_ID_LIST, deviceIds);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }

        @Override
        public void onFailure(Object context, Throwable var2) {
            Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SUB_PROPERTIES_REPORT);
            intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
            intent.putStringArrayListExtra(BaseConstant.SUB_DEVICE_ID_LIST, deviceIds);
            intent.putExtra(BaseConstant.COMMON_ERROR, var2.getMessage());
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }

    /**
     * Reports properties for a child device.
     *
     * @param deviceId Indicates the ID of the child device.
     * @param services Indicates the properties to report.
     */
    public void reportSubDeviceProperties(String deviceId,
                                          List<ServiceProperty> services) {


        DeviceProperty deviceProperty = new DeviceProperty();
        deviceProperty.setDeviceId(deviceId);
        deviceProperty.setServices(services);
        SubDeviceProperties subDeviceProperties = new SubDeviceProperties();
        subDeviceProperties.setDevices(Arrays.asList(deviceProperty));
        reportSubDeviceProperties(subDeviceProperties);

    }

    /**
     * Reports properties for a batches of child devices.
     *
     * @param subDeviceProperties Indicates the properties to report.
     */
    public void reportSubDeviceProperties(SubDeviceProperties subDeviceProperties) {

        String topic = "$oc/devices/" + getDeviceId() + "/sys/gateway/sub_devices/properties/report";

        RawMessage rawMessage = new RawMessage(topic, JsonUtil.convertObject2String(subDeviceProperties));

        List<DeviceProperty> devicePropertyList = subDeviceProperties.getDevices();
        ArrayList<String> deviceIds = new ArrayList<String>();
        for (int i = 0; i < devicePropertyList.size(); i++) {
            deviceIds.add(devicePropertyList.get(i).getDeviceId());
        }

        getClient().publishRawMessage(rawMessage, new SubDevicePropertiesReportActionListener(deviceIds));

    }

    class SubDeviceStatusReportActionListener implements ActionListener {

        private ArrayList<DeviceStatus> statuses;

        public SubDeviceStatusReportActionListener(ArrayList<DeviceStatus> statuses) {
            this.statuses = statuses;
        }

        @Override
        public void onSuccess(Object context) {
            Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SUB_STATUSES_REPORT);
            intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_SUCCESS);
            intent.putParcelableArrayListExtra(BaseConstant.SUB_DEVICE_ID_LIST_STATUS, statuses);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }

        @Override
        public void onFailure(Object context, Throwable var2) {
            Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SUB_STATUSES_REPORT);
            intent.putExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
            intent.putParcelableArrayListExtra(BaseConstant.SUB_DEVICE_ID_LIST_STATUS, statuses);
            intent.putExtra(BaseConstant.COMMON_ERROR, var2.getMessage());
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }

    /**
     * Reports the status for a child device.
     *
     * @param deviceId Indicates the ID of the child device.
     * @param status Indicates the status to report.
     */
    public void reportSubDeviceStatus(String deviceId, String status) {

        DeviceStatus deviceStatus = new DeviceStatus();
        deviceStatus.setDeviceId(deviceId);
        deviceStatus.setStatus(status);
        ArrayList<DeviceStatus> statuses = new ArrayList<DeviceStatus>();
        statuses.add(deviceStatus);

        reportSubDeviceStatus(statuses);

    }


    /**
     * Reports the statuses for a batch of child devices.
     *
     * @param statuses Indicates the statuses to report.
     */
    public void reportSubDeviceStatus(ArrayList<DeviceStatus> statuses) {

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setServiceId("$sub_device_manager");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());
        deviceEvent.setEventType("sub_device_update_status");

        Map<String, Object> para = new HashMap<String, Object>();
        para.put("device_statuses", statuses);
        deviceEvent.setParas(para);

        getClient().reportEvent(deviceEvent, new SubDeviceStatusReportActionListener(statuses));

    }

    /**
     * Adds child devices.
     *
     * @param deviceInfoList Indicates the new child devices.
     */
    public void reportSubDeviceAdd(List<DeviceInfo> deviceInfoList) {
        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setServiceId("$sub_device_manager");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());
        deviceEvent.setEventType("add_sub_device_request");

        Map<String, Object> paras = new HashMap<String, Object>();
        paras.put("devices", deviceInfoList);
        deviceEvent.setParas(paras);

        getClient().reportEvent(deviceEvent, null);
    }

    /**
     * Deletes child devices.
     *
     * @param deviceIds Indicates the child device IDs to delete.
     */
    public void reportSubDeviceDelete(List<String> deviceIds) {
        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setServiceId("$sub_device_manager");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());
        deviceEvent.setEventType("delete_sub_device_request");

        Map<String, Object> paras = new HashMap<String, Object>();
        paras.put("devices", deviceIds);
        deviceEvent.setParas(paras);

        getClient().reportEvent(deviceEvent, null);
    }

    /**
     * Called when events are processed. This method is automatically called by the SDK.
     *
     * @param deviceEvents Indicates device events.
     */
    @Override
    public void onEvent(DeviceEvents deviceEvents) {

        super.onEvent(deviceEvents);

        for (DeviceEvent deviceEvent : deviceEvents.getServices()) {

            if ("add_sub_device_notify".equals(deviceEvent.getEventType())) {

                SubDevicesInfo subDevicesInfo = JsonUtil.convertMap2Object(
                        deviceEvent.getParas(), SubDevicesInfo.class);

                onAddSubDevices(subDevicesInfo);


            } else if ("delete_sub_device_notify".equals(deviceEvent.getEventType())) {

                SubDevicesInfo subDevicesInfo = JsonUtil.convertMap2Object(
                        deviceEvent.getParas(), SubDevicesInfo.class);

                onDeleteSubDevices(subDevicesInfo);

            } else if ("add_sub_device_response".equals(deviceEvent.getEventType())) {
                SubDevicesAddInfo subDevicesAddInfo = JsonUtil.convertMap2Object(
                        deviceEvent.getParas(), SubDevicesAddInfo.class);

                onSubDeviceAddResponse(subDevicesAddInfo);

            } else if ("delete_sub_device_response".equals(deviceEvent.getEventType())) {
                SubDevicesDeleteInfo subDevicesDeleteInfo = JsonUtil.convertMap2Object(
                        deviceEvent.getParas(), SubDevicesDeleteInfo.class);

                onSubDeviceDeleteResponse(subDevicesDeleteInfo);

            }
        }
    }

    private void onSubDeviceDeleteResponse(SubDevicesDeleteInfo subDevicesDeleteInfo) {
        Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SUB_DELETE_DEVICE_RESPONSE);
        intent.putExtra(BaseConstant.SUB_DEVICE_DELETE, subDevicesDeleteInfo);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void onSubDeviceAddResponse(SubDevicesAddInfo subDevicesAddInfo) {
        Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SUB_ADD_DEVICE_RESPONSE);
        intent.putExtra(BaseConstant.SUB_DEVICE_ADD, subDevicesAddInfo);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    /**
     * Called when a message is processed.
     *
     * @param message Indicates the message.
     */
    @Override
    public void onDeviceMessage(DeviceMessage message) {

        // For a child device
        if (message.getDeviceId() != null && !message.getDeviceId().equals(this.getDeviceId())) {

            this.onSubdevMessage(message);
            return;
        }
    }

    /**
     * Called when a command is processed.
     *
     * @param requestId Indicates a request ID.
     * @param command Indicates the command.
     */
    @Override
    public void onCommand(String requestId, Command command) {

        // For a child device
        if (command.getDeviceId() != null && !command.getDeviceId().equals(this.getDeviceId())) {

            this.onSubdevCommand(requestId, command);
            return;
        }

        // For a gateway
        super.onCommand(requestId, command);

    }

    /**
     * Called when a property setting request is processed.
     *
     * @param requestId Indicates a request ID.
     * @param propsSet Indicates the property setting request.
     */
    @Override
    public void onPropertiesSet(String requestId, PropsSet propsSet) {
        // For a child device
        if (propsSet.getDeviceId() != null && !propsSet.getDeviceId().equals(this.getDeviceId())) {

            this.onSubdevPropertiesSet(requestId, propsSet);
            return;
        }

        // For a gateway
        super.onPropertiesSet(requestId, propsSet);

    }

    /**
     * Called when a property query request is processed.
     *
     * @param requestId Indicates a request ID.
     * @param propsGet Indicates the property query request.
     */
    @Override
    public void onPropertiesGet(String requestId, PropsGet propsGet) {

        // For a child device
        if (propsGet.getDeviceId() != null && !propsGet.getDeviceId().equals(this.getDeviceId())) {

            this.onSubdevPropertiesGet(requestId, propsGet);
            return;
        }

        // For a gateway
        super.onPropertiesGet(requestId, propsGet);
    }

    /**
     * Called when a child device addition request is processed.
     *
     * @param subDevicesInfo Indicates the child device details.
     */
    public void onAddSubDevices(SubDevicesInfo subDevicesInfo) {
        if (subDevicesPersistence != null) {
            subDevicesPersistence.addSubDevices(subDevicesInfo);
        }
        Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SUB_ADD_DEVICE_NOTIFY);
        intent.putExtra(BaseConstant.SUB_DEVICE_LIST, subDevicesInfo);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    /**
     * Called when a child device deletion request is processed.
     *
     * @param subDevicesInfo Indicates the child device details.
     */
    public void onDeleteSubDevices(SubDevicesInfo subDevicesInfo) {
        if (subDevicesPersistence != null) {
            subDevicesPersistence.deleteSubDevices(subDevicesInfo);
        }
        Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_SYS_SUB_DELETE_DEVICE_NOTIFY);
        intent.putExtra(BaseConstant.SUB_DEVICE_LIST, subDevicesInfo);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    /**
     * Synchronizes child device details to the platform.
     */
    protected void syncSubDevices() {
        Log.i(TAG, "start to syncSubDevices, local version is " + subDevicesPersistence.getVersion());

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setEventType("sub_device_sync_request");
        deviceEvent.setServiceId("sub_device_manager");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());

        Map<String, Object> para = new HashMap<String, Object>();
        para.put("version", subDevicesPersistence.getVersion());
        deviceEvent.setParas(para);
        getClient().reportEvent(deviceEvent, null);

    }


    /**
     * Called when a command delivered to a child device is processed. The gateway must forward such a command to the child device. This method must be implemented by the child class.
     *
     * @param requestId Indicates a request ID.
     * @param command Indicates the command.
     */
    public abstract void onSubdevCommand(String requestId, Command command);

    /**
     * Called when a property setting request delivered to a child device is processed. The gateway must forward such a request to the child device. This method must be implemented by the child class.
     *
     * @param requestId Indicates a request ID.
     * @param propsSet Indicates the properties to set.
     */
    public abstract void onSubdevPropertiesSet(String requestId, PropsSet propsSet);

    /**
     * Called when a property query request delivered to a child device is processed. The gateway must forward such a request to the child device. This method must be implemented by the child class.
     *
     * @param requestId Indicates a request ID.
     * @param propsGet Indicates the properties to query.
     */
    public abstract void onSubdevPropertiesGet(String requestId, PropsGet propsGet);

    /**
     * Called when a message delivered to a child device is processed. The gateway must forward such a message to the child device. This method must be implemented by the child class.
     *
     * @param message Indicates the message.
     */
    public abstract void onSubdevMessage(DeviceMessage message);
}
