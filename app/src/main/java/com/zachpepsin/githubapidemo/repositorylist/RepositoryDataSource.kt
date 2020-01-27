package com.zachpepsin.githubapidemo.repositorylist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.zachpepsin.githubapidemo.network.Repository
import com.zachpepsin.githubapidemo.network.RepositoryApiService
import kotlinx.coroutines.*

class RepositoryDataSource(
    private val scope: CoroutineScope,
    private val service: RepositoryApiService,
    private val user: String,
    private val query: String?
) : PageKeyedDataSource<Int, Repository>() {

    private var supervisorJob = SupervisorJob()
    private val networkState = MutableLiveData<RepositoryApiStatus>()

    // Keep reference of the last query (so we can retry in case one fails)
    private var retryQuery: (() -> Any)? = null

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Repository>
    ) {
        retryQuery = { loadInitial(params, callback) }
        executeQuery(1, params.requestedLoadSize) {
            callback.onResult(it, null, 2)
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Repository>) {
        // Unnecessary because we will only be appending to the initial data
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Repository>) {
        val page = params.key
        retryQuery = { loadAfter(params, callback) }
        executeQuery(page, params.requestedLoadSize) {
            callback.onResult(it, page + 1)
        }
    }

    private fun executeQuery(page: Int, perPage: Int, callback: (List<Repository>) -> Unit) {
        networkState.postValue(RepositoryApiStatus.LOADING)
        scope.launch(getJobErrorHandler() + supervisorJob) {
            val repositories =
                if (query.isNullOrBlank()) {
                    // We are not performing a search
                    service.getRepositories(
                        user = user,
                        page = page,
                        per_page = perPage
                    )
                } else {
                    // We are performing a search
                    service.getRepositoriesQuery(
                        query = "$query+user:$user",
                        page = page,
                        per_page = perPage
                    ).items
                }

            retryQuery = null
            networkState.postValue(RepositoryApiStatus.DONE)
            callback(repositories)
        }
    }

    private fun getJobErrorHandler() = CoroutineExceptionHandler { _, e ->
        Log.e(RepositoryDataSource::class.java.simpleName, "Error retrieving repositories: $e")
        networkState.postValue(RepositoryApiStatus.ERROR)
    }

    override fun invalidate() {
        super.invalidate()
        supervisorJob.cancelChildren() // Cancel possible running job to only keep last result
    }


    // Public APIs
    fun getNetworkState(): LiveData<RepositoryApiStatus> =
        networkState

    fun refresh() =
        this.invalidate()

    fun retryFailedQuery() {
        val prevQuery = retryQuery
        retryQuery = null
        prevQuery?.invoke()
    }
}