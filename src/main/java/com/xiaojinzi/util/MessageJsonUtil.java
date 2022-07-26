package com.xiaojinzi.util;

import com.xiaojinzi.anno.NotNull;
import com.xiaojinzi.bean.Message;
import org.json.JSONObject;

public class MessageJsonUtil {

    public static boolean isOwnerValid(@NotNull JSONObject jb) {
        return !isOwnerInvalid(jb);
    }

    public static boolean isOwnerInvalid(@NotNull JSONObject jb) {
        JSONObject ownerJsonObject = jb.getJSONObject(Message.ATTR_OWNER);
        String uid = ownerJsonObject.optString(Message.Owner.ATTR_UID);
        String name = ownerJsonObject.optString(Message.Owner.ATTR_NAME);
        return Strings.isEmpty(uid) || Strings.isEmpty(name);
    }

}
