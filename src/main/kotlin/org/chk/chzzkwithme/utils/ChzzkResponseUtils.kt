package org.chk.chzzkwithme.utils

import org.chk.chzzkwithme.utils.ChzzkResponseUtils.CHZZK_CODE
import org.chk.chzzkwithme.utils.ChzzkResponseUtils.OK

object ChzzkResponseUtils {
    const val CHZZK_CODE = "code"
    const val OK = "200"
    const val MESSAGE = "message"
}

internal fun <T, R> T.resolveResultOrFailProcess(
    body: String,
    block: T.() -> R,
): R {
    val jsonNode = readTree(body)
    val code = jsonNode.get(CHZZK_CODE).asText() ?: throw IllegalStateException("Invalid Chzzk Response")

    if (code != OK) {
        throw IllegalStateException("Invalid Chzzk Response ${jsonNode.get(ChzzkResponseUtils.MESSAGE).asText()}")
    }

    return block(this)
}
