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

package com.huaweicloud.sdk.iot.device.utils;

public class ExceptionUtil {
    private static final int DEFAULT_LINE = 10;

    public static String getExceptionCause(Throwable e) {
        StringBuilder sb;
        for (sb = new StringBuilder(); e != null; e = e.getCause()) {
            sb.append(e.toString()).append("\n");
        }

        return sb.toString();
    }

    public static String getAllExceptionStackTrace(Throwable e) {
        if (e == null) {
            return "";
        } else {
            StringBuilder stackTrace = new StringBuilder(e.toString());
            StackTraceElement[] astacktraceelement = e.getStackTrace();
            StackTraceElement[] var3 = astacktraceelement;
            int var4 = astacktraceelement.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                StackTraceElement anAstacktraceelement = var3[var5];
                stackTrace.append("\r\n").append("\tat ").append(anAstacktraceelement);
            }

            return stackTrace.toString();
        }
    }

    public static String getExceptionStackTrace(Throwable e, int lineNum) {
        if (e == null) {
            return "";
        } else {
            StringBuilder stackTrace = new StringBuilder(e.toString());
            StackTraceElement[] astacktraceelement = e.getStackTrace();
            int size = lineNum > astacktraceelement.length ? astacktraceelement.length : lineNum;

            for (int i = 0; i < size; ++i) {
                stackTrace.append("\r\n").append("\tat ").append(astacktraceelement[i]);
            }

            return stackTrace.toString();
        }
    }

    public static String getBriefStackTrace(Throwable e) {
        return getExceptionStackTrace(e, DEFAULT_LINE);
    }
}
