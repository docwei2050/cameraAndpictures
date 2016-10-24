package utils;

import android.text.TextUtils;

/**
 * Created by EX-LIYONGCHANG001 on 27/1/16.
 */
public class StringUtil {
    /**
     * 字符串是为空或者为""
     *
     * @Description
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {

        return TextUtils.isEmpty(str);
    }
}
