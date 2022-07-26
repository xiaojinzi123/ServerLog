package com.xiaojinzi.util;

import com.xiaojinzi.anno.Nullable;

public class Strings {

    public static boolean isEmpty(@Nullable String value) {
        return value == null || value.isEmpty();
    }

}
