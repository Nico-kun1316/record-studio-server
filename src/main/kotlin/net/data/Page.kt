package net.data

import io.ktor.http.*
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SortOrder

const val minPerPage = 5
const val perPage = 25
const val maxPerPage = 100
const val minPage = 0
const val defaultPage = 0

data class Page(val number: Int, val itemCount: Int, val ordering: SortOrder)

val Parameters.page: Page
    get() {
        val number = (this["page"]?.toInt() ?: defaultPage).coerceAtLeast(minPage)
        val itemCount = (this["perPage"]?.toInt() ?: perPage).coerceIn(minPerPage..maxPerPage)
        val ordering = SortOrder.values().find { it.name.toLowerCase() == this["order"] }
                ?: SortOrder.ASC

        return Page(number, itemCount, ordering)
    }

fun <E : UUIDEntity> UUIDEntityClass<E>.paged(page: Page): SizedIterable<E> = all()
        .limit(page.itemCount, page.number.toLong() * page.itemCount.toLong())
        .orderBy(table.id to page.ordering)
