package com.example.grocerylist

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.grocerylist.AccountData.getList
import com.example.grocerylist.AccountData.listName
import com.example.grocerylist.AccountData.listToken
import com.example.grocerylist.AccountData.newListDocument
import com.example.grocerylist.AccountData.nickname
import com.example.grocerylist.AccountData.resetAccountData
import com.example.grocerylist.AccountData.verifyUserAuthenticated
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.LinkedList

class EditItemActivity : AppCompatActivity(), UIActivity, NavigationFragment.OnFragmentActionListener, TopNavBar.OnTopNavBarActionListener {
    private lateinit var database: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var itemNameEditText: EditText
    private lateinit var itemQtyEditText: EditText
    private lateinit var totalCostEditText: EditText
    private lateinit var dateTextView: TextView
    private lateinit var itemPurchaseByDateButton: Button
    private lateinit var saveEditedItemButton: Button
    private var itemIndex = -1

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
        setContentView(R.layout.activity_edit_item)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.editItem)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Variables:
        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        itemIndex = intent.getIntExtra("ITEM_INDEX",-1)

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
     * Description: Verifies user inputs and modifies the [Item] in the database
     */
    private fun editItem() {
        if (itemNameEditText.text.toString().isNotEmpty()
            || itemQtyEditText.text.toString().isNotEmpty()
            || totalCostEditText.text.toString().isNotEmpty()
            || dateTextView.text.toString() != "Buy Before Date"
        ) {
            val context = this
            val editItem = Item(
                    itemName = intent.getStringExtra("ITEM_NAME")!!,
                    buyBefore = intent.getStringExtra("ITEM_DATE")!!,
                    quantity = intent.getStringExtra("ITEM_QUANTITY")!!,
                    price = intent.getDoubleExtra("ITEM_PRICE",0.0),
                    addedBy = intent.getStringExtra("LAST_EDITED_BY")!!
                )
            var newDoc: HashMap<String,*>
            var list: LinkedList<Map<String, String>>

            // Edit the Item based on the user inputs
            if (itemNameEditText.text.toString().isNotEmpty())
                editItem.itemName = itemNameEditText.text.toString()
            if (itemQtyEditText.text.toString().isNotEmpty())
                editItem.quantity = itemQtyEditText.text.toString()
            if (totalCostEditText.text.toString().isNotEmpty())
                editItem.price = totalCostEditText.text.toString().toDouble()
            if (dateTextView.text.toString() != "Buy Before Date")
                editItem.buyBefore = dateTextView.text.toString()
            editItem.addedBy = nickname!!

            lifecycleScope.launch {
                // Get the current list items
                list = getList(database)

                // Since the list is stored backwards in Firestore, get the corresponding index of the item:
                val dbListItemIndex = list.size - 1 - itemIndex // Subtract the desired index from the last index of the list

                // Edit the correct list item
                list[dbListItemIndex] = editItem.toMap()
                Log.d("Values", "Item edited at index $dbListItemIndex")

                // Make a new document to replace the one with the edited item
                newDoc = newListDocument(list)

                // Replace the Firebase list items with the updated document
                database.collection("lists").document(listToken!!)
                    .set(newDoc) // This feels EXTREMELY inefficient... But it works lol
                    .addOnSuccessListener {
                        Log.d("Values", "Item edited and uploaded successfully")
                        Toast.makeText(context, "Item edited successfully", Toast.LENGTH_SHORT)
                            .show()
                        startActivity(Intent(context, MainActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Values", "Item upload failed:${e.message}")
                        Toast.makeText(context, "Error adding Item: ${e.message}", Toast.LENGTH_SHORT)
                            .show()
                        setResult(RESULT_CANCELED)
                        finish()
                    }
            }
        } else Toast.makeText(
            this,
            "No fields were modified",
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Description: Sets up the views for the activity and attaches the appropriate listeners
     */
    override fun setupViews() {
        itemNameEditText = findViewById(R.id.edit_item_name)
        itemQtyEditText = findViewById(R.id.edit_item_quantity)
        totalCostEditText = findViewById(R.id.edit_item_total_cost)
        dateTextView = findViewById(R.id.edit_item_date_text)
        itemPurchaseByDateButton = findViewById(R.id.edit_item_date_button)
        saveEditedItemButton = findViewById(R.id.save_edited_item_button)

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
        saveEditedItemButton.setOnClickListener {
            editItem()
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