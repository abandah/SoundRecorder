package com.playback.soundrec.ui.main

import android.annotation.SuppressLint
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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatToggleButton
import androidx.lifecycle.ViewModelProvider
import com.playback.soundrec.Pref
import com.playback.soundrec.bases.BaseActivity
import com.playback.soundrec.R
import com.playback.soundrec.databinding.ActivityMainBinding
import com.playback.soundrec.ui.settings.SettingsActivity
import com.playback.soundrec.widget.WaveformView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.LinkedList
import java.util.Queue

class MainActivity : BaseActivity(), MainActivityNav {
    var binding : ActivityMainBinding? = null
    var viewModel : MainActivityViewModel? = null
    var waveformView : WaveformView? = null
    var waveformView2 : WaveformView? = null

    var audioRecord: AudioRecord? = null
    var audioTrack: AudioTrack? = null
    var intBufferSize: Int? = null
    var thread: Thread? = null

    private fun initRecorders() {
        thread = Thread {
            threadLoop()
        }


    }
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        binding?.viewModel = viewModel
        binding?.lifecycleOwner = this
        viewModel?.mainActivityNav = this

        setContentView(binding?.root)
        super.onCreate(savedInstanceState)


    }
    @SuppressLint("MissingPermission")
    private fun threadLoop() {
        runOnUiThread(
            Runnable {
                viewModel?.recordInfo?.value = "${Pref.getInstance().getFormat()} " +
                        ", ${Pref.getInstance().getSampleRate()}KHz " +
                        "\n with ${Pref.getInstance().getDelay()}s delay"
            }
        )

        // Get the sample rate from the spinner
        val intRecordSampleRate = Pref.getInstance().getSampleRate().toInt() //AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_SYSTEM)
        waveformView?.sampleRate=intRecordSampleRate
        waveformView2?.sampleRate =intRecordSampleRate

        intBufferSize = AudioRecord.getMinBufferSize(
            intRecordSampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        // Delay time in seconds entered by the user (for example, from an EditText or Spinner).
        val delayInSeconds = Pref.getInstance().getDelay()  // Default to 5 if input is invalid
        // Calculate the number of buffers needed for the desired delay.
        val buffersNeededForDelay = (intRecordSampleRate * delayInSeconds) / intBufferSize!!

        val audioQueue: Queue<ShortArray> = LinkedList()

        // ... Initialize audioRecord and audioTrack ...
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
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
        var sampleStartTime = startTime + Pref.getInstance().getTimeToStartSoundSample() * 1000
        var sampleEndTime = sampleStartTime + Pref.getInstance().getSoundSampleDuration() * 1000
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
                if (!isSampling && Pref.getInstance().getEnableSendDataToServer() && System.currentTimeMillis() >= sampleStartTime) {
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
                    sampleStartTime = System.currentTimeMillis() + Pref.getInstance().getTimeToStartSoundSample() * 1000
                    sampleEndTime = sampleStartTime + Pref.getInstance().getSoundSampleDuration() * 1000
                }
            }
        }

        // Stop and release the AudioRecord and AudioTrack to avoid resource leaks.
        audioRecord?.release()
        audioTrack?.release()
    }

    private fun processSampledAudio(sampleBuffer: LinkedList<ShortArray>) {
        var type = Pref.getInstance().getFormat()
        if (type == "PCM") {
            saveAsPCMFile(sampleBuffer)
        } else {
            saveAsACCMFile(sampleBuffer)
        }

    }

    private fun saveAsPCMFile(sampleBuffer: LinkedList<ShortArray>) {
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
        } catch (e: IOException) {
            // Handle exceptions
            Log.e("AudioSample", "Failed to save audio sample", e)
        }
    }

    private fun saveAsACCMFile(sampleBuffer: LinkedList<ShortArray>) {
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
                val inputBufferIndex = codec.dequeueInputBuffer(-1)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = inputBuffers[inputBufferIndex]
                    inputBuffer.clear()
                    inputBuffer.put(buffer.toByteBuffer())
                    codec.queueInputBuffer(inputBufferIndex, 0, buffer.size * 2, presentationTimeUs, 0)
                    presentationTimeUs += (1000000L * buffer.size / sampleRate)
                }

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
        byteBuffer.flip()
        return byteBuffer // This line was missing
    }

    private fun startRecording() {
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


    override fun setupUI() {
        super.setupUI()

        waveformView = binding?.waveformView
        waveformView?.waveColor = Color.WHITE// Set color
        waveformView?.lineWidth = 10f // Set line width
        waveformView?.gain = 10f // Set gain

        waveformView2 = binding?.waveformView2
        waveformView2?.waveColor= Color.WHITE // Set color
        waveformView2?.lineWidth=10f // Set line width
        waveformView2?.gain =10f // Set gain

        initRecorders()

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
}