package com.suki.wallet.app.walletConnect

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.PowerManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.innopage.core.base.BaseFragment
import com.innopage.core.webservice.config.Constants
import com.suki.wallet.R
import com.suki.wallet.app.scanQrCode.ScanQrCodeActivity
import com.suki.wallet.databinding.FragmentWalletConnectTestingBinding
import com.suki.wallet.service.WalletConnectService
import com.suki.wallet.utility.WalletConnect
import timber.log.Timber

class WalletConnectTestingFragment : BaseFragment<WalletConnectTestingViewModel>() {

    override val viewModel: WalletConnectTestingViewModel by lazy { setUpViewModel(requireActivity().application) as WalletConnectTestingViewModel }

    private lateinit var binding: FragmentWalletConnectTestingBinding

    private var inputUri: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_wallet_connect_testing, container, false)
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_SCAN_QR_CODE && resultCode == Activity.RESULT_OK) {
            val toWalletAddress = data?.getStringExtra(Constants.EXTRA_WALLET_ADDRESS)
            inputUri = toWalletAddress
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (WalletConnect.isConnected.value == true) {
            setupDisconnectButton()
            binding.textCurrentSession.text = WalletConnect.connectedSession.value
        } else setupConnectButton()


        binding.textCurrentSession.isVisible = WalletConnect.isConnected.value == true
        binding.btnScan.isVisible = WalletConnect.connectedSession.value.isNullOrEmpty()
        binding.textCurrentSession.isVisible = !WalletConnect.connectedSession.value.isNullOrEmpty()
        binding.textCurrentSession.text = WalletConnect.connectedSession.value


        binding.btnScan.setOnClickListener { goToScanQrCode() }

        WalletConnect.isConnected.observe(viewLifecycleOwner) {
            if (it){
                setupDisconnectButton()
                startWalletConnectService()
            } else {
                setupConnectButton()
            }
        }
        WalletConnect.connectedSession.observe(viewLifecycleOwner) {
            binding.btnScan.isVisible = it.isNullOrEmpty()
            binding.textCurrentSession.isVisible = !it.isNullOrEmpty()
            binding.textCurrentSession.text = it
        }
    }

    private fun setupConnectButton() {
        binding.btnConnect.text = resources.getString(R.string.wallet_connect)
        binding.btnConnect.setOnClickListener {
            inputUri?.let { it1 -> connect(it1) }
        }
    }

    fun connect(uri: String) {
        Timber.i("uri = ${uri}")
        WalletConnect.connect(inputUri)
    }

    private fun setupDisconnectButton() {
        binding.btnConnect.text = resources.getString(R.string.wallet_connect_kill)
        binding.btnConnect.setOnClickListener {
            WalletConnect.disconnect()
            val serviceIntent = Intent(context, WalletConnectService::class.java)
            serviceIntent.action = WalletConnectService.ACTION_STOP
            context?.startForegroundService(serviceIntent)
        }
    }

    private fun goToScanQrCode() {
        val intent = Intent(requireContext(), ScanQrCodeActivity::class.java)
        startActivityForResult(intent, Constants.REQUEST_SCAN_QR_CODE)
    }

    private fun startWalletConnectService() {
        //pop up for power saving dialog
        val powerManager =
            requireActivity().applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
//            val i = Intent()
//            Timber.i("isIgnoring = " + powerManager.isIgnoringBatteryOptimizations(packageName.packageName))
//            if (!powerManager.isIgnoringBatteryOptimizations(packageName.packageName)) {
//
//                AlertDialog.Builder(requireContext())
//                    .setMessage(resources.getString(R.string.sensor_background_reminder))
//                    .setPositiveButton(resources.getString(R.string.sensor_background_reminder_next)) { dialog, which ->
//                        var name = packageName.packageName
//                        i.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
//                        i.data = Uri.parse("package:$name")
//                        startActivity(i)
//                        dialog.dismiss()
//                    }
//                    .show()
//            }

        val serviceIntent = Intent(context, WalletConnectService::class.java)
        context?.startForegroundService(serviceIntent)
    }
}