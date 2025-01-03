package com.example.grocerylist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grocerylist.AccountData.listName
import com.example.grocerylist.AccountData.listToken
import com.example.grocerylist.AccountData.nickname
import com.example.grocerylist.AccountData.resetAccountData
import com.example.grocerylist.AccountData.verifyUserAuthenticated
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity(), UIActivity, ListAdapter.RecyclerViewEvent, NavigationFragment.OnFragmentActionListener, TopNavBar.OnTopNavBarActionListener {
    // Activity Variables:
    private lateinit var database: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var addButton: Button
    private lateinit var totalPriceTextView: TextView
    private lateinit var listNameTextView: TextView
    private var totalPrice = 0.00


    private val addItemLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.i("Activities", "MainActivity - Returned from TaskActivity")
            listenForItems()
        }
    }

    /**
     * Description: Overrides the [AppCompatActivity.onCreate] lifecycle callback method
     */
    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val selectedTheme = sharedPreferences.getString("selectedTheme", "AppTheme.Light")
        when (selectedTheme) {
            "Theme.GroceryList" -> setTheme(R.style.Green_Theme_GroceryList)
            "Blue.Theme.GroceryList" -> setTheme(R.style.Blue_Theme_GroceryList)
            "Purple.Theme.GroceryList" -> setTheme(R.style.Purple_Theme_GroceryList)
            else -> setTheme(R.style.Theme_GroceryList)
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Variables:
        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Check if user is authenticated
        verifyUserAuthenticated(this)

        getPrefs()

        // Check if the user is logged into a list
        if (listToken!=null)
            this.validateToken()
        else {
            val intent = Intent(this, ListLoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Insert the Fragments for themes, help, and navigation
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.top_nav_bar, TopNavBar())
                .replace(R.id.navigation_buttons,NavigationFragment())
                .commit()
        }

        setupViews()

        // Start listening for changes in the dataset
        listenForItems()
    }

    override fun onStart() {
        super.onStart()
        listNameTextView.text = listName
    }

    override fun onStop() {
        super.onStop()
        setPrefs()
    }

    /**
     * Description:
     *
     * @param item
     * @param position
     */
    @Override
    override fun onItemClick(item: Item, position: Int) {
        Log.i("Buttons", "MainActivity - Task item $position pressed")
        val viewIntent = Intent(this, ViewItemActivity::class.java)
        viewIntent.putExtra("ITEM_NAME", itemList[position].itemName)
        viewIntent.putExtra("ITEM_DATE", itemList[position].buyBefore)
        viewIntent.putExtra("ITEM_QUANTITY", itemList[position].quantity)
        viewIntent.putExtra("ITEM_PRICE", itemList[position].price)
        viewIntent.putExtra("LAST_EDITED_BY", itemList[position].addedBy)
        viewIntent.putExtra("ITEM_INDEX", position)
        startActivity(viewIntent)
    }

    /**
     * Description: Attaches a SnapshotListener that observes changes in the firebase collection of grocery lists
     */
    private fun listenForItems() {
        if (listToken != null) {
            Log.d("MainActivity", "Listening for items")
            database.collection("lists").document(listToken!!)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("MainActivity", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val items = snapshot.data?.get("list items") as List<Map<String,String>>
                        Log.d("MainActivity", "Received ${items.size} tasks")
                        itemList.clear()
                        totalPrice = 0.0
                        var index = items.size -1
                        while (index >= 0) {
                            val item = items[index]
                            itemList.add(
                                Item(item))
                            totalPrice += item["item price"].toString().toDouble()
                            index--
                        }
                        adapter.notifyDataSetChanged()

                        val price = String.format(Locale.US,"$%.2f",totalPrice)
                        totalPriceTextView.text = price
                    }
                }
        }
    }

    /**
     * Description: Sets up the views for the activity and attaches the appropriate listeners
     */
    override fun setupViews() {
        // Setup the RecyclerView
        recyclerView = findViewById(R.id.items_home)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ListAdapter(itemList, this)
        recyclerView.adapter = adapter

        totalPriceTextView = findViewById(R.id.list_total_price)
        listNameTextView = findViewById(R.id.title)

        val price = String.format(Locale.US,"$%.2f",totalPrice)
        totalPriceTextView.text = price

        listNameTextView.text = listName

        // Setup the Buttons
        addButton = findViewById(R.id.add_item_button)

        addButton.setOnClickListener {
            Log.i("Buttons", "Add button pressed - MainActivity")
            val taskIntent = Intent(this, AddItemActivity::class.java)
            addItemLauncher.launch(taskIntent)
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
     *
     * Makes a [Toast] notifying the user that they are already at the home activity
     */
    override fun onHomeButton() {
        Toast.makeText(this,"Already at Home Page",Toast.LENGTH_SHORT).show()
    }

    /**
     * Description: Defines the behavior of the listener for the Logout [Button]
     *
     * Logs the user out of the List and brings them to the [ListLoginActivity]
     */
    override fun onLogoutButton() {
        Toast.makeText(this,"Signing out of $listName", Toast.LENGTH_SHORT).show()
        resetAccountData()
        startActivity(Intent(this, ListLoginActivity::class.java))
        finish()
    }



    /**
     * Verifies that the token matches an existing Grocery List and launches the MainActivity with that token
     */
    private fun validateToken() {
        val context = this
        lifecycleScope.launch {
            val isValid = AccountData.validateToken(database, listToken!!)

            // Check if the token is valid
            if (!isValid) {
                val intent = Intent(context, ListLoginActivity::class.java)
                startActivity(intent)
                finish()
                Toast.makeText(context,"The list you were last logged into seems to have changed. Please login again.",Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        /** List of [Item]s that have been added to the list */
        val itemList = mutableListOf<Item>()
    }

    private fun setPrefs() {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString("TOKEN", listToken)
            putString("NICKNAME", nickname)
            putString("LIST_NAME", listName)
            apply()
        }
    }

    private fun getPrefs() {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        listToken = sharedPref.getString("TOKEN",listToken)
        nickname = sharedPref.getString("NICKNAME",nickname)
        listName = sharedPref.getString("LIST_NAME",listName)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.run {
            putString("TOKEN", listToken)
            putString("NICKNAME", nickname)
            putString("LIST_NAME", listName)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.run {
            listToken = getString("TOKEN")
            nickname = getString("NICKNAME")
            listName = getString("LIST_NAME")
        }
    }
}