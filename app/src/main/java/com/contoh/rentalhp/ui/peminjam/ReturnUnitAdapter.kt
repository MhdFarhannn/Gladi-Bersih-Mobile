package com.contoh.rentalhp.ui.peminjam

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.model.KondisiUnitRequest
import com.contoh.rentalhp.databinding.ItemReturnUnitBinding

data class ReturnUnitInput(
    val idUnitAlat: Long,
    val kodeInventaris: String,
    val namaAlat: String,
    var statusKondisi: String = "baik",
    var catatan: String = ""
)

class ReturnUnitAdapter : RecyclerView.Adapter<ReturnUnitAdapter.ViewHolder>() {
    private val items = mutableListOf<ReturnUnitInput>()

    fun submitItems(newItems: List<ReturnUnitInput>) {
        val previousSize = items.size
        items.clear()
        if (previousSize > 0) notifyItemRangeRemoved(0, previousSize)
        items.addAll(newItems)
        if (items.isNotEmpty()) notifyItemRangeInserted(0, items.size)
    }

    fun requests(): List<KondisiUnitRequest> {
        return items.map {
            KondisiUnitRequest(
                idUnitAlat = it.idUnitAlat,
                statusKondisi = it.statusKondisi,
                catatan = it.catatan.trim().ifBlank { null }
            )
        }
    }

    inner class ViewHolder(
        private val binding: ItemReturnUnitBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private var noteWatcher: TextWatcher? = null

        fun bind(item: ReturnUnitInput) {
            binding.textName.text = item.namaAlat
            binding.textCode.text = binding.root.context.getString(R.string.inventory_code_value, item.kodeInventaris)
            val labels = binding.root.resources.getStringArray(R.array.return_condition_labels)
            val values = binding.root.resources.getStringArray(R.array.return_condition_values)
            binding.inputCondition.setAdapter(
                ArrayAdapter(binding.root.context, android.R.layout.simple_dropdown_item_1line, labels)
            )
            val selectedIndex = values.indexOf(item.statusKondisi).coerceAtLeast(0)
            binding.inputCondition.setText(labels[selectedIndex], false)
            binding.inputCondition.setOnItemClickListener { _, _, position, _ ->
                item.statusKondisi = values[position]
            }

            noteWatcher?.let(binding.inputUnitNote::removeTextChangedListener)
            binding.inputUnitNote.setText(item.catatan)
            noteWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    item.catatan = s?.toString().orEmpty()
                }
                override fun afterTextChanged(s: Editable?) = Unit
            }.also(binding.inputUnitNote::addTextChangedListener)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemReturnUnitBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}
