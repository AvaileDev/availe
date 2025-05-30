package io.availe.models

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class BranchId(val value: String) {
    companion object {
        val root: BranchId = BranchId("root")

        @OptIn(ExperimentalUuidApi::class)
        fun random(): BranchId = BranchId(Uuid.random().toString())
    }

    override fun toString(): String = value
}
