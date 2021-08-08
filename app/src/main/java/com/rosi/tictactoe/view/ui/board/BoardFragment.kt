package com.rosi.tictactoe.view.ui.board

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.Snackbar
import com.rosi.tictactoe.R
import com.rosi.tictactoe.base.di.BoardViewModelFactory
import com.rosi.tictactoe.databinding.BoardBinding
import com.rosi.tictactoe.databinding.FragmentBoardBinding
import com.rosi.tictactoe.model.game.*
import com.rosi.tictactoe.view.GameUIEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BoardFragment : Fragment() {

    private lateinit var viewBinding: FragmentBoardBinding
    private lateinit var boardBinding: BoardBinding
    private lateinit var cellsViews: Array<Array<ImageView>>
    private val viewModel: BoardViewModel by viewModels {
        BoardViewModelFactory(requireContext())
    }
    private var exitSnackbar: Snackbar? = null
    private val gameEndBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            exitSnackbar?.dismiss()
            findNavController().navigateUp()
        }
    }
    private val gameActiveBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val thisCallback: OnBackPressedCallback = this

            MaterialDialog(requireContext()).show {
                message(res = R.string.exit_game_message)
                cancelOnTouchOutside(false)
                cancelable(false)
                positiveButton(res = R.string.exit_game_positive_action) {
                    viewModel.exitGame()
                    thisCallback.isEnabled = false
                    findNavController().navigateUp()
                }
                negativeButton(res = R.string.exit_game_negative_action) {

                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            viewModel.notifications
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect(::onViewNotification)
        }

        //NOTE: S.R - notes about Chain of Responsibility pattern: https://developer.android.com/guide/navigation/navigation-custom-back
        requireActivity().onBackPressedDispatcher.addCallback(gameEndBackPressedCallback)
        requireActivity().onBackPressedDispatcher.addCallback(gameActiveBackPressedCallback)
    }

    private fun onViewNotification(event: GameUIEvent) {
        when (event) {
            is GameUIEvent.GameStatusUIEvent -> {
                updateBoard(event.state.board)
                updateMyPlayer(event.state.myPlayer)
                updateOtherPlayer(event.state.otherPlayer)
                updateCurrentPlayer(event.state.currentPlayer)
                updateWin(event.state.win)
            }
            is GameUIEvent.DisconnectedUIEvent -> {
                gameActiveBackPressedCallback.isEnabled = false
                exitSnackbar = Snackbar.make(viewBinding.root, resources.getString(R.string.player_has_left_the_game, event.user.name), Snackbar.LENGTH_INDEFINITE).apply {
                    setAction(R.string.exit_game_action) { findNavController().navigateUp() }
                    show()
                }
            }
            is GameUIEvent.PlayAgainRequestUIEvent -> {
                MaterialDialog(requireContext()).show {
                    message(text = getString(R.string.player_request_to_play_again, event.requestBy.name))
                    cancelOnTouchOutside(false)
                    cancelable(false)
                    positiveButton(res = R.string.accept_play_again_request_action) {
                        viewModel.acceptPlayAgain()
                    }
                    negativeButton(res = R.string.decline_play_again_request_action) {
                        viewModel.declinePlayAgain()
                    }
                }
            }
            is GameUIEvent.PlayAgainResponseUIEvent -> {
                val textResId = if (event.isAccepted) R.string.accept_play_again_request_action else R.string.decline_play_again_request_action
                Toast.makeText(requireContext(), "${event.answeredBy.name} ${getString(textResId)}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentBoardBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        boardBinding = viewBinding.board

        boardBinding.imageView00.setOnClickListener { onCellClick(0, 0) }
        boardBinding.imageView01.setOnClickListener { onCellClick(1, 0) }
        boardBinding.imageView02.setOnClickListener { onCellClick(2, 0) }

        boardBinding.imageView10.setOnClickListener { onCellClick(0, 1) }
        boardBinding.imageView11.setOnClickListener { onCellClick(1, 1) }
        boardBinding.imageView12.setOnClickListener { onCellClick(2, 1) }

        boardBinding.imageView20.setOnClickListener { onCellClick(0, 2) }
        boardBinding.imageView21.setOnClickListener { onCellClick(1, 2) }
        boardBinding.imageView22.setOnClickListener { onCellClick(2, 2) }


        cellsViews = arrayOf(
            arrayOf(
                boardBinding.imageView00,
                boardBinding.imageView01,
                boardBinding.imageView02
            ),
            arrayOf(
                boardBinding.imageView10,
                boardBinding.imageView11,
                boardBinding.imageView12
            ),
            arrayOf(
                boardBinding.imageView20,
                boardBinding.imageView21,
                boardBinding.imageView22
            )
        )

        cellsViews.flatten().forEach { it.drawable.level = 0 }

        viewBinding.buttonPlayAgain.setOnClickListener {
            viewModel.playAgain()
        }
    }

    private fun getPlayerDrawableByPlayerToken(token: PlayerToken?) = when (token) {
        PlayerToken.X -> R.drawable.ic_token_x
        PlayerToken.O -> R.drawable.ic_token_o
        null, PlayerToken.None -> R.drawable.ic_empty_cell
    }

    private fun onCellClick(x: Int, y: Int) {
        viewModel.makeGameMove(x, y)
    }


    private fun updateCurrentPlayer(currentPlayer: Player) {
        viewBinding.textGameStatus.text = getString(R.string.player_make_your_move, currentPlayer.name)
    }

    private fun updateOtherPlayer(player: Player?) {
        viewBinding.textOtherPlayer.text = player?.name
        val resId = getPlayerDrawableByPlayerToken(player?.token)
        viewBinding.imageOtherToken.setImageDrawable(ResourcesCompat.getDrawable(resources, resId, null))
    }

    private fun updateMyPlayer(player: Player?) {
        viewBinding.textMyPlayer.text = player?.name
        val resId = getPlayerDrawableByPlayerToken(player?.token)
        viewBinding.imageMyToken.setImageDrawable(ResourcesCompat.getDrawable(resources, resId, null))
    }

    private fun updateWin(win: Win?) {

        when (win) {
            is Win.My -> {
                viewBinding.textEndGameMessage.text = resources.getString(R.string.game_end_message_my_player_is_winner)
                viewBinding.textGameStatus.text = ""
                viewBinding.endGameView.visibility = View.VISIBLE
            }
            is Win.Other -> {
                viewBinding.textEndGameMessage.text = resources.getString(R.string.game_end_message_other_player_is_winner, win.player.name)
                viewBinding.textGameStatus.text = ""
                viewBinding.endGameView.visibility = View.VISIBLE
            }
            is Win.Draw -> {
                viewBinding.textEndGameMessage.text = resources.getString(R.string.game_end_message_draw)
                viewBinding.textGameStatus.text = ""
                viewBinding.endGameView.visibility = View.VISIBLE
            }
            else -> {
                viewBinding.endGameView.visibility = View.INVISIBLE
            }
        }
    }

    private fun updateBoard(board: List<List<Cell>>) {

        board.forEachCell { y, x, cell ->
            val level = when (cell.player.token) {
                PlayerToken.X -> 1
                PlayerToken.O -> 2
                PlayerToken.None -> 0
            }

            cellsViews[y][x].drawable.level = level
        }
    }
}

