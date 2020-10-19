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

package com.huaweicloud.sdk.iot.device.filemanager;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class UrlParam implements Parcelable {

    private String url;

    @SerializedName("bucket_name")
    private String bucketName;

    @SerializedName("object_name")
    private String objectName;

    private Integer expire;

    protected UrlParam(Parcel in) {
        url = in.readString();
        bucketName = in.readString();
        objectName = in.readString();
        if (in.readByte() == 0) {
            expire = null;
        } else {
            expire = in.readInt();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(bucketName);
        dest.writeString(objectName);
        if (expire == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(expire);
        }
    }

    public UrlParam() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UrlParam> CREATOR = new Creator<UrlParam>() {
        @Override
        public UrlParam createFromParcel(Parcel in) {
            return new UrlParam(in);
        }

        @Override
        public UrlParam[] newArray(int size) {
            return new UrlParam[size];
        }
    };

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public Integer getExpire() {
        return expire;
    }

    public void setExpire(Integer expire) {
        this.expire = expire;
    }
}
