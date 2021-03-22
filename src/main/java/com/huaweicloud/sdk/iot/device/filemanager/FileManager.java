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

package com.huaweicloud.sdk.iot.device.filemanager;

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

import static com.huaweicloud.sdk.iot.device.constant.BaseConstant.URLPARAM_INFO;
import static com.huaweicloud.sdk.iot.device.constant.IotDeviceIntent.ACTION_IOT_DEVICE_GET_DOWNLOAD_URL;
import static com.huaweicloud.sdk.iot.device.constant.IotDeviceIntent.ACTION_IOT_DEVICE_GET_UPLOAD_URL;


/**
 * Not completed
 */
public class FileManager extends AbstractService {

    private static final String TAG = "FileManager";

    private Context mContext;

    public FileManager(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Obtains the URL for file upload.
     *
     * @param fileName Indicates the name of the file to upload.
     * @param fileAttributes Indicates the file attributes.
     */
    public void getUploadUrl(String fileName, Map<String, Object> fileAttributes) {

        Map<String, Object> node = new HashMap<String, Object>();
        node.put("file_name", fileName);
        node.put("file_attributes", fileAttributes);

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setEventType("get_upload_url");
        deviceEvent.setParas(node);
        deviceEvent.setServiceId("$file_manager");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());

        getIotDevice().getClient().reportEvent(deviceEvent, new GetUploadUrlActionListener());

    }

    static class GetUploadUrlActionListener implements ActionListener {
        @Override
        public void onSuccess(Object context) {

        }

        @Override
        public void onFailure(Object context, Throwable var2) {
            Log.e(TAG, "reportEvent failed: " + var2.getMessage());
        }
    }

    /**
     * Obtains the URL for file download.
     *
     * @param fileName Indicates the file name.
     * @param fileAttributes Indicates the file attributes.
     */
    public void getDownloadUrl(String fileName, Map<String, Object> fileAttributes) {

        Map<String, Object> node = new HashMap<String, Object>();
        node.put("file_name", fileName);
        node.put("file_attributes", fileAttributes);

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setEventType("get_download_url");
        deviceEvent.setParas(node);
        deviceEvent.setServiceId("$file_manager");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());

        getIotDevice().getClient().reportEvent(deviceEvent, new GetDownloadUrlActionListener());

    }

    static class GetDownloadUrlActionListener implements ActionListener {
        @Override
        public void onSuccess(Object context) {

        }

        @Override
        public void onFailure(Object context, Throwable var2) {
            Log.e(TAG, "reportEvent failed: " + var2.getMessage());
        }
    }

    /**
     * Reports the file upload result.
     *
     * @param paras Indicates event parameters.
     */
    public void uploadResultReport(Map<String, Object> paras) {

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setEventType("upload_result_report");
        deviceEvent.setParas(paras);
        deviceEvent.setServiceId("$file_manager");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());

        getIotDevice().getClient().reportEvent(deviceEvent, new UploadResultReportActionListener());
    }

    static class UploadResultReportActionListener implements ActionListener {
        @Override
        public void onSuccess(Object context) {

        }

        @Override
        public void onFailure(Object context, Throwable var2) {
            Log.e(TAG, "reportEvent failed: " + var2.getMessage());
        }
    }

    /**
     * Reports the file download result.
     *
     * @param paras Indicates event parameters.
     */
    public void downloadResultReport(Map<String, Object> paras) {
        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setEventType("download_result_report");
        deviceEvent.setParas(paras);
        deviceEvent.setServiceId("$file_manager");
        deviceEvent.setEventTime(IotUtil.getTimeStamp());

        getIotDevice().getClient().reportEvent(deviceEvent, new DownloadResultReportActionListener());
    }

    static class DownloadResultReportActionListener implements ActionListener {
        @Override
        public void onSuccess(Object context) {

        }

        @Override
        public void onFailure(Object context, Throwable var2) {
            Log.e(TAG, "reportEvent failed: " + var2.getMessage());
        }
    }

    /**
     * Called when the file is processed.
     *
     * @param deviceEvent Indicates the service event.
     */
    @Override
    public void onEvent(DeviceEvent deviceEvent) {

        if (deviceEvent.getEventType().equalsIgnoreCase("get_upload_url_response")) {
            UrlParam urlParam = JsonUtil.convertMap2Object(deviceEvent.getParas(), UrlParam.class);
            onUploadUrl(urlParam);
        } else if (deviceEvent.getEventType().equalsIgnoreCase("get_download_url_response")) {
            UrlParam urlParam = JsonUtil.convertMap2Object(deviceEvent.getParas(), UrlParam.class);
            onDownloadUrl(urlParam);
        }
    }

    private void onDownloadUrl(UrlParam urlParam) {
        Intent intent = new Intent(ACTION_IOT_DEVICE_GET_DOWNLOAD_URL);
        intent.putExtra(URLPARAM_INFO, urlParam);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void onUploadUrl(UrlParam urlParam) {
        Intent intent = new Intent(ACTION_IOT_DEVICE_GET_UPLOAD_URL);
        intent.putExtra(URLPARAM_INFO, urlParam);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

}
