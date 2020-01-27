package com.zachpepsin.githubapidemo.issuelist

import android.os.Build
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import com.zachpepsin.githubapidemo.R
import com.zachpepsin.githubapidemo.network.Issue
import com.zachpepsin.githubapidemo.network.IssueState
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * When there is no issue data (data is null), hide the [RecyclerView], otherwise show it.
 */

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: PagedList<Issue>?) {
    val adapter = recyclerView.adapter as IssueListAdapter
    adapter.submitList(data)
}


@BindingAdapter("issueState")
fun TextView.setIssueState(item: IssueState) {
    setTextColor(
        if (item == IssueState.open) ContextCompat.getColor(
            context,
            R.color.issue_state_open
        ) else ContextCompat.getColor(context, R.color.issue_state_closed)
    )
}

@BindingAdapter("date")
fun TextView.setDate(dateString: String?) {
    text = if (dateString.isNullOrBlank()) {
        context.getString(R.string.unknown)
    } else {
        val origDateFormatString = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        val newDateFormatString = "d MMMM YYYY"
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val origFormat = DateTimeFormatter.ofPattern(origDateFormatString)
                val date = LocalDateTime.parse(dateString, origFormat)
                val newFormat = DateTimeFormatter.ofPattern(newDateFormatString)
                date.format(newFormat)
            } else {
                val origFormat = SimpleDateFormat(origDateFormatString, Locale.US)
                val date = origFormat.parse(dateString)
                if (date == null) {
                    context.getString(R.string.unknown)
                    return
                }
                val newFormat = SimpleDateFormat(newDateFormatString, Locale.US)
                newFormat.format(date)
            }
        } catch (e: Exception) {
            Log.e("BindingAdapter", "Unable to parse date. $e")
            context.getString(R.string.unknown)
        }
    }
}