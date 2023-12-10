package com.roy93group.notes.model.converter

import androidx.room.TypeConverter
import com.roy93group.notes.model.entity.NoteStatus
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object NoteStatusConverter : KSerializer<NoteStatus> {

    @TypeConverter
    @JvmStatic
    fun toInt(status: NoteStatus) = status.value

    @TypeConverter
    @JvmStatic
    fun toStatus(value: Int) = NoteStatus.fromValue(value)

    override val descriptor = PrimitiveSerialDescriptor("NoteStatus", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: NoteStatus) = encoder.encodeInt(toInt(value))

    override fun deserialize(decoder: Decoder) = toStatus(decoder.decodeInt())
}
