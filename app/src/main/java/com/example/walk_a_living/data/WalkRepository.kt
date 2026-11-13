package com.example.walk_a_living.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class WalkRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("stridebank_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY = "stridebank:v1"
    }

    fun loadState(): AppState {
        val raw = prefs.getString(KEY, null) ?: return AppState(offers = DEFAULT_REWARDS)
        return try {
            val json = JSONObject(raw)
            val daysMap = mutableMapOf<String, DayData>()
            if (json.has("days")) {
                val daysObj = json.getJSONObject("days")
                daysObj.keys().forEach { key ->
                    val dObj = daysObj.getJSONObject(key)
                    daysMap[key] = DayData(
                        steps = dObj.optInt("steps", 0),
                        pts = dObj.optInt("pts", 0)
                    )
                }
            }

            val balance = json.optInt("balance", 0)

            val couponsList = mutableListOf<Coupon>()
            if (json.has("coupons")) {
                val cArray = json.getJSONArray("coupons")
                for (i in 0 until cArray.length()) {
                    val cObj = cArray.getJSONObject(i)
                    couponsList.add(
                        Coupon(
                            id = cObj.optString("id", ""),
                            name = cObj.optString("name", ""),
                            by = cObj.optString("by", ""),
                            code = cObj.optString("code", ""),
                            cost = cObj.optInt("cost", 0),
                            mid = cObj.optString("mid", ""),
                            exp = cObj.optLong("exp", 0L),
                            used = cObj.optBoolean("used", false),
                            open = cObj.optBoolean("open", true),
                            usedAt = if (cObj.has("usedAt") && !cObj.isNull("usedAt")) cObj.optLong("usedAt") else null
                        )
                    )
                }
            }

            var consentState: ConsentState? = null
            if (json.has("consent") && !json.isNull("consent")) {
                val cObj = json.getJSONObject("consent")
                consentState = ConsentState(
                    v = cObj.optString("v", "2026-09"),
                    at = cObj.optLong("at", System.currentTimeMillis()),
                    adult = cObj.optBoolean("adult", true),
                    steps = cObj.optBoolean("steps", true),
                    reports = cObj.optBoolean("reports", false),
                    surveys = cObj.optBoolean("surveys", false),
                    mood = cObj.optBoolean("mood", false)
                )
            }

            val moodMap = mutableMapOf<String, Int>()
            if (json.has("mood")) {
                val moodObj = json.getJSONObject("mood")
                moodObj.keys().forEach { k ->
                    moodMap[k] = moodObj.optInt(k, 0)
                }
            }

            val give = json.optInt("give", 0)

            val offersList = mutableListOf<Offer>()
            if (json.has("offers")) {
                val oArray = json.getJSONArray("offers")
                for (i in 0 until oArray.length()) {
                    val oObj = oArray.getJSONObject(i)
                    offersList.add(
                        Offer(
                            id = oObj.optString("id", ""),
                            name = oObj.optString("name", ""),
                            by = oObj.optString("by", ""),
                            cost = oObj.optInt("cost", 0),
                            pre = oObj.optString("pre", "XX"),
                            active = oObj.optBoolean("active", true),
                            merchantId = if (oObj.has("merchantId") && !oObj.isNull("merchantId")) oObj.optString("merchantId") else null
                        )
                    )
                }
            }

            val finalOffers = if (offersList.isEmpty()) DEFAULT_REWARDS else offersList

            AppState(
                days = daysMap,
                balance = balance,
                coupons = couponsList,
                consent = consentState,
                mood = moodMap,
                give = give,
                offers = finalOffers
            )
        } catch (e: Exception) {
            AppState(offers = DEFAULT_REWARDS)
        }
    }

    fun saveState(state: AppState) {
        try {
            val json = JSONObject()
            val daysObj = JSONObject()
            state.days.forEach { (k, v) ->
                val dObj = JSONObject()
                dObj.put("steps", v.steps)
                dObj.put("pts", v.pts)
                daysObj.put(k, dObj)
            }
            json.put("days", daysObj)
            json.put("balance", state.balance)

            val cArray = JSONArray()
            state.coupons.forEach { c ->
                val cObj = JSONObject()
                cObj.put("id", c.id)
                cObj.put("name", c.name)
                cObj.put("by", c.by)
                cObj.put("code", c.code)
                cObj.put("cost", c.cost)
                cObj.put("mid", c.mid)
                cObj.put("exp", c.exp)
                cObj.put("used", c.used)
                cObj.put("open", c.open)
                c.usedAt?.let { cObj.put("usedAt", it) }
                cArray.put(cObj)
            }
            json.put("coupons", cArray)

            if (state.consent != null) {
                val cObj = JSONObject()
                cObj.put("v", state.consent.v)
                cObj.put("at", state.consent.at)
                cObj.put("adult", state.consent.adult)
                cObj.put("steps", state.consent.steps)
                cObj.put("reports", state.consent.reports)
                cObj.put("surveys", state.consent.surveys)
                cObj.put("mood", state.consent.mood)
                json.put("consent", cObj)
            } else {
                json.put("consent", JSONObject.NULL)
            }

            val moodObj = JSONObject()
            state.mood.forEach { (k, v) -> moodObj.put(k, v) }
            json.put("mood", moodObj)

            json.put("give", state.give)

            val oArray = JSONArray()
            state.offers.forEach { o ->
                val oObj = JSONObject()
                oObj.put("id", o.id)
                oObj.put("name", o.name)
                oObj.put("by", o.by)
                oObj.put("cost", o.cost)
                oObj.put("pre", o.pre)
                oObj.put("active", o.active)
                o.merchantId?.let { oObj.put("merchantId", it) }
                oArray.put(oObj)
            }
            json.put("offers", oArray)

            prefs.edit().putString(KEY, json.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
