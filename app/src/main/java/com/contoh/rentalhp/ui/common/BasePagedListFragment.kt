package com.contoh.rentalhp.ui.common

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.model.PagedData
import com.contoh.rentalhp.databinding.FragmentPagedListBinding
import com.contoh.rentalhp.util.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class BasePagedListFragment<T> : Fragment() {
    private var viewBinding: FragmentPagedListBinding? = null
    protected val binding: FragmentPagedListBinding
        get() = requireNotNull(viewBinding)

    private val loadedItems = mutableListOf<T>()
    private lateinit var listAdapter: EntityListAdapter<T>
    private var currentPage = 0
    private var totalItems = 0L
    private var loading = false
    private var mutationRunning = false
    private var activeQuery: String? = null
    private var generation = 0
    private var loadJob: Job? = null

    protected open val supportsSearch = true
    protected open val showAddButton = false
    protected open val usePhoneGrid = false
    protected open val welcomeMessage: String? = null
    protected open val shortcutText: String? = null
    protected open val searchHint: String
        get() = getString(R.string.search)
    protected open val addButtonText: String
        get() = getString(R.string.add)
    protected open val emptyTitle: String
        get() = getString(R.string.empty_data)
    protected open val emptyMessage: String
        get() = getString(R.string.empty_data_description)

    abstract suspend fun requestPage(page: Int, pageSize: Int, search: String?): PagedData<T>
    abstract fun createRow(item: T): ListRow<T>

    protected open fun onItemClick(item: T) = Unit
    protected open fun onPrimaryAction(item: T) = Unit
    protected open fun onSecondaryAction(item: T) = Unit
    protected open fun onAddClick() = Unit
    protected open fun onShortcutClick() = Unit
    protected open fun summaryText(count: Long): String = resources.getQuantityString(
        R.plurals.list_count, count.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), count
    )
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflatedBinding = FragmentPagedListBinding.inflate(inflater, container, false)
        viewBinding = inflatedBinding
        return inflatedBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        UiStyleUtil.styleRefresh(binding.swipeRefresh)
        binding.searchField.hint = searchHint
        binding.cardWelcome.visibility = if (welcomeMessage == null) View.GONE else View.VISIBLE
        welcomeMessage?.let {
            val name = SessionManager.getInstance(requireContext()).currentUser?.nama.orEmpty()
            binding.textGreeting.text = getString(R.string.greeting_named, name)
            binding.textWelcomeMessage.text = it
        }
        binding.buttonShortcut.apply {
            visibility = if (shortcutText == null) View.GONE else View.VISIBLE
            text = shortcutText
            setOnClickListener { onShortcutClick() }
        }
        listAdapter = EntityListAdapter(
            onItemClick = ::onItemClick,
            onPrimaryAction = ::onPrimaryAction,
            onSecondaryAction = ::onSecondaryAction
        )
        binding.recyclerView.apply {
            val width = resources.configuration.screenWidthDp
            val columns = when {
                width >= 840 -> 3
                width >= 600 -> 2
                else -> 1
            }
            layoutManager = if (usePhoneGrid) GridLayoutManager(requireContext(), columns) else LinearLayoutManager(requireContext())
            adapter = listAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layout = recyclerView.layoutManager as LinearLayoutManager
                    if (dy > 0 && layout.findLastVisibleItemPosition() >= listAdapter.itemCount - 4) {
                        loadNextPage()
                    }
                }
            })
        }
        binding.layoutSearch.visibility = if (supportsSearch) View.VISIBLE else View.GONE
        binding.buttonSearch.setOnClickListener { search() }
        binding.inputSearch.setOnEditorActionListener { _, _, _ ->
            search()
            true
        }
        binding.swipeRefresh.setOnRefreshListener { refreshList(showLoading = false) }
        binding.buttonRetry.setOnClickListener { refreshList() }
        binding.buttonAdd.apply {
            visibility = if (showAddButton) View.VISIBLE else View.GONE
            text = addButtonText
            setOnClickListener { onAddClick() }
        }
        refreshList()
    }

    protected fun refreshList(showLoading: Boolean = true) {
        load(reset = true, showFullLoading = showLoading)
    }

    protected fun confirmMutation(
        title: String,
        message: String,
        positiveText: String,
        action: suspend () -> String
    ) {
        if (mutationRunning) return
        DialogHelper.confirm(requireContext(), title, message, positiveText) {
            if (mutationRunning) return@confirm
            mutationRunning = true
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val successMessage = action()
                    Snackbar.make(binding.root, successMessage, Snackbar.LENGTH_SHORT).show()
                    refreshList(showLoading = false)
                } catch (error: Throwable) {
                    Snackbar.make(binding.root, ApiErrorHelper.getMessage(error), Snackbar.LENGTH_LONG).show()
                } finally {
                    mutationRunning = false
                }
            }
        }
    }

    private fun search() {
        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(binding.inputSearch.windowToken, 0)
        activeQuery = binding.inputSearch.text?.toString()?.trim()?.takeIf { it.isNotBlank() }
        refreshList()
    }

    private fun loadNextPage() {
        if (!loading && loadedItems.size.toLong() < totalItems) {
            load(reset = false, showFullLoading = false)
        }
    }

    private fun load(reset: Boolean, showFullLoading: Boolean) {
        if (!reset && loading) return
        if (reset) {
            generation += 1
            loadJob?.cancel()
            loading = false
        }
        val requestGeneration = generation
        loading = true
        val targetPage = if (reset) 1 else currentPage + 1
        if (showFullLoading) {
            if (loadedItems.isEmpty()) showLoadingState() else binding.swipeRefresh.isRefreshing = true
        }
        binding.progressMore.visibility = if (!reset) View.VISIBLE else View.GONE

        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val page = requestPage(targetPage, PAGE_SIZE, activeQuery)
                if (requestGeneration != generation) return@launch
                if (reset) loadedItems.clear()
                val knownIds = loadedItems.mapTo(mutableSetOf()) { createRow(it).id }
                page.items.forEach { item ->
                    if (knownIds.add(createRow(item).id)) loadedItems.add(item)
                }
                currentPage = page.page
                totalItems = page.total
                binding.textSummary.text = if (activeQuery == null) summaryText(totalItems) else resources.getQuantityString(
                    R.plurals.search_results_count, totalItems.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), totalItems
                )
                binding.textSummary.visibility = View.VISIBLE
                listAdapter.submitList(loadedItems.map(::createRow))
                if (loadedItems.isEmpty()) showEmptyState() else showContentState()
            } catch (error: Throwable) {
                if (requestGeneration != generation) return@launch
                if (loadedItems.isEmpty()) {
                    showErrorState(ApiErrorHelper.getMessage(error))
                } else {
                    Snackbar.make(binding.root, ApiErrorHelper.getMessage(error), Snackbar.LENGTH_LONG).show()
                    showContentState()
                }
            } finally {
                if (requestGeneration == generation) {
                    loading = false
                    viewBinding?.let {
                        it.swipeRefresh.isRefreshing = false
                        it.progressMore.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun showLoadingState() {
        binding.recyclerView.visibility = View.GONE
        binding.layoutState.visibility = View.VISIBLE
        binding.progressState.visibility = View.VISIBLE
        binding.imageState.visibility = View.GONE
        binding.textStateTitle.setText(R.string.loading_data)
        binding.textStateMessage.visibility = View.GONE
        binding.buttonRetry.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.recyclerView.visibility = View.GONE
        binding.layoutState.visibility = View.VISIBLE
        binding.progressState.visibility = View.GONE
        binding.imageState.visibility = View.VISIBLE
        binding.imageState.setImageResource(R.drawable.ic_empty)
        binding.textStateTitle.text = emptyTitle
        binding.textStateMessage.text = emptyMessage
        binding.textStateMessage.visibility = View.VISIBLE
        binding.buttonRetry.visibility = View.GONE
    }

    private fun showErrorState(message: String) {
        binding.recyclerView.visibility = View.GONE
        binding.layoutState.visibility = View.VISIBLE
        binding.progressState.visibility = View.GONE
        binding.imageState.visibility = View.VISIBLE
        binding.imageState.setImageResource(R.drawable.ic_error)
        binding.textStateTitle.setText(R.string.failed_load_data)
        binding.textStateMessage.text = message
        binding.textStateMessage.visibility = View.VISIBLE
        binding.buttonRetry.visibility = View.VISIBLE
    }

    private fun showContentState() {
        binding.recyclerView.visibility = View.VISIBLE
        binding.layoutState.visibility = View.GONE
    }

    override fun onDestroyView() {
        loadJob?.cancel()
        binding.recyclerView.adapter = null
        viewBinding = null
        super.onDestroyView()
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
