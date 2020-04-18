/*
 * Copyright 2020 Nicolas Maltais
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maltaisn.notes.ui.edit.adapter


sealed class EditListItem {
    abstract val type: Int
}

data class EditTitleItem(var title: CharSequence,
                         var editable: Boolean) : EditListItem() {
    override val type: Int
        get() = EditAdapter.VIEW_TYPE_TITLE
}

data class EditContentItem(var content: CharSequence,
                           val editable: Boolean) : EditListItem() {
    override val type: Int
        get() = EditAdapter.VIEW_TYPE_CONTENT
}

data class EditItemItem(var content: CharSequence,
                        var checked: Boolean, val editable: Boolean) : EditListItem() {
    override val type: Int
        get() = EditAdapter.VIEW_TYPE_ITEM
}

class EditItemAddItem : EditListItem() {
    override val type: Int
        get() = EditAdapter.VIEW_TYPE_ITEM_ADD
}
