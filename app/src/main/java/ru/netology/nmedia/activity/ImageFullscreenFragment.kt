package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.databinding.FragmentImageFullscreenBinding
import ru.netology.nmedia.viewmodel.PostViewModel
import kotlin.getValue

class ImageFullscreenFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentImageFullscreenBinding.inflate(inflater, container, false)
        val imageUrl = arguments?.getString("imageUrl")
        imageUrl?.let {
            Glide.with(this)
                .load(it)
                .into(binding.fullscreenImage)
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        return binding.root
    }
}