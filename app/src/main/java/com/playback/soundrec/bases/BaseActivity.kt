package com.playback.soundrec.bases

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

open class BaseActivity : AppCompatActivity() {

    private val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1
    private val WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 2
    private val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  setupUI()
         //Check and request permissions if not granted
        if (checkAndRequestPermission(Manifest.permission.RECORD_AUDIO, RECORD_AUDIO_PERMISSION_REQUEST_CODE)) {
            // Permissions are already granted, set up UI elements
         //   setupUI()
        }
        else{
            // Permissions are not granted, so request permissions first and then set up UI elements
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                RECORD_AUDIO_PERMISSION_REQUEST_CODE
            )
        }
     //   setupUI()
    }


    private fun checkAndRequestPermission(permission: String, requestCode: Int): Boolean {
        val granted =
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, permission)
        if (!granted) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
        return granted
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                RECORD_AUDIO_PERMISSION_REQUEST_CODE -> {
                    // Handle the result for the RECORD_AUDIO permission
                    // You can perform any specific action related to this permission here
                }
                WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE -> {
                    // Handle the result for the WRITE_EXTERNAL_STORAGE permission
                    // You can perform any specific action related to this permission here
                }
                READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE -> {
                    // Handle the result for the READ_EXTERNAL_STORAGE permission
                    // You can perform any specific action related to this permission here
                }
            }
            // If all required permissions are granted, set up UI elements
            if (checkPermission(Manifest.permission.RECORD_AUDIO) &&
                checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            ) {
            //    setupUI()
            }
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, permission)
    }

//    @SuppressLint("MissingPermission")
//    private fun threadLoop2() {
//        val intRecordSampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_SYSTEM)
//
//        intBufferSize = AudioRecord.getMinBufferSize(
//            intRecordSampleRate,
//            AudioFormat.CHANNEL_IN_MONO,
//            AudioFormat.ENCODING_PCM_16BIT
//        )
//
//        shortAudioData = ShortArray(intBufferSize!!)
//        audioRecord = AudioRecord(
//            MediaRecorder.AudioSource.MIC,
//            intRecordSampleRate,
//            AudioFormat.CHANNEL_IN_MONO,
//            AudioFormat.ENCODING_PCM_16BIT,
//            intBufferSize!!
//        )
//        audioTrack = AudioTrack(
//            AudioManager.MODE_IN_COMMUNICATION,
//            intRecordSampleRate,
//            AudioFormat.CHANNEL_OUT_MONO,
//            AudioFormat.ENCODING_PCM_16BIT,
//            intBufferSize!!,
//            AudioTrack.MODE_STREAM
//        )
//        audioTrack?.playbackRate = intRecordSampleRate
//
//        audioRecord?.startRecording()
//
//        Handler(Looper.getMainLooper()).postDelayed({
//            audioTrack?.play()
//        }, 5000)
//
//        while (isActive) {
//            audioRecord?.read(shortAudioData!!, 0, shortAudioData!!.size, AudioRecord.READ_BLOCKING)
//            audioTrack?.write(shortAudioData!!, 0, shortAudioData!!.size, AudioTrack.WRITE_BLOCKING)
//
//        }
//
//    }

}