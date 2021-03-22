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

package com.huaweicloud.sdk.iot.device.log;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.service.AbstractService;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 日志上传服务
 */
public class LogService extends AbstractService {

    private static final String TAG = "LogService";

    private static final String SWITCH_ON = "on";

    public static final String MQTT_CONNECTION_SUCCESS = "MQTT_CONNECTION_SUCCESS";    //mqtt连接成功

    public static final String MQTT_CONNECTION_FAILURE = "MQTT_CONNECTION_FAILURE";    //mqtt连接失败

    public static final String MQTT_CONNECTION_COMPLETE = "MQTT_CONNECTION_COMPLETE";  //mqtt重连

    public static final String MQTT_CONNECTION_LOST = "MQTT_CONNECTION_LOST";          //mqtt连接丢失

    public static final String IOT_ERROR_LOG = "iot_error_log";

    private Context mContext;

    public LogService(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 日志上报
     *
     * @param content 日志内容
     */
    public void reportLog(String content) {
        Map<String, Object> paras = new HashMap<String, Object>();
        paras.put("type", "DEVICE_STATUS");
        paras.put("content", content);

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setParas(paras);
        deviceEvent.setEventType("log_report");
        deviceEvent.setServiceId("$log");
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

    @Override
    public void onEvent(DeviceEvent deviceEvent) {
        if (deviceEvent.getEventType().equalsIgnoreCase("log_config")) {
            LogMessage logMessage = JsonUtil.convertMap2Object(deviceEvent.getParas(), LogMessage.class);
            Log.i(TAG, "onEvent: " + logMessage);

            if (SWITCH_ON.equalsIgnoreCase(logMessage.getSwitchFlag())) {
                reportLog(getLogContent());
            }
        }
    }

    public String getLogContent() {
        SharedPreferences sp = mContext.getSharedPreferences(IOT_ERROR_LOG, Context.MODE_PRIVATE);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(sp.getString(MQTT_CONNECTION_FAILURE, ""));
        stringBuilder.append("\n");
        stringBuilder.append(sp.getString(MQTT_CONNECTION_LOST, ""));
        return stringBuilder.toString();
    }

}
