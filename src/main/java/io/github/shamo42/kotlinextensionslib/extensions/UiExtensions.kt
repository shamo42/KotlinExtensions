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

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.shamo42.kotlinextensionslib.R
import io.github.shamo42.kotlinextensionslib.objects.ResultObject
import com.tapadoo.alerter.Alerter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import java.lang.NullPointerException
import java.util.concurrent.TimeUnit

private const val TAG = "UiExtensions"

private const val DURATION_LONG = 2500L
private const val DURATION_SHORT = 800L

fun Context.toast(text: String, longDuration: Boolean = false) {
    Toast.makeText(this, text.trim(), if (longDuration) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}

fun Context.toast(exception: Exception, tag: String? = "", longDuration: Boolean = false) {
    Toast.makeText(this, "error $tag ${exception.localizedMessage?.trim()}", if (longDuration) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}


fun <T> Context.toast(resultObject: ResultObject<T>, longDuration: Boolean = false) {
    when (resultObject) {
        is ResultObject.Success -> this.toast("Success", longDuration)
        is ResultObject.Error -> this.toast(resultObject.throwable.localizedMessage?: resultObject.throwable.stackTrace.toString(), true)
        is ResultObject.Loading -> this.toast(resultObject.message?: "Loading", longDuration)
    }
}

fun Activity.alert(text: String, title: String? = null, longDuration: Boolean = false, backgroundColor: Int? = null, onClickListener: View.OnClickListener? = null) {
    val color = backgroundColor?: R.color.grey_120_default
    Alerter.create(this)
        .setText(text.trim())
        .setBackgroundColorRes(color)
        .setEnterAnimation(R.anim.alerter_slide_in_from_top)
        .setEnterAnimation(R.anim.alerter_from_top_down)
        .setExitAnimation(R.anim.alerter_from_bottom_up)
        .setDuration(if (longDuration) DURATION_LONG else DURATION_SHORT)
        .run {
            if (title != null) setTitle(title)
            //setBackgroundColorRes(color)
            if (onClickListener != null) setOnClickListener(onClickListener)
            show()
        }
}
fun Activity.alert(exception: Exception, tag: String? = null, longDuration: Boolean = true, backgroundColor: Int? = null, onClickListener: View.OnClickListener? = null) {
    val color = backgroundColor?: R.color.material_red_error
    Alerter.create(this)
        .setText(exception.localizedMessage?.trim()?: "Unknown Error")
        .setBackgroundColorRes(color)
        .setIcon(R.drawable.ic_error_outline_24dp)
        .setEnterAnimation(R.anim.alerter_from_top_down)
        .setExitAnimation(R.anim.alerter_from_bottom_up)
        .setDuration(if (longDuration) DURATION_LONG else DURATION_SHORT)
        .run {
            if (tag != null) setTitle("Error on $tag")
            //setBackgroundColorRes(color)
            if (onClickListener != null) setOnClickListener(onClickListener)
            show()
        }
}
fun Activity.alert(throwable: Throwable, tag: String? = null, longDuration: Boolean = true, backgroundColor: Int? = null, onClickListener: View.OnClickListener? = null) {
    this.alert(Exception(throwable.localizedMessage), tag, longDuration, backgroundColor, onClickListener)
}

fun <T> Activity.alert(resultObject: ResultObject<T>, tag: String? = null, longDuration: Boolean? = null, backgroundColor: Int? = null, onClickListener: View.OnClickListener? = null) {
    when (resultObject) {
        is ResultObject.Success -> this.alert("Success", tag, longDuration?: false, backgroundColor, onClickListener)
        is ResultObject.Error -> this.alert(resultObject.throwable, tag, longDuration?: true, backgroundColor, onClickListener)
        is ResultObject.Loading -> this.alert(resultObject.message?: "Loading", tag, longDuration?: false, backgroundColor, onClickListener)
    }
}

fun Fragment.alert(text: String, title: String? = null, longDuration: Boolean = false, backgroundColor: Int? = null, onClickListener: View.OnClickListener? = null) {
    this.activity?.alert(text, title, longDuration, backgroundColor, onClickListener)
}
fun Fragment.alert(exception: Exception, tag: String? = null, longDuration: Boolean = true, backgroundColor: Int? = null, onClickListener: View.OnClickListener? = null) {
    this.activity?.alert(exception, tag, longDuration, backgroundColor, onClickListener)
}
fun Fragment.alert(throwable: Throwable, tag: String? = null, longDuration: Boolean = true, backgroundColor: Int? = null, onClickListener: View.OnClickListener? = null) {
    this.activity?.alert(throwable, tag, longDuration, backgroundColor, onClickListener)
}
fun <T> Fragment.alert(resultObject: ResultObject<T>, tag: String?= null, longDuration: Boolean? = null, backgroundColor: Int? = null, onClickListener: View.OnClickListener? = null) {
    this.activity?.alert(resultObject, tag, longDuration, backgroundColor, onClickListener)
}


@ColorInt
fun Context.getColorFromAttr(@AttrRes attrColorId: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true): Int {
    theme.resolveAttribute(attrColorId, typedValue, resolveRefs)
    return typedValue.data
}

fun Context.getColorIdIdFromAttr(@AttrRes attrColorId: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true): Int {
    theme.resolveAttribute(attrColorId, typedValue, resolveRefs)
    return typedValue.resourceId
}

fun Activity.hideKeyBoard() {
    val view = this.currentFocus
    if (view != null) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun View.blink() {
    val anim = AlphaAnimation(0.3f, 1.0f)
    anim.duration = 16
    this.startAnimation(anim)
}


fun Activity.lockRotation(lock: Boolean) {
    requestedOrientation = if (lock) {
        resources.configuration.orientation.let { currentOrientation ->
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
            }
        }
    } else ActivityInfo.SCREEN_ORIENTATION_USER
}

fun View.toFlowable(throttleTimeMs: Long = 500): Flowable<Boolean> {
    return Flowable.create<Boolean>({emitter ->
        View.OnClickListener{
            emitter.onNext(true)
        }
            .also {
                setOnClickListener(it)
            }
    }, BackpressureStrategy.DROP)
        .throttleFirst(throttleTimeMs, TimeUnit.MILLISECONDS)
}

fun View.toClickSingle(): Single<Boolean> {
    return Single.create<Boolean> {emitter ->
        View.OnClickListener{
            emitter.onSuccess(true)
        }
            .also {
                setOnClickListener(it)
            }
    }
}

fun SearchView.toFlowable(debounceTimeMs: Long = 500): Flowable<String> {
    return Flowable.create<String>({ emitter ->
        emitter.setCancellable {
            this.setOnQueryTextListener(null)
        }
        this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) emitter.onNext(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) emitter.onNext(newText)
                return false
            }
        })
    }, BackpressureStrategy.LATEST)
        .debounce(debounceTimeMs, TimeUnit.MILLISECONDS)
        .distinctUntilChanged()
        .map { it.replace("  ", " ") }
}



fun EditText.toFlowable(debounceTimeMs: Long = 500): Flowable<ResultObject<String>> {
    return Flowable.create<ResultObject<String>>({ emitter ->
        object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrBlank()) emitter.onNext(ResultObject.Error(NullPointerException()))
                else emitter.onNext(ResultObject.Success(s.toString()))
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

        }.also { textWatcher ->
            this.addTextChangedListener(textWatcher)
            emitter.setCancellable {
                Log.i(TAG, "editTextFlowable CANCELLED")
                this.removeTextChangedListener(textWatcher)
            }
        }
    }, BackpressureStrategy.LATEST)
        .distinctUntilChanged()
        .debounce(debounceTimeMs, TimeUnit.MILLISECONDS)
}


fun PopupMenu.toIdClickSingle(menuId: Int): Single<Int> {
    return Single.create<Int> { emitter ->
        PopupMenu.OnMenuItemClickListener { item ->
            emitter.onSuccess(item.itemId)
            this.dismiss()
            true
        }
            .also {
                inflate(menuId)
                setOnMenuItemClickListener(it)
                show()
            }
    }
        .subscribeOn(AndroidSchedulers.mainThread())
}




fun EditText.setTextCursorEnd(text: String) {
    this.setText(text)
    this.setSelection(this.text?.length?: 0)
}

/*fun Spinner.toPositionFlowable(): Flowable<Int> {
    return Flowable.create({ emitter ->
        this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) { emitter.onNext(AdapterView.INVALID_POSITION)}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                emitter.onNext(position)
            }
        }
    }, BackpressureStrategy.LATEST)

}*/



fun RecyclerView.hideFabOnScroll(fab: FloatingActionButton?) {
    var canShow: Boolean
    fab?.also {
        this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                canShow = false
                if (dy > 0 || dy < 0 && it.isShown) { it.hide() }
            }
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    canShow = true
                    Handler().postDelayed({if (canShow) it.show()}, DURATION_LONG)
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }
}




fun Activity.getActionbar(): ActionBar? {
    return if (this is AppCompatActivity) this.supportActionBar else null
}
fun Fragment.getActionbar(): ActionBar? {
    return if (this.activity != null && this.activity is AppCompatActivity) (this.activity as AppCompatActivity).supportActionBar else null
}






const val DISMISS_ALERT = 0
const val BUTTON_POSITIVE = 1
const val BUTTON_NEGATIVE = 2


fun Activity.alertDialogRx(
    @StringRes title: Int,
    @StringRes message: Int? = null,
    @StringRes positiveButton: Int = android.R.string.ok,
    @StringRes negativeButton: Int? = null): Single<Int> {


    return Single.create<Int> { emitter ->
        val builder = AlertDialog.Builder(this)
            .setOnDismissListener { if (!emitter.isDisposed) emitter.onSuccess(DISMISS_ALERT) }

        title.also { builder.setTitle(it)}
        message?.also { builder.setMessage(it)}
        positiveButton.let { builder.setPositiveButton(positiveButton) { _, _ -> emitter.onSuccess(BUTTON_POSITIVE)} }
        negativeButton?.let { builder.setNegativeButton(negativeButton) { _, _ -> emitter.onSuccess(BUTTON_NEGATIVE)} }

        builder.show().also { dialog ->
            dialog.setOnCancelListener { emitter.onError(InterruptedException()) }
            emitter.setCancellable { dialog.dismiss() }
        }
    }
}
fun Fragment.alertDialogRx(
    @StringRes title: Int,
    @StringRes message: Int? = null,
    @StringRes positiveButton: Int = android.R.string.ok,
    @StringRes negativeButton: Int? = null): Single<Int> {
    return if (activity == null) Single.error(Throwable("activity null"))
    else { activity!!.alertDialogRx(title, message, positiveButton, negativeButton) }
}



fun Activity.editTextDialogRx(@StringRes title: Int? = null,
                              @StringRes positiveButton: Int = android.R.string.ok,
                              @StringRes negativeButton: Int = android.R.string.cancel): Single<String> {
    return Single.create { emitter ->
        val builder = AlertDialog.Builder(this)
        //.setOnDismissListener { emitter.onSuccess(ResultObject.Error(InterruptedException())) }

        val dialogView = layoutInflater.inflate(R.layout.dialog_edittext, null)
        builder.setView(dialogView)

        val editText: TextInputEditText = dialogView.findViewById(R.id.et_editText)

        title?.also { builder.setTitle(it)}
        builder.setPositiveButton(positiveButton) { _, _ ->
            editText.text.also { s ->
                if (s.isNullOrBlank()) emitter.onError(Throwable(getString(R.string.cannot_be_empty)))
                else emitter.onSuccess(s.toString())
            }
        }
        builder.setNegativeButton(negativeButton) { _, _ ->
            emitter.onError(InterruptedException())
        }//emitter.onSuccess(ResultObject.Error(InterruptedException()))}

        builder.show().also { dialog ->
            editText.setOnKeyListener { _, keyCode, event ->
                if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    dialog.getButton(BUTTON_POSITIVE); true
                } else false
            }
            dialog.setOnCancelListener { emitter.onError(InterruptedException()) }
            emitter.setCancellable { dialog.dismiss() }
        }
    }
}

fun Fragment.editTextDialogRx(@StringRes title: Int? = null,
@StringRes positiveButton: Int = android.R.string.ok,
@StringRes negativeButton: Int = android.R.string.cancel): Single<String> {
    return if (activity == null) Single.error(Throwable("activity null"))
    else { activity!!.editTextDialogRx(title, positiveButton, negativeButton) }
}


fun Activity.passwordDialogRx(@StringRes title: Int? = null,
                           @StringRes positiveButton: Int = android.R.string.ok,
                           @StringRes negativeButton: Int = android.R.string.cancel): Single<String> {

    return Single.create { emitter ->
        val builder = AlertDialog.Builder(this)
        //.setOnDismissListener { emitter.onSuccess(ResultObject.Error(InterruptedException())) }

        val dialogView = layoutInflater.inflate(R.layout.dialog_password, null)
        builder.setView(dialogView)

        val passwordEditText: TextInputEditText = dialogView.findViewById(R.id.et_password)

        title?.also { builder.setTitle(it)}
        builder.setPositiveButton(positiveButton) { _, _ ->
            passwordEditText.text.also { s ->
                if (s.isNullOrBlank()) emitter.onError(Throwable(getString(R.string.cannot_be_empty)))
                else emitter.onSuccess(s.toString())
            }
        }


        builder.setNegativeButton(negativeButton) { _, _ ->
            emitter.onError(InterruptedException())
        }

        builder.show().also { dialog ->
            passwordEditText.setOnKeyListener { _, keyCode, event ->
                if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    dialog.getButton(BUTTON_POSITIVE); true
                } else false
            }
            dialog.setOnCancelListener { emitter.onError(InterruptedException()) }
            emitter.setCancellable { dialog.dismiss() }
        }
    }
}

fun Fragment.passwordDialogRx(@StringRes title: Int? = null,
                              @StringRes positiveButton: Int = android.R.string.ok,
                              @StringRes negativeButton: Int = android.R.string.cancel): Single<String> {
    return if (activity == null) Single.error(Throwable("activity null"))
    else { requireActivity().passwordDialogRx(title, positiveButton, negativeButton) }
}








fun Activity.passwordDialogVerifyRx(@StringRes title: Int? = null,
                                    @StringRes positiveButton: Int = android.R.string.ok,
                                    @StringRes negativeButton: Int = android.R.string.cancel): Single<String> {


    return Single.create { emitter ->
        val builder = AlertDialog.Builder(this)

        val dialogView = layoutInflater.inflate(R.layout.dialog_password_verification, null)
        builder.setView(dialogView)

        title?.also { builder.setTitle(it)}

        val passwordEditText: TextInputEditText = dialogView.findViewById(R.id.et_password)
        val passwordVerifyEditText: TextInputEditText = dialogView.findViewById(R.id.et_password_verify)
        val passwordVerifyTIL = dialogView.findViewById<TextInputLayout>(R.id.til_password_verify)

        builder.setPositiveButton(positiveButton) { _, _ ->
            val password = passwordEditText.text
            emitter.onSuccess(password.toString())
        }
        builder.setNegativeButton(negativeButton) { _, _ ->
            emitter.onError(InterruptedException())
        }

        builder.show().also { dialog ->

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
            var startShowingMatchError = false

            // check first editText
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) { }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    when {
                        s.isNullOrBlank() -> {
                            passwordVerifyTIL.error = getString(R.string.cannot_be_empty)
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        }
                        s.toString() == passwordVerifyEditText.text.toString() -> {
                            passwordVerifyTIL.error = ""
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                        }
                        else -> {
                            if (startShowingMatchError) passwordVerifyTIL.error = getString(R.string.passwords_dont_match)
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        }
                    }
                }
            }.also { passwordEditText.addTextChangedListener(it) }

            // check second editText
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) { }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    startShowingMatchError = true
                    when {
                        s.isNullOrBlank() -> {
                            passwordVerifyTIL.error = getString(R.string.cannot_be_empty)
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        }
                        s.toString() == passwordEditText.text.toString() -> {
                            passwordVerifyTIL.error = ""
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                        }
                        else -> {
                            passwordVerifyTIL.error = getString(R.string.passwords_dont_match)
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        }
                    }
                }
            }.also { passwordVerifyEditText.addTextChangedListener(it) }

            passwordVerifyEditText.setOnKeyListener { _, keyCode, event ->
                if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    dialog.getButton(BUTTON_POSITIVE); true
                } else false
            }

            dialog.setOnCancelListener { emitter.onError(InterruptedException()) }
            emitter.setCancellable { dialog.dismiss() }
        }
    }
}

fun Fragment.passwordVerifyDialogRx(@StringRes title: Int? = null,
                              @StringRes positiveButton: Int = android.R.string.ok,
                              @StringRes negativeButton: Int = android.R.string.cancel): Single<String> {
    return if (activity == null) Single.error(Throwable("activity null"))
    else { requireActivity().passwordDialogVerifyRx(title, positiveButton, negativeButton) }
}





