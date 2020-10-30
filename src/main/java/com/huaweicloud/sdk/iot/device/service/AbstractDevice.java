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

package com.huaweicloud.sdk.iot.device.service;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.client.DeviceClient;
import com.huaweicloud.sdk.iot.device.client.IotResult;
import com.huaweicloud.sdk.iot.device.client.requests.Command;
import com.huaweicloud.sdk.iot.device.client.requests.CommandRsp;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvents;
import com.huaweicloud.sdk.iot.device.client.requests.DeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.PropsGet;
import com.huaweicloud.sdk.iot.device.client.requests.PropsSet;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.filemanager.FileManager;
import com.huaweicloud.sdk.iot.device.ota.OTAService;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 抽象设备类
 */
public class AbstractDevice {

    private static final String TAG = "AbstractDevice";
    private DeviceClient client;
    private String deviceId;

    private Map<String, AbstractService> services = new ConcurrentHashMap<String, AbstractService>();
    private OTAService otaService;
    private FileManager fileManager;
    private Context mContext;

    /**
     * 构造函数，使用密码创建设备
     *
     * @param mContext     上下文
     * @param serverUri    平台访问地址，比如ssl://iot-acc.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId     设备id
     * @param deviceSecret 设备密码
     */
    public AbstractDevice(Context mContext, String serverUri, String deviceId, String deviceSecret) {

        ClientConf clientConf = new ClientConf();
        clientConf.setServerUri(serverUri);
        clientConf.setDeviceId(deviceId);
        clientConf.setSecret(deviceSecret);
        this.deviceId = deviceId;
        this.mContext = mContext;
        this.client = new DeviceClient(mContext, clientConf, this);
        initSysServices();
        Log.i(TAG, "create device: " + clientConf.getDeviceId());

    }

    /**
     * 构造函数，使用证书创建设备
     *
     * @param mContext    上下文
     * @param serverUri   平台访问地址，比如ssl://iot-acc.cn-north-4.myhuaweicloud.com:8883
     * @param deviceId    设备id
     * @param keyStore    证书容器
     * @param keyPassword 证书密码
     */
    public AbstractDevice(Context mContext, String serverUri, String deviceId, KeyStore keyStore, String keyPassword) {

        ClientConf clientConf = new ClientConf();
        clientConf.setServerUri(serverUri);
        clientConf.setDeviceId(deviceId);
        clientConf.setKeyPassword(keyPassword);
        clientConf.setKeyStore(keyStore);
        this.deviceId = deviceId;
        this.mContext = mContext;
        this.client = new DeviceClient(mContext, clientConf, this);
        initSysServices();
        Log.i(TAG, "create device: " + clientConf.getDeviceId());
    }

    /**
     * 构造函数，直接使用客户端配置创建设备，一般不推荐这种做法
     *
     * @param mContext   上下文
     * @param clientConf 客户端配置
     */
    public AbstractDevice(Context mContext, ClientConf clientConf) {
        this.client = new DeviceClient(mContext, clientConf, this);
        this.deviceId = clientConf.getDeviceId();
        this.mContext = mContext;
        initSysServices();
        Log.i(TAG, "create device: " + clientConf.getDeviceId());
    }

    /**
     * 初始化系统默认service，系统service以$作为开头
     */
    private void initSysServices() {
        this.otaService = new OTAService(mContext);
        this.addService("$ota", otaService);
        this.addService("$sdk", new SdkInfo());
        this.fileManager = new FileManager(mContext);
        this.addService("$file_manager", fileManager);
    }


    /**
     * 初始化，创建到平台的连接
     */
    public void init() {
        client.connect();
    }

    /**
     * 关闭到平台的连接
     */
    public void close() {
        client.close();
    }

    /**
     * 添加服务。用户基于AbstractService定义自己的设备服务，并添加到设备
     *
     * @param serviceId     服务id，要和设备模型定义一致
     * @param deviceService 服务实例
     */
    public void addService(String serviceId, AbstractService deviceService) {

        deviceService.setIotDevice(this);
        deviceService.setServiceId(serviceId);

        AbstractService existedDeviceService = services.get(serviceId);
        if (null == existedDeviceService) {
            services.put(serviceId, deviceService);
        }

    }

    /**
     * 删除服务
     *
     * @param serviceId 服务id
     */
    public void delService(String serviceId) {
        services.remove(serviceId);
    }


    /**
     * 查询服务
     *
     * @param serviceId 服务id
     * @return AbstractService 服务实例
     */
    public AbstractService getService(String serviceId) {
        if (TextUtils.isEmpty(serviceId)) {
            return null;
        }

        return services.get(serviceId);
    }


    /**
     * 触发属性变化，SDK会上报变化的属性
     *
     * @param serviceId  服务id
     * @param properties 属性列表
     */
    protected void firePropertiesChanged(String serviceId, String... properties) {
        AbstractService deviceService = getService(serviceId);
        if (deviceService == null) {
            return;
        }
        Map props = deviceService.onRead(properties);

        final ServiceProperty serviceProperty = new ServiceProperty();
        serviceProperty.setServiceId(deviceService.getServiceId());
        serviceProperty.setProperties(props);
        serviceProperty.setEventTime(IotUtil.getTimeStamp());

        getClient().scheduleTask(new Runnable() {
            @Override
            public void run() {
                client.reportPropertiesForInner(Arrays.asList(serviceProperty), new ActionListener() {
                    @Override
                    public void onSuccess(Object context) {

                    }

                    @Override
                    public void onFailure(Object context, Throwable var2) {
                        Log.e(TAG, "reportProperties failed: " + var2.toString());
                    }
                });
            }
        });

    }

    /**
     * 触发多个服务的属性变化，SDK自动上报变化的属性到平台
     *
     * @param serviceIds 发生变化的服务id列表
     */
    protected void fireServicesChanged(List<String> serviceIds) {
        final List<ServiceProperty> serviceProperties = new ArrayList<ServiceProperty>();
        for (String serviceId : serviceIds) {
            AbstractService deviceService = getService(serviceId);
            if (deviceService == null) {
                Log.e(TAG, "service not found: " + serviceId);
                continue;
            }

            Map props = deviceService.onRead();

            ServiceProperty serviceProperty = new ServiceProperty();
            serviceProperty.setServiceId(deviceService.getServiceId());
            serviceProperty.setProperties(props);
            serviceProperty.setEventTime(IotUtil.getTimeStamp());
            serviceProperties.add(serviceProperty);
        }

        if (serviceProperties.isEmpty()) {
            return;
        }

        getClient().scheduleTask(new Runnable() {
            @Override
            public void run() {
                client.reportPropertiesForInner(serviceProperties, new ActionListener() {
                    @Override
                    public void onSuccess(Object context) {

                    }

                    @Override
                    public void onFailure(Object context, Throwable var2) {
                        Log.e(TAG, "reportProperties failed: " + var2.toString());
                    }
                });
            }
        });
    }

    /**
     * 获取设备客户端。获取到设备客户端后，可以直接调用客户端提供的消息、属性、命令等接口
     *
     * @return 设备客户端实例
     */
    public DeviceClient getClient() {
        return client;
    }


    /**
     * 查询设备id
     *
     * @return 设备id
     */
    public String getDeviceId() {
        return deviceId;
    }


    /**
     * 命令回调函数，由SDK自动调用
     *
     * @param requestId 请求id
     * @param command   命令
     */
    public void onCommand(String requestId, Command command) {

        IService service = getService(command.getServiceId());

        if (service != null) {
            CommandRsp rsp = service.onCommand(command);
            client.respondCommand(requestId, rsp);
        }

    }

    /**
     * 属性设置回调，，由SDK自动调用
     *
     * @param requestId 请求id
     * @param propsSet  属性设置请求
     */
    public void onPropertiesSet(String requestId, PropsSet propsSet) {

        boolean existedDeviceService = false;

        for (ServiceProperty serviceProp : propsSet.getServices()) {
            IService deviceService = getService(serviceProp.getServiceId());

            if (deviceService != null) {
                existedDeviceService = true;
                //如果部分失败直接返回
                IotResult result = deviceService.onWrite(serviceProp.getProperties());
                if (result.getResultCode() != IotResult.SUCCESS.getResultCode()) {
                    client.respondPropsSet(requestId, result);
                    return;
                }
            }
        }
        if (existedDeviceService) {
            client.respondPropsSet(requestId, IotResult.SUCCESS);
        }

    }

    /**
     * 属性查询回调，由SDK自动调用
     *
     * @param requestId 请求id
     * @param propsGet  属性查询请求
     */
    public void onPropertiesGet(String requestId, PropsGet propsGet) {

        List<ServiceProperty> serviceProperties = new ArrayList<ServiceProperty>();
        boolean existedDeviceService = false;
        //查询所有
        if (propsGet.getServiceId() == null) {

            for (String ss : services.keySet()) {
                IService deviceService = getService(ss);
                if (deviceService != null) {
                    existedDeviceService = true;
                    Map properties = deviceService.onRead();
                    ServiceProperty serviceProperty = new ServiceProperty();
                    serviceProperty.setProperties(properties);
                    serviceProperty.setServiceId(ss);
                    serviceProperties.add(serviceProperty);
                }
            }
        } else {
            IService deviceService = getService(propsGet.getServiceId());

            if (deviceService != null) {
                existedDeviceService = true;
                Map properties = deviceService.onRead();
                ServiceProperty serviceProperty = new ServiceProperty();
                serviceProperty.setProperties(properties);
                serviceProperty.setServiceId(propsGet.getServiceId());
                serviceProperties.add(serviceProperty);

            }
        }
        if (existedDeviceService) {
            client.respondPropsGet(requestId, serviceProperties);
        }

    }

    /**
     * 事件回调，由SDK自动调用
     *
     * @param deviceEvents 设备事件
     */
    public void onEvent(DeviceEvents deviceEvents) {

        //子设备的
        if (deviceEvents.getDeviceId() != null && !deviceEvents.getDeviceId().equals(getDeviceId())) {
            return;
        }

        for (DeviceEvent event : deviceEvents.getServices()) {
            IService deviceService = getService(event.getServiceId());
            if (deviceService != null) {
                deviceService.onEvent(event);
            }
        }
    }

    /**
     * 消息回调，由SDK自动调用
     *
     * @param message 消息
     */
    public void onDeviceMessage(DeviceMessage message) {

    }

    /**
     * 获取OTA服务
     *
     * @return OTAService
     */
    public OTAService getOtaService() {
        return otaService;
    }

    /**
     * 获取文件上传下载服务
     *
     * @return FileManager
     */
    public FileManager getFileManager() {
        return fileManager;
    }

}
