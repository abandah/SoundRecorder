package com.playback.soundrec.ui.userlist

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.playback.soundrec.R
import com.playback.soundrec.bases.BaseActivity
import com.playback.soundrec.databinding.ActivityUserListBinding
import com.playback.soundrec.model.User
import com.playback.soundrec.providers.FireBaseService
import com.playback.soundrec.ui.userdetails.UserDetailsActivity

class UserListActivity : BaseActivity() {
    private lateinit var binding: ActivityUserListBinding
    private var viewModel: UserListViewModel? = null
    private var adapter :UserAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(UserListViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setContentView(binding.root)

        adapter = UserAdapter() { user ->
            val intent = Intent(this, UserDetailsActivity::class.java)
            intent.putExtra("USER_ID", user.user_id)
            startActivity(intent)
        }
        binding!!.recyclerView.adapter = adapter
        viewModel!!.users.observe(this, Observer { users ->
           adapter!!.replaceItems(users)
        })
        binding!!.fabAddUser.setOnClickListener {
         //   viewModel!!.fetchUsers()
            showCreateUserDialog()
        }
        binding!!.btnBack.setOnClickListener {
            finish()
        }
        viewModel!!.fetchUsers()
    }

    private fun showCreateUserDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_user, null)
        val usernameEditText = dialogView.findViewById<EditText>(R.id.editTextUsername)
        val passwordEditText = dialogView.findViewById<EditText>(R.id.editTextPassword)
        usernameEditText.setText( "m3bndah@gmail.com")
        passwordEditText.setText( "123456789")

        AlertDialog.Builder(this, R.style.MyDialogTheme)
            .setView(dialogView)
            .setTitle("Create New User")
            .setPositiveButton("Create") { dialog, _ ->
                val username = usernameEditText.text.toString()
                val password = passwordEditText.text.toString()
                createUser(username, password)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun createUser(username: String, password: String) {
        val newUser = User().apply {
            this.userName = username
            this.password = password
            // Set other necessary fields
        }
        FireBaseService.INSTANCE!!.createUser(newUser) { userId ->
            if (userId != null) {
                // User created successfully
            } else {
                // User creation failed
            }
        }
    }
}
