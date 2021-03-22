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
 * The SDK reports UI message broadcasts.
 */
public class IotDeviceIntent {

    /**
     * Indicates a device property reporting event.
     */
    public static final String ACTION_IOT_DEVICE_PROPERTIES_REPORT = "huaweicloud.iot.device.intent.action.PROPERTIES.REPORT";

    /**
     * Indicates a V3 device property reporting event.
     */
    public static final String ACTION_IOT_DEVICE_PROPERTIES_REPORT_V3 = "huaweicloud.iot.device.intent.action.PROPERTIES.REPORT.V3";

    /**
     * Indicates a V3 device property reporting event in binary code streams.
     */
    public static final String ACTION_IOT_DEVICE_PROPERTIES_BINARY_V3 = "huaweicloud.iot.device.intent.action.PROPERTIES.BINARY.V3";

    /**
     * Indicates a command delivery event.
     */
    public static final String ACTION_IOT_DEVICE_SYS_COMMANDS = "huaweicloud.iot.device.intent.action.SYS.COMMANDS";

    /**
     * Indicates a V3 command delivery event.
     */
    public static final String ACTION_IOT_DEVICE_SYS_COMMANDS_V3 = "huaweicloud.iot.device.intent.action.SYS.COMMANDS.V3";

    /**
     * Indicates a message reporting event.
     */
    public static final String ACTION_IOT_DEVICE_SYS_MESSAGES_UP = "huaweicloud.iot.device.intent.action.SYS.MESSAGES.UP";

    /**
     * Indicates a message delivery event.
     */
    public static final String ACTION_IOT_DEVICE_SYS_MESSAGES_DOWN = "huaweicloud.iot.device.intent.action.SYS.MESSAGES.DOWN";

    /**
     * Indicates a device property setting event.
     */
    public static final String ACTION_IOT_DEVICE_SYS_PROPERTIES_SET = "huaweicloud.iot.device.intent.action.SYS.PROPERTIES.SET";

    /**
     * Indicates a device property query event.
     */
    public static final String ACTION_IOT_DEVICE_SYS_PROPERTIES_GET = "huaweicloud.iot.device.intent.action.SYS.PROPERTIES.GET";

    /**
     *  Indicates an MQTT connection status event.
     */
    public static final String ACTION_IOT_DEVICE_CONNECT = "huaweicloud.iot.device.intent.action.CONNECT";

    /**
     * MQTT设备引导连接状态广播
     */
    public static final String ACTION_IOT_DEVICE_BOOTSTRAP_CONNECT = "huaweicloud.iot.device.intent.action.BOOTSTRAP.CONNECT";

    /**
     * 设备引导状态广播
     */
    public static final String ACTION_IOT_DEVICE_BOOTSTRAP = "huaweicloud.iot.device.intent.action.BOOTSTRAP";

    /**
     * Indicates a custom topic subscription status event.
     */
    public static final String ACTION_IOT_DEVICE_CUSTOMIZED_TOPIC_CONNECT = "huaweicloud.iot.device.intent.action.CUSTOMIZED.TOPIC.CONNECT";

    /**
     * Indicates a custom topic message delivery event.
     */
    public static final String ACTION_IOT_DEVICE_CUSTOMIZED_TOPIC_MESSAGE = "huaweicloud.iot.device.intent.action.CUSTOMIZED.TOPIC.MESSAGE";

    /**
     * Indicates a custom topic result reporting event.
     */
    public static final String ACTION_IOT_DEVICE_CUSTOMIZED_TOPIC_REPORT = "huaweicloud.iot.device.intent.action.CUSTOMIZED.TOPIC.REPORT";

    /**
     * Indicates an upgrade notification event.
     */
    public static final String ACTION_IOT_DEVICE_UPGRADE_EVENT = "huaweicloud.iot.device.intent.action.UPGRADE.EVENT";

    /**
     * Indicates a version query event.
     */
    public static final String ACTION_IOT_DEVICE_VERSION_QUERY_EVENT = "huaweicloud.iot.device.intent.action.VERSION.QUERY.EVENT";

    /**
     * Indicates a temporary URL for file upload.
     */
    public static final String ACTION_IOT_DEVICE_GET_UPLOAD_URL = "huaweicloud.iot.device.intent.action.GET.UPLOAD.URL";

    /**
     * Indicates a temporary URL for file download.
     */
    public static final String ACTION_IOT_DEVICE_GET_DOWNLOAD_URL = "huaweicloud.iot.device.intent.action.GET.DOWNLOAD.URL";

    /**
     * Indicates a device shadow setting event.
     */
    public static final String ACTION_IOT_DEVICE_SYS_SHADOW_GET = "huaweicloud.iot.device.intent.action.SYS.SHADOW.GET";


    /**
     * Indicates a message reporting event initiated by a child device.
     */
    public static final String ACTION_IOT_DEVICE_SYS_SUB_MESSAGES_UP = "huaweicloud.iot.device.intent.action.SYS.SUB.MESSAGES.UP";

    /**
     * Indicates a child device property reporting event.
     */
    public static final String ACTION_IOT_DEVICE_SYS_SUB_PROPERTIES_REPORT = "huaweicloud.iot.device.intent.action.SYS.SUB.PROPERTIES.REPORT";

    /**
     * Indicates a child device status reporting event.
     */
    public static final String ACTION_IOT_DEVICE_SYS_SUB_STATUSES_REPORT = "huaweicloud.iot.device.intent.action.SYS.SUB.STATUSES.REPORT";

    /**
     * Indicates a child device addition notification delivered by the platform.
     */
    public static final String ACTION_IOT_DEVICE_SYS_SUB_ADD_DEVICE_NOTIFY = "huaweicloud.iot.device.intent.action.SYS.SUB.ADD.DEVICE.NOTIFY";

    /**
     * Indicates a child device deletion notification delivered by the platform.
     */
    public static final String ACTION_IOT_DEVICE_SYS_SUB_DELETE_DEVICE_NOTIFY = "huaweicloud.iot.device.intent.action.SYS.SUB.DELETE.DEVICE.NOTIFY";

    /**
     * Indicates a response to the child device addition notification.
     */
    public static final String ACTION_IOT_DEVICE_SYS_SUB_ADD_DEVICE_RESPONSE = "huaweicloud.iot.device.intent.action.SYS.SUB.ADD.DEVICE.RESPONSE";

    /**
     * Indicates a response to the child device deletion notification.
     */
    public static final String ACTION_IOT_DEVICE_SYS_SUB_DELETE_DEVICE_RESPONSE = "huaweicloud.iot.device.intent.action.SYS.SUB.DELETE.DEVICE.RESPONSE";

    /**
     * 设备时间同步响应
     */
    public static final String ACTION_IOT_DEVICE_TIME_SYNC_RESPONSE = "huaweicloud.iot.device.intent.action.TIME.SYNC.RESPONSE";

    /**
     * 设备发放重引导广播
     */
    public static final String ACTION_IOT_DEVICE_BOOTSTRAP_REQUEST_TRIGGER = "huaweicloud.iot.device.intent.action.BOOTSTRAP.REQUEST.TRIGGER";

}
