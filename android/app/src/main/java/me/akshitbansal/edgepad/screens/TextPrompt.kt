package me.akshitbansal.edgepad.screens

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import me.akshitbansal.edgepad.R
import me.akshitbansal.edgepad.Space
import me.akshitbansal.edgepad.protocol.Frame
import me.akshitbansal.edgepad.protocol.TextKind

/**
 * AND-A-9: the phone's own keyboard, dictation and paste, typed on the laptop as one TEXT frame per 255 bytes.
 * Shared by the keyboard screen and the control surface (#65).
 */
fun askText(
    context: Context,
    owner: View,
    send: (Frame) -> Unit,
) {
    val field =
        EditText(context).apply {
            hint = context.getString(R.string.keyboard_type_text)
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
            val pad = (Space.L * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
        }
    AlertDialog
        .Builder(context)
        .setTitle(R.string.keyboard_type_text)
        .setView(field)
        .setPositiveButton(R.string.keyboard_type_send) { _, _ ->
            TextChunks.split(field.text.toString()).forEach { send(Frame.Text(TextKind.TYPE.id, it)) }
        }.setNegativeButton(android.R.string.cancel, null)
        .openOver(owner) { dialog ->
            // The dialog exists to take text, so the phone's keyboard belongs on screen with it: the
            // surface's Type text (#65) is opened from a pad with no focused field, and on the keyboard
            // screen the board's keys are the app's own, so nothing there had asked for the keyboard.
            field.requestFocus()
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            // Asked again once the window has the focus: the flag above is ignored on some builds when the
            // dialog opens in landscape, and a keyboard that only appears on a second tap is the bug.
            field.post {
                val keyboard = context.getSystemService(InputMethodManager::class.java)
                keyboard?.showSoftInput(field, 0)
            }
        }
}
