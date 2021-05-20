package io.github.shamo42.kotlinextensionslib.extensions

import com.squareup.moshi.JsonDataException
import io.github.shamo42.kotlinextensionslib.objects.*
import io.reactivex.rxjava3.core.*
import okhttp3.ResponseBody
import org.reactivestreams.Publisher
import retrofit2.Response


inline fun <T> ResultObject<List<T>>.filterResultObject(predicate: (T) -> Boolean): ResultObject<List<T>> = try {
    when (this) {
        is ResultObject.Success<List<T>> -> ResultObject.Success<List<T>>(data.filter(predicate))
        is ResultObject.Loading<List<T>> -> ResultObject.Loading<List<T>>(
            partialData?.filter(
                predicate
            ), message
        )
        is ResultObject.Error<List<T>> -> ResultObject.Error<List<T>>(throwable, errorCode)
    }
} catch (e: Throwable) {
    ResultObject.Error<List<T>>(e)
}

inline fun <T, U, V> ResultObject<V>.map(transform: (T) -> U): ResultObject<List<U>> where V : Iterable<T> = try {
    when (this) {
        is ResultObject.Success<V> -> ResultObject.Success<List<U>>(data.map(transform))
        is ResultObject.Loading<V> -> ResultObject.Loading<List<U>>(partialData?.map(transform), message)
        is ResultObject.Error<V> -> ResultObject.Error<List<U>>(throwable, errorCode)
    }
} catch (e: Throwable) {
    ResultObject.Error<List<U>>(e)
}

// map ResultObject data for RxJava
fun <T, R> Single<ResultObject<T>>.mapResult(func1: (T) -> R): Single<ResultObject<R>> {
    return this.map { it.mapResult(func1) }
}
fun <T, R> Maybe<ResultObject<T>>.mapResult(func1: (T) -> R): Maybe<ResultObject<R>> {
    return this.map { it.mapResult(func1) }
}
fun <T, R> Observable<ResultObject<T>>.mapResult(func1: (T) -> R): Observable<ResultObject<R>> {
    return this.map { it.mapResult(func1) }
}
fun <T, R> Flowable<ResultObject<T>>.mapResult(func1: (T) -> R): Flowable<ResultObject<R>> {
    return this.map { it.mapResult(func1) }
}

/**
 * filter ResultObject
 */

private fun <T> ResultObject<T>.filter(predicate: (T) -> Boolean): Boolean {
    return when (this) {
        is ResultObject.Success<T> -> predicate(this.data)
        is ResultObject.Loading -> if (this.partialData != null) predicate(this.partialData) else true
        is ResultObject.Error -> true
    }
}
fun <T> Single<ResultObject<T>>.filterResult(predicate: (T) -> Boolean): Maybe<ResultObject<T>> {
    return this.filter { it.filter(predicate) }
}
fun <T> Maybe<ResultObject<T>>.filterResult(predicate: (T) -> Boolean): Maybe<ResultObject<T>> {
    return this.filter { it.filter(predicate) }
}
fun <T> Observable<ResultObject<T>>.filterResult(predicate: (T) -> Boolean): Observable<ResultObject<T>> {
    return this.filter { it.filter(predicate) }
}
fun <T> Flowable<ResultObject<T>>.filterResult(predicate: (T) -> Boolean): Flowable<ResultObject<T>> {
    return this.filter { it.filter(predicate) }
}


/**
 * flatMap ResultObject todo Maybe
 */

fun <T, R> Single<ResultObject<T>>.flatMapResult(func1: (T) -> SingleSource<R>): Single<ResultObject<R>> {
    return this.flatMap { result ->
        when (result) {
            is ResultObject.Success -> {
                this.map { (it as ResultObject.Success<T>).data }
                    .flatMap(func1)
                    .map { ResultObject.Success<R>(it) }
            }
            is ResultObject.Loading -> {
                if (result.partialData != null) {
                    this.map { (it as ResultObject.Loading<T>).partialData!! }
                        .flatMap(func1)
                        .map { ResultObject.Loading<R>(it) }
                } else {
                    Single.just(ResultObject.Loading<R>())
                }
            }
            is ResultObject.Error -> {
                Single.just(ResultObject.Error<R>(result.throwable, result.errorCode))
            }
        }
    }
}
fun <T, R> Single<ResultObject<T>>.flatMapPublisherResult(func1: (T) -> Publisher<R>): Flowable<ResultObject<R>> {
    return this.flatMapPublisher { result ->
        when (result) {
            is ResultObject.Success -> {
                this.map { (it as ResultObject.Success<T>).data }
                    .flatMapPublisher(func1)
                    .map { ResultObject.Success<R>(it) }
            }
            is ResultObject.Loading -> {
                if (result.partialData != null) {
                    this.map { (it as ResultObject.Loading<T>).partialData!! }
                        .flatMapPublisher(func1)
                        .map { ResultObject.Loading<R>(it) }
                } else {
                    Flowable.just(ResultObject.Loading<R>())
                }
            }
            is ResultObject.Error -> {
                Flowable.just(ResultObject.Error<R>(result.throwable, result.errorCode))
            }
        }
    }
}
fun <T, R> Observable<ResultObject<T>>.flatMapResult(func1: (T) -> ObservableSource<R>): Observable<ResultObject<R>> {
    return this.flatMap { result ->
        when (result) {
            is ResultObject.Success -> {
                this.map { (it as ResultObject.Success<T>).data }
                    .flatMap(func1)
                    .map { ResultObject.Success<R>(it) }
            }
            is ResultObject.Loading -> {
                if (result.partialData != null) {
                    this.map { (it as ResultObject.Loading<T>).partialData!! }
                        .flatMap(func1)
                        .map { ResultObject.Loading<R>(it) }
                } else {
                    Observable.just(ResultObject.Loading<R>())
                }
            }
            is ResultObject.Error -> {
                Observable.just(ResultObject.Error<R>(result.throwable, result.errorCode))
            }
        }
    }
}
fun <T, R> Flowable<ResultObject<T>>.flatMapResult(func1: (T) -> Publisher<R>): Flowable<ResultObject<R>> {
    return this.flatMap { result ->
        when (result) {
            is ResultObject.Success -> {
                this.map { (it as ResultObject.Success<T>).data }
                    .flatMap(func1)
                    .map { ResultObject.Success<R>(it) }
            }
            is ResultObject.Loading -> {
                if (result.partialData != null) {
                    this.map { (it as ResultObject.Loading<T>).partialData!! }
                        .flatMap(func1)
                        .map { ResultObject.Loading<R>(it) }
                } else {
                    Flowable.just(ResultObject.Loading<R>())
                }
            }
            is ResultObject.Error -> {
                Flowable.just(ResultObject.Error<R>(result.throwable, result.errorCode))
            }
        }
    }
}




/**
 * flatten ResultObject
 *
 */

fun <T, U> Single<ResultObject<List<T>>>.flattenResultObjectAsFlowable(func2: (List<T>) -> Iterable<U>): Flowable<ResultObject<U>> {
    return this.mapResult(func2)
        .flatMapPublisher {
            when (it) {
                is ResultObject.Success -> Flowable.fromIterable(it.data).map { ResultObject.Success<U>(it) }
                is ResultObject.Loading -> {
                    if (it.partialData != null)  Flowable.fromIterable(it.partialData).map { ResultObject.Success<U>(it) }
                    else Flowable.just(ResultObject.Loading<U>(it.partialData, it.message))
                }
                is ResultObject.Error -> Flowable.just(ResultObject.Error<U>(it.throwable, it.errorCode))
            }
        }
}
fun <T, U> Maybe<ResultObject<List<T>>>.flattenResultObjectAsFlowable(func2: (List<T>) -> Iterable<U>): Flowable<ResultObject<U>> {
    return this.mapResult(func2)
        .flatMapPublisher {
            when (it) {
                is ResultObject.Success -> Flowable.fromIterable(it.data).map { ResultObject.Success<U>(it) }
                is ResultObject.Loading -> {
                    if (it.partialData != null)  Flowable.fromIterable(it.partialData).map { ResultObject.Success<U>(it) }
                    else Flowable.just(ResultObject.Loading<U>(it.partialData, it.message))
                }
                is ResultObject.Error -> Flowable.just(ResultObject.Error<U>(it.throwable, it.errorCode))
            }
        }
}
fun <T, U> Flowable<ResultObject<T>>.flattenResultObject(func2: (T) -> Iterable<U>): Flowable<ResultObject<U>> {
    return this.mapResult(func2)
        .flatMap {
            when (it) {
                is ResultObject.Success -> {
                    Flowable.fromIterable(it.extractData)
                        .map { ResultObject.Success<U>(it)}
                }
                is ResultObject.Loading -> { Flowable.just(ResultObject.Loading<U>(message = it.message)) }
                is ResultObject.Error -> { Flowable.just(ResultObject.Error<U>(it.throwable, it.errorCode)) }
            }
        }
}
fun <T, U> Observable<ResultObject<T>>.flattenResultObject(func2: (T) -> Iterable<U>): Observable<ResultObject<U>> {
    return this.mapResult(func2)
        .flatMap {
            when (it) {
                is ResultObject.Success -> {
                    Observable.fromIterable(it.extractData)
                        .map { ResultObject.Success<U>(it)}
                }
                is ResultObject.Loading -> { Observable.just(ResultObject.Loading<U>(message = it.message)) }
                is ResultObject.Error -> { Observable.just(ResultObject.Error<U>(it.throwable, it.errorCode)) }
            }
        }
}



// try to convert retrofit response to ResultObject
inline fun <reified T: Any> Response<ResponseBody>.toResultObject(): ResultObject<T> {
    return if (this.isSuccessful) {
        ResultObject.Success<T>(this.body()!!.string().toJsonObject<T>())
    } else {
        ResultObject.Error<T>(Throwable(this.errorBody()?.string()?:this.message()), this.code())
    }
}
// try to convert retrofit response to ResultObject<List>
inline fun <reified T: Any> Response<ResponseBody>.toResultObjectList(): ResultObject<List<T>> {
    return if (this.isSuccessful) {
        ResultObject.Success<List<T>>(this.body()!!.string().toJsonObjectList<T>())
    } else {
        ResultObject.Error<List<T>>(Throwable(this.errorBody()?.string()?:this.message()), this.code())
    }
}
inline fun <reified T: Any> Single<Response<ResponseBody>>.toResultObject(): Single<ResultObject<T>> {
    return this.map { it.toResultObject<T>() }
}
inline fun <reified T: Any> Single<Response<ResponseBody>>.toResultObjectList(): Single<ResultObject<List<T>>> {
    return this.map { it.toResultObjectList<T>() }
}


/**
 * Convert WebSocket to ResultObject
 * R: intended result
 * C: subscription confirmation on subscribe
 */
inline fun <reified R: Any, reified C: Any> Flowable<WsResponse>.toResultObject(request: String, lenient: Boolean = false): Flowable<ResultObject<R>> {
    return this
        .doOnNext { resp ->
            if (resp is WsOpen) {
                resp.webSocket.send(request)
            }
        }
        .map {
            when (it) {
                is WsTextMessage -> {
                    try { // try is result
                        ResultObject.Success(it.text!!.toJsonObject<R>(lenient = lenient))
                    } catch (e: JsonDataException) {
                        try { // try is sub confirmation
                            it.text!!.toJsonObject<C>(lenient = lenient).let { subConfirmationObj ->
                                ResultObject.Loading<R>(message = subConfirmationObj.toJsonString())
                            }
                        } catch (t: Throwable) { ResultObject.Error<R>(e) }
                    } catch (t: Throwable) { ResultObject.Error<R>(t, it.code) }
                }
                is WsByteMessage -> {
                    val text = it.response?.body()?.string()
                    if (text != null) {
                        try { // try is result
                            ResultObject.Success(text.toJsonObject<R>(lenient = lenient))
                        } catch (t: Throwable) {
                            ResultObject.Error<R>(t)
                        }
                    } else ResultObject.Error<R>(NullPointerException())
                }
                is WsFailure -> ResultObject.Error<R>(Throwable(it.response?.message()), it.code)
                is WsClosing, is WsClosed -> ResultObject.Error<R>(Throwable("Closed: ${it.text}"), it.code)
                else -> {ResultObject.Loading<R>(message = "WS Type: ${it.type}")}
            }
        }
}
/**
 * Convert WebSocket to ResultObject
 * R: intended result
 * C: subscription confirmation on subscribe
 */
inline fun <reified C: Any> Flowable<WsResponse>.toRawResultObject(request: String): Flowable<ResultObject<String>> {
    return this
        .doOnNext { resp ->
            if (resp is WsOpen) {
                resp.webSocket.send(request)
            }
        }
        .map {
            when (it) {
                is WsTextMessage -> {
                    try { // try is result
                        it.text!!.toJsonObject<C>().let { subConfirmationObj ->
                            ResultObject.Loading<String>(message = subConfirmationObj.toJsonString())
                        }
                    } catch (e: JsonDataException) {
                        it.text?.let { ResultObject.Success<String>(it) }
                    } catch (t: Throwable) { ResultObject.Error<String>(t, it.code) }
                }
                is WsFailure -> ResultObject.Error<String>(Throwable(it.response?.message()), it.code)
                is WsClosing, is WsClosed -> ResultObject.Error<String>(Throwable("Closed: ${it.text}"), it.code)
                else -> {ResultObject.Loading<String>(message = "WS Type: ${it.type}")}
            }
        }
}

