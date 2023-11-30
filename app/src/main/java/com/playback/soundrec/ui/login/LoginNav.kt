package com.playback.soundrec.ui.login

interface LoginNav {
   fun  toast(s: String)
    fun openMainActivity()
    fun showLoading()
    fun hideLoading()
}