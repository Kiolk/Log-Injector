package com.github.kiolk.loggingplugin.services

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.kotlin.psi.KtFile

class LogInserterServiceTest : BasePlatformTestCase() {

    private lateinit var service: LogInserterService

    override fun setUp() {
        super.setUp()
        service = LogInserterService.getInstance(project)
    }

    fun testInsertKotlinAssignmentLogs() {
        val before = """
            fun test() {
                var x = 1
                x = 2
            }
        """.trimIndent()

        val after = """
            fun test() {
                var x = 1
                x = 2
                println("TestTag: x assigned new value: ${'$'}{x}")
            }
        """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile
        
        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinAssignmentLogs(psiFile, "TestTag")
        }

        myFixture.checkResult(after)
    }

    fun testInsertKotlinAssignmentLogsIdempotency() {
        val content = """
            fun test() {
                var x = 1
                x = 2
                println("TestTag: x assigned new value: ${'$'}{x}")
            }
        """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", content) as KtFile
        
        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinAssignmentLogs(psiFile, "TestTag")
        }

        myFixture.checkResult(content)
    }

    fun testInsertKotlinMethodLogs() {
        val before = """
            fun test(param: String) {
                val y = 0
            }
        """.trimIndent()

        val after = """
            fun test(param: String) {
                println("TestTag: test(param=${'$'}{param})")
                val y = 0
            }
        """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile
        
        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinMethodLogs(psiFile, "TestTag")
        }

        myFixture.checkResult(after)
    }
}
