package com.example.grocerylist

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.random.Random

class ListAdapter(
    private var items: List<Item> = listOf(),
    private val listener: RecyclerViewEvent
) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    val randomGenerator = Random.Default

    /**
     * Description: This is the interface that supports the RecyclerView listener
     */
    interface RecyclerViewEvent {
        fun onItemClick(item: Item, position: Int)
    }

    /**
     * Description: Defines the assets and [View]s for the [RecyclerView.ViewHolder]
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val username: TextView = view.findViewById(R.id.user_name)
        val price: TextView = view.findViewById(R.id.item_price)
        val itemDetails: TextView = view.findViewById(R.id.item_name_qty)
        val buyBefore: TextView = view.findViewById(R.id.item_buy_before)
        private val profileImg: ImageView = view.findViewById(R.id.user_profile_img)

        private val icon = when (randomGenerator.nextInt(0,3)) {
            0 -> R.drawable.grocery_list_app_3_itemicon0
            1 -> R.drawable.grocery_list_app_4_itemicon1
            else -> R.drawable.grocery_list_app_5_itemicon2
        }

        init {
            profileImg.setImageResource(icon)
            profileImg.setOnClickListener(this)
            username.setOnClickListener(this)
            price.setOnClickListener(this)
            itemDetails.setOnClickListener(this)
            buyBefore.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(items[position], position)
            }
        }
    }

    /**
     * Description: Sets up the [ViewHolder] for the list items with a custom layout
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_layout, viewGroup, false)
        return ViewHolder(view)
    }

    /**
     * Description: Sets the data for each item to the correct Views in the layout
     *
     * @param viewHolder A [ViewHolder] that acts as the displayed and interactable list for each item
     * @param position The sequential position of the item in the list
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = items[position]
        Log.d("TaskAdapter", "Binding task: ${item.itemName} at position $position")
        val itemDetails = "${item.itemName}, ${item.quantity}"
        viewHolder.itemDetails.text = itemDetails
        viewHolder.buyBefore.text = item.buyBefore
        viewHolder.price.text = item.formatTotalPrice()
        viewHolder.username.text = item.addedBy
    }

    /**
     * Description: Gets the count of items on the list
     *
     * @return The number of items in the data set
     */
    override fun getItemCount() = items.size
}