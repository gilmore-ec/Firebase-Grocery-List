package com.example.grocerylist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

/**
 * UI Activity allowing user to navigate to other activities that either create a new list or join an existing list
 */
class ListLoginActivity : AppCompatActivity(), UIActivity {
    private lateinit var auth: FirebaseAuth
    private lateinit var createButton: Button
    private lateinit var tokenButton: Button


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
        setContentView(R.layout.activity_list_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.listLogin)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = FirebaseAuth.getInstance()

        // Check if user is authenticated
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupViews()
    }

    /**
     * Description: Sets up the views for the activity and attaches the appropriate listeners
     */
    override fun setupViews() {
        createButton = findViewById(R.id.create_new_list_button)
        tokenButton = findViewById(R.id.enter_token_button)

        createButton.setOnClickListener {
            startActivity(Intent(this, NewListFormActivity::class.java))
            finish()
        }

        tokenButton.setOnClickListener {
            startActivity(Intent(this, TokenActivity::class.java))
            finish()
        }
    }
}