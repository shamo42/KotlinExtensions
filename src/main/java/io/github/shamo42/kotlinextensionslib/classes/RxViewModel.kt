package io.github.shamo42.kotlinextensionslib.classes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable

open class RxViewModel : ViewModel() {
    protected val onClearedDisposables by lazy { CompositeDisposable() }

    protected inline fun <reified T> Flowable<T>.runOnceLiveData(): LiveData<T> {
        return MutableLiveData<T>().also { mld ->
            onClearedDisposables.add(this.subscribe { mld.postValue(it) })
        }
    }


    override fun onCleared() {
        onClearedDisposables.dispose()
    }
}