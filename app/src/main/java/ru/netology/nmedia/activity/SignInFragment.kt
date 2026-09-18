package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSignInBinding
import ru.netology.nmedia.viewmodel.SignInViewModel
import kotlin.getValue

class SignInFragment : Fragment() {
    private val viewModel: SignInViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignInBinding.inflate(inflater, container, false)
        binding.buttonSignIn.setOnClickListener {
            viewModel.signIn(
                binding.textLogin.text.toString(),
                binding.textPassword.text.toString()
            )
        }
        viewModel.successSignIn.observe(viewLifecycleOwner){
            findNavController().navigateUp()
        }
        viewModel.dataState.observe(viewLifecycleOwner){state ->
            binding.buttonSignIn.isEnabled = !state.loading
            if(state.errorEvent != 0){
                Toast.makeText(requireContext(), getString(state.errorEvent), Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }
}