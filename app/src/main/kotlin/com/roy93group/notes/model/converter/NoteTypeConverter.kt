package com.roy93group.notes.model.converter

import androidx.room.TypeConverter
import com.roy93group.notes.model.entity.NoteType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object NoteTypeConverter : KSerializer<NoteType> {

    @TypeConverter
    @JvmStatic
    fun toInt(type: NoteType) = type.value

    @TypeConverter
    @JvmStatic
    fun toType(value: Int) = NoteType.fromValue(value)

    override val descriptor = PrimitiveSerialDescriptor("NoteType", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: NoteType) = encoder.encodeInt(toInt(value))

    override fun deserialize(decoder: Decoder) = toType(decoder.decodeInt())
}
