package com.rosi.tictactoe.view.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.rosi.tictactoe.R
import com.rosi.tictactoe.base.di.MainViewModelFactory
import com.rosi.tictactoe.databinding.ActivityMainBinding
import com.rosi.tictactoe.model.connect.User

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewBinding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        setSupportActionBar(viewBinding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // multiple top-level destinations, see: https://stackoverflow.com/a/57169105
//        appBarConfiguration = AppBarConfiguration(navController.graph)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_join_game_fragment, R.id.navigation_players_fragment, R.id.navigation_board_fragment))
        setupActionBarWithNavController(navController, appBarConfiguration)

        viewModel.appStart()
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.appStop()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()

        viewModel.onResume()
    }

    override fun onPause() {
        super.onPause()

        viewModel.onPause()
    }

    fun showIncomingCallNotification(user: User) {
        MaterialDialog(this).show {
            message(text = "Incoming connection from ${user.name}")
            cancelOnTouchOutside(true)
            cancelable(true)
            setOnCancelListener {
                viewModel.rejectCall(user)
            }
            positiveButton(text = "Accept") {
                viewModel.acceptCall(user)
            }
            negativeButton(text = "Reject") {
                viewModel.rejectCall(user)
            }
        }
    }
}