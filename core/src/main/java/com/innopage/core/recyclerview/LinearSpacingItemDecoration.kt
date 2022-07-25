package com.innopage.core.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Benny on 9/23/2016.
 */

class LinearSpacingItemDecoration : RecyclerView.ItemDecoration {

    private var left: Int = 0
    private var top: Int = 0
    private var right: Int = 0
    private var bottom: Int = 0
    private var first: Int = 0
    private var last: Int = 0
    private var showFirstDivider: Boolean = false
    private var showLastDivider: Boolean = false

    constructor(spacing: Int, showFirstDivider: Boolean, showLastDivider: Boolean) {
        this.left = spacing
        this.top = spacing
        this.right = spacing
        this.bottom = spacing
        this.first = spacing
        this.last = spacing
        this.showFirstDivider = showFirstDivider
        this.showLastDivider = showLastDivider
    }

    constructor(
        spacing: Int,
        first: Int,
        last: Int,
        showFirstDivider: Boolean,
        showLastDivider: Boolean
    ) {
        this.left = spacing
        this.top = spacing
        this.right = spacing
        this.bottom = spacing
        this.first = first
        this.last = last
        this.showFirstDivider = showFirstDivider
        this.showLastDivider = showLastDivider
    }

    constructor(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        first: Int,
        last: Int,
        showFirstDivider: Boolean,
        showLastDivider: Boolean
    ) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
        this.first = first
        this.last = last
        this.showFirstDivider = showFirstDivider
        this.showLastDivider = showLastDivider
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        parent.adapter?.let {
            val position = parent.getChildAdapterPosition(view)
            val linearLayoutManager = parent.layoutManager as LinearLayoutManager

            when (linearLayoutManager.orientation) {
                RecyclerView.VERTICAL -> {
                    outRect.left = left
                    outRect.right = right

                    if (showFirstDivider) {
                        if (linearLayoutManager.reverseLayout) {
                            outRect.top = if (position == it.itemCount - 1) first else 0
                        } else {
                            outRect.top = if (position == 0) first else 0
                        }
                    } else {
                        outRect.top = 0
                    }

                    if (showLastDivider) {
                        if (linearLayoutManager.reverseLayout) {
                            outRect.bottom = if (position == 0) last else bottom
                        } else {
                            outRect.bottom = if (position == it.itemCount - 1) last else bottom
                        }
                    } else {
                        if (linearLayoutManager.reverseLayout) {
                            outRect.bottom = if (position == 0) 0 else bottom
                        } else {
                            outRect.bottom = if (position == it.itemCount - 1) 0 else bottom
                        }
                    }
                }
                RecyclerView.HORIZONTAL -> {
                    outRect.top = top
                    outRect.bottom = bottom

                    if (showFirstDivider) {
                        outRect.left = if (position == 0) first else 0
                    } else {
                        outRect.left = 0
                    }

                    if (showLastDivider) {
                        outRect.right = if (position == it.itemCount - 1) last else right
                    } else {
                        outRect.right = if (position == it.itemCount - 1) 0 else right
                    }
                }
                else -> {
                    outRect.left = left
                    outRect.right = right
                    outRect.top = top
                    outRect.bottom = bottom
                }
            }
        }
    }
}
