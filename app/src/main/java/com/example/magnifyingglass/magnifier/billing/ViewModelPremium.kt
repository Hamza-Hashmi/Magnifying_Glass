package com.example.magnifyingglass.magnifier.billing

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

class ViewModelPremium (private var repositoryPremium: RepositoryPremium) :
    ViewModel() {

    private val viewModelScope = CoroutineScope(Job() + Dispatchers.Main)

    var subSkuDetailsModelListLiveData: LiveData<List<SkuDetailsModel>>

    init {
        repositoryPremium.startDataSourceConnections()
        subSkuDetailsModelListLiveData = repositoryPremium.subSkuDetailsModelListLiveData
    }

    fun getBySkuId(skuId: String): SkuDetailsModel? {
        if (subSkuDetailsModelListLiveData.value != null)
            for (item in subSkuDetailsModelListLiveData.value!!) {
                if (item.sku == skuId) {
                    return item
                }
            }
        return null
    }

    override fun onCleared() {
        super.onCleared()
        repositoryPremium.endDataSourceConnections()
        viewModelScope.coroutineContext.cancel()
    }

    fun makePurchase(activity: Activity, skuDetailsModel: SkuDetailsModel) {
        repositoryPremium.launchBillingFlow(activity, skuDetailsModel)
    }

    fun addBillingCancelListener(billingResultListener: BillingCancelListener) {
        repositoryPremium.addBillingResponseListener(billingResultListener)
    }

}