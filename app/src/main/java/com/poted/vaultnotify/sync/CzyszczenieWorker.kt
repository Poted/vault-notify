package com.poted.vaultnotify.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.poted.vaultnotify.data.AppDatabase
import com.poted.vaultnotify.data.Status
import java.util.concurrent.TimeUnit

/** Usuwa wpisy WYSLANY starsze niz 90 dni - Room jest tylko buforem, archiwum to arkusz. */
class CzyszczenieWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val granica = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90)
        AppDatabase.pobierz(applicationContext).wydatekDao().usunStarszeNiz(Status.WYSLANY, granica)
        return Result.success()
    }

    companion object {
        const val NAZWA = "czyszczenie_starych_wpisow"
    }
}
