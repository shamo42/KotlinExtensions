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

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.ResponseBody
import retrofit2.Response


inline fun <reified T: Any> T.toJsonString(customBuilder: Moshi.Builder = Moshi.Builder(), lenient: Boolean = false): String
        = customBuilder
    .add(KotlinJsonAdapterFactory()).build().let { moshi ->
        if (lenient) moshi.adapter<T>(T::class.java).lenient().toJson(this)
        else moshi.adapter<T>(T::class.java).toJson(this)
    }



inline fun <reified T: Any> String.toJsonObject(customBuilder: Moshi.Builder = Moshi.Builder(), lenient: Boolean = false): T
        = customBuilder
    .add(KotlinJsonAdapterFactory()).build().let { moshi ->
        if (lenient) moshi.adapter(T::class.java).lenient().fromJson(this)!!
        else moshi.adapter(T::class.java).fromJson(this)!!
    }


inline fun <reified T: Any> String.toJsonObjectList(customBuilder: Moshi.Builder = Moshi.Builder(), lenient: Boolean = false): List<T> {
    return Types.newParameterizedType(List::class.java, T::class.java).let { type ->
        customBuilder
            .add(KotlinJsonAdapterFactory()).build().let { moshi ->
                if (lenient) moshi.adapter<List<T>>(type).lenient().fromJson(this)!!
                else moshi.adapter<List<T>>(type).fromJson(this)!!
            }
    }
}

inline fun <reified R: Any> Response<ResponseBody>.toJsonObject(): R {
    return if (this.isSuccessful) {
        this.body()!!.string().toJsonObject<R>()
    } else {
        throw Throwable(this.errorBody()?.string()?: this.message())
    }
}

