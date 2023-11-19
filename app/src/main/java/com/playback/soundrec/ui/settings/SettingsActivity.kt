package com.playback.soundrec.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.playback.soundrec.AppConstants
import com.playback.soundrec.Pref
import com.playback.soundrec.R
import com.playback.soundrec.databinding.ActivitySettingsBinding
import com.playback.soundrec.providers.FireBaseService
import com.playback.soundrec.widget.SettingView


class SettingsActivity : AppCompatActivity(), SettingsActivityNav {
    companion object {
        fun getStartIntent(context: Context?): Intent? {
            val intent = Intent(context, SettingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            return intent
        }
    }

    private var sendSampleToServer: SwitchMaterial? = null
    private var formatSetting: SettingView? = null
    private var formats: Array<String>? = null
    private var formatsKeys: Array<String>? = null
    private var delay: LinearLayout? = null

    private var sampleRateSetting: SettingView? = null
    private var sampleRates: Array<String>? = null
    private var sampleRatesKeys: Array<String>? = null

    var binding: ActivitySettingsBinding? = null
    var viewModel: SettingActivityViewModel? = null

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
            Pref.getInstance().getUser()?.let { user ->
                FireBaseService.INSTANCE?.updateField(
                    "setting",
                    user?.user_id!!,
                    "defaultEnableSendDataToServer",
                    if (isChecked) "1" else "0"
                ) {
                    user.setting?.defaultEnableSendDataToServer = if (isChecked) "1" else "0"
                    Pref.getInstance().saveUser(user)
                }

            }
        }
        formatSetting = binding?.settingRecordingFormat
        formats = resources.getStringArray(R.array.formats2)
        formatsKeys = arrayOf<String>(
            AppConstants.FORMAT_PCM,
            AppConstants.FORMAT_AAC
        )
        formatSetting!!.setData(formats, formatsKeys)
        formatSetting!!.setOnChipCheckListener { key, name, checked ->
            Pref.getInstance().getUser()?.let { user ->

                FireBaseService.INSTANCE?.updateField(
                    "setting",
                    Pref.getInstance().getUser()?.user_id!!,
                    "defaultFormat",
                    key
                ) {
                    user.setting?.defaultFormat = key
                    Pref.getInstance().saveUser(user)
                }
            }

        }
        formatSetting?.setTitle(R.string.recording_format)



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
            Pref.getInstance().getUser()?.let { user ->
                FireBaseService.INSTANCE?.updateField(
                    "setting",
                    Pref.getInstance().getUser()?.user_id!!,
                    "defaultSampleRate",
                    key
                ) {
                    user.setting?.defaultSampleRate = key
                    Pref.getInstance().saveUser(user)
                }


            }
        }
        sampleRateSetting?.setTitle(R.string.sample_rate)
//        sampleRateSetting!!.setOnInfoClickListener { v ->
//            Pref?.getInstance()?.getUser()?.let {
//                it.setting?.defaultSampleRate = key.toInt()
//                Pref.getInstance().saveUser(it)
//            }
//            AndroidUtils.showInfoDialog(
//                this@SettingsActivity,
//                R.string.info_frequency
//            )
//        }
        delay = binding?.llDelay
        delay?.setOnClickListener { viewbt ->

            val container = layoutInflater.inflate(R.layout.view_et, null) as LinearLayout
            var view: View? = null

        //    val container = layoutInflater.inflate(R.layout.line, null)
            view = container?.findViewById<EditText>(R.id.et_content)
                ?.apply {
                    setText(binding?.tvDelay?.text.toString())
                    inputType = android.text.InputType.TYPE_CLASS_NUMBER
                }

            // val editText = EditText(this).apply { setText(currentValue) }
            AlertDialog.Builder(this, R.style.MyDialogTheme)
                .setTitle("Edit delay")
                .setView(container)
                .setPositiveButton("Update") { dialog, _ ->

                    val newValue = (view as EditText).text.toString()
                    Pref.getInstance().getUser()?.let { user ->
                        FireBaseService.INSTANCE?.updateField(
                            "setting",
                            Pref.getInstance().getUser()?.user_id!!,
                            "defaultDelay",
                            newValue
                        ) {
                            user.setting?.defaultDelay = newValue
                            Pref.getInstance().saveUser(user)
                            dialog.dismiss()

                        }

                    }


                }
                .setNegativeButton("Cancel", null)
                .show()

        }


        // fill default values
        val format = Pref.getInstance().getUser()?.setting?.defaultFormat
        val sampleRate = Pref.getInstance().getUser()?.setting?.defaultSampleRate.toString()
        val delay = Pref.getInstance().getUser()?.setting?.defaultDelay
        formatSetting!!.setSelected(format)
        sampleRateSetting!!.setSelected(sampleRate)
        sendSampleToServer!!.isChecked =
            Pref.getInstance().getUser()?.setting?.defaultEnableSendDataToServer?.toLong() == 1L
        // sendSampleToServer!!.isEnabled = false
        binding?.tvDelay?.setText(delay.toString())

        try {
            val pInfo: PackageInfo =
                getPackageManager().getPackageInfo(getPackageName(), 0)
            val version = pInfo.versionName
            binding?.txtversyion?.text = "Version: " + version

        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        binding?.btnChangePassword?.setOnClickListener {
            val container = layoutInflater.inflate(R.layout.view_et, null) as LinearLayout
            var view: EditText? = null

            //    val container = layoutInflater.inflate(R.layout.line, null)
            view = container?.findViewById<EditText>(R.id.et_content)
                ?.apply {
                    hint = "Enter new password"
                    inputType = android.text.InputType.TYPE_CLASS_NUMBER
                }

            // val editText = EditText(this).apply { setText(currentValue) }
            AlertDialog.Builder(this, R.style.MyDialogTheme)
                .setTitle("Edit delay")
                .setView(container)
                .setPositiveButton("Update") { dialog, _ ->

                    val newValue = (view as EditText).text.toString()
                    Pref.getInstance().getUser()?.let { user ->
                        FireBaseService.INSTANCE?.updateField(
                            null,
                            Pref.getInstance().getUser()?.user_id!!,
                            "password",
                            newValue
                        ) {
                            user?.password = newValue
                            Pref.getInstance().saveUser(user)
                            dialog.dismiss()

                        }

                    }


                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onBackClick(view: View) {
        finish()
    }
}