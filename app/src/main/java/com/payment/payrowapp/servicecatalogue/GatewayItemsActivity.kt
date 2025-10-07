package com.payment.payrowapp.servicecatalogue

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.payment.payrowapp.R
import com.payment.payrowapp.adapters.BarItemAdapter
import com.payment.payrowapp.databinding.ActivityStoreItemsBinding
import com.payment.payrowapp.dataclass.Category
import com.payment.payrowapp.dataclass.Service
import com.payment.payrowapp.dataclass.SharedData
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.qrcodescan.BarQRCodeScannerActivity
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.utils.Constants
import com.payment.payrowapp.adapters.GatewayCartExpandableAdapter
import com.payment.payrowapp.utils.ContextUtils


class GatewayItemsActivity : BaseActivity() {
    private lateinit var gatewayItemsAdapter: GatewayCartExpandableAdapter
    private var ring: MediaPlayer? = null
    private val context = this@GatewayItemsActivity
    private lateinit var sharedPreferenceUtil: SharedPreferenceUtil

    private var categoryList = ArrayList<Category>()
    private val productMap: MutableMap<String, ArrayList<Service>> = mutableMapOf()
    private var productList = ArrayList<Service>()
    private var dialog: Dialog? = null
    private var totalItemCount = 0
    private var totalTransactionAmount = 0.0
    private lateinit var storeItemsBinding: ActivityStoreItemsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  setContentView(R.layout.activity_store_items)
        storeItemsBinding = ActivityStoreItemsBinding.inflate(layoutInflater)
        setContentView(storeItemsBinding.root)

      //  setSupportActionBar(myToolbar)
        setupToolbar()

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        supportActionBar?.title = "Add Items"

        sharedPreferenceUtil = SharedPreferenceUtil(this)
        ring = MediaPlayer.create(baseContext, R.raw.sound_button_click)


        val gatewayItemsViewModel =
            ViewModelProvider(
                this,
                GatewayItemsViewModelFactory(this)
            )[GatewayItemsViewModel::class.java]

        SharedData.sharedArrayList = arrayListOf()
        SharedData.barCodeItems = arrayListOf()
        gatewayItemsViewModel.getAddItemDetails()
        gatewayItemsViewModel.getAddItemLiveData().observeOnce(context) {

            if (it?.data != null && it.data.size > 0) {
                for (listItem in it.data) {
                    val category = Category(listItem.categoryId, listItem.categoryName)
                    categoryList.add(category)

                    for (serviceItem in listItem.serviceItems) {
                        val product = Service(
                            serviceItem.serviceId, serviceItem.shortServiceName,
                            0, serviceItem.unitPrice.toDouble(), 0.0, 1
                        )
                        productList.add(product)
                    }

                    productMap[listItem.categoryId] = productList
                }

                gatewayItemsAdapter =
                    GatewayCartExpandableAdapter(ring,sharedPreferenceUtil,
                        this,
                        categoryList,
                        productMap, ::onItemQuantityChanged
                    ) {
                        runOnUiThread {
                            gatewayItemsAdapter.notifyDataSetChanged()
                        }
                    }
                // Update parent view when child quantity changes
                storeItemsBinding.expandableListView.setAdapter(gatewayItemsAdapter)
            }
        }

        storeItemsBinding.ivBarCOde.setOnClickListener {
            ring?.start()
            val intent =
                Intent(baseContext, BarQRCodeScannerActivity::class.java)
            intent.putExtra("itemSelection","GatewayItems")
            barCodeLauncher.launch(intent)
        }

        storeItemsBinding.btnDone.setOnClickListener {
            ring?.start()
            if (storeItemsBinding.countTV.text.toString().toInt() > 0) {
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

    private fun calculateAndDisplayTotal() {
        var totalItems = 0
        var totalPrice = 0.0
        for (item in SharedData.sharedArrayList) {
            totalItems += item.quantity
            totalPrice += item.quantity * item.unitPrice //item.transactionAmount
        }

        totalItemCount = totalItems
        totalTransactionAmount = totalPrice
        storeItemsBinding.countTV.text = totalItems.toString()
        storeItemsBinding.tvTOTAmount.text = ContextUtils.formatWithCommas(totalPrice) //"%.2f AED".format(totalPrice)
    }

    // Function to handle quantity changes
    private fun onItemQuantityChanged(product: Service, isSelected: Boolean) {
        if (isSelected) {
            // Add or update the product in the selectedItems list
            val existingProduct =
                SharedData.sharedArrayList.find { it.serviceCode == product.serviceCode }
            if (existingProduct != null) {
                existingProduct.quantity = product.quantity
            } else {
                SharedData.sharedArrayList.add(product)
            }
        } else {
            // Remove the product from the selectedItems list
            SharedData.sharedArrayList.removeIf { it.serviceCode == product.serviceCode }
        }

        // Recalculate totals and update UI
        calculateAndDisplayTotal()
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
            intent.putExtra("itemSelection","GatewayItems")
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
                    if (childItem.serviceCode == barcodeItems.serviceCode) {
                        childItem.quantity += barcodeItems.quantity

                        if (sharedPreferenceUtil.getVATCalculator()) {
                            val payRowVATAmount = barcodeItems.transactionAmount.toFloat() / 100.0f * Constants.VAT_PER
                            childItem.transactionAmount += payRowVATAmount +barcodeItems.transactionAmount
                        } else {
                            childItem.transactionAmount += barcodeItems.transactionAmount
                        }

                        onItemQuantityChanged(childItem, true)
                        itemUpdated = true
                        parentKeyToExpand = barcodeItems.categoryId // Mark the group to expand
                        break
                    }
                }
            }
        }
        // Refresh the UI
        gatewayItemsAdapter.notifyDataSetChanged()

        // Expand the updated group
        parentKeyToExpand?.let { key ->
            val parentIndex = getParentIndex(key)
            if (parentIndex != -1) storeItemsBinding.expandableListView.expandGroup(parentIndex)
        }
        SharedData.barCodeItems = arrayListOf()
    }

    private fun getParentIndex(parentKey: String): Int {
        return productMap.keys.indexOf(parentKey)
    }
}