package com.zachpepsin.githubapidemo.issuelist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.zachpepsin.githubapidemo.network.Issue
import com.zachpepsin.githubapidemo.network.IssueApiService
import com.zachpepsin.githubapidemo.network.IssueApiState
import kotlinx.coroutines.*


class IssueDataSource(
    private val scope: CoroutineScope,
    private val service: IssueApiService,
    private val user: String,
    private val repoName: String,
    private val state: IssueApiState
) :
    PageKeyedDataSource<Int, Issue>() {

    private var supervisorJob = SupervisorJob()
    private val networkState = MutableLiveData<IssueApiStatus>()

    // Keep reference of the last query (so we can retry in case one fails)
    private var retryQuery: (() -> Any)? = null


    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Issue>
    ) {
        retryQuery = { loadInitial(params, callback) }
        executeQuery(1, params.requestedLoadSize) {
            callback.onResult(it, null, 2)
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Issue>) {
        // Unnecessary because we will only be appending to the initial data
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Issue>) {
        val page = params.key
        retryQuery = { loadAfter(params, callback) }
        executeQuery(page, params.requestedLoadSize) {
            callback.onResult(it, page + 1)
        }
    }

    private fun executeQuery(page: Int, perPage: Int, callback: (List<Issue>) -> Unit) {
        networkState.postValue(IssueApiStatus.LOADING)
        scope.launch(getJobErrorHandler() + supervisorJob) {
            val issues = service.getIssues(
                user = user,
                repoName = repoName,
                state = state.value,
                page = page,
                per_page = perPage
            )
            retryQuery = null
            networkState.postValue(IssueApiStatus.DONE)
            callback(issues)
        }
    }

    private fun getJobErrorHandler() = CoroutineExceptionHandler { _, e ->
        Log.e(IssueDataSource::class.java.simpleName, "Error retrieving issues: $e")
        networkState.postValue(IssueApiStatus.ERROR)
    }

    override fun invalidate() {
        super.invalidate()
        supervisorJob.cancelChildren() // Cancel possible running job to only keep last result
    }


    // Public APIs
    fun getNetworkState(): LiveData<IssueApiStatus> =
        networkState

    fun refresh() =
        this.invalidate()

    fun retryFailedQuery() {
        val prevQuery = retryQuery
        retryQuery = null
        prevQuery?.invoke()
    }
}

