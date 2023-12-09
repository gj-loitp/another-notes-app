

package com.roy93group.notes.model.converter

import androidx.room.TypeConverter
import com.maltaisn.recurpicker.Recurrence
import com.maltaisn.recurpicker.format.RRuleFormatter
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// Can't use external serializer annotation with Recurrence class outside of app module!
// Instead @Serializable(with = ...) is used on every instance that needs to be serialized.
//@Serializer(forClass = Recurrence::class)
object RecurrenceConverter : KSerializer<Recurrence> {

    private val rruleFormatter = RRuleFormatter()

    @TypeConverter
    @JvmStatic
    fun toRecurrence(rrule: String?) = rrule?.let { rruleFormatter.parse(it) }

    @TypeConverter
    @JvmStatic
    fun toRRule(recurrence: Recurrence?) = recurrence?.let { rruleFormatter.format(it) }

    override val descriptor = PrimitiveSerialDescriptor("Recurrence", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Recurrence) =
        encoder.encodeString(toRRule(value)!!)

    override fun deserialize(decoder: Decoder) = toRecurrence(decoder.decodeString())!!
}
