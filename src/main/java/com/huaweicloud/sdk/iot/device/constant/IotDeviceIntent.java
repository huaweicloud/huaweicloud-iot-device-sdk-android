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

package com.huaweicloud.sdk.iot.device.constant;

/**
 * SDK上报UI消息广播
 */
public class IotDeviceIntent {

    /**
     * 上报设备属性
     */
    public static final String ACTION_IOT_DEVICE_PROPERTIES_REPORT = "huaweicloud.iot.device.intent.action.PROPERTIES.REPORT";

    /**
     * V3上报设备属性
     */
    public static final String ACTION_IOT_DEVICE_PROPERTIES_REPORT_V3 = "huaweicloud.iot.device.intent.action.PROPERTIES.REPORT.V3";

    /**
     * V3上报设备的码流
     */
    public static final String ACTION_IOT_DEVICE_PROPERTIES_BINARY_V3 = "huaweicloud.iot.device.intent.action.PROPERTIES.BINARY.V3";

    /**
     * 平台命令下发
     */
    public static final String ACTION_IOT_DEVICE_SYS_COMMANDS = "huaweicloud.iot.device.intent.action.SYS.COMMANDS";

    /**
     * 平台V3命令下发
     */
    public static final String ACTION_IOT_DEVICE_SYS_COMMANDS_V3 = "huaweicloud.iot.device.intent.action.SYS.COMMANDS.V3";

    /**
     * 设备消息上报
     */
    public static final String ACTION_IOT_DEVICE_SYS_MESSAGES_UP = "huaweicloud.iot.device.intent.action.SYS.MESSAGES.UP";

    /**
     * 平台消息下发
     */
    public static final String ACTION_IOT_DEVICE_SYS_MESSAGES_DOWN = "huaweicloud.iot.device.intent.action.SYS.MESSAGES.DOWN";

    /**
     * 平台设置设备属性
     */
    public static final String ACTION_IOT_DEVICE_SYS_PROPERTIES_SET = "huaweicloud.iot.device.intent.action.SYS.PROPERTIES.SET";

    /**
     * 平台查询设备属性
     */
    public static final String ACTION_IOT_DEVICE_SYS_PROPERTIES_GET = "huaweicloud.iot.device.intent.action.SYS.PROPERTIES.GET";

    /**
     * MQTT链接状态广播
     */
    public static final String ACTION_IOT_DEVICE_CONNECT = "huaweicloud.iot.device.intent.action.CONNECT";

    /**
     * 自定义topic订阅状态广播
     */
    public static final String ACTION_IOT_DEVICE_CUSTOMIZED_TOPIC_CONNECT = "huaweicloud.iot.device.intent.action.CUSTOMIZED.TOPIC.CONNECT";

    /**
     * 自定义topic消息下发广播
     */
    public static final String ACTION_IOT_DEVICE_CUSTOMIZED_TOPIC_MESSAGE = "huaweicloud.iot.device.intent.action.CUSTOMIZED.TOPIC.MESSAGE";

    /**
     * 设备上报自定义Topic结果广播
     */
    public static final String ACTION_IOT_DEVICE_CUSTOMIZED_TOPIC_REPORT = "huaweicloud.iot.device.intent.action.CUSTOMIZED.TOPIC.REPORT";

    /**
     * 平台下发升级通知广播
     */
    public static final String ACTION_IOT_DEVICE_UPGRADE_EVENT = "huaweicloud.iot.device.intent.action.UPGRADE.EVENT";

    /**
     * 平台下发获取版本信息广播
     */
    public static final String ACTION_IOT_DEVICE_VERSION_QUERY_EVENT = "huaweicloud.iot.device.intent.action.VERSION.QUERY.EVENT";

    /**
     * 平台下发文件上传临时URL通知广播
     */
    public static final String ACTION_IOT_DEVICE_GET_UPLOAD_URL = "huaweicloud.iot.device.intent.action.GET.UPLOAD.URL";

    /**
     * 平台下发文件下载临时URL通知广播
     */
    public static final String ACTION_IOT_DEVICE_GET_DOWNLOAD_URL = "huaweicloud.iot.device.intent.action.GET.DOWNLOAD.URL";

    /**
     * 平台下发设备影子数据通知广播
     */
    public static final String ACTION_IOT_DEVICE_SYS_SHADOW_GET = "huaweicloud.iot.device.intent.action.SYS.SHADOW.GET";

}
