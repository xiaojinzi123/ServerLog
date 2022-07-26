package com.xiaojinzi.bean

data class DevelopAuthVOReq(val content:String, val startTime:Long, val validTime:Long)

data class DevelopAuthVORes(val content:String, val startTime:Long, val validTime:Long, val endTime:Long)