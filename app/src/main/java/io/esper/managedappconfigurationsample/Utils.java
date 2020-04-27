/*
 * Copyright (C) 2020 Shoonya Enterprises Inc. All rights Reserved.
 */
package io.esper.managedappconfigurationsample;

import android.os.Bundle;
import android.os.Parcelable;

import java.util.Arrays;

class Utils {

    static String toString(Bundle bundle) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\n");
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            if (value instanceof Parcelable[]) {
                Parcelable[] valueAsArray = (Parcelable[]) value;
                String[] converted = new String[valueAsArray.length];
                if (valueAsArray.length != 0 && valueAsArray[0] instanceof Bundle) {
                    for (int i = 0; i < valueAsArray.length; i++) {
                        // convert to String and replace
                        converted[i] = toString((Bundle) valueAsArray[i]);
                    }
                }
                value = Arrays.toString(converted);
            } else if (value instanceof String[]) {
                String[] valueAsArray = (String[]) value;
                value = Arrays.toString(valueAsArray);
            } else if (value instanceof Bundle) {
                Bundle valueAsBundle = (Bundle) value;
                value = toString(valueAsBundle);
            }
            stringBuilder.append(key).append("=").append(value).append("\n");
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
