package com.zachpepsin.githubapidemo.repositorylist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.zachpepsin.githubapidemo.network.Repository
import com.zachpepsin.githubapidemo.network.RepositoryApi

enum class RepositoryApiStatus { LOADING, ERROR, DONE }

/**
 * The [ViewModel] that is attached to the [RepositoryListFragment]].
 */
class RepositoryListViewModel(
    user: String
) : ViewModel() {

    private var repositoryDataSource: RepositoryDataSourceFactory

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<RepositoryApiStatus>()

    // The external immutable LiveData for the request status
    val status: LiveData<RepositoryApiStatus>
        get() = _status

    // Internally, we use a MutableLiveData, because we will be updating the List of Repository
    // with new values  Use a PagedList of repositories to handle pagination.
    private val _repositories: LiveData<PagedList<Repository>>

    // The external LiveData interface to the repository is immutable, so only this class can modify
    val repositories: LiveData<PagedList<Repository>>
        get() = _repositories

    // Internally, we use a MutableLiveData to handle navigation to the selected repository
    private val _navigateToSelectedRepository = MutableLiveData<Repository>()

    // The external immutable LiveData for the navigation repository
    val navigateToSelectedRepository: LiveData<Repository>
        get() = _navigateToSelectedRepository

    // Internally, we use a MutableLiveData to handle te search dialog event
    private val _eventDisplaySearchDialog = MutableLiveData<Boolean>()

    // The external immutable LiveData for the search dialog event
    val eventDisplayStateDialog: LiveData<Boolean>
        get() = _eventDisplaySearchDialog

    fun onFabClicked() {
        _eventDisplaySearchDialog.value = true
    }

    /**
     * Call getRepositories() on init so we can display status immediately.
     */
    init {
        val config = PagedList.Config.Builder()
            .setPageSize(30) // The number of items to load per page
            .setEnablePlaceholders(false)
            .build()

        repositoryDataSource = RepositoryDataSourceFactory(
            service = RepositoryApi.retrofitService,
            user = user,
            query = null // Do not perform a search on first load
        )

        val initializedPagedListBuilder =
            LivePagedListBuilder<Int, Repository>(repositoryDataSource, config)

        _repositories = initializedPagedListBuilder.build()
    }

    /**
     * When the repository is clicked, set the [_navigateToSelectedRepository] [MutableLiveData]
     * @param repository The [Repository] that was clicked on.
     */
    fun displayRepositoryDetails(repository: Repository) {
        _navigateToSelectedRepository.value = repository
    }

    /**
     * After the navigation has taken place, make sure navigateToSelectedRepository is set to null
     */
    fun displayRepositoryDetailsComplete() {
        _navigateToSelectedRepository.value = null
    }

    /**
     * After the dialog has been displayed, make sure eventDisplayStateDialog is set to false
     */
    fun displaySearchDialogComplete() {
        _eventDisplaySearchDialog.value = false
    }

    /**
     * Updates the data set filter for the web services by querying the data with the new filter
     * by calling [updateQuery] on the data source
     * @param query the query string to search for
     */
    fun updateQuery(query: String?) {
        repositoryDataSource.updateQuery(query)
    }


    /**
     * Updates the data set by querying the data with the new user and reset filter
     * by calling [updateUser] on the data source
     * @param user the username to query repos from
     */
    fun updateUser(user: String) {
        repositoryDataSource.updateUser(user)
    }
}
