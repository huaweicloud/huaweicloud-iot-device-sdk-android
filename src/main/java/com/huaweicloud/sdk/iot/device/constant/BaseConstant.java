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

public class BaseConstant {

    /**
     * Indicates broadcast constants.
     */
    public static final String BROADCAST_STATUS = "status";
    public static final String COMMON_ERROR = "error";

    /**
     * Indicates statuses.
     */
    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_FAIL = 1;
    public static final int STATUS_RECONNECT = 2;
    public static final int STATUS_LOST = 3;

    /**
     * Indicates a device property reporting failure.
     */
    public static final String PROPERTIES_REPORT_ERROR = "properties_report_error";

    /**
     * Indicates a command delivered by the platform.
     */
    public static final String SYS_COMMANDS = "command";

    /**
     * Indicates a message delivered by the platform.
     */
    public static final String SYS_DOWN_MESSAGES = "message";

    /**
     * Indicates a request_id in MQTT interactions.
     */
    public static final String REQUEST_ID = "request_id";

    /**
     * Indicates a service_id in MQTT interactions.
     */
    public static final String SERVICE_ID = "service_id";

    /**
     * Indicates a device property setting request delivered by the platform.
     */
    public static final String SYS_PROPERTIES_SET = "props_set";

    /**
     * Indicates a service URI used for device interconnection.
     */
    public static final String SERVERURI = "serverURI";

    /**
     * Specifies whether the device is reconnected to the platform.
     */
    public static final String RECONNECT = "reconnect";

    /**
     * Indicates a custom topic name.
     */
    public static final String CUSTOMIZED_TOPIC_NAME = "customized_topic_name";

    /**
     * Indicates a custom topic message.
     */
    public static final String CUSTOMIZED_TOPIC_MESSAGE = "customized_topic_message";

    /**
     * Indicates an upgrade notification message.
     */
    public static final String OTAPACKAGE_INFO = "otapackage_info";

    /**
     * Indicates a file upload or download URL.
     */
    public static final String URLPARAM_INFO = "urlparam_info";

    /**
     * Indicates a piece of device shadow data.
     */
    public static final String SHADOW_DATA = "shadow_data";

    /**
     * Indicates a child device ID.
     */
    public static final String SUB_DEVICE_ID = "sub.device.id";

    /**
     * Indicates a child device status list.
     */
    public static final String SUB_DEVICE_ID_LIST_STATUS = "sub.device.id.list.status";

    /**
     * Indicates a child device ID list.
     */
    public static final String SUB_DEVICE_ID_LIST = "sub.device.id.list";

    /**
     * Indicates a child device list.
     */
    public static final String SUB_DEVICE_LIST = "sub.device.list";

    /**
     * Indicates a child device addition notification delivered by the platform.
     */
    public static final String SUB_DEVICE_ADD = "sub.device.add";

    /**
     * Indicates a child device deletion notification delivered by the platform.
     */
    public static final String SUB_DEVICE_DELETE = "sub.device.delete";

    /**
     * 设备时间同步平台响应的消息广播常量
     */
    public static final String DEVICE_TIME_SYNC_MESSAGE = "device.time.sync.message";

    /**
     * 设备引导信息广播常量
     */
    public static final String BOOTSTRAP_MESSAGE = "bootstrap.message";

}
