package com.example.magnifyingglass.magnifier.billing

object PremiumSkus {

 //   test
//"android.test.purchased"
    const val monthlySubscriptionId = "sku_monthly_sub_language_translator"

    const val yearlySubscriptionId = "sku_yearly_sub_language_translator"


    const val weeklySubscriptionId = "sku_weekly_sub_language_translator"

    const val KEY_IS_PURCHASED = "KEY_IS_PURCHASED"


    val SUBS_SKUS = listOf(
        monthlySubscriptionId,
        yearlySubscriptionId,
        weeklySubscriptionId
    )

    val NON_CONSUMABLE_SKUS = SUBS_SKUS

}