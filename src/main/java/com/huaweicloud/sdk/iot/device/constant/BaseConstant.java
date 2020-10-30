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
     * 广播常量
     */
    public static final String BROADCAST_STATUS = "status";
    public static final String COMMON_ERROR = "error";

    /**
     * 状态广播常量
     */
    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_FAIL = 1;
    public static final int STATUS_RECONNECT = 2;
    public static final int STATUS_LOST = 3;

    /**
     * 设备属性上报失败常量
     */
    public static final String PROPERTIES_REPORT_ERROR = "properties_report_error";

    /**
     * 平台命令下发参数名字常量
     */
    public static final String SYS_COMMANDS = "command";

    /**
     * 平台下发消息参数名字常量
     */
    public static final String SYS_DOWN_MESSAGES = "message";

    /**
     * mqtt交互通用request_id名字常量
     */
    public static final String REQUEST_ID = "request_id";

    /**
     * mqtt交互通用service_id名字常量
     */
    public static final String SERVICE_ID = "service_id";

    /**
     * 平台设置设备属性参数名字常量
     */
    public static final String SYS_PROPERTIES_SET = "props_set";

    /**
     * 设备对接的服务URL参数名字常量
     */
    public static final String SERVERURI = "serverURI";

    /**
     * 设备是否重连平台
     */
    public static final String RECONNECT = "reconnect";

    /**
     * 自定义topic参数名字常量
     */
    public static final String CUSTOMIZED_TOPIC_NAME = "customized_topic_name";

    /**
     * 自定义topic下发消息参数名字常量
     */
    public static final String CUSTOMIZED_TOPIC_MESSAGE = "customized_topic_message";

    /**
     * 平台下发升级通知信息名字常量
     */
    public static final String OTAPACKAGE_INFO = "otapackage_info";

    /**
     * 文件上传下载URL信息名字常量
     */
    public static final String URLPARAM_INFO = "urlparam_info";

    /**
     * 设备影子数据信息名字常量
     */
    public static final String SHADOW_DATA = "shadow_data";

    /**
     * 子设备ID广播常量
     */
    public static final String SUB_DEVICE_ID = "sub.device.id";

    /**
     * 子设备状态列表广播常量
     */
    public static final String SUB_DEVICE_ID_LIST_STATUS = "sub.device.id.list.status";

    /**
     * 子设备列表广播常量
     */
    public static final String SUB_DEVICE_ID_LIST = "sub.device.id.list";

    /**
     * 子设备列表广播常量
     */
    public static final String SUB_DEVICE_LIST = "sub.device.list";

    /**
     * 新增子设备信息广播常量
     */
    public static final String SUB_DEVICE_ADD = "sub.device.add";

    /**
     * 删除子设备信息广播常量
     */
    public static final String SUB_DEVICE_DELETE = "sub.device.delete";

}
