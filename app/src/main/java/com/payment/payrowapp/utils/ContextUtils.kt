package com.payment.payrowapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.Build
import android.os.LocaleList
import android.util.Base64
import android.util.Log
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.ItemDetail
import com.payment.payrowapp.newpayment.TinyDB
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import java.io.UnsupportedEncodingException
import java.math.BigDecimal
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

class ContextUtils(base: Context) : ContextWrapper(base) {


    companion object {


        fun formatWithCommas(amount: Double): String {
            return if (amount == 0.0) {
                "0.00"
            } else {
                val amount = BigDecimal(amount)
                val formatter = DecimalFormat("#,###.##")
                formatter.format(amount)
            }
        }

        fun updateLocale(c: Context, localeToSwitchTo: Locale): ContextWrapper {
            var context = c
            val resources: Resources = context.resources
            val configuration: Configuration = resources.configuration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val localeList = LocaleList(localeToSwitchTo)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
            } else {
                configuration.locale = localeToSwitchTo
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                context = context.createConfigurationContext(configuration)
            } else {
                resources.updateConfiguration(configuration, resources.displayMetrics)
            }
            return ContextUtils(context)
        }

        @SuppressLint("HardwareIds")
        fun getDeviceId(context: Context): String {
//            val telephonyManager:TelephonyManager=context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )
        }


        fun decoded(JWTEncoded: String) {
            try {
                val split = JWTEncoded.split(".")
                Log.d("JWT_DECODED", "Header: " + getJson(split[0]))
                Log.d("JWT_DECODED", "Body: " + getJson(split[1]))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }

        private fun getJson(strEncoded: String): String {
            val decodeBytes: ByteArray = Base64.decode(strEncoded, Base64.URL_SAFE)
            return String(decodeBytes, Charset.defaultCharset())
        }


        fun clearPreferences(context: Context) {
            val preference =
                context.getSharedPreferences(
                    context.resources.getString(R.string.app_name),
                    Context.MODE_PRIVATE
                )
            preference.all.clear()
        }


        fun getCurrentDate(): String {
            val calender = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("MMdd", Locale.getDefault())
            return dateFormat.format(calender)
        }

        fun getCurrentTime(): String {
            val calender = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("HHmmss", Locale.getDefault())
            return dateFormat.format(calender)
        }

        fun formatDatetoformat(year: Int, month: Int, day: Int): String {
            val cal = Calendar.getInstance()
            cal.timeInMillis = 0
            cal[year, month] = day
            val date = cal.time
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            return sdf.format(date)
        }

        fun randomValue(): Int {
            return (10000..99999).shuffled().last()
        }


        fun getSelecteItems(tinyDB: TinyDB): ArrayList<ItemDetail> {
            return tinyDB.getListObject(
                Constants.SELECTED_ITEMS,
                ItemDetail::class.java
            ) as ArrayList<ItemDetail>
        }

        fun isNetworkConnected(mContext: Context): Boolean {
            val cm = mContext
                .getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            return Objects.requireNonNull(cm).activeNetworkInfo != null
        }

        fun responseMessage(responseCode: String): Int? {
            when (responseCode) {
                "01" -> return R.string.response_one
                "02" -> return R.string.response_two
                "03" -> return R.string.response_three
                "04" -> return R.string.response_four
                "05" -> return R.string.response_five
                "06" -> return R.string.response_six
                "07" -> return R.string.response_seven
                "08" -> return R.string.response_eight

                "09" -> return R.string.response_nine
                "10" -> return R.string.response_ten
                "11" -> return R.string.response_eleven
                "12" -> return R.string.response_twelve
                "13" -> return R.string.response_thirteen
                "14" -> return R.string.response_fourtheen
                "15" -> return R.string.response_fifteen
                "16" -> return R.string.response_sixteen

                "17" -> return R.string.response_seventeen
                "18" -> return R.string.response_eighteen
                "19" -> return R.string.response_nineteen
                "20" -> return R.string.response_twenty
                "21" -> return R.string.response_twentone
                "22" -> return R.string.response_twentytwo
                "23" -> return R.string.response_twentythree
                "24" -> return R.string.response_twentyfour

                "25" -> return R.string.response_twentyfive
                "26" -> return R.string.response_twentysix
                "27" -> return R.string.response_twentyseven
                "28" -> return R.string.response_twentyeight
                "29" -> return R.string.response_twentynine
                "30" -> return R.string.response_twirty
                "31" -> return R.string.response_twirtyone
                "32" -> return R.string.response_twirtytwo

                "33" -> return R.string.response_twirtythree
                "34" -> return R.string.response_twirtyfour
                "35" -> return R.string.response_twirtyfive
                "36" -> return R.string.response_twirtysix
                "37" -> return R.string.response_twirtyseven
                "38" -> return R.string.response_twirtyeight
                "39" -> return R.string.response_twirtynine
                "40" -> return R.string.response_fourty

                "41" -> return R.string.response_fourtyone
                "42" -> return R.string.response_fourtytwo
                "43" -> return R.string.response_fourtythree
                "44" -> return R.string.response_fourtyfour
                "45" -> return R.string.response_fourtyfive
                "46" -> return R.string.response_fourtysix
                "47" -> return R.string.response_fourtyseven
                "48" -> return R.string.response_fourtyeight

                "49" -> return R.string.response_fourtynine
                "50" -> return R.string.response_fifty
                "51" -> return R.string.response_fiftyone
                "52" -> return R.string.response_fiftytwo
                "53" -> return R.string.response_fiftythree
                "54" -> return R.string.response_fiftyfour
                "55" -> return R.string.response_fiftyfive
                "56" -> return R.string.response_fiftysix

                "57" -> return R.string.response_fiftyseven
                "58" -> return R.string.response_fiftyeight
                "59" -> return R.string.response_fiftynine
                "60" -> return R.string.response_sixty
                "61" -> return R.string.response_sixtyone
                "62" -> return R.string.response_sixtytwo
                "63" -> return R.string.response_sixtythree
                "64" -> return R.string.response_sixtyfour

                "65" -> return R.string.response_sixtyfive
                "66" -> return R.string.response_sixtysix
                "67" -> return R.string.response_sixtyseven

                "68" -> return R.string.response_sixtyeight
                "69" -> return R.string.response_sixtynine
                "70" -> return R.string.response_seventy
                "71" -> return R.string.response_seventyone
                "72" -> return R.string.response_seventytwo
                "73" -> return R.string.response_seventythree
                "74" -> return R.string.response_seventyfour
                "75" -> return R.string.response_seventyfive

                "76" -> return R.string.response_seventysix
                "77" -> return R.string.response_seventyseven
                "78" -> return R.string.response_seventyeight
                "79" -> return R.string.response_seventynine
                "80" -> return R.string.response_eighty
                "81" -> return R.string.response_eightyone
                "82" -> return R.string.response_eightytwo
                "83" -> return R.string.response_eightythree

                "84" -> return R.string.response_eightyfour
                "85" -> return R.string.response_eightyfive
                "86" -> return R.string.response_eightysix
                "87" -> return R.string.response_eightyseven
                "88" -> return R.string.response_eightyeight
                "89" -> return R.string.response_eightynine
                "90" -> return R.string.response_ninghty
                "91" -> return R.string.response_ninghtyone

                "92" -> return R.string.response_ninghtytwo
                "93" -> return R.string.response_ninghtythree
                "94" -> return R.string.response_ninghtyfour
                "95" -> return R.string.response_ninghtyfive
                "96" -> return R.string.response_ninghtysix
                "97" -> return R.string.response_ninghtyseven
                "98" -> return R.string.response_ninghtyeight
                "99" -> return R.string.response_ninghtynine
                "XA" -> return R.string.xa
                "XB" -> return R.string.xb
                "XC" -> return R.string.xc
                "XD" -> return R.string.xd
                "XE" -> return R.string.xe
                "XF" -> return R.string.xf
                "XG" -> return R.string.xg
                "XH" -> return R.string.xh
                "XI" -> return R.string.xi

                "YM" -> return R.string.ym
                "YN" -> return R.string.yn
                "YO" -> return R.string.yo
                "YP" -> return R.string.yp
                "YQ" -> return R.string.yq
                "YR" -> return R.string.yr

                "SD" -> return R.string.sd
                "IS" -> return R.string.i_s
                "AI" -> return R.string.ai
            }
            return R.string.failed
        }

        fun getBase64String(invoiceNo: String): String {
            val data = invoiceNo.toByteArray(StandardCharsets.UTF_8)
            return Base64.encodeToString(data, Base64.DEFAULT)
        }


        fun getHeaderMap(
            accessToken: String,
            timeStamp: String,
            xSignature: String,
            context: Context
        ): Map<String, String> {
            val sharedpreferenceUtil = SharedPreferenceUtil(context)
            val headerMap = mutableMapOf<String, String>()
            headerMap["x-deviceid"] = getDeviceId(context)
            headerMap["x-uuid"] = sharedpreferenceUtil.getUUID()//HeaderSignatureUtil.getDeviceSN()
            headerMap["x-timestamp"] = timeStamp
            headerMap["x-signature"] = xSignature
            if (accessToken.isNotEmpty()) {
                headerMap["Authorization"] = accessToken
            }

            return headerMap
        }

        fun generateUUID(): String {
            var uuid: String? = null
            uuid = UUID.randomUUID().toString().replace("-".toRegex(), "")
                .uppercase(Locale.getDefault())
            return uuid
        }

        fun postDate(): String {
            val date = LocalDate.now()
            // Define a formatter for DDMMYYYY
            val formatter = DateTimeFormatter.ofPattern("ddMMyyyy")
            return date.format(formatter)
        }

        fun acqTransactionCompletionDate(): String? {
            val currentDateTime = LocalDateTime.now()
            // Define a formatter for "YYYY-MM-DD HH:mm:ss" format
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            // Format the current date-time
            return currentDateTime.format(formatter)
        }

        fun getCardBrand(cardNumber: String): String {
            val cardBin = extractBin(cardNumber)
            when {
                isBinInRange(cardBin, "222100", "272099") -> {
                    return "MASTERCARD"
                }
                isBinInRange(cardBin, "510000", "559999") -> {
                    return "MASTERCARD"
                }
                isBinInRange(cardBin, "400000", "499999") -> {
                    return "VISA"
                }
                isBinInRange(cardBin, "500000", "509999") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "560000", "575886") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "575888", "589999") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "600000", "601381") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "601383", "601427") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "601429", "602906") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "602908", "602968") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "602970", "603264") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "603266", "603366") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "603368", "603600") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "603602", "603693") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "603695", "603707") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "603709", "619999") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "621800", "621976") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "621978", "622125") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "627000", "627065") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "627068", "628199") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "629400", "650007") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "650009", "650400") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "650402", "650482") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "650484", "656000") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "656002", "670892") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "670894", "677174") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "677176", "677365") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "677367", "677517") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "677519", "685799") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "628900", "629099") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "685900", "690749") -> {
                    return "MAESTRO"
                }
                isBinInRange(cardBin, "690760", "699999") -> {
                    return "MAESTRO"
                }
                else -> return ""
            }
        }

        private fun isBinInRange(cardBin: String, startBin: String, endBin: String): Boolean {
            // Convert BINs to integers to compare ranges
            val bin = cardBin.toInt()
            val start = startBin.toInt()
            val end = endBin.toInt()

            // Check if the card's BIN falls within the given range
            return bin in start..end
        }

        private fun extractBin(cardNumber: String): String {
            // Extract the first 6 digits as BIN
            return cardNumber.take(6)
        }

        fun getRandomLastValue(): Int {
            return (1000..9999).shuffled().last()
        }

        fun splitDecimal(serviceCharges: Float): String {
            return String.format("%.2f", serviceCharges)
        }

        fun formatShortDateTime(isoString: String): String {
            try {
                // Parse the ISO date
                val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                //   isoFormat.timeZone = TimeZone.getTimeZone("UTC") // Set timezone to UTC

                // Parse the date from ISO string
                val date: Date = isoFormat.parse(isoString) ?: Date()

                // Format to short date/time format
                val shortFormat = SimpleDateFormat(
                    "MMM d",
                    Locale.getDefault()
                ) // e.g., "Nov 13, 1:00 PM"
                return shortFormat.format(date)
            } catch (e: Exception) {
                return isoString.substring(0, 10)
            }
        }

        fun getTransAmount(amount: String): String {
            var amountValue = amount.toFloat()
            amountValue *= 100
            val fAmount = (amountValue + 0.1).toInt()
            return getHexaDecimal(fAmount.toString())
        }

        private fun getHexaDecimal(amount: String): String {
            var myAmount = amount
            when (amount.length) {
                1 -> {
                    myAmount = "000000000" + amount + "00"
                }
                2 -> {
                    myAmount = "00000000" + amount + "00"
                }
                3 -> {
                    myAmount = "000000000$amount"
                }
                4 -> {
                    myAmount = "00000000$amount"
                }
                5 -> {
                    myAmount = "0000000$amount"
                }
                6 -> {
                    myAmount = "000000$amount"
                }
                7 -> {
                    myAmount = "00000$amount"
                }
                8 -> {
                    myAmount = "0000$amount"
                }
                else -> {
                    myAmount = "000000000000"
                }
            }

            return myAmount
        }
        fun getVersionName(context: Context): String {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionName
        }
    }//Object ends
}