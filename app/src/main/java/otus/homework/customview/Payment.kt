package otus.homework.customview

import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Int
)
