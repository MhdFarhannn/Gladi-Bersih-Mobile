package com.contoh.rentalhp.ui.peminjam

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.data.model.AlatResponse
import com.contoh.rentalhp.data.model.PeminjamanItemRequest
import com.contoh.rentalhp.data.model.PeminjamanRequest
import com.contoh.rentalhp.databinding.ActivityPeminjamanFormBinding
import com.contoh.rentalhp.ui.common.ApiErrorHelper
import com.contoh.rentalhp.ui.common.AccessibilityUtil
import com.contoh.rentalhp.ui.common.BaseActivity
import com.contoh.rentalhp.ui.common.DatePickerHelper
import com.contoh.rentalhp.ui.common.UiStyleUtil
import com.contoh.rentalhp.util.FormatUtil
import com.contoh.rentalhp.util.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate

class PeminjamanFormActivity : BaseActivity() {
    private lateinit var binding: ActivityPeminjamanFormBinding
    private lateinit var adapter: RentalSelectionAdapter
    private val catalogItems = mutableListOf<AlatResponse>()
    private val pinnedItems = linkedMapOf<Long, AlatResponse>()
    private val selectedQuantities = linkedMapOf<Long, Int>()
    private var currentPage = 0
    private var totalItems = 0L
    private var loading = false
    private var activeSearch: String? = null
    private var startDate = FormatUtil.todayApiDate()
    private var endDate = LocalDate.now().plusDays(1).toString()
    private var rentalId = 0L
    private var existingBorrowerId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPeminjamanFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rentalId = intent.getLongExtra(EXTRA_RENTAL_ID, 0L)
        setupViews()
        loadInitialData()
    }

    private fun setupViews() {
        UiStyleUtil.styleRefresh(binding.swipeRefresh)
        binding.toolbar.title = if (rentalId > 0) getString(R.string.edit_rental) else getString(R.string.apply_rental)
        binding.toolbar.setNavigationOnClickListener { finish() }
        adapter = RentalSelectionAdapter(
            quantityFor = { selectedQuantities[it] ?: 0 },
            onQuantityChanged = { item, quantity ->
                if (quantity > 0) {
                    pinnedItems[item.id] = item
                    selectedQuantities[item.id] = quantity
                } else {
                    selectedQuantities.remove(item.id)
                    pinnedItems.remove(item.id)
                }
                updateEstimate()
            }
        )
        binding.recyclerPhones.layoutManager = LinearLayoutManager(this)
        binding.recyclerPhones.adapter = adapter
        binding.recyclerPhones.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layout = recyclerView.layoutManager as LinearLayoutManager
                if (dy > 0 && layout.findLastVisibleItemPosition() >= adapter.itemCount - 4) loadNextPage()
            }
        })
        binding.inputStartDate.setText(FormatUtil.formatDate(startDate))
        binding.inputEndDate.setText(FormatUtil.formatDate(endDate))
        binding.inputStartDate.setOnClickListener {
            DatePickerHelper.show(supportFragmentManager, getString(R.string.start_date), startDate) {
                startDate = it
                binding.inputStartDate.setText(FormatUtil.formatDate(it))
                binding.layoutStartDate.error = null
                updateEstimate()
            }
        }
        AccessibilityUtil.activateOnKeyboard(binding.inputStartDate)
        binding.inputEndDate.setOnClickListener {
            DatePickerHelper.show(supportFragmentManager, getString(R.string.expected_return_date), endDate) {
                endDate = it
                binding.inputEndDate.setText(FormatUtil.formatDate(it))
                binding.layoutEndDate.error = null
                updateEstimate()
            }
        }
        AccessibilityUtil.activateOnKeyboard(binding.inputEndDate)
        binding.buttonSearch.setOnClickListener {
            if (loading) return@setOnClickListener
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(binding.inputSearch.windowToken, 0)
            activeSearch = binding.inputSearch.text?.toString()?.trim()?.takeIf { it.isNotBlank() }
            loadCatalog(reset = true, showLoading = true)
        }
        binding.inputSearch.setOnEditorActionListener { _, _, _ ->
            binding.buttonSearch.performClick()
            true
        }
        binding.swipeRefresh.setOnRefreshListener { loadCatalog(reset = true, showLoading = false) }
        binding.buttonRetry.setOnClickListener { loadInitialData() }
        binding.buttonSubmit.setOnClickListener { submit() }
        updateEstimate()
    }

    private fun loadInitialData() {
        if (loading) return
        loading = true
        showLoadingState()
        lifecycleScope.launch {
            try {
                if (rentalId > 0) {
                    val rentalResponse = RetrofitClient.apiService.getPeminjamanDetail(rentalId)
                    val rental = rentalResponse.data ?: error(rentalResponse.message)
                    existingBorrowerId = rental.idPeminjam
                    startDate = rental.tanggalPeminjaman
                    endDate = rental.tanggalRencanaKembali
                    binding.inputStartDate.setText(FormatUtil.formatDate(startDate))
                    binding.inputEndDate.setText(FormatUtil.formatDate(endDate))
                    val details = coroutineScope {
                        rental.items.map { detail ->
                            async {
                                val itemResponse = RetrofitClient.apiService.getAlatDetail(detail.idAlat)
                                val item = itemResponse.data ?: error(itemResponse.message)
                                item to detail.jumlah
                            }
                        }.awaitAll()
                    }
                    details.forEach { (item, quantity) ->
                        pinnedItems[item.id] = item
                        selectedQuantities[item.id] = quantity
                    }
                } else {
                    intent.getLongExtra(EXTRA_ALAT_ID, 0L).takeIf { it > 0 }?.let { id ->
                        val response = RetrofitClient.apiService.getAlatDetail(id)
                        val item = response.data ?: error(response.message)
                        pinnedItems[item.id] = item
                        selectedQuantities[item.id] = 1
                    }
                }
                loadCatalogPage(reset = true)
                updateEstimate()
            } catch (error: Throwable) {
                showErrorState(ApiErrorHelper.getMessage(error))
            } finally {
                loading = false
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun loadCatalog(reset: Boolean, showLoading: Boolean) {
        if (loading) return
        loading = true
        if (showLoading) showLoadingState()
        lifecycleScope.launch {
            try {
                loadCatalogPage(reset)
            } catch (error: Throwable) {
                if (catalogItems.isEmpty()) showErrorState(ApiErrorHelper.getMessage(error))
                else Snackbar.make(binding.root, ApiErrorHelper.getMessage(error), Snackbar.LENGTH_LONG).show()
            } finally {
                loading = false
                binding.swipeRefresh.isRefreshing = false
                binding.progressMore.visibility = View.GONE
            }
        }
    }

    private suspend fun loadCatalogPage(reset: Boolean) {
        val targetPage = if (reset) 1 else currentPage + 1
        val response = RetrofitClient.apiService.getAlat(targetPage, PAGE_SIZE, activeSearch)
        val page = response.data ?: error(response.message)
        if (reset) catalogItems.clear()
        catalogItems.addAll(page.items.filter { incoming -> catalogItems.none { it.id == incoming.id } })
        currentPage = page.page
        totalItems = page.total
        showCatalog()
    }

    private fun loadNextPage() {
        if (loading || catalogItems.size.toLong() >= totalItems) return
        binding.progressMore.visibility = View.VISIBLE
        loadCatalog(reset = false, showLoading = false)
    }

    private fun showCatalog() {
        val items = (pinnedItems.values + catalogItems)
            .distinctBy { it.id }
        adapter.submitList(items)
        binding.progress.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.layoutEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        binding.swipeRefresh.visibility = View.VISIBLE
    }

    private fun submit() {
        val invalidDate = runCatching { LocalDate.parse(endDate).isBefore(LocalDate.parse(startDate)) }
            .getOrDefault(true)
        binding.layoutEndDate.error = if (invalidDate) getString(R.string.invalid_date_range) else null
        if (invalidDate) return
        if (selectedQuantities.isEmpty()) {
            Snackbar.make(binding.root, R.string.select_phone_first, Snackbar.LENGTH_LONG).show()
            return
        }
        val invalidSelection = selectedQuantities.entries.firstOrNull { (id, quantity) ->
            val item = pinnedItems[id] ?: catalogItems.firstOrNull { it.id == id }
            item == null || quantity < 1 || quantity > item.stokTersedia
        }
        if (invalidSelection != null) {
            Snackbar.make(binding.root, R.string.quantity_exceeds_stock, Snackbar.LENGTH_LONG).show()
            return
        }

        val request = PeminjamanRequest(
            idPeminjam = if (SessionManager.getInstance(this).role == SessionManager.ROLE_ADMIN) {
                existingBorrowerId
            } else {
                null
            },
            tanggalPeminjaman = startDate,
            tanggalRencanaKembali = endDate,
            items = selectedQuantities.map { PeminjamanItemRequest(it.key, it.value) }
        )
        setSubmitting(true)
        lifecycleScope.launch {
            try {
                val response = if (rentalId > 0) {
                    RetrofitClient.apiService.updatePeminjaman(rentalId, request)
                } else {
                    RetrofitClient.apiService.createPeminjaman(request)
                }
                val rental = response.data ?: error(response.message)
                setResult(RESULT_OK)
                if (rentalId == 0L) {
                    startActivity(PeminjamanDetailActivity.createIntent(this@PeminjamanFormActivity, rental.id))
                }
                finish()
            } catch (error: Throwable) {
                Snackbar.make(binding.root, ApiErrorHelper.getMessage(error), Snackbar.LENGTH_LONG).show()
            } finally {
                if (!isFinishing) setSubmitting(false)
            }
        }
    }

    private fun updateEstimate() {
        val dailyTotal = selectedQuantities.entries.fold(BigDecimal.ZERO) { total, entry ->
            val item = pinnedItems[entry.key] ?: catalogItems.firstOrNull { it.id == entry.key }
            total + (item?.hargaSewa ?: BigDecimal.ZERO).multiply(entry.value.toBigDecimal())
        }
        val estimate = dailyTotal.multiply(FormatUtil.rentalDays(startDate, endDate).toBigDecimal())
        binding.textEstimate.text = getString(R.string.estimated_total_value, FormatUtil.formatRupiah(estimate))
    }

    private fun showLoadingState() {
        binding.progress.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
        binding.swipeRefresh.visibility = View.GONE
    }

    private fun showErrorState(message: String) {
        binding.progress.visibility = View.GONE
        binding.swipeRefresh.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.textError.text = message
    }

    private fun setSubmitting(submitting: Boolean) {
        binding.buttonSubmit.isEnabled = !submitting
        binding.progressSubmit.visibility = if (submitting) View.VISIBLE else View.GONE
    }

    companion object {
        private const val EXTRA_ALAT_ID = "extra_alat_id"
        private const val EXTRA_RENTAL_ID = "extra_rental_id"
        private const val PAGE_SIZE = 20

        fun createIntent(context: Context, alatId: Long = 0L): Intent {
            return Intent(context, PeminjamanFormActivity::class.java).putExtra(EXTRA_ALAT_ID, alatId)
        }

        fun createEditIntent(context: Context, rentalId: Long): Intent {
            return Intent(context, PeminjamanFormActivity::class.java).putExtra(EXTRA_RENTAL_ID, rentalId)
        }
    }
}
