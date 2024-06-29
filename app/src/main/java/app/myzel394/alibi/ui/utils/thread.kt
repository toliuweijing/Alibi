package app.myzel394.alibi.ui.utils

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Process

fun createHandler(name: String, priority: Int = Process.THREAD_PRIORITY_FOREGROUND): Handler {
    val thread = HandlerThread(name, priority).apply {
        start()
    }
    return Handler(thread.looper)
}

fun Handler.runOrPost(runnable: () -> Unit) {
    if (Looper.myLooper() == looper) {
        runnable.invoke()
    } else {
        post(runnable)
    }
}
