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
import com.huaweicloud.sdk.iot.device.client.requests.RawDeviceMessage;
import com.huaweicloud.sdk.iot.device.client.requests.ServiceProperty;
import com.huaweicloud.sdk.iot.device.filemanager.FileManager;
import com.huaweicloud.sdk.iot.device.log.LogService;
import com.huaweicloud.sdk.iot.device.ota.OTAService;
import com.huaweicloud.sdk.iot.device.timesync.TimeSyncService;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * An abstract device class.
 */
public class AbstractDevice {

    private static final String TAG = "AbstractDevice";
    private DeviceClient client;
    private String deviceId;

    private Map<String, AbstractService> services = new ConcurrentHashMap<String, AbstractService>();
    private OTAService otaService;
    private FileManager fileManager;
    private TimeSyncService timeSyncService;
    private LogService logService;
    private Context mContext;

    /**
     * Constructor used to create an AbstractDevice object. In this method, secret authentication is used.
     *
     * @param mContext Indicates the context.
     * @param serverUri Indicates the device access address, for example, ssl://iot-acc.cn-north-4.myhuaweicloud.com:8883.
     * @param deviceId Indicates a device ID.
     * @param deviceSecret Indicates a secret.
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
     * Constructor used to create an AbstractDevice object. In this method, certificate authentication is used.
     *
     * @param mContext Indicates the context.
     * @param serverUri Indicates the device access address, for example, ssl://iot-acc.cn-north-4.myhuaweicloud.com:8883.
     * @param deviceId Indicates a device ID.
     * @param keyStore Indicates a certificate keystore.
     * @param keyPassword Indicates the password of the certificate.
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
     * Constructor used to create an AbstractDevice object. In this method, the client configuration is used. This method is not recommended.
     *
     * @param mContext Indicates the context.
     * @param clientConf Indicates the client configuration.
     */
    public AbstractDevice(Context mContext, ClientConf clientConf) {
        this.client = new DeviceClient(mContext, clientConf, this);
        this.deviceId = clientConf.getDeviceId();
        this.mContext = mContext;
        initSysServices();
        Log.i(TAG, "create device: " + clientConf.getDeviceId());
    }

    /**
     * Initializes the default system service, which starts with a dollar sign ($).
     */
    private void initSysServices() {
        this.otaService = new OTAService(mContext);
        this.addService("$ota", otaService);
        this.addService("$sdk", new SdkInfo(mContext));
        this.fileManager = new FileManager(mContext);
        this.addService("$file_manager", fileManager);
        this.timeSyncService = new TimeSyncService(mContext);
        this.addService("$time_sync", timeSyncService);
        this.logService = new LogService(mContext);
        this.addService("$log", logService);

    }


    /**
     * Creates a connection to the platform.
     */
    public void init() {
        client.connect();
    }

    /**
     * Closes the connection to the platform.
     */
    public void close() {
        client.close();
    }

    /**
     * Adds a service. You can use AbstractService to define your device service and add the service to the device.
     *
     * @param serviceId Indicates a service ID, which must be defined in the device model.
     * @param deviceService Indicates the service to add.
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
     * Deletes a service.
     *
     * @param serviceId Indicates the service ID.
     */
    public void delService(String serviceId) {
        services.remove(serviceId);
    }


    /**
     * Obtains a service.
     *
     * @param serviceId Indicates the service ID.
     * @return Returns an AbstractService  instance.
     */
    public AbstractService getService(String serviceId) {
        if (TextUtils.isEmpty(serviceId)) {
            return null;
        }

        return services.get(serviceId);
    }


    /**
     * Reports a property change for a specific service. The SDK reports the changed properties.
     *
     * @param serviceId Indicates the service ID.
     * @param properties Indicates the properties.
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
     * Reports a property change for multiple services. The SDK reports the changed properties.
     *
     * @param serviceIds Indicates the service IDs whose properties are changed.
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
     * Obtains a device client. After a device client is obtained, you can call the message, property, and message APIs provided by the device client.
     *
     * @return Returns a DeviceClient instance.
     */
    public DeviceClient getClient() {
        return client;
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
     * Called when a command is received. This method is automatically called by the SDK.
     *
     * @param requestId Indicates a request ID.
     * @param command Indicates a command.
     */
    public void onCommand(String requestId, Command command) {

        IService service = getService(command.getServiceId());

        if (service != null) {
            CommandRsp rsp = service.onCommand(command);
            client.respondCommand(requestId, rsp);
        }

    }

    /**
     * Called when a property setting request is received. This method is automatically called by the SDK.
     *
     * @param requestId Indicates a request ID.
     * @param propsSet Indicates the property setting request.
     */
    public void onPropertiesSet(String requestId, PropsSet propsSet) {

        boolean existedDeviceService = false;

        for (ServiceProperty serviceProp : propsSet.getServices()) {
            IService deviceService = getService(serviceProp.getServiceId());

            if (deviceService != null) {
                existedDeviceService = true;
                // Returns the result for partial failure.
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
     * Called when a property query request is received. This method is automatically called by the SDK.
     *
     * @param requestId Indicates a request ID.
     * @param propsGet Indicates the property query request.
     */
    public void onPropertiesGet(String requestId, PropsGet propsGet) {

        List<ServiceProperty> serviceProperties = new ArrayList<ServiceProperty>();
        boolean existedDeviceService = false;
        // Queries all service IDs.
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
     * Called when events are received. This method is automatically called by the SDK.
     *
     * @param deviceEvents Indicates the events.
     */
    public void onEvent(DeviceEvents deviceEvents) {

        // For a child device
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
     * Called when a message is reported. This method is automatically called by the SDK.
     *
     * @param message Indicates the message.
     */
    public void onDeviceMessage(RawDeviceMessage message) {

    }

    /**
     * Obtains an OTA service.
     *
     * @return Returns an OTAService instance.
     */
    public OTAService getOtaService() {
        return otaService;
    }

    /**
     * Obtains a file upload/download service.
     *
     * @return Returns a FileManager instance.
     */
    public FileManager getFileManager() {
        return fileManager;
    }

    /**
     * 获取时间同步服务
     *
     * @return TimeSyncService
     */
    public TimeSyncService getTimeSyncService() {
        return timeSyncService;
    }

    /**
     * 获取日志服务
     *
     * @return LogService
     */
    public LogService getLogService() {
        return logService;
    }
}
