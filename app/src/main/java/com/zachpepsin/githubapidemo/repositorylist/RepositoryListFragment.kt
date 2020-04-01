package com.zachpepsin.githubapidemo.repositorylist

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.zachpepsin.githubapidemo.R
import com.zachpepsin.githubapidemo.databinding.FragmentRepositoryListBinding

class RepositoryListFragment : Fragment(), OnSharedPreferenceChangeListener {
    private lateinit var user: String

    private lateinit var sharedPreferences: SharedPreferences

    /**
     * Lazily initialize our [RepositoryListViewModel]
     */
    private val viewModel: RepositoryListViewModel by lazy {
        ViewModelProvider(this, RepositoryListViewModelFactory(user))
            .get(RepositoryListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    /**
     * Inflates the layout with Data Binding, sets its lifecycle owner to the [RepositoryListFragment]
     * to enable Data Binding to observe LiveData, and sets up the RecyclerView with an adapter.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)

        // Initialize the user using its shared pref, or use it's default if not found
        //val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val defaultUser = resources.getString(R.string.default_user)
        user = sharedPreferences.getString(getString(R.string.preference_user_key), defaultUser)
            ?: defaultUser

        if (user.isBlank()) {
            // If the user is blank, use the default user
            user = getString(R.string.default_user)
        }

        val binding = FragmentRepositoryListBinding.inflate(inflater)

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        // Giving the binding access to the RepositoryListViewModel
        binding.viewModel = viewModel

        // Sets the adapter of the Repository RecyclerView with clickHandler lambda that
        // tells the viewModel when our repository is clicked
        val repositoryListAdapter = RepositoryDataAdapter(RepositoryItemListener { repository ->
            viewModel.displayRepositoryDetails(repository)
        })

        binding.recyclerRepository.apply {
            itemAnimator = DefaultItemAnimator()
            adapter = repositoryListAdapter

            // Add divider for recycler
            addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // Hide FAB when scrolling down, show when scrolling up
                    if (dy > 0) {
                        binding.fabRepositoryList.hide()
                    } else {
                        binding.fabRepositoryList.show()
                    }
                }
            })
        }

        viewModel.repositories.observe(viewLifecycleOwner, Observer { pagingData ->
            repositoryListAdapter.submitData(lifecycle, pagingData)
        })


        // Observe the eventDisplayStateDialog LiveData and display the dialog when it is true
        // After navigating, call displayStateDialogComplete() so that the ViewModel is ready
        // for another state dialog event.
        viewModel.eventDisplayStateDialog.observe(viewLifecycleOwner, Observer { issue ->
            if (issue) {
                activity?.let {
                    val builder = AlertDialog.Builder(it)

                    // Inflate and set the layout for the dialog
                    val dialogInflater = requireActivity().layoutInflater

                    // Pass null as the parent view because its going in the dialog layout
                    @SuppressLint("InflateParams")
                    val view = dialogInflater.inflate(R.layout.dialog_repository_search, null)

                    val searchEditText =
                        view.findViewById(R.id.edit_text_repository_search) as EditText
                    builder.setView(view)
                        // Add action buttons
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            // Perform a search with the input text
                            val searchText = searchEditText.text.toString()
                            viewModel.updateQuery(searchText)
                        }
                        .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                            dialog.cancel()
                        }
                        .setNeutralButton(getString(R.string.reset)) { _, _ ->
                            // Reset button.  Perform non-search query
                            viewModel.updateQuery(null)
                        }
                    builder.create()
                    builder.show() // Display the dialog

                    // Tell the ViewModel we've displayed the dialog to prevent multiple dialogs
                    viewModel.displaySearchDialogComplete()
                } ?: throw IllegalStateException("Activity cannot be null")
            }
        })


        // Observe the navigateToSelectedRepository LiveData and Navigate when it isn't null
        // After navigating, call displayRepositoryDetailsComplete() so that the ViewModel is ready
        // for another navigation event.
        viewModel.navigateToSelectedRepository.observe(viewLifecycleOwner, Observer { repository ->
            if (repository != null) {
                // Must find the NavController from the Fragment
                this.findNavController().navigate(
                    RepositoryListFragmentDirections.actionRepositoryListFragmentToIssueListFragment(
                        user, repository
                    )
                )
                // Tell the ViewModel we've made the navigate call to prevent multiple navigation
                viewModel.displayRepositoryDetailsComplete()
            }
        })

        return binding.root
    }

    override fun onDestroy() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_repository_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item, view!!.findNavController())
                || super.onOptionsItemSelected(item)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.d("RepositoryListFragment", "SHARED PREF CHANGED.")
        when (key) {
            getString(R.string.preference_user_key) -> {
                // User changed.  Pass the new user into the view model and update
                var newUser =
                    sharedPreferences!!.getString(getString(R.string.preference_user_key), "")
                        ?: user
                if (newUser.isBlank()) {
                    // If the user is blank, use the default user
                    newUser = getString(R.string.default_user)
                }
                viewModel.updateUser(newUser)
            }
        }
    }
}
