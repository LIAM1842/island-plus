package com.akira.island.core

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalConfiguration
import com.akira.island.data.AppLanguage
import com.akira.island.data.SettingsRepo
import java.util.Locale

/**
 * Язык приложения и острова: русский или английский. «Как в системе» — русский, если телефон на русском,
 * иначе английский. Строки пишутся парой прямо в коде: tr("Зарядка", "Charging").
 */
object Lang {
    /** Иврит: строки берутся из HebrewStrings, а где перевода нет — остаётся английский */
    val he: Boolean
        get() = when (SettingsRepo.current.language) {
            AppLanguage.HE -> true
            AppLanguage.SYSTEM -> systemLanguage().let { it == "he" || it == "iw" }
            else -> false
        }

    /** Английский текст (для иврита это запасной вариант, там, где нет перевода) */
    val en: Boolean
        get() = when (SettingsRepo.current.language) {
            AppLanguage.EN, AppLanguage.HE -> true
            AppLanguage.RU -> false
            AppLanguage.SYSTEM -> systemLanguage() != "ru"
        }

    /** Для дат: «пятница, 24 октября» или «Friday, October 24» */
    val locale: Locale
        get() = when {
            he -> Locale.forLanguageTag("he")
            en -> Locale.ENGLISH
            else -> Locale.forLanguageTag("ru")
        }

    private fun systemLanguage(): String =
        runCatching { Resources.getSystem().configuration.locales[0].language }.getOrNull() ?: Locale.getDefault().language
}

/** Строка на текущем языке. */
fun tr(ru: String, en: String): String = when {
    Lang.he -> HebrewStrings.map[en] ?: en
    Lang.en -> en
    else -> ru
}

/**
 * Сменили язык (в настройках или в системе) — всё внутри рисуется заново уже на новом языке.
 * Состояние снаружи (открытая вкладка и экран) остаётся.
 */
@Composable
fun LangScope(content: @Composable () -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    // Язык системы меняется вместе с конфигурацией — чтение подписывает на неё
    LocalConfiguration.current
    key(s.language, Lang.en) { content() }
}
