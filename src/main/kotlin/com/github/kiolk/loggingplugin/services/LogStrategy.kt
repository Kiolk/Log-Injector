package com.github.kiolk.loggingplugin.services

import com.github.kiolk.loggingplugin.settings.LoggingSettings
import com.intellij.psi.PsiElementFactory
import org.jetbrains.kotlin.psi.KtPsiFactory

interface LogStrategy {
    fun createKotlinLog(
        factory: KtPsiFactory,
        tag: String,
        message: String,
    ): String

    fun createJavaLog(
        factory: PsiElementFactory,
        tag: String,
        message: String,
    ): String

    fun getRemovalPatterns(tag: String): List<String>

    fun getKotlinImport(): String?

    fun getJavaImport(): String?
}

class PrintlnStrategy : LogStrategy {
    override fun createKotlinLog(
        factory: KtPsiFactory,
        tag: String,
        message: String,
    ): String = "println(\"$tag: $message\")"

    override fun createJavaLog(
        factory: PsiElementFactory,
        tag: String,
        message: String,
    ): String = "System.out.println(\"$tag: $message\");"

    override fun getRemovalPatterns(tag: String): List<String> = listOf(tag)

    override fun getKotlinImport(): String? = null

    override fun getJavaImport(): String? = null
}

class TimberStrategy : LogStrategy {
    override fun createKotlinLog(
        factory: KtPsiFactory,
        tag: String,
        message: String,
    ): String = "Timber.tag(\"$tag\").d(\"$message\")"

    override fun createJavaLog(
        factory: PsiElementFactory,
        tag: String,
        message: String,
    ): String = "Timber.tag(\"$tag\").d(\"$message\");"

    override fun getRemovalPatterns(tag: String): List<String> = listOf("Timber.tag(\"$tag\")", tag)

    override fun getKotlinImport(): String = "timber.log.Timber"

    override fun getJavaImport(): String = "timber.log.Timber"
}

class NapierStrategy : LogStrategy {
    override fun createKotlinLog(
        factory: KtPsiFactory,
        tag: String,
        message: String,
    ): String = "Napier.d(\"$message\", tag = \"$tag\")"

    override fun createJavaLog(
        factory: PsiElementFactory,
        tag: String,
        message: String,
    ): String = "Napier.d(\"$message\", tag = \"$tag\");"

    override fun getRemovalPatterns(tag: String): List<String> = listOf("tag = \"$tag\"", tag)

    override fun getKotlinImport(): String = "io.github.aakira.napier.Napier"

    override fun getJavaImport(): String? = null
}

object LogStrategyFactory {
    fun getStrategy(framework: LoggingSettings.LoggingFramework): LogStrategy {
        return when (framework) {
            LoggingSettings.LoggingFramework.PRINTLN -> PrintlnStrategy()
            LoggingSettings.LoggingFramework.TIMBER -> TimberStrategy()
            LoggingSettings.LoggingFramework.NAPIER -> NapierStrategy()
        }
    }
}
