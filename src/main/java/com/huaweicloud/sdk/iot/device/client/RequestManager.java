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
 * Provides APIs to manage requests.
 */
public class RequestManager {

    private static final String TAG = "RequestManager";

    private DeviceClient iotClient;
    private ConcurrentMap<String, IotRequest> pendingRequests = new ConcurrentHashMap<String, IotRequest>();

    /**
     * Constructor used to create a RequestManager object.
     *
     * @param client Indicates a client.
     */
    public RequestManager(DeviceClient client) {
        this.iotClient = client;
    }

    /**
     * Executes a synchronous request. 
     *
     * @param iotRequest Indicates a request.
     * @return Returns the request execution result.
     */
    public Object executeSyncRequest(IotRequest iotRequest) {

        RawMessage rawMessage = iotRequest.getRawMessage();
        iotClient.publishRawMessage(rawMessage, null);
        pendingRequests.put(iotRequest.getRequestId(), iotRequest);
        iotRequest.runSync();
        return iotRequest.getResult();
    }

    /**
     * Executes an asynchronous request.
     *
     * @param iotRequest Indicates a request.
     * @param listener Indicates a listener, so the client receives a notification when the request execution is complete.
     */
    public void executeAsyncRequest(IotRequest iotRequest, RequestListener listener) {

        RawMessage rawMessage = iotRequest.getRawMessage();
        iotClient.publishRawMessage(rawMessage, null);
        pendingRequests.put(iotRequest.getRequestId(), iotRequest);
        iotRequest.runAync(listener);
    }

    /**
     * Called when the request is responded.
     *
     * @param message Indicates a response.
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
