package com.playback.soundrec.widget
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class WaveformView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var audioData: ShortArray? = null
    private val paint = Paint()

    fun setAudioData(data: ShortArray) {
        audioData = data
        invalidate() // Redraw the view
    }
    init {
        // Initialize with a default waveform
    //    audioData = createDefaultWaveform()
    }


    var waveColor: Int = Color.GREEN
        set(value) {
            field = value
            invalidate()
        }
    var lineWidth: Float = 2f
        set(value) {
            field = value
            invalidate()
        }
    var gain: Float = 1f
        set(value) {
            field = value
            invalidate()
        }
    public var sampleRate: Int = 44100 // Default sample rate
        set(value) {
            field = value
            invalidate()
        }





    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val data = audioData ?: return
        val width = width.toFloat()
        val height = height.toFloat()
        val centerY = height / 2
        val maxAmplitude = Short.MAX_VALUE.toFloat()

        paint.apply {
            strokeWidth = lineWidth
            color = waveColor
        }

        // Drawing the waveform with gain
        for (i in data.indices) {
            val x = (i.toFloat() / data.size) * width
            val amplitude = (data[i] / maxAmplitude) * gain
            val y = amplitude * centerY + centerY
            canvas.drawLine(x, centerY, x, y, paint)
        }

        // Drawing the time axis
       // drawTimeAxis(canvas, width, height)
    }


}