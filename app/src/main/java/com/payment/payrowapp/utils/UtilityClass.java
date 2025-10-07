package com.payment.payrowapp.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UtilityClass {

    public static void setMargins (Context context,View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

            final float scale = context.getResources().getDisplayMetrics().density;
            // convert the DP into pixel
            int l =  (int)(left * scale + 0.5f);
            int r =  (int)(right * scale + 0.5f);
            int t =  (int)(top * scale + 0.5f);
            int b =  (int)(bottom * scale + 0.5f);

            p.setMargins(l, t, r, b);
            view.requestLayout();
        }
    }

    public static String null2String(String str) {
        return str == null ? "" : str;
    }


    public static String CompareTwoDatesCount(String format,
                                              String FromDate, String ToDate)

            throws ParseException {

        Date date1;
        Date date2;
        SimpleDateFormat dates = new SimpleDateFormat(format);

        // Setting dates
        date1 = dates.parse(FromDate);

        date2 = dates.parse(ToDate);

        // Comparing dates
        long difference = Math.abs(date2.getTime() - date1.getTime());

        long differenceDates = difference / (24 * 60 * 60 * 1000);

        // Convert long to String
        String dayDifference = Long.toString(differenceDates + 1);

        // Plus 1 is added additionally.Bcoz, days difference not taking start
        // date itself..

        return dayDifference;

    }

    public static String stringToHex(String str) {
        StringBuilder hexString = new StringBuilder();
        char[] chars = str.toCharArray();
        for (char ch : chars) {
            hexString.append(Integer.toHexString((int) ch));
        }
        return hexString.toString();
    }
}
