package com.suki.wallet.app.walletAbstract

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.BarcodeFormat
import com.innopage.core.base.BaseFragment
import com.innopage.core.webservice.config.Constants
import com.suki.wallet.LaunchActivity
import com.suki.wallet.MyApplication
import com.suki.wallet.R
import com.suki.wallet.app.walletAbstract.send.SendTransactionFragment
import com.suki.wallet.databinding.FragmentWalletAbstractBinding
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.suki.wallet.app.walletConnect.WalletConnectTestingActivity
import timber.log.Timber

class WalletAbstractFragment : BaseFragment<WalletViewModel>() {

    companion object {
        fun newInstance() = WalletAbstractFragment()
    }
    override val viewModel by lazy {
        setUpViewModel(
            ViewModelProvider(
                requireActivity(), WalletViewModelFactory(
                    requireActivity().application
                )
            ).get(WalletViewModel::class.java)
        )
    }

    private lateinit var binding: FragmentWalletAbstractBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_wallet_abstract, container, false)
        binding.lifecycleOwner= this
        binding.viewModel = viewModel
        binding.executePendingBindings()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.btnLogout.setOnClickListener {
            MyApplication.INSTANCE.addressWords = ""
            goToLaunchActivty()
        }
        binding.layoutEthBalance.setOnClickListener {
            goToSendFragment(isErcToken = false)
        }
        binding.layoutTokenBalance.setOnClickListener {
            goToSendFragment(isErcToken = true)
        }
        binding.btnConnect.setOnClickListener {
            goToWalletConnect()
        }
        handleObserver()
        viewModel.init()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clear()
    }

    private fun handleObserver(){
        viewModel.addressEip55.observe(viewLifecycleOwner, Observer { address ->
            generateQrCode(address)
            binding.btnCopy.setOnClickListener { copyTextToClipboard(address) }
            binding.btnHistory.setOnClickListener { goToViewHistory(address) }
            binding.btnRecover.setOnClickListener { popUpRecoverPhrase() }
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

    private fun generateQrCode(code: String){
        try {
            val barcodeEncoder = BarcodeEncoder()
            val size = resources.getDimensionPixelSize(R.dimen.size_image_qr_code)
            val bitmap = barcodeEncoder.encodeBitmap(code, BarcodeFormat.QR_CODE, size, size)
            binding.imageWalletAddress.setImageBitmap(bitmap)
        } catch (e: Exception) {
            showErrorMessage(Constants.RESPONSE_LINK_ERROR, "Cannot generate qr code", null)
        }
    }

    private fun copyTextToClipboard(text: String){
        val clipboard: ClipboardManager? =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("label", text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Text copied to clipboard", Toast.LENGTH_LONG).show()
        Timber.i("address = $text")
    }

    private fun goToSendFragment(isErcToken: Boolean){
        val fragment = SendTransactionFragment(isErcToken)
        requireActivity().supportFragmentManager.beginTransaction()
            .add(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun goToViewHistory(address: String){
        val url = "https://ropsten.etherscan.io/address/$address"
//        val url = "https://etherscan.io/address/$address"
        openWebView(url, false)
    }

    private fun popUpRecoverPhrase() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_recover)
            .setMessage(MyApplication.INSTANCE.addressWords)
            .setPositiveButton(R.string.btn_ok) { dialog, _ ->
                dialog.dismiss()
            }.create()
        alertDialog.show()
    }

    private fun goToWalletConnect(){
        val intent = Intent(requireContext(), WalletConnectTestingActivity::class.java)
        startActivity(intent)
    }

}