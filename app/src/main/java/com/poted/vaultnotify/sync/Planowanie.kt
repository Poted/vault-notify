package com.poted.vaultnotify.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import java.util.concurrent.TimeUnit

private val WYMAGA_SIECI = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .build()

fun zaplanujOkresowaSynchronizacje(context: Context) {
    val zadanie = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
        .setConstraints(WYMAGA_SIECI)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        SyncWorker.NAZWA_OKRESOWA,
        ExistingPeriodicWorkPolicy.KEEP,
        zadanie
    )
}

/** Wyzwalane przyciskiem ręcznej synchronizacji w UI. */
fun uruchomRecznaSynchronizacje(context: Context) {
    val zadanie = OneTimeWorkRequestBuilder<SyncWorker>()
        .setConstraints(WYMAGA_SIECI)
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
        SyncWorker.NAZWA_RECZNA,
        ExistingWorkPolicy.KEEP,
        zadanie
    )
}

fun zaplanujCzyszczenie(context: Context) {
    val zadanie = PeriodicWorkRequestBuilder<CzyszczenieWorker>(1, TimeUnit.DAYS).build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        CzyszczenieWorker.NAZWA,
        ExistingPeriodicWorkPolicy.KEEP,
        zadanie
    )
}
