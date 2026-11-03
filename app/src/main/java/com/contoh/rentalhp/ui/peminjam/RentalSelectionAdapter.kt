package com.contoh.rentalhp.ui.peminjam

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.model.AlatResponse
import com.contoh.rentalhp.databinding.ItemRentalSelectionBinding
import com.contoh.rentalhp.ui.common.PhoneImageLoader
import com.contoh.rentalhp.util.FormatUtil
import com.contoh.rentalhp.util.StatusUtil

class RentalSelectionAdapter(
    private val quantityFor: (Long) -> Int,
    private val onQuantityChanged: (AlatResponse, Int) -> Unit
) : ListAdapter<AlatResponse, RentalSelectionAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(
        private val binding: ItemRentalSelectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AlatResponse) {
            val quantity = quantityFor(item.id)
            binding.root.isActivated = quantity > 0
            binding.textName.text = item.nama
            binding.checkSelected.contentDescription = binding.root.context.getString(
                R.string.select_phone_named,
                item.nama
            )
            binding.buttonMinus.contentDescription = binding.root.context.getString(
                R.string.decrease_phone_quantity,
                item.nama
            )
            binding.buttonPlus.contentDescription = binding.root.context.getString(
                R.string.increase_phone_quantity,
                item.nama
            )
            binding.textQuantity.contentDescription = binding.root.context.getString(
                R.string.phone_quantity_value,
                item.nama,
                quantity
            )
            binding.textMeta.text = binding.root.context.getString(
                R.string.selection_item_meta,
                FormatUtil.formatRupiah(item.hargaSewa),
                item.stokTersedia
            )
            PhoneImageLoader.load(binding.imagePhone, item.imagePath, item.nama)
            StatusUtil.apply(binding.textStatus, if (item.stokTersedia > 0) "tersedia" else "tidak_tersedia")

            binding.checkSelected.setOnCheckedChangeListener(null)
            binding.checkSelected.isEnabled = item.stokTersedia > 0 || quantity > 0
            binding.checkSelected.isChecked = quantity > 0
            binding.layoutQuantity.visibility = if (quantity > 0) View.VISIBLE else View.GONE
            binding.textQuantity.text = binding.root.context.getString(R.string.quantity_number, quantity)
            binding.buttonMinus.isEnabled = quantity > 0
            binding.buttonPlus.isEnabled = quantity in 1 until item.stokTersedia

            binding.checkSelected.setOnCheckedChangeListener { _, checked ->
                onQuantityChanged(item, if (checked) 1 else 0)
                notifyCurrentRow()
            }
            binding.buttonMinus.setOnClickListener {
                onQuantityChanged(item, if (quantity <= 1) 0 else quantity - 1)
                notifyCurrentRow()
            }
            binding.buttonPlus.setOnClickListener {
                if (quantity < item.stokTersedia) {
                    onQuantityChanged(item, quantity + 1)
                    notifyCurrentRow()
                }
            }
        }

        private fun notifyCurrentRow() {
            bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }?.let(::notifyItemChanged)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRentalSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCallback : DiffUtil.ItemCallback<AlatResponse>() {
        override fun areItemsTheSame(oldItem: AlatResponse, newItem: AlatResponse) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AlatResponse, newItem: AlatResponse) = oldItem == newItem
    }
}
