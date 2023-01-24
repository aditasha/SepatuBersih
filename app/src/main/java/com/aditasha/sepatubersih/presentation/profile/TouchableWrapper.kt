package com.aditasha.sepatubersih.presentation.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.SupportMapFragment


class MySupportMapFragment : SupportMapFragment() {
    private var mListener: OnTouchListener? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout: View = super.onCreateView(inflater, parent, savedInstanceState)
        val frameLayout = TouchableWrapper(
            requireContext()
        )
        frameLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        (layout as ViewGroup).addView(
            frameLayout,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        return layout
    }

    fun setListener(listener: OnTouchListener?) {
        mListener = listener
    }

    interface OnTouchListener {
        fun onTouch()
    }

    inner class TouchableWrapper(context: Context) :
        FrameLayout(context) {
        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            mListener?.let {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> it.onTouch()
                    MotionEvent.ACTION_UP -> it.onTouch()
                }
            }
            return super.dispatchTouchEvent(event)
        }
    }
}