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
 * 命令响应
 */
public class CommandRsp {

    public static final int SUCCESS = 0;
    public static final int FAIL = -1;

    /**
     * 结果码，0表示成功，其他表示失败。不带默认认为成功
     */
    @SerializedName("result_code")
    int resultCode;

    /**
     * 命令的响应名称，在设备关联的产品模型中定义。可选
     */
    @SerializedName("response_name")
    String responseName;

    /**
     * 命令的响应参数，具体字段在设备关联的产品模型中定义。可选
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
