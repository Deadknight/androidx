/*
 * Copyright 2023 The Android Open Source Project
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

package androidx.compose.ui.text.input

import View
import cocoapods.ToppingCompose.IME_OPTION
import cocoapods.ToppingCompose.IME_OPTION_DEFAULT
import cocoapods.ToppingCompose.IME_OPTION_DONE
import cocoapods.ToppingCompose.IME_OPTION_GO
import cocoapods.ToppingCompose.IME_OPTION_NEXT
import cocoapods.ToppingCompose.IME_OPTION_NONE
import cocoapods.ToppingCompose.IME_OPTION_PREVIOUS
import cocoapods.ToppingCompose.IME_OPTION_SEARCH
import cocoapods.ToppingCompose.IME_OPTION_SEND
import cocoapods.ToppingCompose.LGEditText
import toLuaTranslator

fun ImeAction.toIOSImeAction(): IME_OPTION {
    return when (this) {
        ImeAction.None -> IME_OPTION_NONE
        ImeAction.Default -> IME_OPTION_DEFAULT
        ImeAction.Go -> IME_OPTION_GO
        ImeAction.Search -> IME_OPTION_SEARCH
        ImeAction.Send -> IME_OPTION_SEND
        ImeAction.Previous -> IME_OPTION_PREVIOUS
        ImeAction.Next -> IME_OPTION_NEXT
        ImeAction.Done -> IME_OPTION_DONE
        else -> IME_OPTION_DEFAULT
    }
}

class TextInputServiceAndroid(
    val view: View,
    //rootPositionCalculator: PositionCalculator
) : PlatformTextInputService {
    var onEditCommand: ((List<EditCommand>) -> Unit)? = null
    var onImeActionPerformed: ((ImeAction) -> Unit)? = null
    override fun startInput(
        value: TextFieldValue,
        imeOptions: ImeOptions,
        onEditCommand: (List<EditCommand>) -> Unit,
        onImeActionPerformed: (ImeAction) -> Unit
    ) {
        if(view is LGEditText) {
            view.setImeOption(imeOptions.imeAction.toIOSImeAction())
            view.setImeAction {
                onImeActionPerformed.invoke(imeOptions.imeAction)
            }
            //TODO:Add other commands
            val ltTextChangedListener: ((LGEditText, String) -> Unit) = { etv, strval ->
                onEditCommand.invoke(listOf(CommitTextCommand(strval, 0)))
            }
            view.ltTextChangedListener = ltTextChangedListener.toLuaTranslator(view)
        }

        this.onEditCommand = onEditCommand
        this.onImeActionPerformed = onImeActionPerformed
        view._view?.becomeFirstResponder()
    }

    override fun stopInput() {
        view._view?.resignFirstResponder()
    }

    override fun showSoftwareKeyboard() {
        view._view?.becomeFirstResponder()
    }

    override fun hideSoftwareKeyboard() {
        view._view?.resignFirstResponder()
    }

    override fun updateState(oldValue: TextFieldValue?, newValue: TextFieldValue) {
        if(oldValue == newValue)
            return

        if(view is LGEditText) {
            view.setTextInternal(newValue.text)
        }
    }
}