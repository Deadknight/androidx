/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.ui.text.intl

import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.localeWithLocaleIdentifier
import platform.Foundation.scriptCode

/**
 * An Android implementation of Locale object
 */
internal class AndroidLocale(val javaLocale: NSLocale) : PlatformLocale {
    override val language: String
        get() = javaLocale.languageCode

    override val script: String
        get() = javaLocale.scriptCode ?: ""

    override val region: String
        get() = javaLocale.countryCode ?: ""

    //TODO: Check this
    override fun toLanguageTag(): String = javaLocale.toString()
}

/**
 * An Android implementation of LocaleDelegate object for API 23
 */
internal class AndroidLocaleDelegate : PlatformLocaleDelegate {

    override val current: LocaleList
        get() = LocaleList(listOf(Locale(AndroidLocale(NSLocale.currentLocale))))

    override fun parseLanguageTag(languageTag: String): PlatformLocale =
        AndroidLocale(NSLocale.localeWithLocaleIdentifier(languageTag))
}
