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

package com.huaweicloud.sdk.iot.device.timesync;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.huaweicloud.sdk.iot.device.client.requests.DeviceEvent;
import com.huaweicloud.sdk.iot.device.constant.BaseConstant;
import com.huaweicloud.sdk.iot.device.constant.IotDeviceIntent;
import com.huaweicloud.sdk.iot.device.service.AbstractService;
import com.huaweicloud.sdk.iot.device.transport.ActionListener;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 时间同步服务，提供简单的时间同步服务，使用方法：
 * IoTDevice device = new IoTDevice(...
 * TimeSyncService timeSyncService = device.getTimeSyncService();
 * timeSyncService.requestTimeSync();
 */
public class TimeSyncService extends AbstractService {

    private static final String TAG = "TimeSyncService";

    private Context mContext;

    public TimeSyncService(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 发起时间同步请求，使用TimeSyncListener接收响应
     */
    public void requestTimeSync() {

        Map<String, Object> node = new HashMap<String, Object>();
        node.put("device_send_time", System.currentTimeMillis());

        DeviceEvent deviceEvent = new DeviceEvent();
        deviceEvent.setEventType("time_sync_request");
        deviceEvent.setParas(node);
        deviceEvent.setServiceId("$time_sync");
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

        if (deviceEvent.getEventType().equalsIgnoreCase("time_sync_response")) {
            TimeSyncMessage timeSyncMessage = JsonUtil.convertMap2Object(deviceEvent.getParas(), TimeSyncMessage.class);
            Intent intent = new Intent(IotDeviceIntent.ACTION_IOT_DEVICE_TIME_SYNC_RESPONSE);
            intent.putExtra(BaseConstant.DEVICE_TIME_SYNC_MESSAGE, timeSyncMessage);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    }

}
