package com.innopage.core.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import kotlin.math.min

class BaseHeaderFooterListAdapter<T> :
    ListAdapter<BaseHeaderFooterListAdapter.DataItem, RecyclerView.ViewHolder> {
    lateinit var activity: FragmentActivity
    private var layoutId: Int = 0

    private var headerLayoutId: Int = 0
    private var footerLayoutId: Int = 0

    private var dataId: Int = 0
    private var headerDataId: Int = 0
    private var footerDataId: Int = 0

    private var variableIds: List<Int> = emptyList()
    private var variables: List<Any> = emptyList()

    private var headerVariableIds: List<Int> = emptyList()
    private var headerVariable: List<Any> = emptyList()

    private var footerVariableIds: List<Int> = emptyList()
    private var footerVariable: List<Any> = emptyList()

    private var hasHeader: Boolean = false
    private var hasFooter: Boolean = false

    constructor(
        activity: FragmentActivity,

        layoutId: Int,
        headerLayoutId: Int,
        footerLayoutId: Int,

        dataId: Int,
        headerDataId: Int,
        footerDataId: Int,

        variableIds: List<Int> = emptyList(),
        variables: List<Any> = emptyList(),
        headerVariableIds: List<Int> = emptyList(),
        headerVariable: List<Any> = emptyList(),
        footerVariableIds: List<Int> = emptyList(),
        footerVariable: List<Any> = emptyList(),

        diffUtil: DiffUtil.ItemCallback<DataItem>
    ) : super(diffUtil) {
        this.activity = activity
        this.layoutId = layoutId
        this.headerLayoutId = headerLayoutId
        this.footerLayoutId = footerLayoutId

        this.dataId = dataId
        this.headerDataId = headerDataId
        this.footerDataId = footerDataId

        this.variableIds = variableIds
        this.variables = variables
        this.headerVariableIds = headerVariableIds
        this.headerVariable = headerVariable
        this.footerVariableIds = footerVariableIds
        this.footerVariable = footerVariable
        this.hasFooter = true
        this.hasHeader = true
    }


    constructor(
        activity: FragmentActivity,
        layoutId: Int,
        headerOrFooterLayoutId: Int,
        dataId: Int,
        headerOrFooterDataId: Int,
        variableIds: List<Int> = emptyList(),
        variables: List<Any> = emptyList(),
        headerOrFooterVariableIds: List<Int> = emptyList(),
        headerOrFooterVariable: List<Any> = emptyList(),
        diffUtil: DiffUtil.ItemCallback<DataItem>,
        hasFooter: Boolean // true for footer
    ) : super(diffUtil) {
        this.activity = activity
        this.layoutId = layoutId
        if (hasFooter){
            this.footerLayoutId = headerOrFooterLayoutId
            this.footerDataId = headerOrFooterDataId
            this.footerVariableIds = headerOrFooterVariableIds
            this.footerVariable = headerOrFooterVariable
            this.hasFooter = true
        } else {
            this.headerLayoutId = headerOrFooterLayoutId
            this.headerDataId = headerOrFooterDataId
            this.headerVariableIds = headerOrFooterVariableIds
            this.headerVariable = headerOrFooterVariable
            this.hasHeader = true
        }
        this.dataId = dataId
        this.variableIds = variableIds
        this.variables = variables
    }


    companion object {
        const val TYPE_ITEM_NORMAL = 1
        const val TYPE_ITEM_FOOTER = 2
        const val TYPE_ITEM_HEADER = 3
    }

//    override fun submitList(list: MutableList<T>?) {
//        super.submitList(list)
//        notifyDataSetChanged()
//    }

    class BaseViewHolder<T>(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            activity: FragmentActivity,
            dataId: Int,
            data: DataItem.Item<T>,
            variableIds: List<Int>,
            variables: List<Any>
        ) {
            with(binding) {
                lifecycleOwner = activity
                setVariable(dataId, data)
                for (i in 0 until min(variableIds.size, variables.size)) {
                    setVariable(variableIds[i], variables[i])
                }
                executePendingBindings()
            }
        }
    }

    class HeaderViewHolder<T>(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            activity: FragmentActivity,
            dataId: Int,
            data: DataItem.Header,
            variableIds: List<Int>,
            variables: List<Any>
        ) {
            with(binding) {
                lifecycleOwner = activity
                setVariable(dataId, data)
                for (i in 0 until min(variableIds.size, variables.size)) {
                    setVariable(variableIds[i], variables[i])
                }
                executePendingBindings()
            }
        }
    }

    class FooterViewHolder<T>(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            activity: FragmentActivity,
            dataId: Int,
            data: DataItem.Footer,
            variableIds: List<Int>,
            variables: List<Any>
        ) {
            with(binding) {
                lifecycleOwner = activity
                setVariable(dataId, data)
                for (i in 0 until min(variableIds.size, variables.size)) {
                    setVariable(variableIds[i], variables[i])
                }
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        when (viewType) {
            TYPE_ITEM_HEADER -> {
                val binding: ViewDataBinding =
                    DataBindingUtil.inflate(layoutInflater, headerLayoutId, parent, false)
                return HeaderViewHolder<T>(binding)
            }
            TYPE_ITEM_NORMAL -> {
                val binding: ViewDataBinding =
                    DataBindingUtil.inflate(layoutInflater, layoutId, parent, false)
                return BaseViewHolder<T>(binding)
            }
            TYPE_ITEM_FOOTER -> {
                val binding: ViewDataBinding =
                    DataBindingUtil.inflate(layoutInflater, footerLayoutId, parent, false)
                return FooterViewHolder<T>(binding)
            }
            else -> {
                val binding: ViewDataBinding =
                    DataBindingUtil.inflate(layoutInflater, layoutId, parent, false)
                return BaseViewHolder<T>(binding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            hasFooter && hasHeader -> {
                when (position) {
                    0 -> TYPE_ITEM_HEADER
                    currentList.size - 1 -> TYPE_ITEM_FOOTER
                    else -> TYPE_ITEM_NORMAL
                }
            }
            hasHeader -> {
                when (position) {
                    0 -> TYPE_ITEM_HEADER
                    else -> TYPE_ITEM_NORMAL
                }
            }
            hasFooter -> {
                when (position) {
                    currentList.size - 1 -> TYPE_ITEM_HEADER
                    else -> TYPE_ITEM_NORMAL
                }
            }
            else -> {
                TYPE_ITEM_NORMAL
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position < itemCount) {
            getItem(position)?.apply {
                Timber.i("position: ${position}")
                Timber.i("holder: ${holder}")
                when (holder) {
                    is HeaderViewHolder<*> -> {
                        (holder as HeaderViewHolder<T>).bind(
                            activity,
                            headerDataId,
                            (this as DataItem.Header),
                            headerVariableIds,
                            headerVariable
                        )
                    }
                    is BaseViewHolder<*> -> {
                        (holder as BaseViewHolder<T>).bind(
                            activity,
                            dataId,
                            (this as DataItem.Item<T>),
                            variableIds,
                            variables
                        )
                    }
                    is FooterViewHolder<*> -> {
                        (holder as FooterViewHolder<T>).bind(
                            activity,
                            footerDataId,
                            (this as DataItem.Footer),
                            footerVariableIds,
                            footerVariable
                        )
                    }
                }
            }
        }
    }

//    override fun getItem(position: Int): T {
//        return if (hasHeader){
//            super.getItem(position - 1)
//        } else {
//            super.getItem(position )
//        }
//    }

    fun submitHeaderList(list: List<T>) {
        val temp = arrayListOf<DataItem>()
        if (hasHeader) {
            temp.add(DataItem.Header())
        }
        temp.addAll(list.map {
            DataItem.Item(it)
        })
        if (hasFooter) {
            temp.add(DataItem.Footer())
        }
        submitList(temp)
    }

    sealed class DataItem {

        data class Item<T>(val t: T) : DataItem() {

        }

        class Header() : DataItem() {

        }

        class Footer() : DataItem() {

        }
    }
}