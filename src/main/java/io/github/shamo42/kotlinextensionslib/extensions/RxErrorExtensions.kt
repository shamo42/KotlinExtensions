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

import android.util.Log
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import okhttp3.ResponseBody
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

private const val TAG = "RxErrorExtensions"





fun <T> Flowable<T>.waitAndRetry(waitMs: Long, message: String? = null): Flowable<T> {
    return this.retry { i: Int, t: Throwable ->
        Log.w(TAG, "Retry nr $i msg: $message", t)
        Thread.sleep(waitMs)
        true
    }
}

fun <T> Flowable<T>.waitAndRetry(maxRetries: Int, waitMs: Long, message: String? = null): Flowable<T> {
    return this.retry { i: Int, t: Throwable ->
        Log.w(TAG, "Retry nr $i msg: $message", t)
        Thread.sleep(waitMs)
        i <= maxRetries
    }
}


fun <T> Single<T>.retryIfTimeOut(maxRetries: Int, waitMs: Long): Single<T> {
    return this.retry { i: Int, t: Throwable ->
        val shouldRetry = i <= maxRetries && t is SocketTimeoutException
        Log.w(TAG, "Retry nr $i $shouldRetry $shouldRetry", t)
        if (shouldRetry) {
            Thread.sleep(waitMs)
        }
        shouldRetry
    }
}
fun <T> Single<T>.retryIfTimeOut(waitMs: Long): Single<T> {
    return this.retry { i: Int, t: Throwable ->
        val shouldRetry = t is SocketTimeoutException
        Log.w(TAG, "Retry nr $i $shouldRetry $shouldRetry", t)
        if (shouldRetry) {
            Thread.sleep(waitMs)
        }
        shouldRetry
    }
}

fun <T> Flowable<T>.retryIfTimeOut(maxRetries: Int, waitMs: Long): Flowable<T> {
    return this.retry { i: Int, t: Throwable ->
        val shouldRetry = i <= maxRetries && t is SocketTimeoutException
        Log.w(TAG, "Retry nr $i $shouldRetry $shouldRetry", t)
        if (shouldRetry) {
            Thread.sleep(waitMs)
        }
        shouldRetry
    }
}


fun <T> Single<T>.retryIfErrorCode(maxRetries: Int, code: Int): Single<T> {
    return this.retry { i: Int, t: Throwable ->
        val shouldRetry = i <= maxRetries && (t.localizedMessage != null && t.localizedMessage!!.contains(code.toString()))
        Log.w(TAG, "Retry error code nr $i ${t.localizedMessage} shouldRetry $shouldRetry", t)
        if (shouldRetry) {
            Thread.sleep(2000L * i)
        }
        shouldRetry
    }
}


fun Single<Response<ResponseBody>>.throwIfWrongResponseCode(code: Int): Maybe<Response<ResponseBody>> {
    return this.filter {
        val success = code == it.code()
        if (!success) {
            Log.w(TAG, "throwIfWrongResponseCode $success")
            throw Throwable("responseCode ${it.code()} ${it.errorBody()?.string()}")
        }
        success }
}
fun Maybe<Response<ResponseBody>>.throwIfWrongResponseCode(code: Int): Maybe<Response<ResponseBody>> {
    return this.filter {
        val success = code == it.code()
        if (!success) {
            Log.w(TAG, "throwIfWrongResponseCode $success")
            throw Throwable("responseCode ${it.code()} ${it.errorBody()?.string()}")
        }
        success }
}

fun <T> Single<T>.waitAndRetryIfOverRateLimit(rateError: String, timeWaitMs: Long): Single<T> {
    return this.retry { i, t ->
        val shouldRetry = i < 3 && t.localizedMessage != null && t.localizedMessage!!.contains(rateError)
        if (shouldRetry) {
            Log.w(TAG, "Wait & retry $i because of rate limit ${t.localizedMessage}", t)
            Thread.sleep(timeWaitMs)
        }
        shouldRetry
    }
}
fun <T> Flowable<T>.waitAndRetryIfOverRateLimit(rateError: String, timeWaitMs: Long): Flowable<T> {
    return this.retry { i, t ->
        val shouldRetry = i < 3 && t.localizedMessage != null && t.localizedMessage!!.contains(rateError)
        if (shouldRetry) {
            Log.w(TAG, "Wait & retry $i because of rate limit ${t.localizedMessage}", t)
            Thread.sleep(timeWaitMs)
        }
        shouldRetry
    }
}


fun <T> Maybe<T>.waitAndRetryIfOverRateLimit(rateError: String, timeWaitMs: Long): Maybe<T> {
    return this.retry { i, t ->
        val shouldRetry = i < 3 && t.localizedMessage != null && t.localizedMessage!!.contains(rateError)
        if (shouldRetry) {
            Log.w(TAG, "Wait & retry $i because of rate limit ${t.localizedMessage}", t)
            Thread.sleep(timeWaitMs * i)
        }
        shouldRetry
    }
}


fun <T> Flowable<T>.retryWhenConnectionLost(maxRetries: Int, timeWaitMs: Long): Flowable<T> {
    return this.retry { i, t ->
        val shouldRetry = i <= maxRetries && (t is UnknownHostException || t is  SSLException)
        if (shouldRetry) {
            Log.w(TAG, "retry $i of $maxRetries ${t.localizedMessage}", t)
            Thread.sleep(timeWaitMs)
        }
        shouldRetry
    }
}


fun <T> Single<T>.retryWhenConnectionLost(maxRetries: Int, timeWaitMs: Long): Single<T> {
    return this.retry { i, t ->
        val shouldRetry = i <= maxRetries && (t is UnknownHostException || t is  SSLException)
        if (shouldRetry) {
            Log.w(TAG, "retry $i of $maxRetries ${t.localizedMessage}", t)
            Thread.sleep(timeWaitMs)
        }
        shouldRetry
    }
}





/*fun <T> Flowable<T>.responseToResult(): Flowable<ServerTime<T>> {
    return this.map { it.asResult() }
            .onErrorReturn {
                if (it is HttpException || it is IOException) {
                    return@onErrorReturn it.asErrorResult<T>()
                } else {
                    throw it
                }
            }
}

fun <T> Single<T>.responseToResult(): Single<ServerTime<T>> {
    return this.map { it.asResult() }
            .onErrorReturn {
                if (it is HttpException || it is IOException) {
                    return@onErrorReturn it.asErrorResult<T>()
                } else {
                    throw it
                }
            }
}

fun <T> T.asResult(): ServerTime<T> {
    return ServerTime.Success<T>(this)
}

fun <T> Throwable.asErrorResult(): ServerTime<T> {
    return ServerTime.Error<T>(this)
}*/


