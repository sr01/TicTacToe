package com.rosi.tictactoe.view.ui.players

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rosi.tictactoe.R
import com.rosi.tictactoe.model.connect.User
import com.rosi.tictactoe.utils.ViewHolderListener

class UsersAdapter(private val viewHolderListener: ViewHolderListener<User>) :
    RecyclerView.Adapter<UserViewHolder>() {

    private val users = mutableListOf<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view, viewHolderListener)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = users.size

    override fun onViewRecycled(holder: UserViewHolder) {
        super.onViewRecycled(holder)
        holder.recycle()
    }

    fun add(user: User) {
        users.add(user)
        notifyItemInserted(users.size - 1)
    }

    fun remove(user: User) {
        val index = users.indexOf(user)
        if (index > -1) {
            users.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun replaceAll(users: List<User>) {
        this.users.clear()
        this.users.addAll(users)
        notifyDataSetChanged()
    }

    fun clear() {
        users.clear()
        notifyDataSetChanged()
    }

    fun update(user: User) {
        val index = this.users.indexOf(user)
        if (index > -1) {
            this.users.removeAt(index)
            this.users.add(index, user)
            notifyItemChanged(index)
        } else {
            this.add(user)
        }
    }
}
