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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.constant.BaseConstant;
import com.huaweicloud.sdk.iot.device.constant.IotDeviceIntent;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Provides SDK information.
 */
public class SdkInfo extends AbstractService {

    private static final String TAG = "SdkInfo";

    @Property(writeable = false)
    private String type = "Android";

    @Property(writeable = false)
    private String version = "1.0.0";

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    private static final String IOT_VERSION_INFO = "iot_version_info";

    private static final String IOT_VERSION_INFO_SDK = "iot_version_info_sdk";

    private static final String IOT_VERSION_INFO_FW = "iot_version_info_fw";

    private static final String IOT_VERSION_INFO_SW = "iot_version_info_sw";

    private ScheduledExecutorService executorService;

    private Context mContext;

    private ConnectReceiver connectReceiver = new ConnectReceiver();

    public SdkInfo(Context mContext) {
        this.mContext = mContext;
        LocalBroadcastManager.getInstance(mContext).registerReceiver(connectReceiver, new IntentFilter(IotDeviceIntent.ACTION_IOT_DEVICE_CONNECT));
    }

    /**
     * 上报设备版本信息
     *
     * @param swVersion 软件版本信息
     * @param fwVersion 固件版本信息
     */
    public void reportDeviceVersion(String swVersion, String fwVersion) {
        String sdkVersion = getType() + "_v" + getVersion();

        SharedPreferences sp = mContext.getSharedPreferences(IOT_VERSION_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(IOT_VERSION_INFO_SDK, sdkVersion);
        editor.putString(IOT_VERSION_INFO_SW, swVersion);
        editor.putString(IOT_VERSION_INFO_FW, fwVersion);

        editor.commit();

        reportDeviceVersion(swVersion, fwVersion, sdkVersion);
    }

    private void reportDeviceVersion(String swVersion, String fwVersion, String sdkVersion) {
        Map<String, Object> paras = new HashMap<String, Object>();
        paras.put("device_sdk_version", sdkVersion);
        paras.put("sw_version", swVersion);
        paras.put("fw_version", fwVersion);

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setParas(paras);
        deviceEvent.setEventType("sdk_info_report");
        deviceEvent.setServiceId("$sdk_info");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());

        getIotDevice().getClient().reportEvent(deviceEvent, new ActionListener() {
            @Override
            public void onSuccess(Object context) {

            }

            @Override
            public void onFailure(Object context, Throwable var2) {
                Log.e(TAG, "reportEvent failed: " + var2.getMessage());
            }
        });
    }

    private class ConnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (IotDeviceIntent.ACTION_IOT_DEVICE_CONNECT.equals(intent.getAction())) {
                int broadcastStatus = intent.getIntExtra(BaseConstant.BROADCAST_STATUS, BaseConstant.STATUS_FAIL);
                if (broadcastStatus != BaseConstant.STATUS_SUCCESS) {
                    return;
                }
                //定时上报设备版本信息
                if (executorService == null) {
                    executorService = Executors.newScheduledThreadPool(1);
                    executorService.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            String swVersion = "";
                            String fwVersion = "";
                            String sdkVersion = "";
                            SharedPreferences sp = mContext.getSharedPreferences(IOT_VERSION_INFO, Context.MODE_PRIVATE);
                            sdkVersion = sp.getString(IOT_VERSION_INFO_SDK, "");
                            String curSdkVersion = getType() + "_v" + getVersion();
                            if (curSdkVersion.equals(sdkVersion)) {
                                return;
                            }
                            sdkVersion = curSdkVersion;
                            swVersion = sp.getString(IOT_VERSION_INFO_SW, "");
                            fwVersion = sp.getString(IOT_VERSION_INFO_FW, "");
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString(IOT_VERSION_INFO_SDK, sdkVersion);
                            editor.commit();

                            reportDeviceVersion(swVersion, fwVersion, sdkVersion);
                        }
                    }, 0, 5, TimeUnit.DAYS);
                }
            }
        }
    }
}
