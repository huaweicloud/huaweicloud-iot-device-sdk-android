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

package com.huaweicloud.sdk.iot.device.ota;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.service.AbstractService;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.util.HashMap;
import java.util.Map;

import static com.huaweicloud.sdk.iot.device.constant.BaseConstant.OTAPACKAGE_INFO;
import static com.huaweicloud.sdk.iot.device.constant.IotDeviceIntent.ACTION_IOT_DEVICE_UPGRADE_EVENT;
import static com.huaweicloud.sdk.iot.device.constant.IotDeviceIntent.ACTION_IOT_DEVICE_VERSION_QUERY_EVENT;


/**
 * Provides APIs related to OTA upgrades.
 * IoTDevice device = new IoTDevice(...
 * OTAService otaService = device.getOtaService();
 * otaService.setOtaListener(new OTAListener() {
 * For details, see OTASample.
 */
public class OTAService extends AbstractService {

    // Error codes reported during an upgrade. You can also define your own error codes.
    public static final int OTA_CODE_SUCCESS = 0;// Upgraded.
    public static final int OTA_CODE_BUSY = 1;  // The device is in use.
    public static final int OTA_CODE_SIGNAL_BAD = 2;  // Poor signal.
    public static final int OTA_CODE_NO_NEED = 3;  // Already the latest version.
    public static final int OTA_CODE_LOW_POWER = 4;  // Low battery.
    public static final int OTA_CODE_LOW_SPACE = 5;  // Insufficient free space.
    public static final int OTA_CODE_DOWNLOAD_TIMEOUT = 6;  // Download timed out.
    public static final int OTA_CODE_CHECK_FAIL = 7;  // Upgrade package verification failed.
    public static final int OTA_CODE_UNKNOWN_TYPE = 8;  // Unsupported upgrade package type.
    public static final int OTA_CODE_LOW_MEMORY = 9;  // Insufficient memory.
    public static final int OTA_CODE_INSTALL_FAIL = 10;  // Upgrade package installation failed.
    public static final int OTA_CODE_INNER_ERROR = 255;  // Internal exception.

    private static final String TAG = "OTAService";
    private Context mContext;

    public OTAService(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Reports the upgrade status.
     *
     * @param result Indicates the upgrade result.
     * @param progress Indicates the upgrade progress, ranging from 0 to 100.
     * @param version Indicates the current version.
     * @param description Indicates the description of the failure. It is optional.
     */
    public void reportOtaStatus(int result, int progress, String version, String description) {

        Map<String, Object> node = new HashMap<String, Object>();
        node.put("result_code", result);
        node.put("progress", progress);
        if (description != null) {
            node.put("description", description);
        }
        node.put("version", version);

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setEventType("upgrade_progress_report");
        deviceEvent.setParas(node);
        deviceEvent.setServiceId("$ota");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());

        getIotDevice().getClient().reportEvent(deviceEvent, new ReportOtaStatusActionListener());
    }

    static class ReportOtaStatusActionListener implements ActionListener {
        @Override
        public void onSuccess(Object context) {

        }

        @Override
        public void onFailure(Object context, Throwable var2) {
            Log.e(TAG, "reportOtaStatus failed: " + var2.getMessage());
        }
    }

    /**
     * Reports the firmware version.
     *
     * @param fwVersion Indicates a firmware version.
     * @param swVersion Indicates a software version.
     */
    public void reportVersion(String fwVersion, String swVersion) {

        Map<String, Object> node = new HashMap<String, Object>();
        node.put("fw_version", fwVersion);
        node.put("sw_version", swVersion);

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setEventType("version_report");
        deviceEvent.setParas(node);
        deviceEvent.setServiceId("$ota");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());

        getIotDevice().getClient().reportEvent(deviceEvent, new ReportVersionActionListener());

    }

    static class ReportVersionActionListener implements ActionListener {
        @Override
        public void onSuccess(Object context) {

        }

        @Override
        public void onFailure(Object context, Throwable var2) {
            Log.e(TAG, "reportVersion failed: " + var2.getMessage());
        }
    }

    /**
     * Called when an OTA upgrade event is received.
     *
     * @param deviceEvent Indicates the event.
     */
    @Override
    public void onEvent(DeviceEvent deviceEvent) {

        if (deviceEvent.getEventType().equalsIgnoreCase("version_query")) {
            onQueryVersion();
        } else if (deviceEvent.getEventType().equalsIgnoreCase("firmware_upgrade")
                || deviceEvent.getEventType().equalsIgnoreCase("software_upgrade")
                || deviceEvent.getEventType().equalsIgnoreCase("firmware_upgrade_v2")
                || deviceEvent.getEventType().equalsIgnoreCase("software_upgrade_v2")) {

            OTAPackage pkg = JsonUtil.convertMap2Object(deviceEvent.getParas(), OTAPackage.class);
            pkg.setEventType(deviceEvent.getEventType());
            onNewPackage(pkg);
        }
    }

    private void onNewPackage(OTAPackage pkg) {
        Intent intent = new Intent(ACTION_IOT_DEVICE_UPGRADE_EVENT);
        intent.putExtra(OTAPACKAGE_INFO, pkg);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void onQueryVersion() {
        Intent intent = new Intent(ACTION_IOT_DEVICE_VERSION_QUERY_EVENT);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
