package com.suki.wallet.app.loginRegister.createWallet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.innopage.core.base.BaseFragment
import com.suki.wallet.LaunchActivity
import com.suki.wallet.MyApplication
import com.suki.wallet.R
import com.suki.wallet.databinding.FragmentCreateWalletBinding
import timber.log.Timber

class CreateWalletFragment : BaseFragment<CreateWalletViewModel>() {

    companion object {
        fun newInstance() = CreateWalletFragment()
    }

    override val viewModel by lazy { setUpViewModel(requireActivity().application) as CreateWalletViewModel }
    private lateinit var binding: FragmentCreateWalletBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_create_wallet, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.executePendingBindings()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupToolbar(binding.toolbar)
        handleObserver()
        binding.btnGenerate.setOnClickListener { viewModel.createWallet() }
        binding.btnTransaction.setOnClickListener { goToLaunchActivty() }
    }

    //    private function
    private fun setupToolbar(toolbar: Toolbar) {
        (activity as AppCompatActivity).run {
            setSupportActionBar(toolbar)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_nav_close)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = getString(R.string.create_wallet)
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
        }
    }

    private fun handleObserver() {
        viewModel.wordList.observe(viewLifecycleOwner, Observer { words ->
            binding.textResult.text = ""
            binding.textResult.text = words.joinToString(" ")
        })

        viewModel.addressEip55.observe(viewLifecycleOwner, Observer {
            Timber.i(MyApplication.INSTANCE.addressWords)
            binding.btnTransaction.isVisible = true
        })
    }

    private fun goToLaunchActivty(){
        startActivity(Intent(requireContext(), LaunchActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        requireActivity().finish()
    }
}