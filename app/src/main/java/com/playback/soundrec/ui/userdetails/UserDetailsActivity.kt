package com.playback.soundrec.ui.userdetails

import android.media.MediaPlayer
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.switchmaterial.SwitchMaterial
import com.playback.soundrec.AppConstants
import com.playback.soundrec.R
import com.playback.soundrec.bases.BaseActivity
import com.playback.soundrec.databinding.ActivityUserDetailsBinding
import com.playback.soundrec.model.EditViews
import com.playback.soundrec.model.User
import com.playback.soundrec.providers.FireBaseService
import com.playback.soundrec.ui.settings.SettingsMapper
import com.playback.soundrec.widget.SettingView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class UserDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityUserDetailsBinding
    private lateinit var userId: String
    private lateinit var firebaseService: FireBaseService
    private val backgroundScope = CoroutineScope(Dispatchers.IO)
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        binding!!.lifecycleOwner = this
        setContentView(binding.root)

        firebaseService = FireBaseService.INSTANCE!!
        userId = intent.getStringExtra("USER_ID") ?: return

        fetchUserDetails(userId)

    }

    private fun fetchUserDetails(userId: String) {
        firebaseService.getUserInfo(userId) { user ->
            user?.let {
                updateUI(it)
            }
        }
    }

    private fun updateUI(user: User) {
        binding?.btnBack?.setOnClickListener {
            finish()
        }
        addView("Email",null, "userName", user.userName, EditViews.String)
        addView("Name",null, "name", user.name, EditViews.String)
        addView("Phone number",null, "phonenumber", user.phonenumber, EditViews.String)
        addView("Password",null, "password", user.password, EditViews.String)
        addView("Sample Rate",
            "setting",
            "defaultSampleRate",
            user.setting?.defaultSampleRate.toString(),
            EditViews.MULTIABLE,
            arrayOf<String>(
                SettingsMapper.SAMPLE_RATE_8000,
                SettingsMapper.SAMPLE_RATE_16000,
                SettingsMapper.SAMPLE_RATE_22050,
                SettingsMapper.SAMPLE_RATE_32000,
                SettingsMapper.SAMPLE_RATE_44100,
                SettingsMapper.SAMPLE_RATE_48000
            ), resources.getStringArray(R.array.sample_rates2)
        )
//        addView("Format",
//            "setting",
//            "defaultFormat",
//            user.setting?.defaultFormat,
//            EditViews.MULTIABLE,
//            arrayOf<String>(
//                AppConstants.FORMAT_PCM,
//                AppConstants.FORMAT_AAC
//            ),
//            resources.getStringArray(R.array.formats2)
//        )
        addView("Send data to server",
            "setting",
            "defaultEnableSendDataToServer",
            user.setting?.defaultEnableSendDataToServer.toString(),
            EditViews.Boolean
        )
        addView("Delay","setting", "defaultDelay", user.setting?.defaultDelay.toString(), EditViews.Int)
        addView("Start Sample From ",
            "setting",
            "defaultTimeToStartSoundSample",
            user.setting?.defaultTimeToStartSoundSample.toString(),
            EditViews.Int
        )
        addView("Sample Duration",
            "setting",
            "defaultSoundSampleDuration",
            user.setting?.defaultSoundSampleDuration.toString(),
            EditViews.Int
        )
        mediaPlayer = MediaPlayer()
        binding.btnPlaySample?.visibility = View.INVISIBLE
        binding.tvSoundSample?.visibility = View.INVISIBLE
        backgroundScope.launch {
            FireBaseService.INSTANCE?.getFile(user.user_id!!) { url ->
                runOnUiThread {
                    if(url == null) {
                        return@runOnUiThread
                    }
                    mediaPlayer?.setDataSource(url.path)
                    mediaPlayer?.setOnPreparedListener(){
                        binding.btnPlaySample?.visibility = View.VISIBLE
                        binding.tvSoundSample?.visibility = View.VISIBLE
                    }
                    mediaPlayer?.prepare()
                    binding.btnPlaySample?.setOnTouchListener() { v, event ->
                        when (event?.action) {
                            MotionEvent.ACTION_DOWN -> {
                                mediaPlayer?.start()
                            }
                            MotionEvent.ACTION_UP -> {
                                mediaPlayer?.pause()
                                mediaPlayer?.seekTo(0)
                            }
                        }
                        true
                    }



                }
            }
        }


    }

    fun addView(
        title:String,
        parent: String?,
        key: String,
        value: String?,
        type: EditViews,
        options: Array<String>? = null,
        options_key: Array<String>? = null
    ) {
        val item = layoutInflater.inflate(com.playback.soundrec.R.layout.item_userinfo, null)
        val value_tv = item.findViewById<TextView>(R.id.value)
        val key_tv = item.findViewById<TextView>(R.id.key)

        key_tv?.setText(title)
        val edit_button = item.findViewById<ImageButton>(R.id.edit)
        value_tv?.setText(value)
        if (value.isNullOrBlank()) {
            value_tv?.setText("Not Set")
        }
        edit_button.setOnClickListener {
            showEditDialog(title,key, value_tv?.text.toString(), type, options, options_key) { newValue ->
                updateUserField(parent, key, newValue) {
                    value_tv?.setText(newValue)
                }
            }
        }
        if (parent == null) {
            binding!!.llUserDetails.addView(item)
        } else {
            binding!!.llSettings.addView(item)

        }
    }

    private fun showEditDialog(
        title:String,
        field: String,
        currentValue: String,
        type: EditViews,
        options: Array<String>?,
        options_key: Array<String>?,
        updateCallback: (String) -> Unit
    ) {


        var container :LinearLayout ? = layoutInflater.inflate(R.layout.view_et, null) as LinearLayout

        var view: View? = null
        when (type) {
            EditViews.String -> {
                container = layoutInflater.inflate(R.layout.view_et, null) as LinearLayout
                view = container?.findViewById<EditText>(R.id.et_content)?.apply { setText(currentValue) }
            }

            EditViews.Int -> {
                container = layoutInflater.inflate(R.layout.view_et, null) as LinearLayout

                view = container?.findViewById<EditText>(R.id.et_content)?.apply {
                        setText(currentValue)
                        inputType = android.text.InputType.TYPE_CLASS_NUMBER
                    }
            }

            EditViews.Boolean -> {
                container = layoutInflater.inflate(R.layout.view_switch, null) as LinearLayout

                val tv = container?.findViewById<TextView>(R.id.tx_switch)?.apply {
                    setText(title)
                }
                view = container?.findViewById<SwitchMaterial>(R.id.et_content)?.apply {
                        isChecked = currentValue.toBoolean()
                    }
            }

            EditViews.MULTIABLE -> {
                container = layoutInflater.inflate(R.layout.view_multi, null) as LinearLayout

                view = container?.findViewById<SettingView>(R.id.et_content)?.apply {
                        setData(options_key, options)
                        selected = currentValue

                    }
            }
        }
        // val editText = EditText(this).apply { setText(currentValue) }
        AlertDialog.Builder(this, R.style.MyDialogTheme)
            .setTitle("Edit $field")
            .setView(container)
            .setPositiveButton("Update") { dialog, _ ->
                when (type) {
                    EditViews.String -> {
                        val newValue = (view as EditText).text.toString()
                        updateCallback(newValue)
                        dialog.dismiss()
                    }

                    EditViews.Int -> {
                        val newValue = (view as EditText).text.toString()
                        updateCallback(newValue)
                        dialog.dismiss()
                    }

                    EditViews.Boolean -> {
                        val newValue = (view as SwitchMaterial).isChecked.toString()
                        updateCallback(newValue)
                        dialog.dismiss()
                    }

                    EditViews.MULTIABLE -> {
                        val newValue = (view as SettingView).selected
                        updateCallback(newValue)
                        dialog.dismiss()
                    }
                }
//                val newValue = editText.text.toString()
//                updateCallback(newValue)
//                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateUserField(
        parent: String?,
        field: String,
        newValue: String,
        updateCallback: (String) -> Unit
    ) {
        firebaseService.updateField(parent, userId, field, newValue) { isSuccess ->

            updateCallback(newValue)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun onBackClick(view: View) {}
}
