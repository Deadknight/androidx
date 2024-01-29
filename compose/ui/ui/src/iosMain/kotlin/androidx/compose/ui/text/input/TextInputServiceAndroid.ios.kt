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

@file:OptIn(ExperimentalTextApi::class)

package androidx.compose.ui.text.input

import View
import androidx.compose.ui.text.ExperimentalTextApi

/**
 * Provide Android specific input service with the Operating System.
 *
 * @param inputCommandProcessorExecutor [Executor] used to schedule the [processInputCommands]
 * function when a input command is first requested for a frame.
 */
internal class TextInputServiceIOS(
    val view: View
) : PlatformTextInputService {
    override fun startInput(
        value: TextFieldValue,
        imeOptions: ImeOptions,
        onEditCommand: (List<EditCommand>) -> Unit,
        onImeActionPerformed: (ImeAction) -> Unit
    ) {

    }

    override fun stopInput() {

    }

    override fun showSoftwareKeyboard() {

    }

    override fun hideSoftwareKeyboard() {

    }

    override fun updateState(oldValue: TextFieldValue?, newValue: TextFieldValue) {

    }

}
