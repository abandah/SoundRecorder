package com.playback.soundrec.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatToggleButton
import androidx.lifecycle.ViewModelProvider
import com.playback.soundrec.Pref
import com.playback.soundrec.R
import com.playback.soundrec.bases.BaseActivity
import com.playback.soundrec.databinding.ActivityMainBinding
import com.playback.soundrec.providers.FireBaseService
import com.playback.soundrec.ui.login.LoginActivity
import com.playback.soundrec.ui.settings.SettingsActivity
import com.playback.soundrec.ui.userlist.UserListActivity
import com.playback.soundrec.widget.WaveformView
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.LinkedList
import java.util.Queue
import kotlinx.coroutines.*
import java.io.DataOutputStream
import java.nio.ByteOrder

class MainActivity : BaseActivity(), MainActivityNav {
    private var sampleBuffer: LinkedList<ShortArray>? = null
    var binding: ActivityMainBinding? = null
    var viewModel: MainActivityViewModel? = null
    private val backgroundScope = CoroutineScope(Dispatchers.Main)
    private var outputFile: File? = null
    var loadingDialog: AlertDialog? = null
    var waveformView: WaveformView? = null
        get() {
            if (field == null) {
                field = binding?.waveformView
                field?.waveColor = Color.WHITE// Set color
                field?.lineWidth = 10f // Set line width
                field?.gain = 10f // Set gain
            }
            return field
        }
    var waveformView2: WaveformView? = null
        get() {
            if (field == null) {
                field = binding?.waveformView2
                field?.waveColor = Color.WHITE// Set color
                field?.lineWidth = 10f // Set line width
                field?.gain = 10f // Set gain
            }
            return field
        }
    var audioRecord: AudioRecord? = null
    var audioTrack: AudioTrack? = null
    var intBufferSize: Int? = null
    var thread: Thread? = null

    // Other variables...
    private var isACCMFileSaved = false
    private fun initRecorders() {
        thread = Thread {
            threadLoop()
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        outputFile = File(cacheDir, "sample.wav")
        FireBaseService.INSTANCE?.let { fi ->
            fi.getUserInfo(Pref.getInstance().getUser()!!.user_id!!) {
                if (it != null) {
                    Pref.getInstance().saveUser(it)
                    binding = ActivityMainBinding.inflate(layoutInflater)
                    viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
                    binding?.viewModel = viewModel
                    binding?.lifecycleOwner = this
                    viewModel?.mainActivityNav = this
                    viewModel?.isAdmin?.value = it.admin?.toLong() == 1L

                    setContentView(binding?.root)
                    createProgressDialog()

                } else {
                    Pref.getInstance().saveUser(null)
                    val i = Intent(this, LoginActivity::class.java)
                    startActivity(i)
                    finish()
                }
            }
        }


    }

    @SuppressLint("MissingPermission")
    private fun threadLoop() {
        runOnUiThread(
            Runnable {
                Pref.getInstance().getUser()?.let {
                    viewModel?.recordInfo?.value = "${it.setting?.defaultSampleRate}KHz " +
                            "\n with ${it.setting?.defaultDelay}s delay"
                }
//                viewModel?.recordInfo?.value = "${Pref.getInstance().getFormat()} " +
//                        ", ${Pref.getInstance().getSampleRate()}KHz " +
//                        "\n with ${Pref.getInstance().getDelay()}s delay"
            }
        )

        // Get the sample rate from the spinner
        //  val intRecordSampleRate = Pref.getInstance().getSampleRate().toInt() //AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_SYSTEM)
        val intRecordSampleRate =
            Pref.getInstance().getUser()?.setting?.defaultSampleRate?.toInt() ?: 44100
        waveformView?.sampleRate = intRecordSampleRate
        waveformView2?.sampleRate = intRecordSampleRate

        intBufferSize = AudioRecord.getMinBufferSize(
            intRecordSampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        // Delay time in seconds entered by the user (for example, from an EditText or Spinner).
        // val delayInSeconds = Pref.getInstance().getDelay()  // Default to 5 if input is invalid
        val delayInSeconds = Pref.getInstance()
            .getUser()?.setting?.defaultDelay!!.toLong()  // Default to 5 if input is invalid
        // Calculate the number of buffers needed for the desired delay.
        val buffersNeededForDelay = (intRecordSampleRate * delayInSeconds) / intBufferSize!!

        val audioQueue: Queue<ShortArray> = LinkedList()

        // ... Initialize audioRecord and audioTrack ...
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.DEFAULT,
            intRecordSampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            intBufferSize!!
        )

        audioTrack = AudioTrack(
            AudioManager.MODE_IN_COMMUNICATION,
            intRecordSampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            intBufferSize!!,
            AudioTrack.MODE_STREAM
        )

        audioRecord?.startRecording()
        audioTrack?.play()

        val startTime = System.currentTimeMillis()
        var sampleStartTime = startTime + Pref.getInstance()
            .getUser()?.setting?.defaultTimeToStartSoundSample!!.toLong() * 1000
        var sampleEndTime = sampleStartTime + Pref.getInstance()
            .getUser()?.setting?.defaultSoundSampleDuration!!.toLong() * 1000
        var isSampling = false
        val sampleBuffer = LinkedList<ShortArray>() // Buffer to hold the sound sample
        while (viewModel?.isRecording?.value == true) {
            // Update the recording time
            val elapsedTimeInSeconds = (System.currentTimeMillis() - startTime) / 1000
            val hours = elapsedTimeInSeconds / 3600
            val minutes = (elapsedTimeInSeconds % 3600) / 60
            val seconds = elapsedTimeInSeconds % 60

            val time = String.format("%02d:%02d:%02d", hours, minutes, seconds)

            // Update UI with the formatted time (if applicable)
            // Remember to update the UI on the main thread
            runOnUiThread {
                viewModel?.progressTime?.value = time
                // Update your TextView or UI element with 'time'
            }
            val tempBuffer = ShortArray(intBufferSize!!)
            val readSize =
                audioRecord?.read(tempBuffer, 0, intBufferSize!!, AudioRecord.READ_BLOCKING) ?: 0

            waveformView?.setAudioData(tempBuffer)
            if (readSize > 0) {
                // If it's time to start sampling and sampling is enabled
                if (!isSampling && Pref.getInstance()
                        .getUser()?.setting?.defaultEnableSendDataToServer?.toLong() == 1L && System.currentTimeMillis() >= sampleStartTime
                ) {
                    isSampling = true
                }

                // Add to the audioQueue for playback
                audioQueue.add(tempBuffer)

                // Add to the sampleBuffer if we are within the sampling window
                if (isSampling && System.currentTimeMillis() <= sampleEndTime) {
                    sampleBuffer.add(tempBuffer.copyOf())
                }

                // Check if the queue has enough data for the desired delay and playback audio
                if (audioQueue.size >= buffersNeededForDelay) {
                    val bufferToPlay = audioQueue.poll()
                    if (viewModel?.isRecording?.value == false) return
                    audioTrack?.write(bufferToPlay, 0, bufferToPlay.size, AudioTrack.WRITE_BLOCKING)

                    waveformView2?.setAudioData(bufferToPlay)

                }

                // If we've passed the end time for sampling, process the sample
                if (isSampling && System.currentTimeMillis() >= sampleEndTime) {
                    isSampling = false
                    processSampledAudio(sampleBuffer)

                    sampleBuffer.clear() // Clear the sample buffer for the next sample
                    // Reset sample times for the next round of sampling
                    sampleStartTime = System.currentTimeMillis() + Pref.getInstance()
                        .getUser()?.setting?.defaultTimeToStartSoundSample!!.toLong() * 1000
                    sampleEndTime = sampleStartTime + Pref.getInstance()
                        .getUser()?.setting?.defaultSoundSampleDuration!!.toLong() * 1000

                }
            }
        }

        // Stop and release the AudioRecord and AudioTrack to avoid resource leaks.
        audioRecord?.release()
        audioTrack?.release()
    }

    private fun processSampledAudio(sampleBuffer: LinkedList<ShortArray>) {
        if (isACCMFileSaved) return // Don't save the sample if it's already saved
        isACCMFileSaved = true

        this.sampleBuffer = sampleBuffer

    }

    fun startSaving() {
        showLoading()

        val success = saveToWav(this.sampleBuffer!!)
        if (success) {
            uploadAndHandleFile()
        } else {
            hideLoading()
            runOnUiThread {
                Toast.makeText(this@MainActivity, "Failed to save file", Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun saveToWav(originalSampleBuffer: LinkedList<ShortArray>): Boolean {
        val sampleBuffer = LinkedList(originalSampleBuffer)

        val sampleRate = 44100 // Change this to your actual sample rate
        val channels = 1 // Mono
        val bitsPerSample = 16
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8

        val totalAudioLen = sampleBuffer.sumOf { it.size } * 2
        val totalDataLen = totalAudioLen + 36

        DataOutputStream(FileOutputStream(outputFile)).use { out ->
            // Write WAV file header
            out.writeBytes("RIFF")
            out.writeInt(Integer.reverseBytes(totalDataLen))
            out.writeBytes("WAVE")
            out.writeBytes("fmt ")
            out.writeInt(Integer.reverseBytes(16)) // Subchunk1Size (16 for PCM)
            out.writeShort(1.toShort().reverseBytes().toInt()) // AudioFormat (PCM)
            out.writeShort(channels.toShort().reverseBytes().toInt()) // NumChannels
            out.writeInt(Integer.reverseBytes(sampleRate)) // SampleRate
            out.writeInt(Integer.reverseBytes(byteRate)) // ByteRate
            out.writeShort(blockAlign.toShort().reverseBytes().toInt()) // BlockAlign
            out.writeShort(bitsPerSample.toShort().reverseBytes().toInt()) // BitsPerSample
            out.writeBytes("data")
            out.writeInt(Integer.reverseBytes(totalAudioLen))

            // Write audio data
            sampleBuffer.forEach { buffer ->
                buffer.forEach { sample ->
                    val low = (sample.toInt() and 0xFF)
                    val high = ((sample.toInt() shr 8) and 0xFF)
                    out.writeByte(low)
                    out.writeByte(high)
                }
            }
        }
        return true


    }

    fun uploadAndHandleFile() {
        // Execute the upload and post-upload logic here
        // This function can be called right after saveToWav completes
        FireBaseService.INSTANCE?.uploadFile(
            Pref.getInstance().getUser()?.user_id!!, outputFile!!
        ) { success ->
            if (success) {
                // Perform actions after successful upload
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Sound sample sent successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                outputFile!!.delete()
                hideLoading()

            }
        }
    }

    private fun Short.reverseBytes(): Short {
        val buffer = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putShort(this)
        return buffer.order(ByteOrder.BIG_ENDIAN).getShort(0)
    }

    private fun startRecording() {
        isACCMFileSaved = false // Reset the flag when starting a new recording

        // Only create a new Thread if the previous one has finished or not been created
        if (thread == null || thread?.isAlive == false) {
            thread = Thread {
                threadLoop()
            }
        }
        viewModel?.isRecording?.value = true
        thread?.start() // Start the newly created thread
    }

    private fun stopRecording() {

        sampleBuffer?.let {
            startSaving()
        }


        // Ensure we're currently recording before attempting to stop
        if (audioRecord?.state == AudioRecord.STATE_INITIALIZED && viewModel?.isRecording?.value == true) {
            audioRecord?.stop()
        }
        viewModel?.progressTime?.value = "00:00:00"
        viewModel?.recordInfo?.value = ""
        // stops the recording activity
        viewModel?.isRecording?.value = false
        audioTrack?.stop()
        audioRecord?.release() // Release resources
        audioRecord = null
        audioTrack?.release()
        audioTrack = null

        thread?.join() // Wait for the thread to finish if necessary
        thread = null // Clear the thread so a new one can be created next time
    }

    override fun onSettingsClick(view: View) {
        SettingsActivity.getStartIntent(this)?.let {
            startActivity(it)
        }
    }

    override fun onRecordClick(view: View) {
        if ((view as AppCompatToggleButton).isChecked) {
            startRecording()
        } else {
            stopRecording()
        }
    }

    override fun onExitClick(view: View) {
        this.finishAffinity();

    }

    override fun onAdminClick(view: View) {
        val intent = Intent(this, UserListActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    fun showLoading() {
        hideLoading()
        loadingDialog?.show()
    }

    private fun hideLoading() {
        loadingDialog?.dismiss()
    }
    private fun  createProgressDialog(){
        val container = layoutInflater.inflate(R.layout.view_progrss, null) as LinearLayout
        loadingDialog = AlertDialog.Builder(this, R.style.MyDialogTheme)
            .setTitle("Sending sample to server")
            .setView(container).create()
    }
}