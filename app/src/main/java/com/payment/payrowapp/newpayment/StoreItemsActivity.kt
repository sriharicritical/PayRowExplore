package com.payment.payrowapp.newpayment

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.payment.payrowapp.R
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.utils.BaseActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.payment.payrowapp.adapters.BarItemAdapter
import com.payment.payrowapp.adapters.CartExpandableAdapter
import com.payment.payrowapp.databinding.ActivityPaymentSuccessfulBinding
import com.payment.payrowapp.databinding.ActivityStoreItemsBinding
import com.payment.payrowapp.dataclass.Category
import com.payment.payrowapp.dataclass.Product
import com.payment.payrowapp.dataclass.SharedData
import com.payment.payrowapp.qrcodescan.BarQRCodeScannerActivity
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.ContextUtils


class StoreItemsActivity : BaseActivity() {
    private lateinit var cartExpandableAdapter: CartExpandableAdapter
    private var ring: MediaPlayer? = null
    private val context = this@StoreItemsActivity
    private lateinit var sharedPreferenceUtil: SharedPreferenceUtil

    private var categoryList = ArrayList<Category>()
    private val productMap: MutableMap<String, ArrayList<Product>> = mutableMapOf()
    private var productList = ArrayList<Product>()
    private var dialog: Dialog? = null
    private var totalItemCount = 0
    private var totalTransactionAmount = 0.0

    private lateinit var binding: ActivityStoreItemsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_store_items)
        binding = ActivityStoreItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

       // setSupportActionBar(toolbar)
        setupToolbar()

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        supportActionBar?.title = "Add Items"

        sharedPreferenceUtil = SharedPreferenceUtil(this)
        ring = MediaPlayer.create(baseContext, R.raw.sound_button_click)


        val storeItemsViewModel =
            ViewModelProvider(
                this,
                StoreItemsViewModelFactory(this)
            )[StoreItemsViewModel::class.java]

        SharedData.sharedArrayList = arrayListOf()
        SharedData.selectedItems = arrayListOf()
        SharedData.barCodeItems = arrayListOf()
        storeItemsViewModel.getAddItemDetails()
        storeItemsViewModel.getAddItemLiveData().observeOnce(context) {

            if (it?.data != null && it.data.size > 0) {
                for (listItem in it.data) {
                    val category = Category(listItem.categoryId, listItem.categoryName)
                    categoryList.add(category)

                    for (serviceItem in listItem.serviceItems) {
                        val product = Product(
                            serviceItem.serviceId, serviceItem.shortServiceName,
                            0, serviceItem.unitPrice.toDouble(), 1, 0.0,
                            serviceItem.serviceType
                        )
                        productList.add(product)
                    }

                    productMap[listItem.categoryId] = productList
                }

                cartExpandableAdapter =
                    CartExpandableAdapter(ring,
                        this,
                        categoryList,
                        productMap, ::onItemQuantityChanged
                    ) {
                        runOnUiThread {
                            cartExpandableAdapter.notifyDataSetChanged()
                        }
                    }
                // Update parent view when child quantity changes
                binding.expandableListView.setAdapter(cartExpandableAdapter)
            }
        }

        binding.ivBarCOde.setOnClickListener {
            val intent =
                Intent(baseContext, BarQRCodeScannerActivity::class.java)
            barCodeLauncher.launch(intent)
        }

        binding.btnDone.setOnClickListener {
            ring?.start()
            if (binding.countTV.text.toString().toInt() > 0) {
                Toast.makeText(baseContext, "Items added successfully", Toast.LENGTH_SHORT).show()

                val intent = Intent()
                intent.putExtra("itemLength", totalItemCount)
                intent.putExtra("transactionAmount", totalTransactionAmount.toString())

                setResult(2, intent)
                finish()
            } else {
                finish()
            }
        }

    }

    private fun calculateAndDisplayTotal() {
        var totalItems = 0
        var totalPrice = 0.0
        for (item in SharedData.selectedItems) {
            totalItems += item.quantity
            totalPrice += item.quantity * item.unitPrice
        }

        totalItemCount = totalItems
        totalTransactionAmount = totalPrice
        binding.countTV.text = totalItems.toString()
        binding.tvTOTAmount.text = "AED "+ ContextUtils.formatWithCommas(totalPrice)//"%.2f AED".format(totalPrice)
    }

    // Function to handle quantity changes
    private fun onItemQuantityChanged(product: Product, isSelected: Boolean) {
        if (isSelected) {
            // Add or update the product in the selectedItems list
            val existingProduct =
                SharedData.selectedItems.find { it.serviceId == product.serviceId }
            if (existingProduct != null) {
                existingProduct.quantity = product.quantity
            } else {
                SharedData.selectedItems.add(product)
            }
        } else {
            // Remove the product from the selectedItems list
            SharedData.selectedItems.removeIf { it.serviceId == product.serviceId }
        }

        // Recalculate totals and update UI
        calculateAndDisplayTotal()
    }

    private val barCodeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //  BarCodeItemDialog(this).show()
            if (dialog != null) {
                dialog?.dismiss()
                showItemDialog()
            } else {
                showItemDialog()
            }
        }
    }

    private fun showItemDialog() {
        val dialogBuilder = AlertDialog.Builder(this)

        // Inflate the custom layout
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.layout_barcode_item, null)

        // Set the custom layout as the dialog view
        dialogBuilder.setView(dialogView)

        // Create and show the dialog
        dialog = dialogBuilder.create()
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)

        // Find views in the custom layout
        val btnSubmit: Button = dialogView.findViewById(R.id.btnSubmit)
        val buttonNext: Button = dialogView.findViewById(R.id.buttonNext)
        val recBarItems: RecyclerView = dialogView.findViewById(R.id.recBarItems)

        val productAdapter = BarItemAdapter(context, SharedData.barCodeItems)

        recBarItems.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recBarItems.adapter = productAdapter


        btnSubmit.setOnClickListener {
            ring?.start()
            dialog?.dismiss()
            updateAdapterWithBarcode()
        }

        buttonNext.setOnClickListener {
            ring?.start()
            val intent =
                Intent(context, BarQRCodeScannerActivity::class.java)
            intent.putExtra("itemSelection","PosItems")
            barCodeLauncher.launch(intent)
            // dismiss()
        }
        dialog?.show()
    }

    private fun updateAdapterWithBarcode() {
        var parentKeyToExpand: String? = null

        for (barcodeItems in SharedData.barCodeItems) {
            var itemUpdated = false
            val childList = productMap[barcodeItems.categoryId]
            childList?.let {
                for (childItem in it) {
                    if (childItem.serviceId == barcodeItems.serviceCode) {
                        childItem.quantity += barcodeItems.quantity
                        childItem.transactionAmount += barcodeItems.transactionAmount
                        onItemQuantityChanged(childItem, true)
                        itemUpdated = true
                        parentKeyToExpand = barcodeItems.categoryId // Mark the group to expand
                        break
                    }
                }
            }
        }
        // Refresh the UI
        cartExpandableAdapter.notifyDataSetChanged()

        // Expand the updated group
        parentKeyToExpand?.let { key ->
            val parentIndex = getParentIndex(key)
            if (parentIndex != -1) binding.expandableListView.expandGroup(parentIndex)
        }
        SharedData.barCodeItems = arrayListOf()
    }

    private fun getParentIndex(parentKey: String): Int {
        return productMap.keys.indexOf(parentKey)
    }
}