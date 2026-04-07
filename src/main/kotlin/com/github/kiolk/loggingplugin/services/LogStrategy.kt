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

class CustomLogStrategy(
    private val kotlinTemplate: String,
    private val javaTemplate: String,
    private val importPath: String?,
) : LogStrategy {
    override fun createKotlinLog(
        factory: KtPsiFactory,
        tag: String,
        message: String,
    ): String = kotlinTemplate.replace("{tag}", tag).replace("{message}", message)

    override fun createJavaLog(
        factory: PsiElementFactory,
        tag: String,
        message: String,
    ): String {
        val tagReplaced = javaTemplate.replace("{tag}", tag)
        return if (message.endsWith(")")) {
            // Method-style: message ends with ")", the template's closing " is needed to form the ")" string literal
            tagReplaced.replace("{message}", message)
        } else {
            // Assignment-style: message ends with a variable (e.g. " + x"), no closing " from template needed
            tagReplaced.replace("\"{message}\"", "\"$message").replace("{message}", message)
        }
    }

    override fun getRemovalPatterns(tag: String): List<String> = listOf(tag)

    override fun getKotlinImport(): String? = importPath?.takeIf { it.isNotBlank() }

    override fun getJavaImport(): String? = importPath?.takeIf { it.isNotBlank() }
}

object LogStrategyFactory {
    fun getStrategy(
        framework: LoggingSettings.LoggingFramework,
        state: LoggingSettings.State? = null,
    ): LogStrategy {
        return when (framework) {
            LoggingSettings.LoggingFramework.PRINTLN -> PrintlnStrategy()
            LoggingSettings.LoggingFramework.TIMBER -> TimberStrategy()
            LoggingSettings.LoggingFramework.NAPIER -> NapierStrategy()
            LoggingSettings.LoggingFramework.CUSTOM ->
                CustomLogStrategy(
                    state?.customKotlinTemplate ?: "Log.d(\"{tag}\", \"{message}\")",
                    state?.customJavaTemplate ?: "Log.d(\"{tag}\", \"{message}\");",
                    state?.customImport,
                )
        }
    }
}
