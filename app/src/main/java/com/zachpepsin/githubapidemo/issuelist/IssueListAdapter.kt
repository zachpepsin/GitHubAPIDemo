package com.zachpepsin.githubapidemo.issuelist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.zachpepsin.githubapidemo.databinding.IssueListItemBinding
import com.zachpepsin.githubapidemo.network.Issue

class IssueListAdapter(private val onClickListener: IssueItemListener) :
    PagedListAdapter<Issue, IssueListAdapter.IssueViewHolder>(DiffCallback) {

    /**
     * Create new [RecyclerView] item views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
        return IssueViewHolder.from(parent)
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        getItem(position)?.let {
            holder.bind(it, onClickListener)
        }
    }

    /**
     * The IssueViewHolder constructor takes the binding variable from the associated
     * list item, which nicely gives it access to the full [Issue] information.
     */
    class IssueViewHolder(private var binding: IssueListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(issue: Issue, onClickListener: IssueItemListener) {
            with(binding) {
                this.issue = issue
                this.onClickListener = onClickListener
                // This is important, because it forces the data binding to execute immediately,
                // which allows the RecyclerView to make the correct view size measurements
                executePendingBindings()
            }
        }

        companion object {
            fun from(
                parent: ViewGroup
            ): IssueViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = IssueListItemBinding.inflate(layoutInflater, parent, false)
                // set the view's size, margins, padding and layout parameters
                //...
                return IssueViewHolder(binding)
            }
        }
    }

    /**
     * Allows the RecyclerView to determine which items have changed when the [List] of [Issue]
     * has been updated.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<Issue>() {
        override fun areItemsTheSame(oldItem: Issue, newItem: Issue) = oldItem == newItem

        override fun areContentsTheSame(oldItem: Issue, newItem: Issue): Boolean {
            return oldItem.id == newItem.id
                    && oldItem.number == newItem.number
                    && oldItem.title == newItem.title
                    && oldItem.body == newItem.body
                    && oldItem.state == newItem.state
                    && oldItem.created_at == newItem.created_at
        }
    }
}

/**
 * Custom listener that handles clicks on [RecyclerView] items.  Passes the [Issue]
 * associated with the current item to the [onClick] function.
 * @param onClickListener lambda that will be called with the current [Issue]
 */
class IssueItemListener(val onClickListener: (item: Issue) -> Unit) {
    fun onClick(item: Issue) = onClickListener(item)
}

