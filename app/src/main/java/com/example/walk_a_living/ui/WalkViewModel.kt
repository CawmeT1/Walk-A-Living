package com.example.walk_a_living.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.walk_a_living.data.AppRole
import com.example.walk_a_living.data.AppState
import com.example.walk_a_living.data.ConsentState
import com.example.walk_a_living.data.Coupon
import com.example.walk_a_living.data.CouponVerificationResult
import com.example.walk_a_living.data.DayData
import com.example.walk_a_living.data.HealthConnectStatus
import com.example.walk_a_living.data.Offer
import com.example.walk_a_living.data.StepRepository
import com.example.walk_a_living.data.WalkRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class WalkViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WalkRepository(application)
    val stepRepository = StepRepository(application)

    private val _appState = MutableStateFlow(repository.loadState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    private val _appRole = MutableStateFlow(AppRole.CUSTOMER)
    val appRole: StateFlow<AppRole> = _appRole.asStateFlow()

    private val _toastFlow = MutableSharedFlow<String>()
    val toastFlow: SharedFlow<String> = _toastFlow.asSharedFlow()

    private val _showConsentDialog = MutableStateFlow(false)
    val showConsentDialog: StateFlow<Boolean> = _showConsentDialog.asStateFlow()

    val healthConnectStatus: StateFlow<HealthConnectStatus> = stepRepository.healthConnectStatus
    val hasHealthPermission: StateFlow<Boolean> = stepRepository.hasPermission

    init {
        viewModelScope.launch {
            stepRepository.updatePermissionState()
            if (stepRepository.hasPermission.value) {
                syncHealthConnectSteps()
            }
        }
    }

    companion object {
        const val RATE = 100
        const val CAP = 20000
        const val GOAL = 10000
        const val VALID_DAYS = 14
        const val GOAL_PTS = 1000
    }

    fun todayKey(date: Date = Date()): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.CANADA).format(date)
    }

    private fun emitToast(msg: String) {
        viewModelScope.launch {
            _toastFlow.emit(msg)
        }
    }

    private fun updateState(transform: (AppState) -> AppState) {
        val newState = transform(_appState.value)
        _appState.value = newState
        repository.saveState(newState)
    }

    fun openConsentDialog() {
        _showConsentDialog.value = true
    }

    fun closeConsentDialog() {
        _showConsentDialog.value = false
    }

    fun checkHealthPermissionState() {
        viewModelScope.launch {
            stepRepository.updatePermissionState()
            if (stepRepository.hasPermission.value) {
                syncHealthConnectSteps()
            }
        }
    }

    fun syncHealthConnectSteps() {
        viewModelScope.launch {
            val hcSteps = stepRepository.syncSteps().toInt()
            if (hcSteps > 0) {
                val k = todayKey()
                val currentDays = _appState.value.days.toMutableMap()
                val d = currentDays[k] ?: DayData(steps = 0, pts = 0)
                val diff = hcSteps - d.steps
                if (diff > 0) {
                    addSteps(diff)
                }
            }
        }
    }

    fun addSteps(n: Int) {
        val currentConsent = _appState.value.consent
        if (currentConsent == null) {
            openConsentDialog()
            return
        }

        if (n < 1 || n > 30000) {
            emitToast("Enter between 1 and 30,000 steps")
            return
        }

        val k = todayKey()
        val currentDays = _appState.value.days.toMutableMap()
        val d = currentDays[k] ?: DayData(steps = 0, pts = 0)
        val newSteps = d.steps + n
        val np = Math.min(newSteps, CAP) / RATE
        val gain = np - d.pts
        val updatedDay = DayData(steps = newSteps, pts = np)
        currentDays[k] = updatedDay

        updateState { state ->
            state.copy(
                days = currentDays,
                balance = state.balance + gain
            )
        }

        val toastMsg = when {
            gain > 0 -> "+${String.format(Locale.US, "%,d", n)} steps, +$gain points"
            newSteps >= CAP -> "Daily cap reached, no more points today"
            else -> "+${String.format(Locale.US, "%,d", n)} steps added"
        }
        emitToast(toastMsg)
    }

    fun syncTracker() {
        viewModelScope.launch {
            if (stepRepository.hasPermission.value) {
                val hcSteps = stepRepository.syncSteps().toInt()
                if (hcSteps > 0) {
                    val k = todayKey()
                    val currentDays = _appState.value.days.toMutableMap()
                    val d = currentDays[k] ?: DayData(steps = 0, pts = 0)
                    val diff = hcSteps - d.steps
                    if (diff > 0) {
                        addSteps(diff)
                        return@launch
                    }
                }
            }
            val randomSteps = Random.nextInt(500, 2501)
            addSteps(randomSteps)
        }
    }

    fun redeemOffer(offerId: String) {
        val state = _appState.value
        val offer = state.offers.find { it.id == offerId }
        if (offer == null || state.balance < offer.cost) {
            emitToast("Not enough points yet")
            return
        }

        val newBalance = state.balance - offer.cost
        val exp = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, VALID_DAYS)
        }.timeInMillis

        val code = generateCode(offer.pre)
        val newCoupon = Coupon(
            id = System.currentTimeMillis().toString(),
            name = offer.name,
            by = offer.by,
            code = code,
            cost = offer.cost,
            mid = offer.merchantId ?: "",
            exp = exp,
            used = false,
            open = true
        )

        updateState {
            it.copy(
                balance = newBalance,
                coupons = listOf(newCoupon) + it.coupons
            )
        }
        emitToast("Coupon added to My coupons")
    }

    private fun generateCode(pre: String): String {
        val charset = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val sb = StringBuilder()
        for (i in 0 until 8) {
            sb.append(charset.random())
        }
        return "$pre-${sb.substring(0, 4)}-${sb.substring(4)}"
    }

    fun toggleCouponOpen(couponId: String) {
        updateState { state ->
            val updatedCoupons = state.coupons.map { c ->
                if (c.id == couponId) c.copy(open = !c.open) else c
            }
            state.copy(coupons = updatedCoupons)
        }
    }

    fun markCouponUsed(couponId: String) {
        updateState { state ->
            val updatedCoupons = state.coupons.map { c ->
                if (c.id == couponId) c.copy(used = true, usedAt = System.currentTimeMillis()) else c
            }
            state.copy(coupons = updatedCoupons)
        }
    }

    fun givePoints(amount: Int) {
        val state = _appState.value
        if (state.balance < amount) {
            emitToast("Not enough points yet")
            return
        }

        updateState {
            it.copy(
                balance = it.balance - amount,
                give = it.give + amount
            )
        }
        emitToast("Thank you. Your points were given.")
    }

    fun saveConsent(adult: Boolean, steps: Boolean, reports: Boolean, surveys: Boolean, mood: Boolean) {
        val consent = ConsentState(
            adult = adult,
            steps = steps,
            reports = reports,
            surveys = surveys,
            mood = mood
        )
        updateState { it.copy(consent = consent) }
        closeConsentDialog()
        emitToast("Choices saved")
    }

    fun withdrawConsent() {
        updateState {
            AppState()
        }
        closeConsentDialog()
        emitToast("Your data was deleted")
    }

    fun recordMood(moodIndex: Int) {
        val state = _appState.value
        if (state.consent == null || !state.consent.mood) return
        val k = todayKey()
        val updatedMood = state.mood.toMutableMap()
        updatedMood[k] = moodIndex
        updateState { it.copy(mood = updatedMood) }
    }

    fun setAppRole(role: AppRole) {
        _appRole.value = role
    }

    fun verifyCouponCode(code: String): CouponVerificationResult {
        val trimmed = code.trim().uppercase(Locale.ROOT)
        if (trimmed.isEmpty()) return CouponVerificationResult.NotFound(code)
        val coupon = _appState.value.coupons.find { it.code.equals(trimmed, ignoreCase = true) }
            ?: return CouponVerificationResult.NotFound(trimmed)
        val now = System.currentTimeMillis()
        return when {
            coupon.used -> CouponVerificationResult.AlreadyUsed(coupon)
            coupon.exp < now -> CouponVerificationResult.Expired(coupon)
            else -> CouponVerificationResult.Valid(coupon)
        }
    }

    fun confirmRedeemCoupon(code: String) {
        val state = _appState.value
        val coupon = state.coupons.find { it.code.equals(code.trim(), ignoreCase = true) }
        if (coupon == null) {
            emitToast("No coupon found with code $code")
            return
        }
        if (coupon.used) {
            emitToast("Coupon already redeemed")
            return
        }
        updateState { s ->
            val updated = s.coupons.map { c ->
                if (c.code.equals(code.trim(), ignoreCase = true)) c.copy(used = true, usedAt = System.currentTimeMillis()) else c
            }
            s.copy(coupons = updated)
        }
        emitToast("Coupon redeemed successfully")
    }

    fun addOffer(name: String, business: String, cost: Int) {
        if (name.isBlank() || cost <= 0) {
            emitToast("Add an offer and a point cost")
            return
        }
        val pre = business.replace(Regex("[^A-Za-z]"), "").take(2).uppercase(Locale.ROOT).ifEmpty { "XX" }
        val newOffer = Offer(
            id = System.currentTimeMillis().toString(),
            name = name,
            by = business.ifBlank { "Local Business" },
            cost = cost,
            pre = pre,
            active = true
        )
        updateState {
            it.copy(offers = it.offers + newOffer)
        }
        emitToast("Offer added")
    }

    fun toggleOfferActive(offerId: String) {
        updateState { state ->
            val updatedOffers = state.offers.map { o ->
                if (o.id == offerId) o.copy(active = !o.active) else o
            }
            state.copy(offers = updatedOffers)
        }
    }
}
