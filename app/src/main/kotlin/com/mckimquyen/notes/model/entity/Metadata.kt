package com.mckimquyen.notes.model.entity

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class NoteMetadata

/**
 * Metadata with no information, for a text note.
 */
@Serializable
@SerialName("blank")
object BlankNoteMetadata : NoteMetadata() {
    override fun toString() = "none"
}

/**
 * Metadata to keep the checked state of each item, for a list note.
 */
@Keep
@Serializable
@SerialName("list")
data class ListNoteMetadata(
    @SerialName("checked")
    val checked: List<Boolean>,
) : NoteMetadata()
