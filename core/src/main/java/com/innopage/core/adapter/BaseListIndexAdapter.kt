package com.innopage.core.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min

class BaseListIndexAdapter<T>(
    private val activity: FragmentActivity,
    private val layoutId: Int,
    private val dataId: Int,
    private val variableIds: List<Int> = emptyList(),
    private val variables: List<Any> = emptyList(),
    diffUtil: DiffUtil.ItemCallback<T>
) : ListAdapter<T, BaseListIndexAdapter.BaseViewHolder<T>>(diffUtil) {

    class BaseViewHolder<T>(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            activity: FragmentActivity,
            dataId: Int,
            data: T,
            variableIds: List<Int>,
            variables: List<Any>,
            itemPosition: Int
        ) {
            with(binding) {
                lifecycleOwner = activity
                setVariable(dataId, data)
                for (i in 0 until min(variableIds.size, variables.size)) {
                    if (i != 1){
                        setVariable(variableIds[i], variables[i])
                    } else{
                        setVariable(variableIds[i], itemPosition)
                    }
                }
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ViewDataBinding =
            DataBindingUtil.inflate(layoutInflater, layoutId, parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        if (position < itemCount) {
            getItem(position)?.apply {
                holder.bind(activity, dataId, this, variableIds, variables, position)
            }
        }
    }
}