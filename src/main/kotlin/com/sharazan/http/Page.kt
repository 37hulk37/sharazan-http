package com.sharazan.http

data class Page<T>(
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val values: Collection<T>
) {

    constructor(values: Collection<T>, page: Int = 0, totalElements: Long = values.size.toLong()) : this(
        page = page,
        size = values.size,
        totalElements = totalElements,
        values = values
    )

}