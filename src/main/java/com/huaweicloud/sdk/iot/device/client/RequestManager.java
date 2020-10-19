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

package com.huaweicloud.sdk.iot.device.client;

import android.util.Log;

import com.huaweicloud.sdk.iot.device.transport.RawMessage;
import com.huaweicloud.sdk.iot.device.utils.IotUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 请求管理器
 */
public class RequestManager {

    private static final String TAG = "RequestManager";

    private DeviceClient iotClient;
    private ConcurrentMap<String, IotRequest> pendingRequests = new ConcurrentHashMap<String, IotRequest>();

    /**
     * 构造函数
     *
     * @param client 客户端
     */
    public RequestManager(DeviceClient client) {
        this.iotClient = client;
    }

    /**
     * 执行同步请求
     *
     * @param iotRequest 请求参数
     * @return 请求执行结果
     */
    public Object executeSyncRequest(IotRequest iotRequest) {

        RawMessage rawMessage = iotRequest.getRawMessage();
        iotClient.publishRawMessage(rawMessage, null);
        pendingRequests.put(iotRequest.getRequestId(), iotRequest);
        iotRequest.runSync();
        return iotRequest.getResult();
    }

    /**
     * 执行异步请求
     *
     * @param iotRequest 请求参数
     * @param listener   请求监听器，用于接收请求完成通知
     */
    public void executeAsyncRequest(IotRequest iotRequest, RequestListener listener) {

        RawMessage rawMessage = iotRequest.getRawMessage();
        iotClient.publishRawMessage(rawMessage, null);
        pendingRequests.put(iotRequest.getRequestId(), iotRequest);
        iotRequest.runAync(listener);
    }

    /**
     * 请求响应回调，由sdk自动调用
     *
     * @param message 响应消息
     */
    public void onRequestResponse(RawMessage message) {
        String requestId = IotUtil.getRequestId(message.getTopic());
        IotRequest request = pendingRequests.remove(requestId);
        if (request == null) {
            Log.e(TAG, "request is null, requestId: " + requestId);
            return;
        }

        request.onFinish(message.toString());
    }
}
