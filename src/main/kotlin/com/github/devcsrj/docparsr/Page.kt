/**
 * Copyright (c) 2020, Reijhanniel Jearl Campos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.devcsrj.docparsr

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.lang.AssertionError
import java.util.*
import kotlin.collections.ArrayList

@JsonIgnoreProperties(ignoreUnknown = true)
data class Page(

    val box: Box,
    val rotation: Rotation,
    @JsonProperty("pageNumber")
    val number: Int,
    val elements: ArrayList<Element<*>>
) {

    fun accept(visitor: PageVisitor) {
        val stack = Stack<Element<*>>()
        stack.addAll(elements.asReversed())
        while (stack.isNotEmpty()) {
            val next = stack.pop()
            if (next.content() is ArrayList<*>) {
                val children = next.content() as ArrayList<Element<*>>
                stack.addAll(children.asReversed())
            }

            when (next) {
                is Heading -> visitor.visitHeading(next)
                is Paragraph -> visitor.visitParagraph(next)
                is Line -> visitor.visitLine(next)
                is Word -> visitor.visitWord(next)
                is AnyElement -> visitor.visitAny(next)
                else -> throw AssertionError("Unexpected type: ${next::class}")
            }
        }
    }
}
