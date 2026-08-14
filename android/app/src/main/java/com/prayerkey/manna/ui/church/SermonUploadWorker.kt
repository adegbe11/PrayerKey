package com.prayerkey.manna.ui.church

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class SermonUploadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        // Production contract: resumable upload endpoint must return the final
        // Whisper transcript and structure. Retry safely until it is deployed.
        return Result.retry()
    }
    companion object {
        fun enqueue(context: Context, metadataPath: String) {
            val request = OneTimeWorkRequestBuilder<SermonUploadWorker>()
                .setInputData(workDataOf("metadata" to metadataPath))
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS).build()
            WorkManager.getInstance(context).enqueueUniqueWork("sermon-upload-$metadataPath", ExistingWorkPolicy.KEEP, request)
        }
    }
}
