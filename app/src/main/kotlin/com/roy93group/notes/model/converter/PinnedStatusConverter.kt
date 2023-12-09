

package com.roy93group.notes.model.converter

import androidx.room.TypeConverter
import com.roy93group.notes.model.entity.PinnedStatus
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object PinnedStatusConverter : KSerializer<PinnedStatus> {

    @TypeConverter
    @JvmStatic
    fun toInt(status: PinnedStatus) = status.value

    @TypeConverter
    @JvmStatic
    fun toStatus(value: Int) = PinnedStatus.fromValue(value)

    override val descriptor = PrimitiveSerialDescriptor("PinnedStatus", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: PinnedStatus) = encoder.encodeInt(toInt(value))

    override fun deserialize(decoder: Decoder) = toStatus(decoder.decodeInt())
}
