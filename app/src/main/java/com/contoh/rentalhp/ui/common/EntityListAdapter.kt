package com.contoh.rentalhp.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.contoh.rentalhp.R
import com.contoh.rentalhp.databinding.ItemEntityBinding
import com.contoh.rentalhp.util.StatusUtil

class EntityListAdapter<T>(
    private val onItemClick: (T) -> Unit,
    private val onPrimaryAction: (T) -> Unit,
    private val onSecondaryAction: (T) -> Unit
) : ListAdapter<ListRow<T>, EntityListAdapter<T>.ViewHolder>(RowDiffCallback()) {

    inner class ViewHolder(
        private val binding: ItemEntityBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: ListRow<T>) {
            binding.textTitle.text = row.title
            bindOptionalText(binding.textSubtitle, row.subtitle)
            bindOptionalText(binding.textMeta, row.meta)
            bindOptionalText(binding.textSupporting, row.supportingText)
            binding.textMeta.setTextAppearance(
                if (row.showImage) R.style.TextAppearance_Rental_Price else R.style.TextAppearance_Rental_BodyMedium
            )

            binding.textStatus.visibility = if (row.status.isNullOrBlank()) View.GONE else View.VISIBLE
            row.status?.let { StatusUtil.apply(binding.textStatus, it) }

            binding.imageItem.visibility = if (row.showImage) View.VISIBLE else View.GONE
            binding.layoutImage.visibility = if (row.showImage) View.VISIBLE else View.GONE
            if (row.showImage) {
                PhoneImageLoader.load(binding.imageItem, row.imageUrl, row.title)
            } else {
                Glide.with(binding.imageItem).clear(binding.imageItem)
            }

            bindAction(binding.buttonPrimary, row.primaryAction) { onPrimaryAction(row.item) }
            bindAction(binding.buttonSecondary, row.secondaryAction) { onSecondaryAction(row.item) }
            binding.layoutActions.visibility = if (row.primaryAction.isNullOrBlank() && row.secondaryAction.isNullOrBlank()) {
                View.GONE
            } else {
                View.VISIBLE
            }
            binding.root.isClickable = row.clickable
            binding.root.isFocusable = row.clickable
            binding.root.setOnClickListener(if (row.clickable) View.OnClickListener { onItemClick(row.item) } else null)
        }

        private fun bindOptionalText(view: android.widget.TextView, value: String?) {
            view.visibility = if (value.isNullOrBlank()) View.GONE else View.VISIBLE
            view.text = value.orEmpty()
        }

        private fun bindAction(
            button: com.google.android.material.button.MaterialButton,
            label: String?,
            action: () -> Unit
        ) {
            button.visibility = if (label.isNullOrBlank()) View.GONE else View.VISIBLE
            button.text = label.orEmpty()
            button.setOnClickListener(if (label.isNullOrBlank()) null else View.OnClickListener { action() })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemEntityBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class RowDiffCallback<T> : DiffUtil.ItemCallback<ListRow<T>>() {
        override fun areItemsTheSame(oldItem: ListRow<T>, newItem: ListRow<T>): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ListRow<T>, newItem: ListRow<T>): Boolean {
            return oldItem == newItem
        }
    }
}
