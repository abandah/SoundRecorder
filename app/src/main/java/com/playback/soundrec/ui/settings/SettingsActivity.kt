package com.playback.soundrec.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.playback.soundrec.AppConstants
import com.playback.soundrec.Pref
import com.playback.soundrec.R
import com.playback.soundrec.databinding.ActivitySettingsBinding
import com.playback.soundrec.widget.SettingView

class SettingsActivity : AppCompatActivity(), SettingsActivityNav {
    companion object{
        fun getStartIntent(context: Context?): Intent? {
            val intent = Intent(context, SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            return intent
        }
    }

    private var sendSampleToServer: SwitchMaterial? = null
    private var formatSetting: SettingView? = null
    private var formats: Array<String>? = null
    private var formatsKeys: Array<String> ? = null
    private var delay:EditText?=null

    private var sampleRateSetting: SettingView? = null
    private var sampleRates: Array<String>?=null
    private var sampleRatesKeys: Array<String>?=null

    var binding : ActivitySettingsBinding? = null
    var viewModel : SettingActivityViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(SettingActivityViewModel::class.java)
        viewModel?.settingsActivityNav = this
        binding?.viewModel = viewModel
        binding?.lifecycleOwner = this

        setContentView(binding?.root)

        sendSampleToServer = binding?.sendSampleToServer
        sendSampleToServer?.setOnCheckedChangeListener { _, isChecked ->
          Pref.getInstance().setEnableSendDataToServer(isChecked)
        }
        formatSetting = binding?.settingRecordingFormat
         formats = resources.getStringArray(R.array.formats2)
         formatsKeys = arrayOf<String>(
            AppConstants.FORMAT_PCM,
            AppConstants.FORMAT_AAC
        )
        formatSetting!!.setData(formats, formatsKeys)
        formatSetting!!.setOnChipCheckListener { key, name, checked ->
            Pref.getInstance().setFormat(key)
//            presenter.setSettingRecordingFormat(
//                key
//            )
        }
        formatSetting?.setTitle(R.string.recording_format)
        formatSetting!!.setOnInfoClickListener { v ->
//            AndroidUtils.showInfoDialog(
//                this@SettingsActivity,
//                R.string.info_format
//            )
        }


        sampleRateSetting = binding?.settingFrequency
        sampleRates = resources.getStringArray(R.array.sample_rates2)
        sampleRatesKeys = arrayOf<String>(
            SettingsMapper.SAMPLE_RATE_8000,
            SettingsMapper.SAMPLE_RATE_16000,
            SettingsMapper.SAMPLE_RATE_22050,
            SettingsMapper.SAMPLE_RATE_32000,
            SettingsMapper.SAMPLE_RATE_44100,
            SettingsMapper.SAMPLE_RATE_48000
        )
        sampleRateSetting!!.setData(sampleRates, sampleRatesKeys)
        sampleRateSetting!!.setOnChipCheckListener { key, name, checked ->
            Pref.getInstance().setSampleRate(key)
//            presenter.setSettingSampleRate(
//                SettingsMapper.keyToSampleRate(key)
//            )
        }
        sampleRateSetting?.setTitle(R.string.sample_rate)
        sampleRateSetting!!.setOnInfoClickListener { v ->
//            AndroidUtils.showInfoDialog(
//                this@SettingsActivity,
//                R.string.info_frequency
//            )
        }
        delay = binding?.tvDelay
        delay?.addTextChangedListener { text ->
            Pref.getInstance().setDelay(text.toString().toInt()) }



        // fill default values
        val format = Pref.getInstance().getFormat()
        val sampleRate = Pref.getInstance().getSampleRate()
        val delay = Pref.getInstance().getDelay()
        formatSetting!!.setSelected(format)
        sampleRateSetting!!.setSelected(sampleRate)
        sendSampleToServer!!.isChecked = Pref.getInstance().getEnableSendDataToServer()
        this.delay?.setText(delay.toString())


    }

    override fun onBackClick(view: View) {
        finish()
    }
}