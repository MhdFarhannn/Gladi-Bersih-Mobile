package com.contoh.rentalhp.ui.peminjam

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.model.DetailPeminjamanResponse
import com.contoh.rentalhp.databinding.ItemRentalDetailBinding
import com.contoh.rentalhp.util.FormatUtil

class RentalDetailItemAdapter : ListAdapter<DetailPeminjamanResponse, RentalDetailItemAdapter.ViewHolder>(DiffCallback()) {
    class ViewHolder(
        private val binding: ItemRentalDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetailPeminjamanResponse) {
            binding.textName.text = item.namaAlat
            binding.textPrice.text = binding.root.resources.getQuantityString(
                R.plurals.rental_item_price,
                item.jumlah,
                item.jumlah,
                FormatUtil.formatRupiah(item.hargaSewaSaatTransaksi)
            )
            binding.textUnits.text = if (item.unit.isEmpty()) {
                binding.root.context.getString(R.string.unit_not_allocated)
            } else {
                binding.root.context.getString(
                    R.string.allocated_units,
                    item.unit.joinToString { it.kodeInventaris }
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemRentalDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    private class DiffCallback : DiffUtil.ItemCallback<DetailPeminjamanResponse>() {
        override fun areItemsTheSame(oldItem: DetailPeminjamanResponse, newItem: DetailPeminjamanResponse) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: DetailPeminjamanResponse, newItem: DetailPeminjamanResponse) = oldItem == newItem
    }
}
