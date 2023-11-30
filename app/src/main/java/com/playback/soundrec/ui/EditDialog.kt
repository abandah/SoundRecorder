package com.playback.soundrec.ui

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.switchmaterial.SwitchMaterial
import com.playback.soundrec.R
import com.playback.soundrec.widget.SettingView

class EditDialogBuilder(context: Activity) : AlertDialog.Builder(context, R.style.MyDialogTheme) {
    val customLayout: View? = null

    init {
        val customLayout: View = context.layoutInflater.inflate(R.layout.dialog_edit, null)
        setView(customLayout)
    }

    override fun setTitle(title: CharSequence?): AlertDialog.Builder {
        customLayout?.findViewById<TextView>(R.id.dialog_title)?.text = title
        return this

    }

    fun setSwitch(key: String, value: Boolean, icon: Int, pos: (Boolean) -> Unit, neg: () -> Unit): AlertDialog {

        val container = customLayout?.findViewById<LinearLayout>(R.id.ll_switch)
        val tv = customLayout?.findViewById<TextView>(R.id.tv_switch)
        val switch = customLayout?.findViewById<SwitchMaterial>(R.id.sw_switch)
        tv?.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
        container?.visibility = View.VISIBLE
        tv?.text = key
        switch?.isChecked = value


        val buttonPos = customLayout?.findViewById<TextView>(R.id.dialog_details_btn)
        buttonPos?.setOnClickListener {
            val value = switch?.isChecked
            pos(value!!)
        }

        val buttonNig = customLayout?.findViewById<TextView>(R.id.dialog_ok_btn)
        buttonNig?.setOnClickListener {
            neg()

        }
        return this.create()
    }

    fun setTextView(key: String, value: String, icon: Int = 0 ,pos: (String) -> Unit, neg: () -> Unit): AlertDialog {

        val container = customLayout?.findViewById<LinearLayout>(R.id.ll_edittext)
        val tv = customLayout?.findViewById<TextView>(R.id.tv_delay)
        val et = customLayout?.findViewById<SwitchMaterial>(R.id.ed_delay)

        tv?.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
        container?.visibility = View.VISIBLE
        tv?.text = key
        et?.setText(value)
        val buttonPos = customLayout?.findViewById<TextView>(R.id.dialog_details_btn)

        buttonPos?.setOnClickListener {
            val value = et?.text.toString()
            pos(value)
        }

        val buttonNig = customLayout?.findViewById<TextView>(R.id.dialog_ok_btn)
        buttonNig?.setOnClickListener {
            neg()

        }
        return this.create()
    }

    fun setSingleSelect(names : Array<String>? , keys: Array<String>?, currentValue: String ,pos: (String) -> Unit, neg: () -> Unit): EditDialogBuilder{
        val setting :SettingView = customLayout?.findViewById<SettingView>(R.id.setting_recording_format)!!
        setting.apply {
            setData(names, keys)
            selected = currentValue
            visibility = View.VISIBLE

        }
        val buttonPos = customLayout?.findViewById<TextView>(R.id.dialog_details_btn)
        buttonPos?.setOnClickListener {
            val value = (setting as SettingView).selected
            pos(value)
        }

        val buttonNig = customLayout?.findViewById<TextView>(R.id.dialog_ok_btn)
        buttonNig?.setOnClickListener {
            neg()

        }
        return this
    }


}