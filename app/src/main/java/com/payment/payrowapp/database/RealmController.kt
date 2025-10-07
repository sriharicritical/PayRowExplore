/*
package com.payment.payrowmobile.database

import android.app.Application
import android.content.Context
import android.util.Log
import com.payment.payrowmobile.dataclass.EmployeeListResponse
import com.payment.payrowmobile.dataclass.StoreListItemsResponse
import io.realm.Realm

open class RealmController {

    private var realmController: RealmController? = null
    private var realm: Realm? = null
    var mContext: Context? = null

    open fun RealmController(application: Application?) {
        realm = Realm.getDefaultInstance()
    }

    open fun getInstance(): RealmController? {
        return realmController
    }

    open fun beginRealm() {
        realm!!.beginTransaction()
    }

    open fun commitRealm() {
        realm!!.commitTransaction()
    }

    open fun with(context: Context): RealmController? {
        if (realmController == null) {
            realmController = RealmController()
        }
        mContext = context
        return realmController
    }

    open fun addEmployeeList(employeeListResponse: ArrayList<EmployeeListResponse>) {
        beginRealm()
        realm?.executeTransactionAsync {
            realm!!.copyToRealmOrUpdate(employeeListResponse)
        }
        commitRealm()
    }

    open fun addStoreItemsList(storeListItemsResponse: ArrayList<StoreListItemsResponse>) {
        beginRealm()
        realm?.executeTransactionAsync {
            realm!!.copyToRealmOrUpdate(storeListItemsResponse)
        }
        commitRealm()
        Log.i("realmdb", "StoreItemsList added")
    }
}*/
