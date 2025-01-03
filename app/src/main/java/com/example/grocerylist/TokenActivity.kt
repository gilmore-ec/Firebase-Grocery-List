package com.example.grocerylist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class TokenActivity : AppCompatActivity(), UIActivity {
    private lateinit var database: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var tokenEditText: EditText
    private lateinit var nicknameEditText: EditText
    private lateinit var signInButton: Button
    private lateinit var backButton: Button

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
        setContentView(R.layout.activity_token)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tokenActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        database = FirebaseFirestore.getInstance()
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
     * Verifies that the token matches an existing Grocery List and launches the MainActivity with that token
     *
     * @param token The Document ID of the [FirebaseFirestore] grocery list document
     */
    private fun validateToken(token: String) {
        val context = this
        lifecycleScope.launch {
            val isValid = AccountData.validateToken(database, token)

            // Check if the token is valid
            if (isValid) {
                AccountData.updateUserProfile(context, auth, nicknameEditText.text.toString())
                AccountData.listToken = token
                val intent = Intent(context, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            else Toast.makeText(context,"The token you entered is invalid. Please try again",Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Description: Sets up the views for the activity and attaches the appropriate listeners
     */
    override fun setupViews() {
        tokenEditText = findViewById(R.id.token_input)
        nicknameEditText = findViewById(R.id.user_nickname)
        signInButton = findViewById(R.id.list_sign_in_button)
        backButton = findViewById(R.id.back_button)

        if (AccountData.listToken != null)
            tokenEditText.setText(AccountData.listToken,TextView.BufferType.EDITABLE)

        signInButton.setOnClickListener {
            if (tokenEditText.text.toString().isNotEmpty()
                && validateNickname()) {
                validateToken(tokenEditText.text.toString())
            }
            else Toast.makeText(this,"Please enter a valid token and nickname or press back to create a new list",Toast.LENGTH_SHORT).show()
        }
        backButton.setOnClickListener {
            startActivity(Intent(this, ListLoginActivity::class.java))
            finish()
        }
    }

    /**
     * Checks if the nickname meets the criteria
     *
     * @return A boolean representing whether the nickname is valid or not
     */
    private fun validateNickname(): Boolean {
        val isIt = nicknameEditText.text.toString().isNotEmpty()
                && nicknameEditText.text.toString().length >= 3
                && nicknameEditText.text.toString().length <= 9
        if (!isIt) Toast.makeText(this,"Please enter a valid nickname",Toast.LENGTH_SHORT).show()
        return isIt
    }
}