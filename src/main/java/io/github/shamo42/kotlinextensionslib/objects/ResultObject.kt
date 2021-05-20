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

package io.github.shamo42.kotlinextensionslib.objects

sealed class ResultObject<out T> {
    data class Success<out T>(val data: T) : ResultObject<T>()
    data class Loading<out T>(val partialData: T? = null, val message: String? = null) : ResultObject<T>()
    data class Error<out T>(val throwable: Throwable, val errorCode: Int? = null) : ResultObject<T>()


    val extractData: T? get() = when (this) {
        is Success -> data
        is Loading -> partialData
        is Error -> null
    }

    inline fun <Y> mapResult(crossinline transform: (T) -> Y): ResultObject<Y> = try {
        when (this) {
            is Success<T> -> Success(transform(data))
            is Loading<T> -> Loading(partialData?.let { transform(it) }, message)
            is Error<T> -> Error(throwable, errorCode)
        }
    } catch (e: Throwable) {
        Error(e)
    }

    fun asIs(): ResultObject<T> = this


    fun onSuccess(onSuccess: (data: T) -> Unit): ResultObject<T> {
        if (this is Success)
            onSuccess(data)

        return this
    }

    fun onLoading(onLoading: (partialData: T?) -> Unit): ResultObject<T> {
        if (this is Loading)
            onLoading(partialData)

        return this
    }

    fun onError(onError: (throwable: Throwable?) -> Unit): ResultObject<T> {
        if (this is Error)
            onError(throwable)
        return this
    }

}



