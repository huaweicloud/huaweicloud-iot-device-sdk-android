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
 * OTA服务类，提供设备升级相关接口，使用方法：
 * IoTDevice device = new IoTDevice(...
 * OTAService otaService = device.getOtaService();
 * otaService.setOtaListener(new OTAListener() {
 * 具体参见OTASample
 */
public class OTAService extends AbstractService {

    //升级上报的错误码，用户也可以扩展自己的错误码
    public static final int OTA_CODE_SUCCESS = 0;//成功
    public static final int OTA_CODE_BUSY = 1;  //设备使用中
    public static final int OTA_CODE_SIGNAL_BAD = 2;  //信号质量差
    public static final int OTA_CODE_NO_NEED = 3;  //已经是最新版本
    public static final int OTA_CODE_LOW_POWER = 4;  //电量不足
    public static final int OTA_CODE_LOW_SPACE = 5;  //剩余空间不足
    public static final int OTA_CODE_DOWNLOAD_TIMEOUT = 6;  //下载超时
    public static final int OTA_CODE_CHECK_FAIL = 7;  //升级包校验失败
    public static final int OTA_CODE_UNKNOWN_TYPE = 8;  //升级包类型不支持
    public static final int OTA_CODE_LOW_MEMORY = 9;  //内存不足
    public static final int OTA_CODE_INSTALL_FAIL = 10;  //安装升级包失败
    public static final int OTA_CODE_INNER_ERROR = 255;  // 内部异常

    private static final String TAG = "OTAService";
    private Context mContext;

    public OTAService(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 上报升级状态
     *
     * @param result      升级结果
     * @param progress    升级进度0-100
     * @param version     当前版本
     * @param description 具体失败的原因，可选参数
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
     * 上报固件版本信息
     *
     * @param fwVersion 固件版本
     * @param swVersion 软件版本
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
     * 接收OTA事件处理
     *
     * @param deviceEvent 服务事件
     */
    @Override
    public void onEvent(DeviceEvent deviceEvent) {

        if (deviceEvent.getEventType().equalsIgnoreCase("version_query")) {
            onQueryVersion();
        } else if (deviceEvent.getEventType().equalsIgnoreCase("firmware_upgrade")
                || deviceEvent.getEventType().equalsIgnoreCase("software_upgrade")) {

            OTAPackage pkg = JsonUtil.convertMap2Object(deviceEvent.getParas(), OTAPackage.class);
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
