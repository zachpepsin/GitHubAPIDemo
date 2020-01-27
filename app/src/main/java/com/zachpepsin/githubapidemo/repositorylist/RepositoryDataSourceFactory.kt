package com.zachpepsin.githubapidemo.repositorylist

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.zachpepsin.githubapidemo.network.Repository
import com.zachpepsin.githubapidemo.network.RepositoryApiService
import kotlinx.coroutines.CoroutineScope

class RepositoryDataSourceFactory(
    private val scope: CoroutineScope,
    private val service: RepositoryApiService,
    private var user: String,
    private var query: String?
) : DataSource.Factory<Int, Repository>() {

    private val sourceLiveData = MutableLiveData<RepositoryDataSource>()

    override fun create(): DataSource<Int, Repository> {
        val latestSource = RepositoryDataSource(scope, service, user, query)
        sourceLiveData.postValue(latestSource)
        return latestSource
    }

    // Public APIs
    fun updateUser(user: String) {
        this.user = user
        this.query = null
        sourceLiveData.value?.refresh()
    }

    fun updateQuery(query: String?) {
        this.query = query
        sourceLiveData.value?.refresh()
    }
}