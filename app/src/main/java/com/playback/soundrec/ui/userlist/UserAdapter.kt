package com.playback.soundrec.ui.userlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.playback.soundrec.R
import com.playback.soundrec.model.User

class UserAdapter(private val itemClickListener: (User) -> Unit) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    private val users: ArrayList<User> =ArrayList<User>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.itemView.findViewById<TextView>(R.id.textViewUserName).text = user.userName
        holder.itemView.findViewById<ImageView>(R.id.imageView2).setOnClickListener { itemClickListener(user) }
    }

    override fun getItemCount() = users.size
    fun replaceItems(users: List<User>?) {
        users?.let {
            this.users.clear()
            this.users.addAll(users)
        }
        notifyDataSetChanged()

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}