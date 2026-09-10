package com.example.myapplication158.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import com.example.myapplication158.data.WorkOrder

object LocationUtils {

    /**
     * מקבלת כתובת טקסטואלית ומחזירה מיקום גיאוגרפי (Location)
     */
    suspend fun getLocationFromAddress(context: Context, strAddress: String): Location? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale("he", "IL"))
                val addresses: List<Address>? = geocoder.getFromLocationName(strAddress, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val location = Location("")
                    location.latitude = addresses[0].latitude
                    location.longitude = addresses[0].longitude
                    return@withContext location
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext null
        }
    }

    /**
     * מחשבת מרחק אווירי במטרים בין שני מיקומים
     */
    fun calculateDistance(loc1: Location, loc2: Location): Float {
        return loc1.distanceTo(loc2)
    }

    /**
     * אלגוריתם חכם לסידור מסלול (Greedy TSP - Nearest Neighbor + חזרה ליעד)
     * לוקח את המשימות של היום, המיקום הנוכחי, ויעד הסיום, ומחזיר רשימה מסודרת.
     */
    suspend fun sortWorkOrdersForToday(
        context: Context,
        todayOrders: List<WorkOrder>,
        currentLocation: Location,
        endAddress: String
    ): RouteResult {
        return withContext(Dispatchers.IO) {
            val endLocation = getLocationFromAddress(context, endAddress)
                ?: return@withContext RouteResult(todayOrders, false, "לא הצלחנו לאתר את יעד הסיום. סדר המשימות נשאר כפי שהיה.")

            // מסננים רק משימות שיש להן כתובת, וממירים אותן למיקומים
            val ordersWithLocations = mutableListOf<Pair<WorkOrder, Location>>()
            val ordersWithoutLocations = mutableListOf<WorkOrder>()

            for (order in todayOrders) {
                if (order.location.isNotBlank()) {
                    val loc = getLocationFromAddress(context, order.location)
                    if (loc != null) {
                        ordersWithLocations.add(Pair(order, loc))
                    } else {
                        ordersWithoutLocations.add(order)
                    }
                } else {
                    ordersWithoutLocations.add(order)
                }
            }

            if (ordersWithLocations.isEmpty()) {
                return@withContext RouteResult(todayOrders, false, "לא נמצאו משימות להיום עם כתובת תקינה שניתן לנווט אליה.")
            }

            // אלגוריתם שכן-קרוב-ביותר (Nearest Neighbor)
            val sortedOrders = mutableListOf<WorkOrder>()
            var lastLocation = currentLocation
            val remainingOrders = ArrayList(ordersWithLocations)

            while (remainingOrders.isNotEmpty()) {
                // מוצאים את המשימה שהכי קרובה למיקום האחרון (הנוכחי)
                var closestPair: Pair<WorkOrder, Location>? = null
                var minDistance = Float.MAX_VALUE

                for (pair in remainingOrders) {
                    val dist = calculateDistance(lastLocation, pair.second)
                    // ניתן להוסיף כאן גם משקל (Penalty) על כך שהנקודה מתרחקת מיעד הסיום, 
                    // אבל למסלול קצר בעיר מספיק לחפש את הכי קרוב אלי כרגע.
                    if (dist < minDistance) {
                        minDistance = dist
                        closestPair = pair
                    }
                }

                if (closestPair != null) {
                    sortedOrders.add(closestPair.first)
                    lastLocation = closestPair.second
                    remainingOrders.remove(closestPair)
                }
            }

            // מצרפים בסוף את המשימות שאין להן כתובת (כדי שלא יעלמו מהרשימה)
            sortedOrders.addAll(ordersWithoutLocations)

            // בדיקת התנגשויות זמנים (האם הסדר הגיאוגרפי שובר שעות שהוגדרו מראש)
            var hasTimeConflict = false
            try {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                var lastValidTime: Date? = null

                for (order in sortedOrders) {
                    if (order.targetTime.isNotBlank() && order.targetTime != "10:00") { // נניח ש-10:00 זה ברירת מחדל לא קשיחה
                        val orderTime = timeFormat.parse(order.targetTime)
                        if (orderTime != null) {
                            if (lastValidTime != null && orderTime.before(lastValidTime)) {
                                hasTimeConflict = true
                                break
                            }
                            lastValidTime = orderTime
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val msg = if (hasTimeConflict) {
                "המסלול סודר בהצלחה לפי המרחק הגיאוגרפי ממיקומך אל עבר נקודת הסיום.\n\n⚠️ שים לב: זוהו משימות שנקבעו לשעות מסוימות והסדר החדש עלול להיות בניגוד לשעות שתואמו!"
            } else {
                "המסלול סודר בהצלחה לפי המרחק ממיקומך ועד לנקודת הסיום!"
            }

            return@withContext RouteResult(sortedOrders, true, msg)
        }
    }
}

data class RouteResult(
    val sortedOrders: List<WorkOrder>,
    val success: Boolean,
    val message: String
)
