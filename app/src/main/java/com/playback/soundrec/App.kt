package com.playback.soundrec

import android.app.Application
import android.content.Context
import android.os.Handler

class App : Application()    {

    companion object{
        var context: Context? = null
    }



    override fun onCreate() {
        super.onCreate()
        context = applicationContext


    }
}