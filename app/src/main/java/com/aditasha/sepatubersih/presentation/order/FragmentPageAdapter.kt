package com.aditasha.sepatubersih.presentation.order

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aditasha.sepatubersih.data.RealtimeDatabaseConstants

class FragmentPageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = OrderListFragment()
        return when (position) {
            0 -> {
                val bundle = bundleOf(PAGE to RealtimeDatabaseConstants.ONGOING)
                fragment.arguments = bundle
                fragment
            }
            else -> {
                val bundle = bundleOf(PAGE to RealtimeDatabaseConstants.COMPLETE)
                fragment.arguments = bundle
                fragment
            }
        }
    }

    companion object {
        const val PAGE = "page"
    }
}