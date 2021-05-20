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
import io.github.shamo42.kotlinextensionslib.objects.*
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.BiFunction
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit


private const val TAG = "RxDiverseExtensions"


fun OkHttpClient.toWebSocketFlowable(webSocketUrl: String, backpressureStrategy: BackpressureStrategy = BackpressureStrategy.BUFFER): Flowable<WsResponse> {
    return Flowable.create({ emitter ->
        object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                emitter.onNext(WsResponse.open(webSocket, response))
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                Log.w(TAG, "webSocket onFailure ${t.localizedMessage}", t)
                if (!emitter.isCancelled && response != null) {
                    emitter.onNext(WsResponse.failure(webSocket, response))
                }
                emitter.tryOnError(t)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.w(TAG, "webSocket closing; onFailure ? $reason $code")
                super.onClosing(webSocket, code, reason)
                emitter.onNext(WsResponse.closing(webSocket, code, reason))
                emitter.tryOnError(Exception("webSocket closing; code: $code reason: $reason"))
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                //Log.d(TAG, text)
                emitter.onNext(WsResponse.textMessage(webSocket, text))
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                emitter.onNext(WsResponse.byteMessage(webSocket, bytes))
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                emitter.onNext(WsResponse.closed(webSocket, code, reason))
                emitter.tryOnError(Exception("webSocket closed; code: $code reason: $reason"))
            }
        }.let { listener ->
            this.newWebSocket(Request.Builder().url(webSocketUrl).build(), listener)
                .apply {
                    emitter.setCancellable {
                        close(4814, null)
                        cancel()
                    }
                }
        }
    }, backpressureStrategy)

}



fun <T> Flowable<T>.sampleRepeat(time: Long, timeUnit: TimeUnit): Flowable<T> {
    return Flowable.combineLatest(this, Flowable.interval(0, time, timeUnit), BiFunction { t1: T, _: Long ->  t1 })
}

fun Flowable<MutableList<Float>>.averageIterable(): Flowable<Float> {
    return this.map { it.average().toFloat() }
}

fun <T> Flowable<T>.delayEach(mS: Long): Flowable<T> {
    return this.concatMap { Flowable.just(it).delay(mS, TimeUnit.MILLISECONDS) }
}

fun <T> Single<T>.delaySingle(time: Long, timeUnit: TimeUnit): Single<T> {
    return Single.timer(time, timeUnit)
            .flatMap{ this}
}

fun <T> Flowable<T>.delayFlowable(time: Long, timeUnit: TimeUnit): Flowable<T> {
    return Flowable.timer(time, timeUnit)
            .flatMap{ this}
}


fun <T, R> Flowable<T>.scanMap(func2: (T?, T) -> R): Flowable<R> {
    return this.startWithItem(null as T?) //emit a null priceValue first, otherwise the .buffer() below won't emit at first (needs 2 emissions to emit)
            .buffer(2, 1) //buffer the previous and current emission
            //.filter { it.size >= 2 }
            .map { func2.invoke(it[0], it[1]) }
}
fun <T, R> Flowable<T>.scanMap(initialValue: T, func2: (T, T) -> R): Flowable<R> {
    return this.startWithItem(initialValue) //use initially provided priceValue instead of null
            .buffer(2, 1)
            //.filter { it.size >= 2 }
            .map { func2.invoke(it[0], it[1]) }
}
/**fun <T, R> Flowable<ResultObject<T>>.scanMapTest(initialValue: ResultObject<T>, func2: (T, T) -> R): Flowable<ResultObject<T>> {
    return this.startWithItem(initialValue) //use initially provided priceValue instead of null
            .buffer(2, 1)
            //.filter { it.size >= 2 }
            .map {
                func2.invoke(it[0], it[1])
            }
}*/

inline fun <T, R> Flowable<List<T>>.scanMapList(crossinline func2: (List<T>, List<T>) -> R): Flowable<R> {
    return this.startWithItem(emptyList<T>()) //emit a empty list first, otherwise the .buffer() below won't emit at first (needs 2 emissions to emit)
        .buffer(2, 1) //buffer the previous and current emission
        //.filter { it.size >= 2 }
        .map { func2.invoke(it[0], it[1]) }
}




fun Disposable.disposeOn(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
}

