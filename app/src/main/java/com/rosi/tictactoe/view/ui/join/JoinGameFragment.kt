package com.rosi.tictactoe.view.ui.join

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
import com.rosi.tictactoe.R
import com.rosi.tictactoe.base.di.JoinGameViewModelFactory
import com.rosi.tictactoe.base.di.observe
import com.rosi.tictactoe.databinding.FragmentJoinGameBinding
import com.rosi.tictactoe.view.GameUIEvent
import com.rosi.tictactoe.view.ui.main.MainActivity
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class JoinGameFragment : Fragment() {

    private lateinit var viewBinding: FragmentJoinGameBinding
    private val viewModel: JoinGameViewModel by viewModels {
        JoinGameViewModelFactory(requireContext())
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
        viewBinding = FragmentJoinGameBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.buttonFirst.setOnClickListener {
            val playerName = viewBinding.textName.text.toString()
            viewModel.joinInTheGame(playerName)
        }

        viewModel.run {
            observe(playerName) { playerName -> viewBinding.textName.setText(playerName) }
        }
    }

    private fun onViewNotification(notification: GameUIEvent) {
        when (notification) {
            is GameUIEvent.NavigateToPlayersUIEvent -> findNavController().navigate(R.id.action_JoinGameFragment_to_PlayersFragment)
            is GameUIEvent.InvalidPlayerNameUIEvent -> Toast.makeText(requireContext(), "Invalid player name", Toast.LENGTH_SHORT).show()
            is GameUIEvent.IncomingCallUIEvent -> (requireActivity() as MainActivity).showIncomingCallNotification(notification.user)
            is GameUIEvent.ConnectionAcceptedUIEvent -> findNavController().navigate(R.id.action_navigation_join_game_fragment_to_navigation_board_fragment)
        }
    }
}
