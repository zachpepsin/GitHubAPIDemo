package com.zachpepsin.githubapidemo.issuelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.zachpepsin.githubapidemo.network.Issue
import com.zachpepsin.githubapidemo.network.IssueApi
import com.zachpepsin.githubapidemo.network.IssueApiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

enum class IssueApiStatus { LOADING, ERROR, DONE }

/**
 * The [ViewModel] that is attached to the [IssueListFragment]].
 */
class IssueListViewModel(
    user: String,
    repoName: String
) : ViewModel() {

    private var issueDataSource: IssueDataSourceFactory

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<IssueApiStatus>()

    // The external immutable LiveData for the request status
    val status: LiveData<IssueApiStatus>
        get() = _status

    // Internally, we use a MutableLiveData, because we will be updating the List of Issue
    // with new values.  Use a PagedList of issues to handle pagination.
    private val _issues: LiveData<PagedList<Issue>>

    // The external LiveData interface to the issue is immutable, so only this class can modify
    val issues: LiveData<PagedList<Issue>>
        get() = _issues

    // Internally, we use a MutableLiveData to handle navigation to the selected issue
    private val _navigateToSelectedIssue = MutableLiveData<Issue>()

    // The external immutable LiveData for the navigation issue
    val navigateToSelectedIssue: LiveData<Issue>
        get() = _navigateToSelectedIssue

    // Internally, we use a MutableLiveData to handle te state dialog event
    private val _eventDisplayStateDialog = MutableLiveData<Boolean>()

    // The external immutable LiveData for the state dialog event
    val eventDisplayStateDialog: LiveData<Boolean>
        get() = _eventDisplayStateDialog

    fun onFabClicked() {
        _eventDisplayStateDialog.value = true
    }

    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    /**
     * Call getIssues() on init so we can display status immediately.
     */
    init {
        val config = PagedList.Config.Builder()
            .setPageSize(30) // The number of items to load per page
            .setEnablePlaceholders(false)
            .build()

        issueDataSource = IssueDataSourceFactory(
            scope = coroutineScope,
            service = IssueApi.retrofitService,
            user = user,
            repoName = repoName,
            state = IssueApiState.SHOW_ALL
        )

        val initializedPagedListBuilder = LivePagedListBuilder<Int, Issue>(issueDataSource, config)

        _issues = initializedPagedListBuilder.build()
    }

    /**
     * When the [ViewModel] is finished, we cancel our coroutine [viewModelJob], which tells the
     * Retrofit service to stop.
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    /**
     * When the issue is clicked, set the [_navigateToSelectedIssue] [MutableLiveData]
     * @param issue The [Issue] that was clicked on.
     */
    fun displayIssueDetails(issue: Issue) {
        _navigateToSelectedIssue.value = issue
    }

    /**
     * After the navigation has taken place, make sure navigateToSelectedIssue is set to null
     */
    fun displayIssueDetailsComplete() {
        _navigateToSelectedIssue.value = null
    }

    /**
     * After the dialog has been displayed, make sure eventDisplayStateDialog is set to false
     */
    fun displayStateDialogComplete() {
        _eventDisplayStateDialog.value = false
    }

    /**
     * Updates the data set state for the web services by querying the data with the new state
     * by calling [updateState] on the data source
     * @param state the [IssueApiState] that is sent as part of the web server request
     */
    fun updateState(state: IssueApiState) {
        issueDataSource.updateState(state)
    }
}
