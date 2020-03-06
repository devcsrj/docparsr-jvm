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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object DocumentTest : Spek({

    describe("document") {

        it("can accept visitor") {
            val document = Document(object : ParsingResult {
                override fun id() = "fake-document-id"
                override fun source(format: Format) = javaClass.getResourceAsStream("/document.json")
            })
            val visitor = mock<DocumentVisitor> {}
            document.accept(visitor)

            verify(visitor).visitMeta(any())
            verify(visitor).visitPage(any())
            verify(visitor).visitHeading(any())
            verify(visitor).visitLine(any())
            verify(visitor).visitParagraph(any())
            verify(visitor, times(3)).visitWord(any())
            verify(visitor).visitAnyElement(any())
            verify(visitor).visitFont(any())
        }
    }
})
