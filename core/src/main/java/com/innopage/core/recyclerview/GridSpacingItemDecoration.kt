package com.innopage.core.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Benny on 9/23/2016.
 */

class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val headerNum: Int,
    private val showFirstDivider: Boolean,
    private val showLastDivider: Boolean,
    private val showTopDivider: Boolean,
    private val showBottomDivider: Boolean,
    private val sectionIndexes: List<Int>
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        parent.adapter?.let {
            var position = parent.getChildAdapterPosition(view) - headerNum
            if (position >= 0) {
                if (sectionIndexes.contains(position)) {
                    outRect.left = 0
                    outRect.right = 0
                    outRect.bottom = 0

                    if (showTopDivider) {
                        outRect.top = if (position < spanCount) spacing else 0
                    } else {
                        outRect.top = 0
                    }
                } else {
                    var offset = 0
                    for (sectionIndex in sectionIndexes) {
                        if (position > sectionIndex) {
                            offset++
                        }
                    }
                    position += offset
                    val column = position % spanCount

                    val left = spacing - column * spacing / spanCount
                    if (showFirstDivider) {
                        outRect.left = left
                    } else {
                        outRect.left = 0
                    }

                    val right = (column + 1) * spacing / spanCount
                    if (showLastDivider) {
                        outRect.right = right
                    } else {
                        outRect.right = if (column == spanCount - 1) 0 else right
                    }

                    val top = spacing
                    if (showTopDivider) {
                        outRect.top = if (position < spanCount) top else 0
                    } else {
                        outRect.top = 0
                    }

                    val lastRowCount = (it.itemCount % spanCount).takeUnless { it == 0 }
                        ?: spanCount

                    val bottom = spacing
                    if (showBottomDivider) {
                        outRect.bottom = bottom
                    } else {
                        outRect.bottom = if (position >= it.itemCount - lastRowCount) 0 else bottom
                    }
                }
            } else {
                outRect.left = 0
                outRect.right = 0
                outRect.top = 0
                outRect.bottom = 0
            }
        }
    }
}
