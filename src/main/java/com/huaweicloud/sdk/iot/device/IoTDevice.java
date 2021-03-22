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

package com.huaweicloud.sdk.iot.device;

import android.content.Context;

import com.huaweicloud.sdk.iot.device.client.ClientConf;
import com.huaweicloud.sdk.iot.device.client.DeviceClient;
import com.huaweicloud.sdk.iot.device.service.AbstractDevice;
import com.huaweicloud.sdk.iot.device.service.AbstractService;

import java.security.KeyStore;
import java.util.List;


/**
 * Provides an IoT device class, which is the entry class of the SDK. The following two usage methods are provided:
 * 1. Product model programming: Implement services based on product models. The SDK automatically completes communications between devices and the platform. This method is easy to use and suitable for most scenarios.
 * public class SmokeDetectorService extends AbstractService {
 *
 * @Property int smokeAlarm = 1;
 * <p>
 * public int getSmokeAlarm() {
 * // Read properties from a device.
 * }
 * <p>
 * public void setSmokeAlarm(int smokeAlarm) {
 * // Write properties to a device.
 * }
 * }
 * <p>
 * // Create a device.
 * IoTDevice device = new IoTDevice(serverUri, deviceId, secret);
 * // Add a service.
 * SmokeDetectorService smokeDetectorService = new SmokeDetectorService();
 * device.addService("smokeDetector", smokeDetectorService);
 * device.init();
 * <p>
 * <p>
 * 2. Communication interface programming: Obtain the device client and enable it to directly communicate with the platform. This method is more complex and flexible.
 * <p>
 * IoTDevice device = new IoTDevice(serverUri, deviceId, secret);
 * device.init();
 * device.getClient().reportDeviceMessage(new DeviceMessage("hello"));
 * device.getClient().reportProperties(....)
 */
public class IoTDevice extends AbstractDevice {

    /**
     * A constructor that creates an IoTDevice instance. In this method, secret authentication is used.
     *
     * @param mContext Indicates the context.
     * @param serverUri Indicates the device access address, for example, ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883.
     * @param deviceId Indicates a device ID.
     * @param deviceSecret Indicates a secret.
     */
    public IoTDevice(Context mContext, String serverUri, String deviceId, String deviceSecret) {
        super(mContext, serverUri, deviceId, deviceSecret);

    }

    /**
     * A constructor that creates an IoTDevice instance. In this method, certificate authentication is used.
     *
     * @param mContext Indicates the context.
     * @param serverUri Indicates the device access address, for example, ssl://iot-mqtts.cn-north-4.myhuaweicloud.com:8883.
     * @param deviceId Indicates a device ID.
     * @param keyStore Indicates a keystore of the certificate.
     * @param keyPassword Indicates the password of the certificate.
     */
    public IoTDevice(Context mContext, String serverUri, String deviceId, KeyStore keyStore, String keyPassword) {

        super(mContext, serverUri, deviceId, keyStore, keyPassword);
    }

    /**
     * A constructor that creates an IoTDevice instance. In this method, the client configuration is used. This method is not recommended.
     *
     * @param mContext Indicates the context.
     * @param clientConf Indicates the client configuration.
     */
    public IoTDevice(Context mContext, ClientConf clientConf) {
        super(mContext, clientConf);
    }

    /**
     * Creates a connection to the platform.
     *
     */
    public void init() {
        super.init();
    }

    /**
     * Closes the connection to the platform.
     *
     */
    public void close() {
        super.close();
    }

    /**
     * Adds a service. You can use AbstractService to define your own service and add the service to the device.
     *
     * @param serviceId Indiates a dervice ID, which must be defined in the product model.
     * @param deviceService Indciates the service to add.
     */
    public void addService(String serviceId, AbstractService deviceService) {

        super.addService(serviceId, deviceService);
    }


    /**
     * Obtains a service.
     *
     * @param serviceId Indicates the service ID.
     * @return Returns an AbstractService instance.
     */
    public AbstractService getService(String serviceId) {

        return super.getService(serviceId);
    }


    /**
     * Reports a property change for a specific service. The SDK reports the changed properties.
     *
     * @param serviceId Indicates the service ID.
     * @param properties Indicates the properties.
     */
    public void firePropertiesChanged(String serviceId, String... properties) {
        super.firePropertiesChanged(serviceId, properties);
    }

    /**
     * Reports a property change for multiple services. The SDK reports the changed properties.
     *
     * @param serviceIds Indicates the service IDs whose properties are changed.
     */
    public void fireServicesChanged(List<String> serviceIds) {
        super.fireServicesChanged(serviceIds);
    }

    /**
     * Obtains a device client. After a device client is obtained, you can call the message, property, and command APIs provided by the device client.
     *
     * @return Returns a DeviceClient instance.
     */
    public DeviceClient getClient() {
        return super.getClient();
    }

}
