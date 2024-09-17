package org.olymika.chzzkwithme

import kotlinx.serialization.Serializable

@Serializable
data class ChzzkUser(
    val hasProfile: Boolean = false,
    val userIdHash: String? = null,
    val nickname: String? = null,
    val profileImageUrl: String? = null,
)
