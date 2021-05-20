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

import android.util.Base64
import java.nio.ByteBuffer
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit


fun <T> MutableList<T>.moveItem(fromPos: Int, toPos: Int) {
    if (fromPos < toPos) {
        for (i in fromPos until toPos) {
            Collections.swap(this, i, i + 1)
        }
    } else {
        for (i in fromPos downTo toPos + 1) {
            Collections.swap(this, i, i - 1)
        }
    }
}



fun Float.formatPriceToString(): String {
    return when {
        this < 0.1 -> DecimalFormat("@@@@@").format(this).removeSuffix("00").removeSuffix("0")
        this < 1000 -> DecimalFormat("@@@@@").format(this)
        else -> DecimalFormat("@@@@@@").format(this)
    }
}

fun ByteBuffer.toByteArray(): ByteArray {
    rewind()    // Rewind the buffer to zero
    return ByteArray(remaining()).also { get(it) }
}


fun ByteArray.toB64String(): String {
    return Base64.encodeToString(this, Base64.NO_WRAP)
}

fun String.toArrayFromB64(): ByteArray {
    return Base64.decode(this, Base64.NO_WRAP)
}

fun currentTimeSecs(): Long {
    return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
}

inline fun <reified T : Enum<T>> printAllValues() {
    print(enumValues<T>().joinToString { it.name })
}



