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

import com.google.gson.annotations.SerializedName;
import com.huaweicloud.sdk.iot.device.utils.JsonUtil;

import java.util.Map;

/**
 * Provides APIs related to command responses.
 */
public class CommandRsp {

    public static final int SUCCESS = 0;
    public static final int FAIL = -1;

    /**
     * Indicates a result code. The value 0 indicates a success, and other values indicate a failure. If it is not specified, the command execution is successful.
     */
    @SerializedName("result_code")
    int resultCode;

    /**
     * Indicates a command response name, which is defined in the product model. It is optional.
     */
    @SerializedName("response_name")
    String responseName;

    /**
     * Indicates command response parameters, which are defined in the product model. It is optional.
     */
    private Map<String, Object> paras;

    public CommandRsp(int code) {
        resultCode = code;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResponseName() {
        return responseName;
    }

    public void setResponseName(String responseName) {
        this.responseName = responseName;
    }

    public Map<String, Object> getParas() {
        return paras;
    }

    public void setParas(Map<String, Object> paras) {
        this.paras = paras;
    }

    @Override
    public String toString() {
        return JsonUtil.convertObject2String(this);
    }
}
