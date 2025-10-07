package com.payment.payrowapp.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.NewItemsLayoutBinding
import com.payment.payrowapp.databinding.NewStoreChildLayoutBinding
import com.payment.payrowapp.dataclass.Category
import com.payment.payrowapp.dataclass.Product
import com.payment.payrowapp.utils.ContextUtils
import java.util.*

class CartExpandableAdapter(private val ring: MediaPlayer?,
                            private val context: Context,
                            private val categoryList: List<Category>,
                            private val productMap: MutableMap<String, ArrayList<Product>>,
                            private val onQuantityChanged: (Product, Boolean) -> Unit,
                            private val onQuantityItemChanged: () -> Unit// Callback for quantity changes
) : BaseExpandableListAdapter() {


    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return productMap[categoryList[groupPosition].categoryId]!![childPosition]
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val binding: NewStoreChildLayoutBinding

        var convertView = convertView

        if (convertView == null) {
            val infalInflater = this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            binding = NewStoreChildLayoutBinding.inflate(infalInflater, parent, false)
            convertView = binding.root
            convertView.tag = binding
            //   convertView = infalInflater.inflate(R.layout.new_store_child_layout, null)
        }else {
            binding = convertView.tag as NewStoreChildLayoutBinding
        }

        val product = getChild(groupPosition, childPosition) as Product

        if (product.shortServiceName.length > 15) {
            binding.tvSubCategory.text = product.shortServiceName.substring(0, 15)
        } else {
            binding.tvSubCategory.text = product.shortServiceName
        }
        binding.txtAmount.text = ContextUtils.formatWithCommas(product.unitPrice)+" AED"//"%.2f AED".format(product.unitPrice)
        binding.tvCountLabel.text = product.quantity.toString()

        if (product.quantity > 0) {
            val updatedTotalPrice = product.quantity * product.unitPrice
            binding.tvAmountLabel.text = ContextUtils.formatWithCommas(updatedTotalPrice)+" AED"//"%.2f AED".format(updatedTotalPrice)
        } else {
            binding.tvAmountLabel.text = "0 AED"
        }

        binding.icLeftIV.setOnClickListener {
            ring?.start()
            if (product.quantity > 0) {
                product.quantity--
                product.transactionAmount -= product.unitPrice
                binding.tvCountLabel.text = product.quantity.toString()
                val updatedTotalPrice = product.quantity * product.unitPrice
                binding.tvAmountLabel.text = ContextUtils.formatWithCommas(updatedTotalPrice)+" AED"//"%.2f AED".format(updatedTotalPrice)
                onQuantityChanged(product, product.quantity > 0)
                onQuantityItemChanged()
            }
        }

        binding.icRightIV.setOnClickListener {
            ring?.start()
            if (product.serviceType == "F" && product.quantity > 0) {
                Toast.makeText(
                    context,
                    "You are unable to select the this item multiple times.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                product.quantity++
                product.transactionAmount += product.unitPrice
                binding.tvCountLabel.text = product.quantity.toString()
                val updatedTotalPrice = product.quantity * product.unitPrice
                binding.tvAmountLabel.text = ContextUtils.formatWithCommas(updatedTotalPrice)+" AED"//"%.2f AED".format(updatedTotalPrice)
                onQuantityChanged(product, true)
                onQuantityItemChanged()
            }
        }

        return convertView
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return productMap[categoryList[groupPosition].categoryId]?.size ?: 0
    }

    override fun getGroup(groupPosition: Int): Any {
        return categoryList[groupPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val binding: NewItemsLayoutBinding

        var convertView = convertView
        if (convertView == null) {
            val infalInflater = this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            binding = NewItemsLayoutBinding.inflate(infalInflater, parent, false)
            convertView = binding.root
            convertView.setTag(binding)
            //  convertView = infalInflater.inflate(R.layout.new_items_layout, null)
        } else {
            binding = convertView.tag as NewItemsLayoutBinding
        }
        /* val view = convertView ?: LayoutInflater.from(context)
             .inflate(R.layout.new_items_layout, parent, false)*/
        val category = getGroup(groupPosition) as Category


        if (category.categoryName.length > 17) {
            binding.tvServiceItem.text = category.categoryName.substring(0, 17)
        } else {
            binding.tvServiceItem.text = category.categoryName
        }
        //  itemCountTextView.text = "+${category.itemCount} items"

        if (isExpanded) {
            val img: Drawable =
                context.resources.getDrawable(R.drawable.ic_baseline_keyboard_arrow_up_24, null)
            binding.upArrowImage.setImageDrawable(img)
            // convertView.tvServiceItem.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
        } else {
            val img: Drawable =
                context.resources.getDrawable(R.drawable.ic_baseline_keyboard_arrow_down_24, null)
            binding.upArrowImage.setImageDrawable(img)
            // convertView.tvServiceItem.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)

        }

        // Dynamically calculate total item count for the group
        val products = productMap[category.categoryId]
        val totalItems = products?.sumOf { it.quantity } ?: 0
        val totalPrice = products?.sumOf { it.quantity * it.unitPrice } ?: 0.0
        if (totalItems > 0) {
            binding.tvServiceAmount.visibility = View.VISIBLE
            binding.btnItemCount.visibility = View.VISIBLE
            binding.btnItemCount.text = "+$totalItems items"
            binding.tvServiceAmount.text = ContextUtils.formatWithCommas(totalPrice)+" AED"//"%.2f AED".format(totalPrice)
        } else {
            binding.btnItemCount.visibility = View.GONE
            binding.tvServiceAmount.visibility = View.GONE
        }

        return convertView
    }

    override fun getGroupCount(): Int {
        return categoryList.size
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}
