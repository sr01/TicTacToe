package com.rosi.tictactoe.view.ui.players

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rosi.tictactoe.R
import com.rosi.tictactoe.base.di.PlayersViewModelFactory
import com.rosi.tictactoe.databinding.FragmentPlayersBinding
import com.rosi.tictactoe.model.connect.User
import com.rosi.tictactoe.utils.ViewHolderListener
import com.rosi.tictactoe.view.GameUIEvent
import com.rosi.tictactoe.view.ui.main.MainActivity
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PlayersFragment : Fragment(), ViewHolderListener<User> {

    private lateinit var usersAdapter: UsersAdapter
    private lateinit var viewBinding: FragmentPlayersBinding
    private val viewModel: PlayersViewModel by viewModels {
        PlayersViewModelFactory(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            viewModel.notifications
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect(::onViewNotification)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentPlayersBinding.inflate(inflater, container, false)
        return viewBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usersAdapter = UsersAdapter(this)
        viewBinding.recycler.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.recycler.adapter = usersAdapter

        viewModel.onViewCreated()

        showNoFriends()
    }

    private fun onViewNotification(event: GameUIEvent) {
        when (event) {
            is GameUIEvent.UserAvailableUIEvent -> usersAdapter.add(event.user)
            is GameUIEvent.UserUnavailableUIEvent -> usersAdapter.remove(event.user)
            is GameUIEvent.ConnectingUIEvent -> usersAdapter.update(event.user)
            is GameUIEvent.ConnectedUIEvent -> usersAdapter.update(event.user)
            is GameUIEvent.DisconnectedUIEvent -> usersAdapter.update(event.user)
            is GameUIEvent.FailedToConnectUIEvent -> usersAdapter.update(event.user)
            is GameUIEvent.IncomingCallUIEvent -> {
                usersAdapter.update(event.user)
                (requireActivity() as MainActivity).showIncomingCallNotification(event.user)
            }
            is GameUIEvent.ConnectionRejectedUIEvent -> showConnectionRejectedNotification(event.user)
            is GameUIEvent.ConnectionAcceptedUIEvent -> findNavController().navigate(R.id.action_navigation_players_fragment_to_boardFragment)
        }
    }

    private fun showConnectionRejectedNotification(user: User) {
        usersAdapter.update(user)
        Toast.makeText(requireContext(), "connection rejected", Toast.LENGTH_SHORT).show()
    }

    private fun showNoFriends() {
        usersAdapter.clear()
        viewBinding.progressBar.visibility = View.VISIBLE
        viewBinding.scanMessageTextView.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        viewModel.onViewDestroyed()
        super.onDestroyView()
    }

    override fun onViewHolderClick(item: User) {
        viewModel.connectToUser(item)
    }

    override fun onViewHolderLongClick(item: User): Boolean {
        return false
    }
}

