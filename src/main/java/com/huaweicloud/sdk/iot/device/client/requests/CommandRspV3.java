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

package com.huaweicloud.sdk.iot.device.client.requests;

import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

/**
 * Provides APIs related to V3 command responses.
 */
public class CommandRspV3 {

    /**
     * Indicates a message type. The value is fixed at deviceRsp.
     */
    private String msgType;

    /**
     * Indicates a command ID. It is set to mid carried in the command delivered by the platform.
     */
    private int mid;

    /**
     * Indicates a command execution result code.
     * 0: successful command execution
     * 1: failed command execution
     */
    private int errcode;

    /**
     * Indicates a command response, which is defined in the product model. It is optional.
     */
    private Object body;

    public CommandRspV3(String msgType, int mid, int errcode) {
        this.msgType = msgType;
        this.mid = mid;
        this.errcode = errcode;
    }

    public CommandRspV3(String msgType, int mid, int errcode, Object body) {
        this.msgType = msgType;
        this.mid = mid;
        this.errcode = errcode;
        this.body = body;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return JsonUtil.convertObject2String(this);
    }
}
