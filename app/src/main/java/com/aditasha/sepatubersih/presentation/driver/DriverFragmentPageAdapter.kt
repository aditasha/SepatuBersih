package com.aditasha.sepatubersih.presentation.driver

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class DriverFragmentPageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = OrderUnassignedListDriverFragment()
        return when (position) {
            0 -> {
                val bundle = bundleOf(PAGE to PICKUP)
                fragment.arguments = bundle
                fragment
            }
            else -> {
                val bundle = bundleOf(PAGE to DELIV)
                fragment.arguments = bundle
                fragment
            }
        }
    }

    companion object {
        const val PAGE = "page"
        const val PICKUP = "pickup"
        const val DELIV = "delivery"
    }
}