package com.aditasha.sepatubersih.presentation.admin

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.BuildCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aditasha.sepatubersih.R
import com.aditasha.sepatubersih.data.RealtimeDatabaseConstants
import com.aditasha.sepatubersih.databinding.FragmentAddArticleBinding
import com.aditasha.sepatubersih.domain.model.Result
import com.aditasha.sepatubersih.domain.model.SbArticle
import com.aditasha.sepatubersih.presentation.GlideApp
import com.aditasha.sepatubersih.presentation.uriToFile
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.google.modernstorage.photopicker.PhotoPicker
import dagger.hilt.android.AndroidEntryPoint
import id.zelory.compressor.Compressor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@BuildCompat.PrereleaseSdkCheck
@AndroidEntryPoint
class AddArticleFragment : DialogFragment() {
    private var _binding: FragmentAddArticleBinding? = null

    private val binding get() = _binding!!

    private var articleArgs: SbArticle? = null

    private val adminViewModel: OrderAdminViewModel by activityViewModels()

    @Inject
    lateinit var firebaseStorage: FirebaseStorage
    private val photoPicker =
        registerForActivityResult(PhotoPicker()) { uriList ->
            if (uriList != null && uriList.isNotEmpty()) setImage(uriList[0])
        }

    private var articleImage: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.ThemeOverlay_App_FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddArticleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return dialog
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { dismiss() }

        if (arguments != null)
            articleArgs = requireArguments().getParcelable("article")

        val argsArticle = articleArgs

        binding.apply {
            viewLifecycleOwner.lifecycleScope.launch {
                adminViewModel.articleForm.collectLatest { state ->
                    addButton.isEnabled = state.isDataValid
                    checkLink.isEnabled = state.linkError == false
                    errorImage.isVisible = state.imageError == true

                    if (state.nameError == true) {
                        name.error = getString(R.string.name_cant_empty)
                    } else {
                        name.error = null
                        name.isErrorEnabled = false
                    }

                    if (state.descError == true) {
                        desc.error = getString(R.string.desc_cant_empty)
                    } else {
                        desc.error = null
                        desc.isErrorEnabled = false
                    }

                    if (state.linkError == true) {
                        link.error = getString(R.string.invalid_url)
                    } else {
                        link.error = null
                        link.isErrorEnabled = false
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                adminViewModel.articleResult.collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            loading.isVisible = false
                            if (argsArticle != null) {
                                Toast.makeText(
                                    requireActivity(),
                                    getString(R.string.success_update_article),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    requireActivity(),
                                    getString(R.string.success_add_article),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            findNavController().navigateUp()
                        }
                        is Result.Error -> {
                            loading.isVisible = false
                            val tv = TypedValue()
                            var actionBarHeight: Int? = null
                            if (requireActivity().theme.resolveAttribute(
                                    android.R.attr.actionBarSize,
                                    tv,
                                    true
                                )
                            ) {
                                actionBarHeight = TypedValue.complexToDimensionPixelSize(
                                    tv.data,
                                    resources.displayMetrics
                                )
                            }
                            if (actionBarHeight != null) {
                                result.exception.localizedMessage?.let {
                                    val snack = Snackbar.make(
                                        requireView(),
                                        it,
                                        Snackbar.LENGTH_LONG
                                    )
                                    val params =
                                        snack.view.layoutParams as CoordinatorLayout.LayoutParams
                                    params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                                    params.setMargins(0, actionBarHeight, 0, 0)
                                    snack.view.layoutParams = params
                                    snack.setTextMaxLines(10)
                                    snack.show()
                                }
                            }
                        }
                        is Result.Loading -> loading.isVisible = true
                    }
                }
            }

            if (argsArticle != null) {
                val imageRef = firebaseStorage.reference.child(RealtimeDatabaseConstants.ARTICLE)
                    .child(argsArticle.key!!)
                    .child(argsArticle.image!!)
                nameEditText.setText(argsArticle.name)
                descEditText.setText(argsArticle.desc)
                linkEditText.setText(argsArticle.link)
                GlideApp.with(this@AddArticleFragment)
                    .load(imageRef)
                    .into(binding.image)
                adminViewModel.apply {
                    checkName(argsArticle.name)
                    checkDesc(argsArticle.desc)
                    checkLink(argsArticle.link)
                    checkImage(false)
                }
                root.tag = argsArticle.image
                addButton.text = getString(R.string.update_article)
            }

            nameEditText.doOnTextChanged { text, _, _, _ ->
                val string = text?.toString()
                adminViewModel.checkName(string)
            }

            descEditText.doOnTextChanged { text, _, _, _ ->
                val string = text?.toString()
                adminViewModel.checkDesc(string)
            }

            linkEditText.doOnTextChanged { text, _, _, _ ->
                val string = text?.toString()
                adminViewModel.checkLink(string)
            }

            addButton.setOnClickListener {
                val name = nameEditText.text.toString()
                val desc = descEditText.text.toString()
                val link = linkEditText.text.toString()
                val image = binding.root.tag as String
                val article = SbArticle(name = name, desc = desc, link = link, image = image)
                if (articleArgs != null) {
                    article.key = articleArgs!!.key
                    if (image == articleArgs?.image) {
                        adminViewModel.updateArticle(article, null)
                    } else adminViewModel.updateArticle(article, Uri.fromFile(articleImage))
                } else {
                    adminViewModel.addArticle(article, Uri.fromFile(articleImage))
                }
            }

            checkLink.setOnClickListener {
                CustomTabsIntent.Builder().build()
                    .launchUrl(requireContext(), Uri.parse(linkEditText.text.toString()))
            }

            addImage.setOnClickListener {
                photoPicker.launch(PhotoPicker.Args(PhotoPicker.Type.IMAGES_ONLY, 1))
            }
        }
    }

    private fun setImage(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            val tempFile = uriToFile(uri, requireContext())
            val reducedSize = Compressor.compress(requireContext(), tempFile)
            Glide.with(this@AddArticleFragment)
                .load(reducedSize)
                .into(binding.image)
            articleImage = reducedSize
            binding.root.tag = reducedSize.name
            binding.addImage.text = getString(R.string.article_change_image)
            adminViewModel.checkImage(false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ACTION_BAR_HEIGHT = 168
    }

}