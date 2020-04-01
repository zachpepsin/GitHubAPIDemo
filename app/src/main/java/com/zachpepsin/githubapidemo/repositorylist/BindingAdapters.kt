package com.zachpepsin.githubapidemo.repositorylist

import androidx.databinding.BindingAdapter
import androidx.paging.PagedList
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import com.zachpepsin.githubapidemo.network.Repository

/**
 * When there is no repository data (data is null), hide the [RecyclerView], otherwise show it.
 */

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: PagingData<Repository>?) {
    val adapter = recyclerView.adapter as RepositoryDataAdapter
    adapter.submitData(data)
}

