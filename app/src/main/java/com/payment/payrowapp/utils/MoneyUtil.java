package com.payment.payrowapp.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class MoneyUtil {

    public static long stringMoney2LongCent(String amount) {
        BigDecimal bd = new BigDecimal(amount);
        BigDecimal bigDecimal = new BigDecimal("100");
        return bd.multiply(bigDecimal).longValue();
    }

    public static String longCent2DoubleMoneyStr(long amount) {
        BigDecimal bd = new BigDecimal(amount);
        BigDecimal bigDecimal = new BigDecimal("100");
        double doubleValue = bd.divide(bigDecimal).doubleValue();
        DecimalFormat df = new DecimalFormat("#0.00");
        return df.format(doubleValue);
    }

    public static String parseAmountFromEMV(String emvData) throws Exception {
        String tag = "9F02";
        int index = emvData.indexOf(tag);
        if (index == -1) {
            throw new Exception("Tag 9F02 not found in the data.");
        }

        // Move index to the length byte after the tag
        index += tag.length();

        // Get the length of the value in bytes (2 hex characters = 1 byte)
        int length = Integer.parseInt(emvData.substring(index, index + 2), 16);
        index += 2; // Move index to the start of the value

        // Extract the value
        String valueHex = emvData.substring(index, index + length * 2);

        // Ensure the hex value is 12 characters long (6 bytes) as per EMV standard for the 9F02 tag
        if (valueHex.length() != 12) {
            throw new Exception("Invalid value length for tag 9F02.");
        }

        // Parse the hex value to a long, assuming it's in BCD format
        long value = Long.parseLong(valueHex);

        // Format the value as a currency amount (assuming the value is in cents)
        return String.format("%.2f", value / 100.0);
    }

    public static String decimalToEMVHex(String decimalNumber) {
        // Convert the decimal number to a string
        String decimalString = decimalNumber;

        // Pad with leading zeros to make sure it has at least 12 characters
        while (decimalString.length() < 12) {
            decimalString = "0" + decimalString;
        }

        // Create a StringBuilder to store the BCD result
        StringBuilder bcd = new StringBuilder(decimalString.length() / 2);

        // Convert each pair of characters into a BCD byte
        for (int i = 0; i < decimalString.length(); i += 2) {
            int highNibble = Character.digit(decimalString.charAt(i), 10);
            int lowNibble = Character.digit(decimalString.charAt(i + 1), 10);
            int bcdByte = (highNibble << 4) | lowNibble;
            bcd.append(String.format("%02x", bcdByte));
        }

        return bcd.toString();
    }

}
