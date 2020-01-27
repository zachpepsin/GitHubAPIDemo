package com.zachpepsin.githubapidemo.repositorylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RepositoryListViewModelFactory(
    private val user: String
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RepositoryListViewModel::class.java)) {
            return RepositoryListViewModel(user) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}