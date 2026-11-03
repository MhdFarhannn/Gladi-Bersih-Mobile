package com.contoh.rentalhp.ui.peminjam

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.model.DetailPengembalianResponse
import com.contoh.rentalhp.databinding.ItemReturnDetailBinding
import com.contoh.rentalhp.util.FormatUtil
import com.contoh.rentalhp.util.StatusUtil

class ReturnDetailItemAdapter : ListAdapter<DetailPengembalianResponse, ReturnDetailItemAdapter.ViewHolder>(DiffCallback()) {
    class ViewHolder(
        private val binding: ItemReturnDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetailPengembalianResponse) {
            binding.textName.text = item.namaAlat
            binding.textCode.text = binding.root.context.getString(R.string.inventory_code_value, item.kodeInventaris)
            StatusUtil.apply(binding.textCondition, item.statusKondisi)
            binding.textFine.text = binding.root.context.getString(
                R.string.condition_fine_value,
                FormatUtil.formatRupiah(item.dendaKondisi)
            )
            binding.textNote.visibility = if (item.catatan.isNullOrBlank()) View.GONE else View.VISIBLE
            binding.textNote.text = binding.root.context.getString(R.string.note_value, item.catatan.orEmpty())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemReturnDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    private class DiffCallback : DiffUtil.ItemCallback<DetailPengembalianResponse>() {
        override fun areItemsTheSame(oldItem: DetailPengembalianResponse, newItem: DetailPengembalianResponse) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: DetailPengembalianResponse, newItem: DetailPengembalianResponse) = oldItem == newItem
    }
}
