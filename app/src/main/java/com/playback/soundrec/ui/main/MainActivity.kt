package com.playback.soundrec.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatToggleButton
import androidx.lifecycle.ViewModelProvider
import com.playback.soundrec.Pref
import com.playback.soundrec.bases.BaseActivity
import com.playback.soundrec.databinding.ActivityMainBinding
import com.playback.soundrec.providers.FireBaseService
import com.playback.soundrec.ui.login.LoginActivity
import com.playback.soundrec.ui.settings.SettingsActivity
import com.playback.soundrec.ui.userlist.UserListActivity
import com.playback.soundrec.widget.WaveformView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Integer.min
import java.nio.ByteBuffer
import java.util.LinkedList
import java.util.Queue
import kotlinx.coroutines.*

class MainActivity : BaseActivity(), MainActivityNav {
    var binding : ActivityMainBinding? = null
    var viewModel : MainActivityViewModel? = null
    private val backgroundScope = CoroutineScope(Dispatchers.IO)

    var waveformView : WaveformView? = null
        get() {
            if( field == null)
            {
                field = binding?.waveformView
                field?.waveColor = Color.WHITE// Set color
                field?.lineWidth = 10f // Set line width
                field?.gain = 10f // Set gain
            }
            return field
        }
    var waveformView2 : WaveformView? = null
        get() {
            if( field == null)
            {
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
        FireBaseService.INSTANCE?.let {fi ->
            fi.getUserInfo(Pref.getInstance().getUser()!!.user_id!!){
                if (it != null) {
                    Pref.getInstance().saveUser(it)
                    binding = ActivityMainBinding.inflate(layoutInflater)
                    viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
                    binding?.viewModel = viewModel
                    binding?.lifecycleOwner = this
                    viewModel?.mainActivityNav = this
                    viewModel?.isAdmin?.value = it.admin?.toLong() == 1L

                    setContentView(binding?.root)

                }
                else{
                    Pref.getInstance().saveUser(null)
                    val  i = Intent(this, LoginActivity::class.java)
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
                    viewModel?.recordInfo?.value ="${it.setting?.defaultSampleRate}KHz " +
                            "\n with ${it.setting?.defaultDelay}s delay"
                }
//                viewModel?.recordInfo?.value = "${Pref.getInstance().getFormat()} " +
//                        ", ${Pref.getInstance().getSampleRate()}KHz " +
//                        "\n with ${Pref.getInstance().getDelay()}s delay"
            }
        )

        // Get the sample rate from the spinner
      //  val intRecordSampleRate = Pref.getInstance().getSampleRate().toInt() //AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_SYSTEM)
       val intRecordSampleRate = Pref.getInstance().getUser()?.setting?.defaultSampleRate?.toInt() ?: 44100
        waveformView?.sampleRate=intRecordSampleRate
        waveformView2?.sampleRate =intRecordSampleRate

        intBufferSize = AudioRecord.getMinBufferSize(
            intRecordSampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        // Delay time in seconds entered by the user (for example, from an EditText or Spinner).
       // val delayInSeconds = Pref.getInstance().getDelay()  // Default to 5 if input is invalid
        val delayInSeconds = Pref.getInstance().getUser()?.setting?.defaultDelay!!.toLong()  // Default to 5 if input is invalid
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
        var sampleStartTime = startTime + Pref.getInstance().getUser()?.setting?.defaultTimeToStartSoundSample!!.toLong() * 1000
        var sampleEndTime = sampleStartTime + Pref.getInstance().getUser()?.setting?.defaultSoundSampleDuration!!.toLong() * 1000
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
            val readSize = audioRecord?.read(tempBuffer, 0, intBufferSize!!, AudioRecord.READ_BLOCKING) ?: 0
            waveformView?.setAudioData(tempBuffer)
            if (readSize > 0) {
                // If it's time to start sampling and sampling is enabled
                if (!isSampling && Pref.getInstance().getUser()?.setting?.defaultEnableSendDataToServer?.toLong() == 1L && System.currentTimeMillis() >= sampleStartTime) {
                    isSampling = true
                }

                // Add to the audioQueue for playback
                audioQueue.add(tempBuffer)

                // Add to the sampleBuffer if we are within the sampling window
                if (isSampling && System.currentTimeMillis() <= sampleEndTime) {
                    sampleBuffer.add(tempBuffer)
                }

                // Check if the queue has enough data for the desired delay and playback audio
                if (audioQueue.size >= buffersNeededForDelay) {
                    val bufferToPlay = audioQueue.poll()
                    audioTrack?.write(bufferToPlay, 0, bufferToPlay.size, AudioTrack.WRITE_BLOCKING)

                    waveformView2?.setAudioData(bufferToPlay)

                }

                // If we've passed the end time for sampling, process the sample
                if (isSampling && System.currentTimeMillis() >= sampleEndTime) {
                    isSampling = false
                    processSampledAudio(sampleBuffer)
                    sampleBuffer.clear() // Clear the sample buffer for the next sample
                    // Reset sample times for the next round of sampling
                    sampleStartTime = System.currentTimeMillis() + Pref.getInstance().getUser()?.setting?.defaultTimeToStartSoundSample!!.toLong() * 1000
                    sampleEndTime = sampleStartTime + Pref.getInstance().getUser()?.setting?.defaultSoundSampleDuration!!.toLong() * 1000

                }
            }
        }

        // Stop and release the AudioRecord and AudioTrack to avoid resource leaks.
        audioRecord?.release()
        audioTrack?.release()
    }

    private  fun processSampledAudio(sampleBuffer: LinkedList<ShortArray>) {
        if(isACCMFileSaved) return // Don't save the sample if it's already saved
        isACCMFileSaved = true // Set the flag to true after saving as ACCM file
//        var type = Pref.getInstance().getUser()?.setting?.defaultFormat
//        if (type == "pcm") {
//            backgroundScope.launch {
//                saveAsPCMFile(sampleBuffer)
//            }
//        } else {
            backgroundScope.launch {
                saveAsACCMFile(sampleBuffer)
            }
     //   }

    }

    private suspend fun saveAsPCMFile(sampleBuffer: LinkedList<ShortArray>) {
        // Calculate the total size of the sample
        // Calculate the total size of the sample
        val totalSize = sampleBuffer.sumOf { it.size }

        // Create a single array to hold all the samples
        val audioSample = ShortArray(totalSize)
        var offset = 0
        sampleBuffer.forEach { buffer ->
            System.arraycopy(buffer, 0, audioSample, offset, buffer.size)
            offset += buffer.size
        }

        // Define the output file
        val outputFile = File(cacheDir, "sample.pcm")

        try {
            // Open a FileOutputStream to write the audioSample to the outputFile
            val outputStream = FileOutputStream(outputFile)
            // Convert the short array to a byte array
            val byteBuffer = ByteBuffer.allocate(audioSample.size * 2)
            byteBuffer.asShortBuffer().put(audioSample)
            // Write the byte array to the file
            outputStream.write(byteBuffer.array())
            // Close the stream
            outputStream.close()

            // Inform that the file was saved successfully
            Log.d("AudioSample", "Audio sample saved to ${outputFile.absolutePath}")
            FireBaseService.INSTANCE?.uploadFile(Pref.getInstance().getUser()?.user_id!!, outputFile){
                if (it) {
//                    Pref.getInstance().getUser()?.let { user ->
//                        FireBaseService.INSTANCE?.updateSoundSample(user.user_id!!, outputFile.path) {
//                            user.soundSample = outputFile.path
//                            Pref.getInstance().saveUser(user)
//                        }
//                    }
                }
            }
        } catch (e: IOException) {
            // Handle exceptions
            Log.e("AudioSample", "Failed to save audio sample", e)
        }
    }

    private suspend fun saveAsACCMFile(sampleBuffer: LinkedList<ShortArray>) {
        // The MIME type for AAC audio is "audio/mp4a-latm"
        val mimeType = "audio/mp4a-latm"
        // The desired sample rate for the output
        val sampleRate = 44100 // 44.1 KHz
        // The desired number of channels for the output
        val channelCount = 1 // Mono audio
        // The desired bitrate for the output
        val bitRate = 64000 // 64 kbps is a good quality for human speech

        // Create a MediaCodec encoder for the desired MIME type
        val codec = MediaCodec.createEncoderByType(mimeType).apply {
            val format = MediaFormat.createAudioFormat(mimeType, sampleRate, channelCount).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            }
            configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        }

        // Define the output file for the compressed audio sample
        val outputFile = File(cacheDir, "sample.aac")

        try {
            // Open a FileOutputStream to write the encoded audio to the outputFile
            val outputStream = FileOutputStream(outputFile)

            codec.start()

            // Buffers for the encoder input and output
            val inputBuffers = codec.inputBuffers
            val outputBuffers = codec.outputBuffers
            val bufferInfo = MediaCodec.BufferInfo()

            // Feed the raw audio data into the encoder
            var presentationTimeUs = 0L
            sampleBuffer.forEach { buffer ->
                val byteBuffer = buffer.toByteBuffer()

                var inputBufferIndex = codec.dequeueInputBuffer(-1)
                while (inputBufferIndex < 0) {
                    // Retry to dequeue an input buffer
                    inputBufferIndex = codec.dequeueInputBuffer(-1)
                }

                val inputBuffer = inputBuffers[inputBufferIndex]
                inputBuffer.clear()

                val remaining = byteBuffer.remaining()
                val capacity = inputBuffer.remaining()
                val sizeToWrite = min(remaining, capacity)
                val tempBuffer = ByteArray(sizeToWrite)
                byteBuffer.get(tempBuffer)
                inputBuffer.put(tempBuffer)

                codec.queueInputBuffer(inputBufferIndex, 0, sizeToWrite, presentationTimeUs, 0)
                presentationTimeUs += (1000000L * sizeToWrite / (2 * sampleRate)) // Adjust presentation time

                var outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0)
                while (outputBufferIndex >= 0) {
                    val outputBuffer = outputBuffers[outputBufferIndex]
                    val outData = ByteArray(bufferInfo.size)
                    outputBuffer.get(outData)
                    outputBuffer.clear()
                    outputStream.write(outData)  // Write the compressed data to the file
                    codec.releaseOutputBuffer(outputBufferIndex, false)
                    outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0)
                }
            }

            // Signal end of input
            codec.queueInputBuffer(codec.dequeueInputBuffer(-1), 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)

            // Process any remaining output data
            while (true) {
                val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0)
                if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // No output available yet
                    break
                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // This usually doesn't happen for an encoder, so just ignore it
                } else if (outputBufferIndex >= 0) {
                    val outputBuffer = outputBuffers[outputBufferIndex]
                    val outData = ByteArray(bufferInfo.size)
                    outputBuffer.get(outData)
                    outputBuffer.clear()
                    outputStream.write(outData)  // Write the compressed data to the file
                    codec.releaseOutputBuffer(outputBufferIndex, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        // End of the output stream has been reached
                        break
                    }
                }
            }

            // Release the codec and close the file output stream
            codec.stop()
            codec.release()
            outputStream.close()
            FireBaseService.INSTANCE?.uploadFile(Pref.getInstance().getUser()?.user_id!!, outputFile){
                if (it) {
//                    Pref.getInstance().getUser()?.let { user ->
//                        FireBaseService.INSTANCE?.updateSoundSample(user.user_id!!, outputFile.path) {
//                            user.soundSample = outputFile.path
//                            Pref.getInstance().saveUser(user)
//                        }
//                    }
                }
            }

            Log.d("AudioSample", "Compressed audio sample saved to ${outputFile.absolutePath}")
        } catch (e: IOException) {
            Log.e("AudioSample", "Failed to save compressed audio sample", e)
        }
    }

    // Extension function to convert ShortArray to ByteBuffer
    private fun ShortArray.toByteBuffer(): ByteBuffer {
        val byteBuffer = ByteBuffer.allocate(this.size * 2)
        for (s in this) {
            byteBuffer.putShort(s)
        }
        byteBuffer.flip() // Flipping the buffer to set the limit and position correctly.
        return byteBuffer
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
        if((view as AppCompatToggleButton).isChecked) {
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
}