package com.example.grocerylist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NewListFormActivity : AppCompatActivity(), UIActivity {
    // Activity Variables:
    private lateinit var database: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var listNameEditText: EditText
    private lateinit var saveButton: Button

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
        setContentView(R.layout.activity_new_list_form)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.newList)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Variables:
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
     * Description: Sets up the views for the activity and attaches the appropriate listeners
     */
    override fun setupViews() {
        listNameEditText = findViewById(R.id.new_list_name)
        saveButton = findViewById(R.id.save_new_list_button)

        saveButton.setOnClickListener {
            if (listNameEditText.text.isNotEmpty()) {
                AccountData.listName = listNameEditText.text.toString()
                val newListDocument = hashMapOf(
                    "list name" to AccountData.listName,
                    "list items" to emptyList<Map<String,String>>()
                )
                database.collection("lists")
                    .add(newListDocument)
                    .addOnSuccessListener {
                        AccountData.listToken = it.id
                        startActivity(Intent(this,TokenActivity::class.java))
                        finish()
                    }
            }
            else Toast.makeText(this,"You need to name your grocery list!",Toast.LENGTH_SHORT).show()
        }
    }
}