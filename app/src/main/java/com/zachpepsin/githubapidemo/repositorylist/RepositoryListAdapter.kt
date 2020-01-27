package com.zachpepsin.githubapidemo.repositorylist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.zachpepsin.githubapidemo.databinding.RepositoryListItemBinding
import com.zachpepsin.githubapidemo.network.Repository

class RepositoryListAdapter(private val onClickListener: RepositoryItemListener) :
    PagedListAdapter<Repository, RepositoryListAdapter.RepositoryViewHolder>(DiffCallback) {

    /**
     * Create new [RecyclerView] item views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryViewHolder {
        return RepositoryViewHolder.from(parent)
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: RepositoryViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        getItem(position)?.let {
            holder.bind(it, onClickListener)
        }
    }

    /**
     * The RepositoryViewHolder constructor takes the binding variable from the associated
     * list item, which nicely gives it access to the full [Repository] information.
     */
    class RepositoryViewHolder(private var binding: RepositoryListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(repository: Repository, onClickListener: RepositoryItemListener) {
            binding.repository = repository
            binding.onClickListener = onClickListener
            // This is important, because it forces the data binding to execute immediately,
            // which allows the RecyclerView to make the correct view size measurements
            binding.executePendingBindings()
        }

        companion object {
            fun from(
                parent: ViewGroup
            ): RepositoryViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RepositoryListItemBinding.inflate(layoutInflater, parent, false)
                // set the view's size, margins, padding and layout parameters
                //...
                return RepositoryViewHolder(binding)
            }
        }
    }

    /**
     * Allows the RecyclerView to determine which items have changed when the [List] of [Repository]
     * has been updated.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<Repository>() {
        override fun areItemsTheSame(oldItem: Repository, newItem: Repository): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Repository, newItem: Repository): Boolean {
            return oldItem.id == newItem.id
        }
    }
}

/**
 * Custom listener that handles clicks on [RecyclerView] items.  Passes the [Repository]
 * associated with the current item to the [onClick] function.
 * @param onClickListener lambda that will be called with the current [Repository]
 */
class RepositoryItemListener(val onClickListener: (item: Repository) -> Unit) {
    fun onClick(item: Repository) = onClickListener(item)
}
