package com.rasalexman.erb.common

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.SpannedString
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import java.util.*

object StringUtils {

    fun getFilterSearchColoredText(
        query: String,
        original: String,
        bold: Boolean
    ): SpannedString {
        return buildSpannedString {
            if (bold) {
                bold {
                    onSearchQuery(query, original)
                }
            } else {
                onSearchQuery(query, original)
            }
        }
    }

    private fun SpannableStringBuilder.onSearchQuery(query: String, original: String) {
        if (query.isNotEmpty() && original.isNotEmpty()) {
            val lowerQuery = query.substring(0).lowercase(Locale.getDefault())
            getColoredSpanText(lowerQuery, original)
        } else {
            append(original)
        }
    }

    private fun SpannableStringBuilder.getColoredSpanText(
        query: String,
        original: String
    ): Boolean {
        val indexOfAny = original.indexOf(query, 0, true)
        val isHasQuery = indexOfAny > -1
        if (isHasQuery) {
            val lowerOriginal = original.substring(0).lowercase()
            val beforeText = lowerOriginal.substringBefore(query)
            val afterText = lowerOriginal.substringAfter(query)

            val start = beforeText.length
            val finish = start + query.length
            val last = finish + afterText.length
            val startText = original.substring(0, start)
            val queryText = original.substring(start, finish)
            val endText = original.substring(finish, last)

            append(startText)
            color(Color.RED) {
                append(queryText)
            }
            append(endText)
        } else {
            append(original)
        }
        return isHasQuery
    }
}