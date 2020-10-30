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
 * 抽象网关，实现了子设备管理，子设备消息转发功能
 */
public abstract class AbstractGateway extends IoTDevice {

    private final static String TAG = "AbstractGateway";

    private SubDevicesPersistence subDevicesPersistence;
    private GatewayBroadcastReceiver gatewayBroadcastReceiver = new GatewayBroadcastReceiver();
    private Context mContext;

    /**
     * 构造函数，通过设备密码认证
     *
     * @param mContext              上下文
     * @param subDevicesPersistence 子设备持久化，提供子设备信息保存能力
     * @param serverUri             平台访问地址，比如ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId              设备id
     * @param deviceSecret          设备密码
     */
    public AbstractGateway(Context mContext, SubDevicesPersistence subDevicesPersistence, String serverUri, String deviceId, String deviceSecret) {
        super(mContext, serverUri, deviceId, deviceSecret);
        this.subDevicesPersistence = subDevicesPersistence;
        this.mContext = mContext;
    }

    /**
     * 构造函数，通过设备证书认证
     *
     * @param mContext              上下文
     * @param subDevicesPersistence 子设备持久化，提供子设备信息保存能力
     * @param serverUri             平台访问地址，比如ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId              设备id
     * @param keyStore              证书容器
     * @param keyPassword           证书密码
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
                        //建连时，向平台同步子设备信息
                        syncSubDevices();
                        break;
                    case BaseConstant.STATUS_RECONNECT:
                        //建连或重连时，向平台同步子设备信息
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
     * 初始化，创建到平台的连接
     */
    @Override
    public void init() {
        super.init();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(gatewayBroadcastReceiver,
                new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_CONNECT));
    }

    /**
     * 关闭到平台的连接
     */
    @Override
    public void close() {
        super.close();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(gatewayBroadcastReceiver);
    }


    /**
     * 根据设备标识码查询子设备
     *
     * @param nodeId 设备标识码
     * @return 子设备信息
     */
    public DeviceInfo getSubDeviceByNodeId(String nodeId) {
        return subDevicesPersistence.getSubDevice(nodeId);
    }

    /**
     * 根据设备id查询子设备
     *
     * @param deviceId 设备id
     * @return 子设备信息
     */
    public DeviceInfo getSubDeviceByDeviceId(String deviceId) {
        String nodeId = IotUtil.getNodeIdFromDeviceId(deviceId);
        return subDevicesPersistence.getSubDevice(nodeId);
    }

    /**
     * 上报子设备发现结果
     *
     * @param deviceInfos 子设备信息列表
     * @param listener    发布监听器
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
     * 上报子设备消息
     *
     * @param deviceMessage 设备消息
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
     * 上报子设备属性
     *
     * @param deviceId 子设备id
     * @param services 服务属性列表
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
     * 批量上报子设备属性
     *
     * @param subDeviceProperties 子设备属性列表
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
     * 上报子设备状态
     *
     * @param deviceId 子设备id
     * @param status   设备状态
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
     * 批量上报子设备状态
     *
     * @param statuses 子设备状态列表
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
     * 网关新增子设备
     *
     * @param deviceInfoList 新增子设备列表
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
     * 网关删除子设备
     *
     * @param deviceIds 待删除的子设备ID列表
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
     * 事件处理回调，由SDK自动调用
     *
     * @param deviceEvents 设备事件
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
     * 设备消息处理回调
     *
     * @param message 消息
     */
    @Override
    public void onDeviceMessage(DeviceMessage message) {

        //子设备的
        if (message.getDeviceId() != null && !message.getDeviceId().equals(this.getDeviceId())) {

            this.onSubdevMessage(message);
            return;
        }
    }

    /**
     * 命令处理回调
     *
     * @param requestId 请求id
     * @param command   命令
     */
    @Override
    public void onCommand(String requestId, Command command) {

        //子设备的
        if (command.getDeviceId() != null && !command.getDeviceId().equals(this.getDeviceId())) {

            this.onSubdevCommand(requestId, command);
            return;
        }

        //网关的
        super.onCommand(requestId, command);

    }

    /**
     * 属性设置处理回调
     *
     * @param requestId 请求id
     * @param propsSet  属性设置请求
     */
    @Override
    public void onPropertiesSet(String requestId, PropsSet propsSet) {
        //子设备的
        if (propsSet.getDeviceId() != null && !propsSet.getDeviceId().equals(this.getDeviceId())) {

            this.onSubdevPropertiesSet(requestId, propsSet);
            return;
        }

        //网关的
        super.onPropertiesSet(requestId, propsSet);

    }

    /**
     * 属性查询处理回调
     *
     * @param requestId 请求id
     * @param propsGet  属性查询请求
     */
    @Override
    public void onPropertiesGet(String requestId, PropsGet propsGet) {

        //子设备的
        if (propsGet.getDeviceId() != null && !propsGet.getDeviceId().equals(this.getDeviceId())) {

            this.onSubdevPropertiesGet(requestId, propsGet);
            return;
        }

        //网关的
        super.onPropertiesGet(requestId, propsGet);
    }

    /**
     * 添加子设备处理回调
     *
     * @param subDevicesInfo 子设备信息
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
     * 删除子设备处理回调
     *
     * @param subDevicesInfo 子设备信息
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
     * 向平台请求同步子设备信息
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
     * 子设备命令下发处理，网关需要转发给子设备，需要子类实现
     *
     * @param requestId 请求id
     * @param command   命令
     */
    public abstract void onSubdevCommand(String requestId, Command command);

    /**
     * 子设备属性设置，网关需要转发给子设备，需要子类实现
     *
     * @param requestId 请求id
     * @param propsSet  属性设置
     */
    public abstract void onSubdevPropertiesSet(String requestId, PropsSet propsSet);

    /**
     * 子设备读属性，，网关需要转发给子设备，需要子类实现
     *
     * @param requestId 请求id
     * @param propsGet  属性查询
     */
    public abstract void onSubdevPropertiesGet(String requestId, PropsGet propsGet);

    /**
     * 子设备消息下发，网关需要转发给子设备，需要子类实现
     *
     * @param message 设备消息
     */
    public abstract void onSubdevMessage(DeviceMessage message);
}
