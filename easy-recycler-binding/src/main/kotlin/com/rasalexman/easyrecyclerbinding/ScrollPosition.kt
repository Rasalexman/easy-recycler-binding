package com.rasalexman.easyrecyclerbinding

data class ScrollPosition(var index: Int = 0, var top: Int = 0) {
    fun drop() {
        index = 0
        top = 0
    }
}

/*
// если хотим сохранить последнюю проскролленную позицию
    protected open fun savePreviousPosition() {
        recycler?.let { rec ->
            stopRecyclerScroll()
            previousPosition?.apply {
                index = (rec.layoutManager as? LinearLayoutManager?)?.findFirstVisibleItemPosition()
                    ?: 0
                top = rec.getChildAt(0)?.let { it.top - rec.paddingTop } ?: 0
            }
        }
    }

    // прокручиваем к ранее выбранным элементам
    open fun applyScrollPosition() {
        recycler?.let { rec ->
            stopRecyclerScroll()
            previousPosition?.let {
                (rec.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                    it.index,
                    it.top
                )
            }
        }
    }
 */
