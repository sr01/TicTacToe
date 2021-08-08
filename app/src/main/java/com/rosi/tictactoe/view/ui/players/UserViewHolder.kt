package com.rosi.tictactoe.view.ui.players

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rosi.tictactoe.R
import com.rosi.tictactoe.model.connect.User
import com.rosi.tictactoe.model.connect.UserConnectionState
import com.rosi.tictactoe.utils.ViewHolderListener

class UserViewHolder(view: View, val listener: ViewHolderListener<User>) : RecyclerView.ViewHolder(view) {

    private var connectButton: Button = view.findViewById(R.id.button_connect)
    private var imageView: ImageView = view.findViewById(R.id.imageview_user)
    private var txtFriendName: TextView = view.findViewById(R.id.text_user_name)
    private var txtFriendDescription: TextView = view.findViewById(R.id.text_user_description)
    private var user: User? = null

    init {
        connectButton.setOnClickListener {
            user?.let { user ->
                listener.onViewHolderClick(user)
            }
        }
    }

    fun bind(user: User) {
        txtFriendName.text = user.name
        txtFriendDescription.text = user.address

        connectButton.text = when (user.connectionState) {
            UserConnectionState.Disconnected -> "Connect"
            UserConnectionState.Connecting -> "Connecting..."
            UserConnectionState.AwaitForApproval -> "Pending"
            UserConnectionState.Connected -> "Connected"
            UserConnectionState.FailedToConnect -> "Failed"
        }
        this.user = user
    }

    fun recycle() {
    }
}