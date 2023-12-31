package com.playback.soundrec.providers

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.playback.soundrec.App
import com.playback.soundrec.model.User
import java.io.File

class FireBaseService :CallsAPi {
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var auth: FirebaseAuth = Firebase.auth
    private var storage = FirebaseStorage.getInstance().reference

    private var userRef : DatabaseReference? = null
  //  private var storageRef: StorageReference? = null
    companion object {
        var INSTANCE: FireBaseService? = null
            get() {
                if (field == null) {
                    field = FireBaseService()
                }
                return field
            }
    }

    init {
        // init firebase
        userRef = database.getReference().child("users")
        //storageRef = storage
//        auth!!.signInAnonymously().addOnCompleteListener {
//            if (it.isSuccessful) {
//                //val user = auth!!.currentUser
//            }
//        }
    }

    override fun login(email: String, password: String, callback: (User?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                userId?.let {
                    getUserInfo(it) { user ->
                        callback(user)
                    }
                } ?: callback(null)
            } else {
                callback(null)
            }
        }
    }

    override fun getUserInfo(userId: String, callback: (User?) -> Unit) {
        userRef?.child(userId)
            ?.get()?.addOnSuccessListener { dataSnapshot ->
                val user = dataSnapshot.getValue(User::class.java)
                callback(user)
            }
            ?.addOnFailureListener {
                callback(null)
            }
    }

    override fun uploadFile(userId: String, file: File, callback: (Boolean) -> Unit) {
        // refrence to the node of user

        val audioFileRef = storage.child("audio/$userId/sample.wav")
        audioFileRef.delete().addOnCompleteListener(){
            val fileUri = Uri.fromFile(file)

            audioFileRef.putFile(fileUri).addOnSuccessListener {
                updateSoundSample(userId, it.metadata?.path.toString()) { success ->
                    callback(success)
                }
            }.addOnFailureListener {
                Log.e("TAGTAG", "uploadFile: ", it)
                callback(false)
            }
        }

    }

    override fun createUser(user: User, callback: (String?) -> Unit) {
        user.setting = User.Setting()
        auth.createUserWithEmailAndPassword(user.userName!!, user.password!!).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                user.user_id = userId

                userRef?.child(userId!!)?.setValue(user)
                    ?.addOnSuccessListener {
                        callback(userId)
                    }
                    ?.addOnFailureListener {
                        callback(null)
                    }
            } else {
                callback(null)
            }
        }
    }

    override fun getFile(userId: String, callback: (File?) -> Unit) {
        // Create a reference to the file you want to download
        val audioFileRef = storage.child("audio/$userId/sample.wav")

        // Create a local file where the downloaded file will be stored
        val localFile = File.createTempFile("sample", ".aac", App.Companion.context?.cacheDir)

        // Download the file to the local file
        audioFileRef?.getFile(localFile)
            ?.addOnSuccessListener {
                // File downloaded successfully
               callback.invoke(localFile)
            }
           ?.addOnFailureListener {
                // Handle unsuccessful downloads
                callback.invoke(null)
            }
    }


    override fun updatePassword(userId: String, password: String, callback: (Boolean) -> Unit) {
        // Reference to the specific user by userId
        val userRef = userRef!!.child(userId)

        // Update the password field of the user
        userRef.child("password").setValue(password)
            .addOnSuccessListener {
                // Password update successful
                callback(true)
            }
            .addOnFailureListener {
                // Password update failed
                callback(false)
            }
    }

    override fun updateSoundSample(userId: String, mediaUrl: String, callback: (Boolean) -> Unit) {
        // Reference to the specific user by userId
        val userRef = userRef!!.child(userId)

        // Update the password field of the user
        userRef.child("soundSample").setValue(mediaUrl)
            .addOnSuccessListener {
                // Password update successful
                callback(true)
            }
            .addOnFailureListener {
                // Password update failed
                callback(false)
            }
    }
//    override fun updateSettingSampleRate(userId: String, sampleRate: String, callback: (Boolean) -> Unit) {
//        updateSettingField(userId, "setting/defaultSampleRate", sampleRate, callback)
//    }
//
//    override fun updateSettingFormat(userId: String, format: String, callback: (Boolean) -> Unit) {
//        updateSettingField(userId, "setting/defaultFormat", format, callback)
//    }
//
//    override fun updateSettingEnableSendDataToServer(userId: String, enableSendDataToServer: Boolean, callback: (Boolean) -> Unit) {
//        updateSettingField(userId, "setting/defaultEnableSendDataToServer", enableSendDataToServer, callback)
//    }
//
//    override fun updateSettingDelay(userId: String, delay: String, callback: (Boolean) -> Unit) {
//        updateSettingField(userId, "setting/defaultDelay", delay, callback)
//    }
//
//    override fun updateSettingTimeToStartSoundSample(userId: String, timeToStartSoundSample: String, callback: (Boolean) -> Unit) {
//        updateSettingField(userId, "setting/defaultTimeToStartSoundSample", timeToStartSoundSample, callback)
//    }
//
//    override fun updateSettingSoundSampleDuration(userId: String, soundSampleDuration: String, callback: (Boolean) -> Unit) {
//        updateSettingField(userId, "setting/defaultSoundSampleDuration", soundSampleDuration, callback)
//    }
//
    override fun getAllUsers(callback: (List<User>?) -> Unit) {
       userRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
                callback(users)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })

    }

    override fun updateField(
        parent: String?,
        userId: String,
        field: String,
        value: String,
        callback: (String) -> Unit
    ) {
        var ref = userRef!!.child(userId)
        if(parent!=null) {
            ref = ref.child(parent)
        }
        ref.child(field).setValue(value)
            .addOnSuccessListener {
                // Update successful
                callback(value.toString())
            }
            .addOnFailureListener {
                // Update failed
                callback(value.toString())
            }

    }

    override fun deleteFile(userId: String, callback: (Boolean) -> Unit) {
        // Create a reference to the file to delete
        val audioFileRef = storage.child("audio/$userId/sample.wav")

        // Delete the file
        audioFileRef.delete().addOnSuccessListener {
            // File deleted successfully
            callback(true)
        }.addOnFailureListener {
            // Uh-oh, an error occurred!
            callback(false)
        }
    }

//    override fun updateUserField(
//        userId: String,
//        field: String,
//        value: Any,
//        callback: (String) -> Unit
//    ) {
//        val userRef = userRef!!.child(userId)
//
//        // Update the specific field of the user's settings
//        userRef.child(field).setValue(value)
//            .addOnSuccessListener {
//                // Update successful
//                callback(value.toString())
//            }
//            .addOnFailureListener {
//                // Update failed
//                callback(value.toString())
//            }
//    }
//
//    private fun updateSettingField(userId: String, fieldPath: String, newValue: Any, callback: (Boolean) -> Unit) {
//        val userRef = userRef!!.child(userId).child("setting")
//
//        // Update the specific field of the user's settings
//        userRef.child(fieldPath).setValue(newValue)
//            .addOnSuccessListener {
//                // Update successful
//                callback(true)
//            }
//            .addOnFailureListener {
//                // Update failed
//                callback(false)
//            }
//    }


}