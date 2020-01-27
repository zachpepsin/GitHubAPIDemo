package com.zachpepsin.githubapidemo.issuelist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.zachpepsin.githubapidemo.R
import com.zachpepsin.githubapidemo.databinding.FragmentIssueListBinding
import com.zachpepsin.githubapidemo.network.IssueApiState
import com.zachpepsin.githubapidemo.network.Repository

class IssueListFragment : Fragment() {
    private lateinit var user: String
    private lateinit var repository: Repository

    // Keeps track of which state was selected previously
    private var selectedState = 0

    /**
     * Lazily initialize our [IssueListViewModel]
     */
    private val viewModel: IssueListViewModel by lazy {
        ViewModelProvider(this, IssueListViewModelFactory(user, repository.name))
            .get(IssueListViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            user = IssueListFragmentArgs.fromBundle(it).user
            repository = IssueListFragmentArgs.fromBundle(it).repository
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentIssueListBinding.inflate(inflater)

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        // Giving the binding access to the IssueListViewModel
        binding.viewModel = viewModel

        // Sets the adapter of the Issue RecyclerView with clickHandler lambda that
        // tells the viewModel when our issue is clicked
        val issueListAdapter = IssueListAdapter(IssueItemListener { issue ->
            viewModel.displayIssueDetails(issue)
        })

        binding.recyclerIssue.apply {
            itemAnimator = DefaultItemAnimator()
            adapter = issueListAdapter

            // Add divider for recycler
            addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // Hide FAB when scrolling down, show when scrolling up
                    if (dy > 0) {
                        binding.fabIssueList.hide()
                    } else {
                        binding.fabIssueList.show()
                    }
                }
            })
        }

        // Observe the eventDisplayStateDialog LiveData and display the dialog when it is true
        // After navigating, call displayStateDialogComplete() so that the ViewModel is ready
        // for another state dialog event.
        viewModel.eventDisplayStateDialog.observe(viewLifecycleOwner, Observer { issue ->
            if (issue) {
                // Show a radio button dialog to select a state filter
                val builder = AlertDialog.Builder(context!!)
                    .setTitle(getString(R.string.dialog_header_state_filter))
                    .setSingleChoiceItems(
                        IssueApiState.toArray(),
                        selectedState
                    ) { dialog, which ->
                        viewModel.updateState(IssueApiState.values()[which])
                        selectedState = which
                        dialog.dismiss()
                    }
                val dialog: AlertDialog = builder.create() // Create the alert dialog using builder
                dialog.show() // Display the dialog

                // Tell the ViewModel we've displayed the dialog to prevent multiple dialogs
                viewModel.displayStateDialogComplete()
            }
        })

        // Observe the navigateToSelectedIssue LiveData and Navigate when it isn't null
        // After navigating, call displayIssueDetailsComplete() so that the ViewModel is ready
        // for another navigation event.
        viewModel.navigateToSelectedIssue.observe(viewLifecycleOwner, Observer { issue ->
            if (issue != null) {
                // Go to the URL for the issue
                val url = "https://github.com/$user/${repository.name}/issues/${issue.number}"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                activity?.startActivity(i)

                // Tell the ViewModel we've made the navigate call to prevent multiple navigation
                viewModel.displayIssueDetailsComplete()
            }
        })

        return binding.root
    }

}
