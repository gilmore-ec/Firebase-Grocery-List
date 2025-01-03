package com.example.grocerylist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
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
import java.util.LinkedList

class ViewItemActivity : AppCompatActivity(), UIActivity, NavigationFragment.OnFragmentActionListener, TopNavBar.OnTopNavBarActionListener {
    private lateinit var database: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var editButton: Button
    private lateinit var removeButton: Button
    private lateinit var itemNameTextView: TextView
    private lateinit var itemBuyBeforeDateTextView: TextView
    private lateinit var itemQtyTextView: TextView
    private lateinit var itemPriceTextView: TextView
    private lateinit var item: Item
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
        setContentView(R.layout.activity_view_item)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.viewItem)) { v, insets ->
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
     * Description: Sets up the views for the activity and attaches the appropriate listeners
     */
    override fun setupViews() {
        itemNameTextView = findViewById(R.id.view_item_name)
        itemBuyBeforeDateTextView = findViewById(R.id.view_item_date)
        itemQtyTextView = findViewById(R.id.view_item_qty)
        itemPriceTextView = findViewById(R.id.view_item_cost)

        editButton = findViewById(R.id.edit_item_button)
        removeButton = findViewById(R.id.remove_item_button)

        item = Item(
            itemName = intent.getStringExtra("ITEM_NAME")!!,
            buyBefore = intent.getStringExtra("ITEM_DATE")!!,
            quantity = intent.getStringExtra("ITEM_QUANTITY")!!,
            price = intent.getDoubleExtra("ITEM_PRICE",0.0),
            addedBy = intent.getStringExtra("LAST_EDITED_BY")!!
        )

        itemNameTextView.text = item.itemName
        itemBuyBeforeDateTextView.text = item.buyBefore
        val qtyStr = "Quantity: " + item.quantity
        itemQtyTextView.text = qtyStr
        itemPriceTextView.text = item.formatTotalPrice()

        editButton.setOnClickListener {
            val editIntent = Intent(this, EditItemActivity::class.java)
            editIntent.putExtra("ITEM_INDEX", itemIndex)
            editIntent.putExtra("ITEM_NAME", item.itemName)
            editIntent.putExtra("ITEM_DATE", item.buyBefore)
            editIntent.putExtra("ITEM_QUANTITY", item.quantity)
            editIntent.putExtra("ITEM_PRICE", item.price)
            editIntent.putExtra("LAST_EDITED_BY", item.addedBy)
            startActivity(editIntent)
        }
        removeButton.setOnClickListener {
            removeItem()
        }
    }

    /**
     * Removes the [item] from the database
     */
    private fun removeItem() {
        val context = this
        var newDoc: HashMap<String,*>
        var list: LinkedList<Map<String, String>>

        lifecycleScope.launch {
            // Get the current list items
            list = getList(database)

            // Since the list is stored backwards in Firestore, get the corresponding index of the item:
            val dbListItemIndex = list.size - 1 - itemIndex // Subtract the desired index from the last index of the list

            // Remove the correct list item
            list.removeAt(dbListItemIndex)

            // Make a new document to replace the one with the removed item
            newDoc = newListDocument(list)

            // Replace the Firebase list items with the updated document
            database.collection("lists").document(listToken!!)
                .set(newDoc)
                .addOnSuccessListener {
                    Toast.makeText(context, "Item removed successfully", Toast.LENGTH_SHORT)
                        .show()
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        "Error removing Item: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_CANCELED)
                    finish()
                }
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
            putInt("ITEM_INDEX", itemIndex)
            putString("TOKEN", listToken)
            putString("NICKNAME", nickname)
            putString("LIST_NAME", listName)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.run {
            itemIndex = getInt("ITEM_INDEX",-1)
            listToken = getString("TOKEN")
            nickname = getString("NICKNAME")
            listName = getString("LIST_NAME")
        }
    }
}