/*
 * Copyright (c) 2018-present, Wiltgen Philippe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.shamo42.kotlinextensionslib.extensions

import android.content.Context
import android.os.Environment
import io.github.shamo42.kotlinextensionslib.objects.ResultObject
import java.io.*


/* Checks if external storage is available for read and write */
fun isExternalStorageWritable(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}

/* Checks if external storage is available to at least read */
fun isExternalStorageReadable(): Boolean {
    return Environment.getExternalStorageState() in
            setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
}

fun Context.getPrivateStorageDir(albumName: String = "zeitotp"): ResultObject<File> {
    // Get the directory for the app's private doc directory.
    return File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), albumName).let { file ->
        if (!file.mkdirs()) ResultObject.Error(Throwable("Directory not created"))
        else ResultObject.Success(file)
    }
}
