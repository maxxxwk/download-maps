package com.download.maps.data

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.download.maps.R
import com.download.maps.data.api.DownloadService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class MapDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val downloadService: DownloadService,
) : CoroutineWorker(appContext, params) {

    private val notificationManager: NotificationManager by lazy {
        applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE,
        ) as NotificationManager
    }

    @Suppress("MagicNumber")
    override suspend fun doWork(): Result {
        val fileName = inputData.getString(KEY_FILE_NAME)
            ?: return Result.failure()
        createNotificationChannel()
        setForeground(createForegroundInfo(fileName))

        return try {
            val isSuccess = downloadFile(fileName)
            if (isSuccess) {
                updateNotification(fileName, 100, ongoing = false)
                Result.success()
            } else {
                Result.failure()
            }
        } catch (_: Exception) {
            Result.failure()
        } finally {
            val folder = File(applicationContext.filesDir, "maps")
            val finalFile = File(folder, fileName)
            if (!finalFile.exists()) {
                cleanupTempFile(fileName)
            }
        }
    }

    @Suppress("NestedBlockDepth", "MagicNumber")
    private suspend fun downloadFile(fileName: String): Boolean {
        val folder = File(applicationContext.filesDir, "maps")
        if (!folder.exists()) folder.mkdirs()

        val tempFile = File(folder, "$fileName.tmp")
        val finalFile = File(folder, fileName)

        downloadService.downloadFile(file = fileName).use { body ->
            val contentLength = body.contentLength()

            body.byteStream().use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var downloadedBytes = 0L
                    var lastProgress = 0

                    var bytesRead = inputStream.read(buffer)

                    while (bytesRead != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        if (contentLength > 0) {
                            val progress = (downloadedBytes * 100 / contentLength).toInt()
                            if (progress > lastProgress) {
                                lastProgress = progress
                                setProgress(workDataOf(KEY_PROGRESS to progress))
                                updateNotification(
                                    fileName,
                                    progress,
                                    ongoing = true,
                                )
                            }
                        }
                        bytesRead = inputStream.read(buffer)
                    }
                }
            }
        }

        return if (tempFile.exists()) {
            tempFile.renameTo(finalFile)
        } else {
            false
        }
    }

    private fun cleanupTempFile(fileName: String) {
        val folder = File(applicationContext.filesDir, "maps")
        val tempFile = File(folder, "$fileName.tmp")
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }

    private fun createForegroundInfo(fileName: String): ForegroundInfo {
        val notification = buildNotification(
            fileName = fileName,
            progress = 0,
            ongoing = true,
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
            )
        }
    }

    private fun updateNotification(
        fileName: String,
        progress: Int,
        ongoing: Boolean,
    ) {
        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(
                NOTIFICATION_ID,
                buildNotification(
                    fileName = fileName,
                    progress = progress,
                    ongoing = ongoing,
                ),
            )
        }
    }

    @Suppress("MagicNumber")
    private fun buildNotification(
        fileName: String,
        progress: Int,
        ongoing: Boolean,
    ): Notification {
        return NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID,
        ).setContentTitle(applicationContext.getString(R.string.downloading_notification_title))
            .setContentText(fileName)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(ongoing)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    applicationContext.getString(R.string.downloading_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW,
                ),
            )
        }
    }

    companion object {
        const val KEY_FILE_NAME = "key_file_name"
        const val KEY_PROGRESS = "key_progress"

        private const val CHANNEL_ID = "map_download_channel"
        private const val NOTIFICATION_ID = 1001
        private const val BUFFER_SIZE = 8 * 1024
    }
}
