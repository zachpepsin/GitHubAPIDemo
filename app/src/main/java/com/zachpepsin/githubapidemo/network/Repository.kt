package com.zachpepsin.githubapidemo.network

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Repository(
    val id: String,
    val name: String,
    val description: String?
) : Parcelable

@Parcelize
data class RepositoryQuery(
    val total_count: Int,
    val items: List<Repository>
) : Parcelable

