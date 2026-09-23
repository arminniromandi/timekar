package com.example.domain.model

enum class Category(val key: String, val englishLabel: String, val persianLabel: String) {
    PRODUCT("Product", "Product", "محصول"),
    PRODUCT_CORE("Product Core", "Product Core", "هسته محصول"),
    SPECS("Specs", "Specs", "مستندات"),
    ARCHITECTURE("Architecture", "Architecture", "معماری"),
    ENG("Eng", "Eng", "فنی"),
    DESIGN("Design", "Design", "دیزاین"),
    FINANCE("Finance", "Finance", "مالی"),
    PERSONAL("Personal", "Personal", "شخصی"),
    GENERAL("General", "General", "عمومی");

    companion object {
        fun fromKey(key: String): Category {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: GENERAL
        }
    }
}
