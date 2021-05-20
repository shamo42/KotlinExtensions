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
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.PowerManager
import android.util.Log


object ServiceExtensions {
    const val TAG = "ServiceExtensions"
}



inline fun <reified T: Service> Activity.startServiceUnlessRunning() {
    if (!this.isServiceRunning<T>()) this.startMyService<T>()
}

inline fun <reified T: Service> Activity?.startServiceUnlessRunning(delaysMs: Long)
        = Handler().postDelayed({this?.startServiceUnlessRunning<T>()}, delaysMs)

inline fun <reified T: Service> Context.isServiceRunning(): Boolean {
    (this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?)?.run {
        for (service in getRunningServices(Integer.MAX_VALUE)) {
            if (T::class.java.name == service.service.className) return true //service.foreground
        }
    }
    return false
}

inline fun <reified T: Service> Activity.startMyService() {
    Log.i(ServiceExtensions.TAG, "STARTING SERVICE ${T::class.java.name}")
    return Intent(this, T::class.java).let { serviceIntent ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) this.startForegroundService(serviceIntent)
        else this.startService(serviceIntent)
    }
}


fun Service.getWakeLock(serviceTag: String, packageName: String, timeoutMs: Long? = null): PowerManager.WakeLock {
    return (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
        newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "$packageName:$serviceTag")
                .apply { if (timeoutMs != null) acquire(timeoutMs) else acquire() }
    }
}









