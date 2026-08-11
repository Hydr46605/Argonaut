package it.hydr4.argonaut.widget

import android.content.Context
import android.content.Intent
import it.hydr4.argonaut.MainActivity

/**
 * Resolves what a widget tap opens: the official DidUp Famiglia app when
 * installed (per user preference), otherwise Argonaut itself.
 */
object WidgetLaunch {

    /** DidUP - Famiglia by Argo Software on Google Play. */
    const val DIDUP_FAMIGLIA_PACKAGE = "it.argosoft.didup.famiglia.new"

    fun intent(context: Context): Intent {
        val didUp = context.packageManager.getLaunchIntentForPackage(DIDUP_FAMIGLIA_PACKAGE)
        return didUp ?: Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
