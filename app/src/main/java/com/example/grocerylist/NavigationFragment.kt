package com.example.grocerylist

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class NavigationFragment : Fragment(R.layout.fragment_navigation) {
    interface OnFragmentActionListener {
        /**
         * Description: Defines the behavior of the listener for the Home [Button]
         */
        fun onHomeButton()

        /**
         * Description: Defines the behavior of the listener for the Logout [Button]
         */
        fun onLogoutButton()
    }
    private var listener: OnFragmentActionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        listener = context as OnFragmentActionListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //return super.onCreateView(inflater, container, savedInstanceState)
        val view: View = inflater.inflate(R.layout.fragment_navigation, container, false)
        val logoutBtn: Button = view.findViewById(R.id.logout_button)
        logoutBtn.setOnClickListener {
            listener?.onLogoutButton()
        }
        val homeBtn: Button = view.findViewById(R.id.home_button)
        homeBtn.setOnClickListener {
            listener?.onHomeButton()
        }
        return view
    }
}