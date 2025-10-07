/*
package com.payment.payrowmobile.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.CheckBox
import android.widget.Toast
import com.payment.payrowmobile.R
import com.payment.payrowmobile.dataclass.*
import com.payment.payrowmobile.newpayment.TinyDB
import com.payment.payrowmobile.sharepref.SharedPreferenceUtil
import kotlinx.android.synthetic.main.new_items_layout.view.*
import kotlinx.android.synthetic.main.new_store_child_layout.view.*
import java.util.*


class CustomExpandableListAdapter(
    val sharedPreferenceUtil: SharedPreferenceUtil,
    private val _context: Context,
    categoriesResponse: ArrayList<Services>,
    var listDataChild: HashMap<String, ArrayList<ServiceItems>>,
    var tinyDB: TinyDB,
    var childCheckboxStat: HashMap<Int, BooleanArray>,
    private var itemDetailList1: ArrayList<ItemDetail>,
    var listOfStatusFilters1: ArrayList<ServiceItems>
    // header titles
    // child data in format of header title, child title
) : BaseExpandableListAdapter() {

    private var selectedData = HashMap<String, ArrayList<ServiceItems>>()
    private var _categoriesResponse: ArrayList<Services> = categoriesResponse

    var listOfStatusFilters = listOfStatusFilters1//ArrayList<ServiceItems>()
    private var itemDetailList = itemDetailList1

    private var childCheckboxState =
        childCheckboxStat //HashMap<Int, BooleanArray>()//childCheckboxStat
    private var transactionAmount = 0.0
    var serviceList = ArrayList<Service>()

    override fun getChild(groupPosition: Int, childPosititon: Int): Any {
        return _categoriesResponse[groupPosition].serviceItems[childPosititon].shortServiceName
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getChildView(
        groupPosition: Int, childPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup
    ): View {
        var convertView = convertView

        val childText = getChild(groupPosition, childPosition) as String

        val headerText = this._categoriesResponse[groupPosition]

        if (convertView == null) {
            val infalInflater = this._context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = infalInflater.inflate(R.layout.new_store_child_layout, null)
        }

        if (childText.length > 15) {
            convertView!!.tvSubCategory.text = childText.substring(0, 15)
        } else {
            convertView!!.tvSubCategory.text = childText
        }

        convertView.txtAmount.text =
            listDataChild[headerText.categoryId]!!.get(childPosition).unitPrice + " AED"

        */
/*   if (childCheckboxState.containsKey(groupPosition)) {
               val getChecked = childCheckboxState[groupPosition]
               convertView.cbItemName.isChecked = getChecked!![childPosition]

           } else {
               val getChecked = BooleanArray(getChildrenCount(groupPosition))
               childCheckboxState[groupPosition] = getChecked
               convertView.cbItemName.isChecked = false
           }*//*


        convertView.icLeftIV.setOnClickListener {

            */
/*val getChecked = childCheckboxState[groupPosition]
            getChecked!![childPosition] = false
            childCheckboxState.put(groupPosition, getChecked)*//*


            listOfStatusFilters.remove(listDataChild[headerText.categoryId]!![childPosition])
            selectedData.remove(headerText.categoryId, listOfStatusFilters)
            Log.d("listOfStatusFilters", "--$listOfStatusFilters")

            // service item delete
            val iterator: MutableIterator<ItemDetail> = itemDetailList.iterator()
            while (iterator.hasNext()) {
                val value = iterator.next()
                if (value.serviceCode == listDataChild[headerText.categoryId]!![childPosition].serviceId) {
                    iterator.remove()
                }
            }

            // preparing for feefetch delete
            */
/*val iterator2: MutableIterator<Service> = SharedData.sharedArrayList.iterator()
            while (iterator2.hasNext()) {
                val value = iterator2.next()
                if (value.serviceCode == listDataChild[headerText.categoryId]!![childPosition].serviceId) {
                    iterator2.remove()
                }

                transactionAmount -= listDataChild[headerText.categoryId]!!.get(childPosition).unitPrice.toFloat()
            }*//*


            transactionAmount -= listDataChild[headerText.categoryId]!!.get(childPosition).unitPrice.toFloat()

            for (index in SharedData.sharedArrayList.indices) {
                if (SharedData.sharedArrayList[index].serviceCode == listDataChild[headerText.categoryId]!![childPosition].serviceId) {
                    SharedData.sharedArrayList[index].transactionAmount -= listDataChild[headerText.categoryId]!![childPosition].unitPrice.toFloat()
                    SharedData.sharedArrayList[index].quantity -= 1
                    convertView.tvCountLabel.text =
                        SharedData.sharedArrayList[index].quantity.toString()
                    convertView.tvAmountLabel.text =
                        SharedData.sharedArrayList[index].transactionAmount.toString() + " AED"
                }
            }

            if (listDataChild[headerText.categoryId] != null) {
                headerText.size = listOfStatusFilters.size
                notifyDataSetChanged()
            }
        }

        convertView.icRightIV.setOnClickListener {
            //   convertView.cbItemName.isChecked = !convertView.cbItemName.isChecked
            //  val cb = convertView.icRightIV as CheckBox

            //    if (cb.isChecked) {
            */
/*   val getChecked = childCheckboxState[groupPosition]
               getChecked!![childPosition] = true
               childCheckboxState.put(groupPosition, getChecked)*//*


            listOfStatusFilters.add(
                listDataChild.get(headerText.categoryId)!!.get(childPosition)
            )
            selectedData.put(headerText.categoryId, listOfStatusFilters)
            Log.d("listOfStatusFilters", "--$listOfStatusFilters")

            // preparing order details
            val itemDetail = ItemDetail(
                listDataChild[headerText.categoryId]!![childPosition].serviceId,
                headerText.categoryId,
                listDataChild[headerText.categoryId]!!.get(childPosition).shortServiceName,
                "",
                1,
                listDataChild[headerText.categoryId]!!.get(childPosition).unitPrice.toFloat(),
                0
            )   //listDataChild[headerText._id]!!.get(childPosition).itemName
            itemDetailList.add(itemDetail)

            transactionAmount += listDataChild[headerText.categoryId]!!.get(childPosition).unitPrice.toFloat()

            // preparing for feefetch api
            if (SharedData.sharedArrayList.size == 0) {
                val service = Service(
                    listDataChild[headerText.categoryId]!![childPosition].serviceId,
                    1,
                    listDataChild[headerText.categoryId]!!.get(childPosition).unitPrice.toFloat(),
                    1
                )
                SharedData.sharedArrayList.add(service)
                convertView.tvCountLabel.text =
                    SharedData.sharedArrayList[0].quantity.toString()
                convertView.tvAmountLabel.text =
                    SharedData.sharedArrayList[0].transactionAmount.toString() + " AED"
            } else {
                for (index in SharedData.sharedArrayList.indices) {
                    if (SharedData.sharedArrayList[index].serviceCode == listDataChild[headerText.categoryId]!![childPosition].serviceId) {
                        SharedData.sharedArrayList[index].transactionAmount += listDataChild[headerText.categoryId]!![childPosition].unitPrice.toFloat()
                        SharedData.sharedArrayList[index].quantity += 1
                        convertView.tvCountLabel.text =
                            SharedData.sharedArrayList[index].quantity.toString()
                        convertView.tvAmountLabel.text =
                            SharedData.sharedArrayList[index].transactionAmount.toString() + " AED"
                    } else {
                        if (SharedData.sharedArrayList[index].serviceCode == null) {
                            val service = Service(
                                listDataChild[headerText.categoryId]!![childPosition].serviceId,
                                1,
                                listDataChild[headerText.categoryId]!!.get(childPosition).unitPrice.toFloat(),
                                1
                            )
                            SharedData.sharedArrayList.add(service)
                            convertView.tvCountLabel.text = "1"
                            convertView.tvAmountLabel.text =
                                listDataChild[headerText.categoryId]!!.get(childPosition).unitPrice + " AED"
                        }
                    }
                }
            }

            if (listDataChild[headerText.categoryId] != null) {
                headerText.size = listOfStatusFilters.size
                notifyDataSetChanged()
                //   }
            }
        }

        return convertView
    }

    fun getServiceItemList(): ArrayList<Service> {
        return serviceList
    }

    fun getTransactionAmount(): String {
        return transactionAmount.toString()
    }

    fun getListOfHeaders(): ArrayList<ServiceItems> {
        return listOfStatusFilters
    }

    fun getChildState(): HashMap<Int, BooleanArray> {
        return childCheckboxState
    }

    fun getCheckedItemsCount(): Int {
        return itemDetailList.size
    }

    fun getCheckedItems(): ArrayList<ItemDetail> {
        */
/*  val filteredItemDetailList = java.util.ArrayList<ItemDetail>()
          val length = itemDetailList.size

          val visited = BooleanArray(length)
          Arrays.fill(visited, false)

          for (item in 0 until length) {

              if (visited[item])
                  continue

              var count = 1
              for (j in item + 1 until length) {
                  if (itemDetailList.get(item).serviceCat == itemDetailList.get(j).serviceCat) {
                      visited[j] = true
                      count++
                  }
              }

              val itemDetail = ItemDetail(
                  itemDetailList.get(item).serviceCat,
                  itemDetailList.get(item).serviceCat,
                  itemDetailList.get(item).englishName,
                  "", count, 0, 0,1
              )

              filteredItemDetailList.add(itemDetail)
          }*//*

        return itemDetailList
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        var childSize = 0
        if (this._categoriesResponse[groupPosition].serviceItems != null) {
            childSize = this._categoriesResponse[groupPosition].serviceItems.size
        }
        return childSize
    }

    override fun getGroup(groupPosition: Int): Any {
        return this._categoriesResponse[groupPosition].categoryName
    }

    override fun getGroupCount(): Int {
        return this._categoriesResponse.size
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroupView(
        groupPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup
    ): View {
        var convertView = convertView

        val headerTitle = getGroup(groupPosition) as String
        if (convertView == null) {
            val infalInflater = this._context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = infalInflater.inflate(R.layout.new_items_layout, null)
        }

        if (headerTitle.length > 17) {
            convertView!!.tvServiceItem.text = headerTitle.substring(0, 17)
        } else {
            convertView!!.tvServiceItem.text = headerTitle
        }

        //  _categoriesResponse[groupPosition].size = listOfStatusFilters.size
        if (_categoriesResponse[groupPosition].size > 0) {
            convertView.btnItemCount.visibility = View.VISIBLE
            convertView.btnItemCount.text = "+" + _categoriesResponse[groupPosition].size + " items"
        } else {
            convertView.btnItemCount.visibility = View.GONE
        }


        if (isExpanded) {
            val img: Drawable =
                _context.resources.getDrawable(R.drawable.ic_baseline_keyboard_arrow_up_24, null)
            convertView.upArrowImage.setImageDrawable(img)
            // convertView.tvServiceItem.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
        } else {
            val img: Drawable =
                _context.resources.getDrawable(R.drawable.ic_baseline_keyboard_arrow_down_24, null)
            convertView.upArrowImage.setImageDrawable(img)
            // convertView.tvServiceItem.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)

        }
        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}*/
