package com.example.grocerylist

import android.app.DatePickerDialog
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
import com.example.grocerylist.AccountData.auth
import com.example.grocerylist.AccountData.listName
import com.example.grocerylist.AccountData.listToken
import com.example.grocerylist.AccountData.nickname
import com.example.grocerylist.AccountData.resetAccountData
import com.example.grocerylist.AccountData.verifyUserAuthenticated
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AddItemActivity : AppCompatActivity(), UIActivity, NavigationFragment.OnFragmentActionListener, TopNavBar.OnTopNavBarActionListener {
    private lateinit var database: FirebaseFirestore
    private lateinit var itemNameEditText: EditText
    private lateinit var itemQtyEditText: EditText
    private lateinit var totalCostEditText: EditText
    private lateinit var dateTextView: TextView
    private lateinit var itemPurchaseByDateButton: Button
    private lateinit var saveNewItemButton: Button

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
        setContentView(R.layout.activity_add_item)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addItem)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Variables:
        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Check if user is authenticated
        verifyUserAuthenticated(this)

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
     * Description: Verifies user inputs and user login authentication and adds the new [Item] to the database
     */
    private fun addItem() {
        if (itemNameEditText.text.toString().isNotEmpty()
            && itemQtyEditText.text.toString().isNotEmpty()
            && totalCostEditText.text.toString().isNotEmpty()
            && dateTextView.text.toString() != "Buy Before Date"
        ) {
            // Create a new item
            val newItem = Item(
                itemName = itemNameEditText.text.toString(),
                quantity = itemQtyEditText.text.toString(),
                buyBefore = dateTextView.text.toString(),
                price = totalCostEditText.text.toString().toDouble(),
                addedBy = nickname!!
            )
            database.collection("lists").document(listToken!!)
                .update("list items",FieldValue.arrayUnion(newItem.toMap()))
                .addOnSuccessListener {
                    Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT)
                        .show()
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error adding Item: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }

            Toast.makeText(this,"New Item added successfully!",Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        } else Toast.makeText(
            this,
            "All fields are required to add an item",
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Description: Sets up the views for the activity and attaches the appropriate listeners
     */
    override fun setupViews() {
        itemNameEditText = findViewById(R.id.add_item_name)
        itemQtyEditText = findViewById(R.id.add_item_quantity)
        totalCostEditText = findViewById(R.id.add_item_total_cost)
        dateTextView = findViewById(R.id.add_item_date_text)
        itemPurchaseByDateButton = findViewById(R.id.add_item_date_button)
        saveNewItemButton = findViewById(R.id.save_added_item_button)

        itemPurchaseByDateButton.setOnClickListener {

            val c = Calendar.getInstance()

            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this,
                { _, inputYear, monthOfYear, dayOfMonth ->
                    val temp = "Before: " + (monthOfYear + 1) + "/" + dayOfMonth + "/" + inputYear
                    dateTextView.text = temp
                },
                year, month, day
            )
            datePickerDialog.show()
        }
        saveNewItemButton.setOnClickListener {
            addItem()
        }

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.run {
            putString("ITEM_NAME",itemNameEditText.text.toString())
            putString("ITEM_DATE",dateTextView.text.toString())
            putString("ITEM_QUANTITY",itemQtyEditText.text.toString())
            putString("ITEM_COST", totalCostEditText.text.toString())
            putString("TOKEN", listToken)
            putString("NICKNAME", nickname)
            putString("LIST_NAME", listName)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.run {
            itemNameEditText.setText(getString("ITEM_NAME"),TextView.BufferType.EDITABLE)
            dateTextView.setText(getString("ITEM_DATE"),TextView.BufferType.EDITABLE)
            itemQtyEditText.setText(getString("ITEM_QUANTITY"),TextView.BufferType.EDITABLE)
            totalCostEditText.setText(getString("ITEM_COST"),TextView.BufferType.EDITABLE)
            listToken = getString("TOKEN")
            nickname = getString("NICKNAME")
            listName = getString("LIST_NAME")
        }
    }
}