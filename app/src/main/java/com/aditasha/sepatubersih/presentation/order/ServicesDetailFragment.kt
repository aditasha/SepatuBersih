package com.aditasha.sepatubersih.presentation.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aditasha.sepatubersih.databinding.FragmentServicesDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ServicesDetailFragment : Fragment() {

    private var _binding: FragmentServicesDetailBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServicesDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.orderService.setOnClickListener {
            findNavController().navigate(ServicesDetailFragmentDirections.actionServicesDetailFragmentToOrderFormFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}