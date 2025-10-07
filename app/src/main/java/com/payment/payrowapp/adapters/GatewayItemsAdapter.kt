//package com.payment.payrowmobile.adapters
//
//import android.content.Context
//import android.graphics.drawable.Drawable
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.BaseExpandableListAdapter
//import android.widget.CheckBox
//import android.widget.Toast
//import com.payment.payrowmobile.R
//import com.payment.payrowmobile.dataclass.*
//import com.payment.payrowmobile.newpayment.TinyDB
//import com.payment.payrowmobile.sharepref.SharedPreferenceUtil
//import com.payment.payrowmobile.utils.Constants
//import kotlinx.android.synthetic.main.item_store_child.view.*
//import kotlinx.android.synthetic.main.item_store_parent.view.*
//import java.util.ArrayList
//import java.util.HashMap
//
//class GatewayItemsAdapter(
//    val sharedPreferenceUtil: SharedPreferenceUtil,
//    private val _context: Context,
//    categoriesResponse: ArrayList<Services>,
//    var listDataChild: HashMap<String, ArrayList<ServiceItems>>,
//    var tinyDB: TinyDB,
//    var childCheckboxStat: HashMap<Int, BooleanArray>,
//    private var itemDetailList1: ArrayList<ItemDetail>,
//    var listOfStatusFilters1: ArrayList<ServiceItems>
//    // header titles
//    // child data in format of header title, child title
//) : BaseExpandableListAdapter() {
//
//    private var selectedData = HashMap<String, ArrayList<ServiceItems>>()
//    private var _categoriesResponse: ArrayList<Services> = categoriesResponse
//
//    var listOfStatusFilters = listOfStatusFilters1//ArrayList<ServiceItems>()
//    private var itemDetailList = itemDetailList1
//
//    private var childCheckboxState =
//        childCheckboxStat //HashMap<Int, BooleanArray>()//childCheckboxStat
//    private var transactionAmount = 0.0
//    var serviceList = ArrayList<Service>()
//    var totalAmountVAT: Float = 0.0F
//
//    override fun getChild(groupPosition: Int, childPosititon: Int): Any {
//        return _categoriesResponse[groupPosition].serviceItems[childPosititon].shortServiceName
//    }
//
//    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
//        return childPosition.toLong()
//    }
//
//    override fun getChildView(
//        groupPosition: Int, childPosition: Int,
//        isLastChild: Boolean, convertView: View?, parent: ViewGroup
//    ): View {
//        var convertView = convertView
//
//        val childText = getChild(groupPosition, childPosition) as String
//
//        val headerText = this._categoriesResponse[groupPosition]
//
//        if (convertView == null) {
//            val infalInflater = this._context
//                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//            convertView = infalInflater.inflate(R.layout.item_store_child, null)
//        }
//
//        convertView!!.tvSubCategory.text = childText
//
//
//
//        if (childCheckboxState.containsKey(groupPosition)) {
//            val getChecked = childCheckboxState[groupPosition]
//            convertView.cbItemName.isChecked = getChecked!![childPosition]
//
//        } else {
//            val getChecked = BooleanArray(getChildrenCount(groupPosition))
//            childCheckboxState[groupPosition] = getChecked
//            convertView.cbItemName.isChecked = false
//        }
//
//        convertView.cbItemName.setOnClickListener {
//            //   convertView.cbItemName.isChecked = !convertView.cbItemName.isChecked
//            val cb = convertView.cbItemName as CheckBox
//            // val tag = cb.tag as Pair<Long, Long>
//
//            if (cb.isChecked) {
//                if (!sharedPreferenceUtil.getCataLogAmount() && itemDetailList.isNotEmpty() && itemDetailList.size == 1) {
//                    Toast.makeText(
//                        _context,
//                        "You can able to select one item only. If you want select multiple items.Kindly enable service Catalogue",
//                        Toast.LENGTH_LONG
//                    ).show()
//                } else {
//                    val getChecked = childCheckboxState[groupPosition]
//                        getChecked!![childPosition] = true
//                        childCheckboxState.put(groupPosition, getChecked)
//
//                        //  val catDetail = CatDetail(headerText._id, headerText.categoryName)
//                        listOfStatusFilters.add(
//                            listDataChild.get(headerText.categoryId)!!.get(childPosition)
//                        )
//                        selectedData.put(headerText.categoryId, listOfStatusFilters)
//                        Log.d("listOfStatusFilters", "--$listOfStatusFilters")
//
//                    totalAmountVAT = if (sharedPreferenceUtil.getVATCalculator()) {
//                        val   payRowVATAmount = (listDataChild[headerText.categoryId]!!.get(childPosition).unitPrice.toFloat() / 100.0f) * Constants.VAT_PER
//                        payRowVATAmount + listDataChild[headerText.categoryId]!!.get(childPosition).unitPrice.toFloat()
//                    } else {
//                        listDataChild[headerText.categoryId]!!.get(childPosition).unitPrice.toFloat()
//                    }
//                        // preparing order details
//                        val itemDetail = ItemDetail(
//                            listDataChild[headerText.categoryId]!![childPosition].serviceId,
//                            headerText.categoryId,
//                            listDataChild[headerText.categoryId]!!.get(childPosition).shortServiceName,
//                            "",
//                            1,
//                            totalAmountVAT,
//                            0
//                        )   //listDataChild[headerText._id]!!.get(childPosition).itemName
//                        itemDetailList.add(itemDetail)
//
//                        transactionAmount += listDataChild[headerText.categoryId]!!.get(childPosition).unitPrice.toFloat()
//
//                        // preparing for feefetch api
//                        val service = Service(
//                            listDataChild[headerText.categoryId]!![childPosition].serviceId,
//                            1,
//                            totalAmountVAT,
//                            1
//                        )
//                        SharedData.sharedArrayList.add(service)
//                }
//                //  serviceList.add(service)
//            } else {
//                val getChecked = childCheckboxState[groupPosition]
//                getChecked!![childPosition] = false
//                childCheckboxState.put(groupPosition, getChecked)
//
//                listOfStatusFilters.remove(listDataChild[headerText.categoryId]!![childPosition])
//                selectedData.remove(headerText.categoryId, listOfStatusFilters)
//                Log.d("listOfStatusFilters", "--$listOfStatusFilters")
//
//                // service item delete
//                val iterator: MutableIterator<ItemDetail> = itemDetailList.iterator()
//                while (iterator.hasNext()) {
//                    val value = iterator.next()
//                    if (value.serviceCode == listDataChild[headerText.categoryId]!![childPosition].serviceId) {
//                        iterator.remove()
//                    }
//                }
//
//                // preparing for feefetch delete
//                val iterator2: MutableIterator<Service> = SharedData.sharedArrayList.iterator()
//                while (iterator2.hasNext()) {
//                    val value = iterator2.next()
//                    if (value.serviceCode == listDataChild[headerText.categoryId]!![childPosition].serviceId) {
//                        iterator2.remove()
//                    }
//                }
//
//                transactionAmount -= listDataChild[headerText.categoryId]!!.get(childPosition).unitPrice.toFloat()
//            }
//
//            if (listDataChild[headerText.categoryId] != null) {
//                headerText.size = listOfStatusFilters.size
//                notifyDataSetChanged()
//            }
//        }
//
//
//        convertView.linearLayout.setOnClickListener {
//            convertView.cbItemName.isChecked = !convertView.cbItemName.isChecked
//            val cb = convertView.cbItemName as CheckBox
//            // val tag = cb.tag as Pair<Long, Long>
//
//            if (cb.isChecked) {
//                if (!sharedPreferenceUtil.getCataLogAmount() && itemDetailList.isNotEmpty() && itemDetailList.size == 1) {
//                    Toast.makeText(
//                        _context,
//                        "You can able to select one item only. If you want select multiple items.Kindly enable service catalog",
//                        Toast.LENGTH_LONG
//                    ).show()
//                } else {
//
//                    val getChecked = childCheckboxState[groupPosition]
//                    getChecked!![childPosition] = true
//                    childCheckboxState.put(groupPosition, getChecked)
//
//                    //  val catDetail = CatDetail(headerText._id, headerText.categoryName)
//                    listOfStatusFilters.add(
//                        listDataChild.get(headerText.categoryId)!!.get(childPosition)
//                    )
//                    selectedData.put(headerText.categoryId, listOfStatusFilters)
//                    Log.d("listOfStatusFilters", "--$listOfStatusFilters")
//
//                    totalAmountVAT = if (sharedPreferenceUtil.getVATCalculator()) {
//                        val   payRowVATAmount = (listDataChild[headerText.categoryId]!!.get(childPosition).unitPrice.toFloat() / 100.0f) * Constants.VAT_PER
//                        payRowVATAmount + listDataChild[headerText.categoryId]!!.get(childPosition).unitPrice.toFloat()
//                    } else {
//                        listDataChild[headerText.categoryId]!!.get(childPosition).unitPrice.toFloat()
//                    }
//
//                    // preparing order details
//                    val itemDetail = ItemDetail(
//                        listDataChild[headerText.categoryId]!![childPosition].serviceId,
//                        headerText.categoryId,
//                        listDataChild[headerText.categoryId]!!.get(childPosition).shortServiceName,
//                        "",
//                        1,
//                        totalAmountVAT,
//                        0
//                    )   //listDataChild[headerText._id]!!.get(childPosition).itemName
//                    itemDetailList.add(itemDetail)
//
//                    transactionAmount += listDataChild[headerText.categoryId]!!.get(childPosition).unitPrice.toFloat()
//
//                    // preparing for feefetch api
//                    val service = Service(
//                        listDataChild[headerText.categoryId]!![childPosition].serviceId,
//                        1,
//                        totalAmountVAT,
//                        1
//                    )   //listDataChild[headerText._id]!!.get(childPosition).itemName
//                    // serviceList.add(service)
//                    SharedData.sharedArrayList.add(service)
//                }
//            } else {
//                val getChecked = childCheckboxState[groupPosition]
//                getChecked!![childPosition] = false
//                childCheckboxState.put(groupPosition, getChecked)
//
//                listOfStatusFilters.remove(listDataChild[headerText.categoryId]!![childPosition])
//                selectedData.remove(headerText.categoryId, listOfStatusFilters)
//                Log.d("listOfStatusFilters", "--$listOfStatusFilters")
//
//                // service item delete
//                val iterator: MutableIterator<ItemDetail> = itemDetailList.iterator()
//                while (iterator.hasNext()) {
//                    val value = iterator.next()
//                    if (value.serviceCode == listDataChild[headerText.categoryId]!![childPosition].serviceId) {
//                        iterator.remove()
//                    }
//                }
//
//                // preparing for feefetch delete
//                val iterator2: MutableIterator<Service> = SharedData.sharedArrayList.iterator()
//                while (iterator2.hasNext()) {
//                    val value = iterator2.next()
//                    if (value.serviceCode == listDataChild[headerText.categoryId]!![childPosition].serviceId) {
//                        iterator2.remove()
//                    }
//                }
//
//                transactionAmount -= listDataChild[headerText.categoryId]!!.get(childPosition).unitPrice.toFloat()
//            }
//
//            if (listDataChild[headerText.categoryId] != null) {
//                headerText.size = listOfStatusFilters.size
//                notifyDataSetChanged()
//            }
//        }
//
//        return convertView
//    }
//
//    fun getServiceItemList(): ArrayList<Service> {
//        return serviceList
//    }
//
//    fun getTransactionAmount(): String {
//        return transactionAmount.toString()
//    }
//
//    fun getListOfHeaders(): ArrayList<ServiceItems> {
//        return listOfStatusFilters
//    }
//
//    fun getChildState(): HashMap<Int, BooleanArray> {
//        return childCheckboxState
//    }
//
//    fun getCheckedItemsCount(): Int {
//        return itemDetailList.size
//    }
//
//    fun getCheckedItems(): ArrayList<ItemDetail> {
//        return itemDetailList
//    }
//
//    override fun getChildrenCount(groupPosition: Int): Int {
//        var childSize = 0
//        if (this._categoriesResponse[groupPosition].serviceItems != null) {
//            childSize = this._categoriesResponse[groupPosition].serviceItems.size
//        }
//        return childSize
//    }
//
//    override fun getGroup(groupPosition: Int): Any {
//        return this._categoriesResponse[groupPosition].categoryName
//    }
//
//    override fun getGroupCount(): Int {
//        return this._categoriesResponse.size
//    }
//
//    override fun getGroupId(groupPosition: Int): Long {
//        return groupPosition.toLong()
//    }
//
//    override fun getGroupView(
//        groupPosition: Int, isExpanded: Boolean,
//        convertView: View?, parent: ViewGroup
//    ): View {
//        var convertView = convertView
//
//        val headerTitle = getGroup(groupPosition) as String
//        if (convertView == null) {
//            val infalInflater = this._context
//                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//            convertView = infalInflater.inflate(R.layout.item_store_parent, null)
//        }
//
//        if (headerTitle.length > 17) {
//            convertView!!.tvServiceItem.text = headerTitle.substring(0, 17)
//        } else {
//            convertView!!.tvServiceItem.text = headerTitle
//        }
//
//        //  _categoriesResponse[groupPosition].size = listOfStatusFilters.size
//        if (_categoriesResponse[groupPosition].size > 0) {
//            convertView.btnItemCount.visibility = View.VISIBLE
//            convertView.btnItemCount.text = "+" + _categoriesResponse[groupPosition].size + " items"
//        } else {
//            convertView.btnItemCount.visibility = View.GONE
//        }
//
//
//        if (isExpanded) {
//            val img: Drawable =
//                _context.resources.getDrawable(R.drawable.ic_baseline_keyboard_arrow_up_24, null)
//            convertView.tvServiceItem.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
//        } else {
//            val img: Drawable =
//                _context.resources.getDrawable(R.drawable.ic_baseline_keyboard_arrow_down_24, null)
//            convertView.tvServiceItem.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
//
//        }
//        return convertView
//    }
//
//    override fun hasStableIds(): Boolean {
//        return false
//    }
//
//    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
//        return true
//    }
//}