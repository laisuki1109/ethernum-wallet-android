package com.suki.wallet.utility

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.math.BigDecimal
import java.math.RoundingMode

@BindingAdapter("setBalance")
fun TextView.setBalance(balance: BigDecimal?) {
    balance?.let {
        text = it.setScale(5, RoundingMode.HALF_EVEN).toString() + " ETH"
    }

}