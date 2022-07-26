package com.xiaojinzi.controller

import com.google.gson.Gson
import com.xiaojinzi.bean.DevelopAuthVOReq
import com.xiaojinzi.bean.DevelopAuthVORes
import com.xiaojinzi.bean.ResultVORes
import com.xiaojinzi.util.RSAUtil
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.nio.charset.Charset
import java.util.*

@Controller
@RequestMapping("develop")
class DevelopController {

    companion object {
        private const val PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMtGv5X/dvxI/Mx3bnBdtmE2h0O98DW2sDByFHboO9lL6wxSHlDZbXanJQkhoGYxo9tFIbC3QMhFf95bNH6g32r02n45E3md6WN6PlCfhJFbowcGhW0FVzDkGlrvrizN11iwK1PEsdKxw8N+uaBVT/gvQ3rEmgmz6J4OCOk1dALTAgMBAAECgYEAvzUnVaLde2X0VSgqfAvKWBsAlVn+r6fOh2NnmInOchGVYRuFZKuA6dFDZxl4VWhwJvsaO63EhB3Lr46/DDWqsLQGsrmkHIkVbm2LP/+H3jlteBSDk/Ho17IXzgAmH8MLLegHr68IEAecWYb7T19b0ettO0cq+Ql/+KmfG+Ls/EECQQD5nWiDOX/Sv0UcGYj9yJ8skgY8VNSxFiyJJKcHoNcXfeE9QdHkycbnlyBVPcwwhz3U75XjJc5vnj+T2pmPDlpxAkEA0Hnk+wyfM2HEdPGNsIB60gE1RbGebW7U6zYnJ1CAbC11zsOir8b1/Twjk3EOFHm4iX1ipiS9JV+QYNmhZHbrgwJAHyvA+WIczDyGbNPjf42mEvLJRI9zYAnc1eN12EYFljFqBzRI+cEYzaLZrstgzE6XMhgZJ5x5AwdH+Ta7JlosgQJAa4aTTdECw7OnalG0LNb3gh1RZrLn7bV+aBq0MxjYQ/NmkdBhtpu+AHDmQIPNKU5mmbNsEBle3Une47UcAv87FQJAHdxgoIT6wckkept/TxgxB2jxO6f5kkbGm1N7MCK2XZ0CxKu5NC7wtktRgalzoJsVKil35yEgtIEEOw/EY2fBPg=="
        private const val PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDLRr+V/3b8SPzMd25wXbZhNodDvfA1trAwchR26DvZS+sMUh5Q2W12pyUJIaBmMaPbRSGwt0DIRX/eWzR+oN9q9Np+ORN5neljej5Qn4SRW6MHBoVtBVcw5Bpa764szddYsCtTxLHSscPDfrmgVU/4L0N6xJoJs+ieDgjpNXQC0wIDAQAB"
        private val UTF_8 = Charset.forName("UTF-8")
        private val g = Gson()
    }

    /**
     * 授予客户端可以打开 develop 功能的接口
     *
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("auth")
    @Throws(Exception::class)
    fun auth(@RequestParam("content") content: String?): ResultVORes<String> {
        val privateKey = RSAUtil.string2PrivateKey(PRIVATE_KEY)
        val contentBytes = Base64.getDecoder().decode(content)
        // 解密出来的东西
        val decryptBytes = RSAUtil.privateDecrypt(contentBytes, privateKey)

        val developAuthVOReq = g.fromJson(String(decryptBytes, UTF_8), DevelopAuthVOReq::class.java)
        print("developAuthVOReq = ${developAuthVOReq.toString()}")
        val developAuthVORes = DevelopAuthVORes(
                developAuthVOReq.content,
                developAuthVOReq.startTime,
                developAuthVOReq.validTime,
                // 结束时间
                developAuthVOReq.startTime + developAuthVOReq.validTime
        )
        val encryptBytes = RSAUtil.privateEncrypt(
                g.toJson(developAuthVORes).toByteArray(UTF_8),
                privateKey
        )
        val encryptStr = Base64.getEncoder().encodeToString(encryptBytes)
        return ResultVORes.success(encryptStr)
    }

}