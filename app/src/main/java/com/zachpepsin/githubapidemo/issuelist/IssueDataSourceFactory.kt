package com.zachpepsin.githubapidemo.issuelist

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.zachpepsin.githubapidemo.network.Issue
import com.zachpepsin.githubapidemo.network.IssueApiService
import com.zachpepsin.githubapidemo.network.IssueApiState
import kotlinx.coroutines.CoroutineScope

class IssueDataSourceFactory(
    private val scope: CoroutineScope,
    private val service: IssueApiService,
    private var user: String,
    private var repoName: String,
    private var state: IssueApiState
) : DataSource.Factory<Int, Issue>() {

    private val sourceLiveData = MutableLiveData<IssueDataSource>()

    override fun create(): DataSource<Int, Issue> {
        val latestSource = IssueDataSource(scope, service, user, repoName, state)
        sourceLiveData.postValue(latestSource)
        return latestSource
    }

    // Public APIs
    fun updateState(state: IssueApiState) {
        this.state = state
        sourceLiveData.value?.refresh()
    }
}