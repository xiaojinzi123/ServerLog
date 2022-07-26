package com.xiaojinzi

import com.xiaojinzi.bean.Message
import com.xiaojinzi.bean.MessageFragment
import org.json.JSONObject
import java.util.*

object DataFragment : Runnable {

    const val TAG: String = "DataFragment"

    /*过期时间, 20 秒*/
    const val EXPIRE_TIME = 10 * 1000

    init {
        Thread(this).start()
    }

    private var map: MutableMap<String, LinkedList<MessageFragment>> = mutableMapOf()

    private var dataFirstTimeMap: MutableMap<String, Long> = mutableMapOf()

    /**
     * 添加数据
     */
    fun addData(data: MessageFragment?):Boolean {
        if (data == null) {
            return false
        }
        if (data.uid == null || "" == data.uid) {
            return false;
        }
        if (data.owner == null) {
            return false;
        }
        if (data.owner.uid == null || "" == data.owner.uid) {
            return false;
        }
        if (data.index == null) {
            return false;
        }
        if (data.totalCount == null) {
            return false;
        }
        synchronized(this) {
            var list = map.getOrDefault(data.uid, LinkedList())
            list.add(data)
            map[data.uid!!] = list
            if (!dataFirstTimeMap.containsKey(data.uid)) {
                dataFirstTimeMap[data.uid!!] = System.currentTimeMillis()
            }
        }
        return true
    }

    override fun run() {
        while (true) {
            try {
                checkExpire()
                sendDataIfEnough()
                Thread.sleep(500)
            } catch (_e: Exception) {
                // ignore
            }
        }
    }

    private fun checkExpire() {
        synchronized(this) {
            val keys = dataFirstTimeMap.keys
            val currentTimeMillis = System.currentTimeMillis()
            for (key in keys) {
                // 如果已经过期
                if (currentTimeMillis - dataFirstTimeMap[key]!! > EXPIRE_TIME) {
                    dataFirstTimeMap.remove(key)
                    val list = map.remove(key)
                    if (list == null || list.isEmpty()) {
                        println("$TAG: key = $key 过期了, size = 0")
                    } else{
                        println("$TAG: key = $key 过期了, size = " + list.size + ", totalCount = " + list[0].totalCount)
                    }
                } else{
                    println(TAG + ": key = " + key + " 没有过期, size = " + map.getOrDefault(key, LinkedList()).size)
                }
            }
        }
    }

    private fun sendDataIfEnough() {
        synchronized(this) {
            val keys = map.keys
            for (key in keys) {
                val linkedList = map[key]
                // 如果不为空, 就看下是否数据满了
                if (linkedList != null && linkedList.size > 0) {
                    // 说明这个数据已经满了
                    if (linkedList.size >= linkedList[0].totalCount!!) {
                        map.remove(key)
                        dataFirstTimeMap.remove(key)
                        println("$TAG: key = $key 数据完整收到了")
                        val sb = StringBuffer()
                        linkedList.sortBy { it.index }
                        linkedList.forEach {
                            sb.append(it.data)
                        }
                        var jb = JSONObject(sb.toString())
                        jb.put(Message.ATTR_OWNER, JSONObject(linkedList[0].owner))
                        // 发送实际的数据出去
                        Server.getInstance().forward(jb.toString())
                    }
                }
            }
        }
    }

}