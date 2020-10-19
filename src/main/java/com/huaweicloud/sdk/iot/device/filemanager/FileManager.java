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
 * 未完成
 */
public class FileManager extends AbstractService {

    private static final String TAG = "FileManager";

    private Context mContext;

    public FileManager(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 获取文件上传url
     *
     * @param fileName       文件名
     * @param fileAttributes 文件属性
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
     * 获取文件下载url
     *
     * @param fileName       下载文件名
     * @param fileAttributes 文件属性
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
     * 设备上报文件上传结果
     *
     * @param paras 事件参数
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
     * 设备上报文件下载结果
     *
     * @param paras 事件参数
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
     * 接收文件处理事件
     *
     * @param deviceEvent 服务事件
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
