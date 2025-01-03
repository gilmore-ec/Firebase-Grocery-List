package com.example.grocerylist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.*
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.google.firebase.auth.FirebaseAuth

class TopNavBar : Fragment(R.layout.fragment_top_nav_bar) {
    interface OnTopNavBarActionListener {
        /**
         * Description: Logs out the current [FirebaseAuth] user out and restarts the [LoginActivity]
         */
        fun signout()
    }
    private var listener: OnTopNavBarActionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        listener = context as OnTopNavBarActionListener
    }

    @Override
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //return super.onCreateView(inflater, container, savedInstanceState)
        val view: View = inflater.inflate(R.layout.fragment_top_nav_bar, container, false)
        val appBar: Toolbar = view.findViewById(R.id.app_bar)

        (requireActivity() as AppCompatActivity).setSupportActionBar(appBar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "TopNavBar Fragment"

        // Use MenuProvider to manage menu items
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Inflate the menu items
                menuInflater.inflate(R.menu.app_bar, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle menu item clicks
                return when (menuItem.itemId) {
                    R.id.theme_menu -> {
                        Log.d("Values", "Picking a new theme")
                        showThemePicker()
                        true
                    }
                    R.id.help_menu -> {
                        Log.d("Values", "Help option clicked")
                        launchHelpWebView()
                        true
                    }
                    R.id.logout_menu -> {
                        Log.d("Values", "Logout option clicked")
                        listener?.signout()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        return view
    }private fun showThemePicker() {
        val themes = arrayOf("Green", "Blue", "Purple")
        val sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        AlertDialog.Builder(requireContext())
            .setTitle("Choose Theme")
            .setItems(themes) { _, which ->
                val selectedThemeKey = when (which) {
                    0 -> "Green.Theme.GroceryList"
                    1 -> "Blue.Theme.GroceryList"
                    2 -> "Purple.Theme.GroceryList"
                    else -> "Theme.GroceryList"
                }

                // Save the selected theme in SharedPreferences
                sharedPreferences.edit()
                    .putString("selectedTheme", selectedThemeKey)
                    .apply()

                // Restart the activity to apply the new theme
                requireActivity().recreate()
            }
            .show()
    }

    fun launchHelpWebView() {
        val intent = Intent(requireContext(), WebViewActivity::class.java)
        intent.putExtra("URL", "https://support.google.com/android/#topic=7313011")
        startActivity(intent)
    }
}