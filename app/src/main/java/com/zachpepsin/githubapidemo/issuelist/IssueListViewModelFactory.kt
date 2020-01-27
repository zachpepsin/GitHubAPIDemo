package com.zachpepsin.githubapidemo.issuelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class IssueListViewModelFactory(
    private val user: String,
    private val repoName: String
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IssueListViewModel::class.java)) {
            return IssueListViewModel(user, repoName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}