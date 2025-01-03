package com.example.grocerylist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.grocerylist.AccountData.auth
import com.example.grocerylist.AccountData.listName
import com.example.grocerylist.AccountData.resetAccountData
import com.google.firebase.auth.FirebaseAuth

class WebViewActivity : AppCompatActivity(), UIActivity, NavigationFragment.OnFragmentActionListener, TopNavBar.OnTopNavBarActionListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val selectedTheme = sharedPreferences.getString("selectedTheme", "AppTheme.Light")
        when (selectedTheme) {
            "Green.Theme.GroceryList" -> setTheme(R.style.Green_Theme_GroceryList)
            "Blue.Theme.GroceryList" -> setTheme(R.style.Blue_Theme_GroceryList)
            "Purple.Theme.GroceryList" -> setTheme(R.style.Purple_Theme_GroceryList)
            else -> setTheme(R.style.Theme_GroceryList)
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.web_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.webView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Insert the Fragments for themes, help, and navigation
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.top_nav_bar, TopNavBar())
                .replace(R.id.navigation_buttons,NavigationFragment())
                .commit()
        }
        setupViews()
    }

    /**
     * Description: Defines the behavior of the listener for the Home [Button]
     */
    override fun onHomeButton() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * Description: Defines the behavior of the listener for the Logout [Button]
     */
    override fun onLogoutButton() {
        Toast.makeText(this,"Signing out of $listName", Toast.LENGTH_SHORT).show()
        resetAccountData()
        startActivity(Intent(this, ListLoginActivity::class.java))
        finish()
    }

    /**
     * Description: Sets up the views for the activity and attaches the appropriate listeners
     */
    override fun setupViews() {
        val webView: WebView = findViewById(R.id.web_view)
        val url = intent.getStringExtra("URL") ?: "https://support.google.com/android/#topic=7313011"

        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)
    }

    /**
     * Description: Logs out the current [FirebaseAuth] user out and restarts the [LoginActivity]
     */
    override fun signout() {
        auth.signOut()
        resetAccountData()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}