package com.contoh.rentalhp.ui.petugas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.api.RetrofitClient
import com.contoh.rentalhp.databinding.FragmentReportBinding
import com.contoh.rentalhp.ui.common.ApiErrorHelper
import com.contoh.rentalhp.ui.common.AccessibilityUtil
import com.contoh.rentalhp.ui.common.DatePickerHelper
import com.contoh.rentalhp.ui.common.EntityListAdapter
import com.contoh.rentalhp.ui.common.ListRow
import com.contoh.rentalhp.ui.common.UiStyleUtil
import com.contoh.rentalhp.util.FormatUtil
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class ReportDisplay(
    val id: Long,
    val title: String,
    val subtitle: String,
    val meta: String,
    val status: String
)

class ReportFragment : Fragment() {
    private var viewBinding: FragmentReportBinding? = null
    private val binding: FragmentReportBinding
        get() = requireNotNull(viewBinding)
    private val adapter = EntityListAdapter<ReportDisplay>({}, {}, {})
    private var fromDate = LocalDate.now().withDayOfMonth(1).toString()
    private var toDate = FormatUtil.todayApiDate()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflated = FragmentReportBinding.inflate(inflater, container, false)
        viewBinding = inflated
        return inflated.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        UiStyleUtil.styleRefresh(binding.swipeRefresh)
        binding.recyclerReport.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerReport.adapter = adapter
        binding.inputFrom.setText(FormatUtil.formatDate(fromDate))
        binding.inputTo.setText(FormatUtil.formatDate(toDate))
        binding.inputFrom.setOnClickListener {
            DatePickerHelper.show(parentFragmentManager, getString(R.string.from_date), fromDate) {
                fromDate = it
                binding.inputFrom.setText(FormatUtil.formatDate(it))
                clearDateErrors()
            }
        }
        AccessibilityUtil.activateOnKeyboard(binding.inputFrom)
        binding.inputTo.setOnClickListener {
            DatePickerHelper.show(parentFragmentManager, getString(R.string.to_date), toDate) {
                toDate = it
                binding.inputTo.setText(FormatUtil.formatDate(it))
                clearDateErrors()
            }
        }
        AccessibilityUtil.activateOnKeyboard(binding.inputTo)
        binding.toggleReport.check(R.id.button_report_rental)
        binding.buttonLoad.setOnClickListener { loadReport() }
        binding.buttonRetry.setOnClickListener { loadReport() }
        binding.swipeRefresh.setOnRefreshListener { loadReport(showLoading = false) }
        loadReport()
    }

    private fun loadReport(showLoading: Boolean = true) {
        val from = LocalDate.parse(fromDate)
        val to = LocalDate.parse(toDate)
        if (to.isBefore(from)) {
            binding.layoutTo.error = getString(R.string.invalid_date_range)
            binding.swipeRefresh.isRefreshing = false
            return
        }
        if (ChronoUnit.DAYS.between(from, to) > MAX_REPORT_DAYS) {
            binding.layoutTo.error = resources.getQuantityString(
                R.plurals.report_period_too_long,
                MAX_REPORT_DAYS.toInt(),
                MAX_REPORT_DAYS
            )
            binding.swipeRefresh.isRefreshing = false
            return
        }
        clearDateErrors()
        val requestFrom = fromDate
        val requestTo = toDate
        val returnReport = binding.toggleReport.checkedButtonId == R.id.button_report_return
        if (showLoading) showLoadingState()
        setControlsEnabled(false)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (returnReport) {
                    loadReturnReport(requestFrom, requestTo)
                } else {
                    loadRentalReport(requestFrom, requestTo)
                }
            } catch (error: Throwable) {
                showErrorState(ApiErrorHelper.getMessage(error))
            } finally {
                viewBinding?.let {
                    setControlsEnabled(true)
                    it.swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private suspend fun loadRentalReport(from: String, to: String) {
        val response = RetrofitClient.apiService.getLaporanPeminjaman(from, to)
        val report = response.data ?: error(response.message)
        binding.textSummary.text = resources.getQuantityString(
            R.plurals.rental_report_summary,
            report.ringkasan.jumlahPeminjaman,
            report.ringkasan.jumlahPeminjaman,
            report.ringkasan.jumlahUnit,
            FormatUtil.formatRupiah(report.ringkasan.totalBiaya)
        )
        val rows = report.items.mapIndexed { index, item ->
            val display = ReportDisplay(
                id = "${item.idPeminjaman}-${item.namaAlat}-${item.kodeUnit}-$index".hashCode().toLong(),
                title = getString(R.string.id_name_value, item.idPeminjaman, item.namaPeminjam),
                subtitle = resources.getQuantityString(
                    R.plurals.item_units_value,
                    item.jumlah,
                    item.namaAlat,
                    item.jumlah
                ),
                meta = getString(
                    R.string.date_and_text,
                    FormatUtil.formatDate(item.tanggalPeminjaman),
                    FormatUtil.formatRupiah(item.totalBiaya)
                ),
                status = item.statusPeminjaman
            )
            ListRow(
                id = display.id,
                item = display,
                title = display.title,
                subtitle = display.subtitle,
                meta = display.meta,
                status = display.status,
                clickable = false
            )
        }
        showRows(rows)
    }

    private suspend fun loadReturnReport(from: String, to: String) {
        val response = RetrofitClient.apiService.getLaporanPengembalian(from, to)
        val report = response.data ?: error(response.message)
        binding.textSummary.text = resources.getQuantityString(
            R.plurals.return_report_summary,
            report.ringkasan.jumlahPengembalian,
            report.ringkasan.jumlahPengembalian,
            report.ringkasan.jumlahUnit,
            FormatUtil.formatRupiah(report.ringkasan.totalDenda)
        )
        val rows = report.items.mapIndexed { index, item ->
            val display = ReportDisplay(
                id = "${item.idPengembalian}-${item.kodeInventaris}-$index".hashCode().toLong(),
                title = getString(R.string.id_name_value, item.idPengembalian, item.namaPeminjam),
                subtitle = getString(R.string.item_code_value, item.namaAlat, item.kodeInventaris),
                meta = getString(
                    R.string.return_report_item_meta,
                    FormatUtil.formatDate(item.tanggalKembali),
                    FormatUtil.formatRupiah(item.dendaTotal)
                ),
                status = item.statusKondisi
            )
            ListRow(
                id = display.id,
                item = display,
                title = display.title,
                subtitle = display.subtitle,
                meta = display.meta,
                status = display.status,
                clickable = false
            )
        }
        showRows(rows)
    }

    private fun showRows(rows: List<ListRow<ReportDisplay>>) {
        adapter.submitList(rows)
        binding.progress.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.layoutEmpty.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
        binding.swipeRefresh.visibility = View.VISIBLE
        binding.textSummary.visibility = View.VISIBLE
        binding.cardSummary.visibility = View.VISIBLE
    }

    private fun showLoadingState() {
        binding.progress.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
        binding.swipeRefresh.visibility = View.GONE
        binding.textSummary.visibility = View.GONE
        binding.cardSummary.visibility = View.GONE
    }

    private fun showErrorState(message: String) {
        binding.progress.visibility = View.GONE
        binding.swipeRefresh.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
        binding.textSummary.visibility = View.GONE
        binding.cardSummary.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.textError.text = message
    }

    private fun clearDateErrors() {
        binding.layoutFrom.error = null
        binding.layoutTo.error = null
    }

    private fun setControlsEnabled(enabled: Boolean) {
        binding.buttonLoad.isEnabled = enabled
        binding.buttonReportRental.isEnabled = enabled
        binding.buttonReportReturn.isEnabled = enabled
        binding.inputFrom.isEnabled = enabled
        binding.inputTo.isEnabled = enabled
    }

    override fun onDestroyView() {
        binding.recyclerReport.adapter = null
        viewBinding = null
        super.onDestroyView()
    }

    companion object {
        private const val MAX_REPORT_DAYS = 366L
    }
}
