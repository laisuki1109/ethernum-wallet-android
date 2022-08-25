package com.suki.wallet.app.walletAbstract.send

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.innopage.core.base.BaseFragment
import com.innopage.core.webservice.config.Constants
import com.suki.wallet.R
import com.suki.wallet.app.scanQrCode.ScanQrCodeActivity
import com.suki.wallet.app.walletAbstract.WalletViewModel
import com.suki.wallet.app.walletAbstract.WalletViewModelFactory
import com.suki.wallet.databinding.FragmentSendTransactionBinding
import com.suki.wallet.utility.EthKit


class SendTransactionFragment(val isErcToken: Boolean) : BaseFragment<WalletViewModel>() {
    override val viewModel by lazy {
        setUpViewModel(
            ViewModelProvider(
                requireActivity(), WalletViewModelFactory(
                    requireActivity().application
                )
            ).get(WalletViewModel::class.java)
        )
    }
    private lateinit var binding: FragmentSendTransactionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_send_transaction, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbar)
        binding.btnScan.setOnClickListener { goToScanQrCode() }
        binding.btnSend.setOnClickListener {
            EthKit.send(
                binding.inputWallet.editText?.text.toString(),
                binding.inputAmount.editText?.text.toString().toBigDecimal(),
                isErcToken
            )
        }
        binding.inputAmount.editText?.doOnTextChanged { text, start, before, count ->
            if (!binding.inputWallet.editText?.text.isNullOrEmpty() && !binding.inputAmount.editText?.text.isNullOrEmpty()) {
                EthKit.estimateGas(
                    binding.inputWallet.editText?.text.toString(),
                    binding.inputAmount.editText?.text.toString().toBigDecimal(),
                    isErcToken
                )
            } else {
                binding.textGasEstimated.text = resources.getString(R.string.estimated_gas) + "null"
            }
        }
        handleObserver()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_SCAN_QR_CODE && resultCode == Activity.RESULT_OK) {
            val toWalletAddress = data?.getStringExtra(Constants.EXTRA_WALLET_ADDRESS)
            binding.inputWallet.editText?.setText(toWalletAddress)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // private function
    private fun setupToolbar(toolbar: Toolbar) {
        (activity as AppCompatActivity).run {
            setSupportActionBar(toolbar)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_nav_close)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = getString(R.string.send)
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
        }
    }

    private fun goToScanQrCode() {
        val intent = Intent(requireContext(), ScanQrCodeActivity::class.java)
        startActivityForResult(intent, Constants.REQUEST_SCAN_QR_CODE)
    }

    private fun handleObserver() {
        EthKit.estimatedGas.observe(viewLifecycleOwner, Observer {
            binding.textGasEstimated.text = resources.getString(R.string.estimated_gas) + it
        })

        EthKit.sendStatus.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                Toast.makeText(requireContext(), "Send Successfully", Toast.LENGTH_LONG).show()
                requireActivity().onBackPressed()
            } else {
                Toast.makeText(requireContext(), "Send Error: ${it.message}", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }
}