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

package com.huaweicloud.sdk.iot.device.demo;

import android.content.Context;

import com.huaweicloud.sdk.iot.device.IoTDevice;

import java.security.KeyStore;

public class Device {
    private static IoTDevice ioTDevice;

    public static void init(Context mContext, String serverUri, String deviceId, String deviceSecret) {
        ioTDevice = new IoTDevice(mContext, serverUri, deviceId, deviceSecret);
    }

    public static void init(Context mContext, String serverUri, String deviceId, KeyStore keyStore, String keyPassword) {
        ioTDevice = new IoTDevice(mContext, serverUri, deviceId, keyStore, keyPassword);
    }

    public static IoTDevice getDevice() {
        return ioTDevice;
    }

    public static void connect() {
        if (ioTDevice != null) {
            ioTDevice.init();
        }
    }

    public static void close() {
        if (ioTDevice != null) {
            ioTDevice.close();
        }
    }
}
