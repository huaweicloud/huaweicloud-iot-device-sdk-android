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

package com.huaweicloud.sdk.iot.device.transport;


/**
 * 动作监听器，用户接收动作执行结果
 */
public interface ActionListener {

    /**
     * 执行成功通知
     *
     * @param context 上下文信息
     */
    void onSuccess(Object context);


    /**
     * 执行失败通知
     *
     * @param context 上下文信息
     * @param var2    失败的原因
     */
    void onFailure(Object context, Throwable var2);
}
