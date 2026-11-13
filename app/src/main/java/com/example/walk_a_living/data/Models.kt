package com.example.walk_a_living.data

enum class AppRole {
    CUSTOMER,
    MERCHANT
}

sealed class CouponVerificationResult {
    data class Valid(val coupon: Coupon) : CouponVerificationResult()
    data class AlreadyUsed(val coupon: Coupon) : CouponVerificationResult()
    data class Expired(val coupon: Coupon) : CouponVerificationResult()
    data class NotFound(val code: String) : CouponVerificationResult()
}

data class DayData(
    val steps: Int = 0,
    val pts: Int = 0
)

data class Coupon(
    val id: String,
    val name: String,
    val by: String,
    val code: String,
    val cost: Int,
    val mid: String = "",
    val exp: Long,
    val used: Boolean = false,
    val open: Boolean = true,
    val usedAt: Long? = null
)

data class Offer(
    val id: String,
    val name: String,
    val by: String,
    val cost: Int,
    val pre: String,
    val active: Boolean = true,
    val merchantId: String? = null
)

data class ConsentState(
    val v: String = "2026-09",
    val at: Long = System.currentTimeMillis(),
    val adult: Boolean = true,
    val steps: Boolean = true,
    val reports: Boolean = false,
    val surveys: Boolean = false,
    val mood: Boolean = false
)

data class AppState(
    val days: Map<String, DayData> = emptyMap(),
    val balance: Int = 0,
    val coupons: List<Coupon> = emptyList(),
    val consent: ConsentState? = null,
    val mood: Map<String, Int> = emptyMap(),
    val give: Int = 0,
    val offers: List<Offer> = emptyList()
)

val DEFAULT_REWARDS = listOf(
    Offer("w30", "30 minutes of free Wi-Fi", "Community Internet Café", 60, "IC"),
    Offer("w60", "1 hour of free Wi-Fi", "Community Internet Café", 100, "IC"),
    Offer("p50", "50% off printing (up to 20 pages)", "Community Internet Café", 80, "IC"),
    Offer("job", "Free printing for a job or bursary application (up to 10 pages)", "Community Internet Café", 70, "IC"),
    Offer("bun", "1 hour of Wi-Fi plus 5 free pages", "Community Internet Café", 130, "IC")
)
