package com.akira.island.service

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.AudioRecordingConfiguration
import android.os.Handler
import android.os.Looper
import com.akira.island.core.IslandBus
import com.akira.island.core.PrivacyState
import com.akira.island.data.SettingsRepo

/**
 * Индикатор приватности, как на iPhone: зелёная точка, когда какое-то приложение использует камеру,
 * оранжевая — когда пишет звук с микрофона. Разрешения не нужны: камеру видно по тому, что она занята
 * (CameraManager), микрофон — по обратному вызову записи звука (AudioManager).
 * Какое именно приложение — система другим приложениям не говорит, поэтому остров показывает только сам факт.
 */
class PrivacyMonitor(context: Context) {
    private val main = Handler(Looper.getMainLooper())
    private val camera = context.getSystemService(CameraManager::class.java)
    private val audio = context.getSystemService(AudioManager::class.java)
    private val busyCameras = HashSet<String>()
    private var micRecording = false
    private val publish = Runnable { publishNow() }

    private val cameraCallback = object : CameraManager.AvailabilityCallback() {
        override fun onCameraUnavailable(cameraId: String) {
            busyCameras += cameraId
            schedule()
        }

        override fun onCameraAvailable(cameraId: String) {
            busyCameras -= cameraId
            schedule()
        }
    }

    private val micCallback = object : AudioManager.AudioRecordingCallback() {
        override fun onRecordingConfigChanged(configs: MutableList<AudioRecordingConfiguration>?) {
            micRecording = !configs.isNullOrEmpty()
            schedule()
        }
    }

    fun start() {
        runCatching { camera?.registerAvailabilityCallback(cameraCallback, main) }
        runCatching { audio?.registerAudioRecordingCallback(micCallback, main) }
    }

    fun stop() {
        main.removeCallbacks(publish)
        runCatching { camera?.unregisterAvailabilityCallback(cameraCallback) }
        runCatching { audio?.unregisterAudioRecordingCallback(micCallback) }
        busyCameras.clear()
        micRecording = false
        IslandBus.privacy.value = PrivacyState()
    }

    /** Небольшая задержка: система на долю секунды занимает камеру и микрофон сама (разблокировка по лицу) — это не событие. */
    private fun schedule() {
        main.removeCallbacks(publish)
        main.postDelayed(publish, 400)
    }

    private fun publishNow() {
        val enabled = SettingsRepo.current.evPrivacy
        // Фонарик и экран блокировки тоже занимают камеру — это не съёмка
        val cameraInUse = busyCameras.isNotEmpty() && !IslandBus.torch.value.on && !IslandBus.keyguard.value
        val next = PrivacyState(camera = enabled && cameraInUse, mic = enabled && micRecording)
        if (next != IslandBus.privacy.value) IslandBus.privacy.value = next
    }
}
