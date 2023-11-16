package com.playback.soundrec.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.playback.soundrec.Pref
import com.playback.soundrec.R
import com.playback.soundrec.bases.BaseActivity
import com.playback.soundrec.databinding.ActivityLoginBinding
import com.playback.soundrec.ui.main.MainActivity

class LoginActivity : BaseActivity(),LoginNav{
    var binding: ActivityLoginBinding? = null
    var viewModel: LoginActivityViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        Pref.getInstance().getUser()?.let {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(LoginActivityViewModel::class.java)
        viewModel?.nav = this
        binding?.viewModel = viewModel

        binding?.lifecycleOwner = this
        setContentView(binding?.root)
    }

    override fun toast(s: String) {
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show()
    }

    override fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}