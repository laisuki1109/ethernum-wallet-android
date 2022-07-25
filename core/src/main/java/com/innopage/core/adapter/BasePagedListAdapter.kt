package com.innopage.core.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * A BasePagedListAdapter but allows to bind viewModel and an object into view holder layout
 * @param activity - the activity which holds the fragment
 * @param layoutId - the view holder layout id (For example : BR.link)
 * @param dataId - the binding id of the data which view holder holds
 * @param variableIds - list of variable ids which defined in xml
 * @param variables - list of variables which defined in xml
 * @param diffUtil - diffUtil which the data is difference with id and content
 */
class BasePagedListAdapter<T>(
    private val activity: FragmentActivity,
    private val layoutId: Int,
    private val dataId: Int,
    private val variableIds: List<Int>,
    private val variables: List<Any>,
    diffUtil: DiffUtil.ItemCallback<T>
) : PagedListAdapter<T, BasePagedListAdapter.BaseViewHolder<T>>(diffUtil) {

    class BaseViewHolder<T>(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            activity: FragmentActivity,
            dataId: Int,
            data: T,
            variableIds: List<Int>,
            variables: List<Any>
        ) {
            with(binding) {
                lifecycleOwner = activity
                setVariable(dataId, data)
                for (i in 0 until Math.min(variableIds.size, variables.size)) {
                    setVariable(variableIds[i], variables[i])
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
                holder.bind(activity, dataId, this, variableIds, variables)
            }
        }
    }
}