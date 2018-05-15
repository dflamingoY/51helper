package org.rhona.piclibrary.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by yzm on 2018/5/14.
 */
class FileData() : Parcelable {
    var count = 0 //
    var parentName: String? = null  //文件名
    var path: String? = null//文件路径
    var firstPath: String? = null //显示的头图

    constructor(parcel: Parcel) : this() {
        count = parcel.readInt()
        parentName = parcel.readString()
        path = parcel.readString()
        firstPath = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(count)
        parcel.writeString(parentName)
        parcel.writeString(path)
        parcel.writeString(firstPath)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FileData> {
        override fun createFromParcel(parcel: Parcel): FileData {
            return FileData(parcel)
        }

        override fun newArray(size: Int): Array<FileData?> {
            return arrayOfNulls(size)
        }
    }


}