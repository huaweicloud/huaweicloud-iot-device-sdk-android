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
import com.huaweicloud.sdk.iot.device.utils.ExceptionUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IotRequest {
    private static final String TAG = "IotRequest";
    private String requestId;
    private int timeout;
    private RawMessage rawMessage;
    private Object result = null;
    private boolean sync = true;
    /**
     * 异步请求才有
     */
    private RequestListener listener;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public IotRequest(RawMessage rawMessage, String requestId, int timeout) {

        if (timeout <= 0) {
            timeout = 10000;
        }
        this.timeout = timeout;
        this.rawMessage = rawMessage;
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public RawMessage getRawMessage() {
        return rawMessage;
    }

    public void setRawMessage(RawMessage rawMessage) {
        this.rawMessage = rawMessage;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void runSync() {

        synchronized (this) {
            try {
                wait(timeout);
            } catch (InterruptedException e) {
                Log.e(TAG, ExceptionUtil.getBriefStackTrace(e));
            }

            if (result == null) {
                result = IotResult.TIMEOUT;
            }

        }

    }

    public void runAync(RequestListener listener) {

        sync = false;
        this.listener = listener;
        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (result == null) {
                    result = IotResult.TIMEOUT;
                }
            }
        }, 0, timeout, TimeUnit.MILLISECONDS);
    }

    public void onFinish(String iotResult) {

        synchronized (this) {
            this.result = iotResult;

            if (sync) {
                notifyAll();
            } else {
                if (listener != null) {
                    listener.onFinish(iotResult);
                }
            }

        }

    }

}
