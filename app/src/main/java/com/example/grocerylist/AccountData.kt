package com.example.grocerylist

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.ArrayList
import java.util.LinkedList

object AccountData {
    /** Token associated with the currently logged in list */
    var listToken: String? = null
    /** Name of the currently logged in list */
    var listName: String? = null
    /** Nickname for the current user in the currently logged-in list */
    var nickname: String? = null
    /** Entry point for the [FirebaseAuth] SDK */
    var auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Updates the current user's nickname for the list
     *
     * @param context The [Activity] that this function is being called from
     * @param auth The [FirebaseAuth] for this instance
     * @param nickname The name the user wants to use for the Grocery List
     */
    fun updateUserProfile(context: Activity, auth: FirebaseAuth, nickname: String): Boolean {
        var success = false
        val updateDisplayName =  UserProfileChangeRequest.Builder()
            .setDisplayName(nickname)
            .build()
        auth.currentUser?.updateProfile(updateDisplayName)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context,"Your nickname for this list is $nickname", Toast.LENGTH_SHORT).show()
                    success = true
                    this.nickname = nickname
                } else {
                    Toast.makeText(context,"Error: ${task.exception}", Toast.LENGTH_SHORT).show()
                    success = false
                }
            }
        return success
    }

    /**
     * Resets the class variables to null
     */
    fun resetAccountData() {
        listToken = null
        listName = null
        nickname = null
    }

    /**
     * Verifies the user is logged in. If not, launches the [LoginActivity]
     *
     * @param context The [Activity] that calls this method
     */
    fun verifyUserAuthenticated(context: Activity) {
        if (auth.currentUser == null) {
            Toast.makeText(context,"Login is required for this operation",Toast.LENGTH_SHORT).show()
            context.startActivity(Intent(context, LoginActivity::class.java))
            context.finish()
            return
        }
    }

    /**
     * Checks if the [FirebaseFirestore] contains a document matching the token
     *
     * @param database The [FirebaseFirestore] for the app
     * @param token The token that may or may not be a Firestore document id
     *
     * @return A [Boolean] representing whether the document exists.
     */
    suspend fun validateToken(database: FirebaseFirestore, token: String): Boolean {
        return try {
            val groceryListDocument = database.collection("lists").document(token)
            val documentSnapshot = groceryListDocument.get().await()
            listName = documentSnapshot.data?.get("list name") as String?
            documentSnapshot.exists()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Retrieves the current list items
     *
     * @param database The current instance of the [FirebaseFirestore]
     *
     * @return A [List] containing the elements of the online list of items
     */
    suspend fun getList(database: FirebaseFirestore): LinkedList<Map<String,String>> {
        val document = database.collection("lists").document(listToken!!).get().await()
        val docList = document.get("list items") as ArrayList<Map<String,String>>
        val resultList = LinkedList<Map<String,String>>()
        for (itemMap in docList)
            resultList.add(itemMap)
        return resultList
    }

    /**
     * Creates a new grocery list document for the current grocery list with the given [itemsList]
     *
     * @param itemsList A [List] of item documents
     */
    fun newListDocument(itemsList: List<Map<String, String?>>): HashMap<String, *> {
        return hashMapOf(
            "list name" to listName,
            "list items" to itemsList
        )
    }
}