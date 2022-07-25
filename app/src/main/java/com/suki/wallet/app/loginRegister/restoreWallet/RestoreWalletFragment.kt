package com.suki.wallet.app.loginRegister.restoreWallet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.innopage.core.base.BaseFragment
import com.suki.wallet.LaunchActivity
import com.suki.wallet.R
import com.suki.wallet.databinding.FragmentRestoreWalletBinding


class RestoreWalletFragment : BaseFragment<RestoreWalletViewModel>() {

    companion object {
        fun newInstance() = RestoreWalletFragment()
    }

    override val viewModel by lazy { setUpViewModel(requireActivity().application) as RestoreWalletViewModel }
    private lateinit var binding: FragmentRestoreWalletBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_restore_wallet, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.executePendingBindings()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupToolbar(binding.toolbar)

        binding.btnGenerate.setOnClickListener {
            viewModel.restoreWallet(binding.inputWalletKey.text.toString())
        }

        viewModel.addressEip55.observe(viewLifecycleOwner, Observer {
            val alertDialog = AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.restore_wallet_success_title))
                .setMessage(it)
                .setPositiveButton(resources.getString(R.string.restore_wallet_success_ok)) { dialog, which ->
                    goToLaunchActivty()
                } // A null listener allows the button to dismiss the dialog and take no further action.
                .show()
        })
    }

    //    private function
    private fun setupToolbar(toolbar: Toolbar) {
        (activity as AppCompatActivity).run {
            setSupportActionBar(toolbar)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_nav_close)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = getString(R.string.restore_wallet)
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
        }
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