package com.example.grocerylist

import java.util.Locale

data class Item(
    var itemName: String = "Item Name",
    /** Can have any or no unit type: "3", or "3 gallons" are both totally acceptable */
    var quantity: String = "Qty",
    /** Format: "MM/DD/YYYY" */
    var buyBefore: String = "MM/DD/YYYY",
    /** Price of a single item */
    var price: Double = 0.00,
    /** Use only first name of user */
    var addedBy: String = "User Name",
    /** Stores the identifier that Firebase uses to find this item */
    var id: Int = 0
) {
    constructor(itemMap: Map<String,String>) : this(
        itemName = itemMap.get("item name")!!,
        quantity = itemMap.get("item quantity")!!,
        buyBefore = itemMap.get("item date")!!,
        price = itemMap.get("item price")!!.toDouble(),
        addedBy = itemMap.get("last edited by")!!)

    /**
     * Converts the [price] class variable into its [String] representation
     *
     * @return A [String] representing the total price of the item
     */
    fun formatTotalPrice(): String {
        return String.format(Locale.US,"$%.2f",price)
    }

    /**
     * Converts this [Item] into a map for uploading to Firestore
     *
     * @return a new [Map] containing the [itemName], [quantity], [buyBefore], [price], and [lastUpdatedBy] values
     */
    fun toMap(): Map<String,String> {
        return mutableMapOf(
            "item name" to itemName,
            "item quantity" to quantity,
            "item date" to buyBefore,
            "item price" to price.toString(),
            "last edited by" to lastUpdatedBy()
            )
    }

    /**
     * Returns the name of the last user who edited this item
     *
     * @return The username of the last user that modified the item
     */
    private fun lastUpdatedBy(): String {
        return addedBy
    }
}