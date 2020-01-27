package com.zachpepsin.githubapidemo.network

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

enum class IssueState {
    open,
    closed;
}

@Parcelize
data class Issue(
    val id: String,
    val number: String,
    val title: String,
    val body: String,
    val state: IssueState,
    val created_at: String
) : Parcelable