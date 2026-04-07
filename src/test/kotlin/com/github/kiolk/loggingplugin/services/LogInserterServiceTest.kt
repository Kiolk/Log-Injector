package com.github.kiolk.loggingplugin.services

import com.github.kiolk.loggingplugin.settings.LoggingSettings
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
        val before =
            """
            fun test() {
                var x = 1
                x = 2
            }
            """.trimIndent()

        val after =
            """
            fun test() {
                var x = 1
                x = 2
                println("TestTag: x assigned new value: ${'$'}{x}")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinAssignmentLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.PRINTLN)
        }

        myFixture.checkResult(after)
    }

    fun testInsertKotlinAssignmentLogsIdempotency() {
        val content =
            """
            fun test() {
                var x = 1
                x = 2
                println("TestTag: x assigned new value: ${'$'}{x}")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", content) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinAssignmentLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.PRINTLN)
        }

        myFixture.checkResult(content)
    }

    fun testInsertKotlinMethodLogs() {
        val before =
            """
            fun test(param: String) {
                val y = 0
            }
            """.trimIndent()

        val after =
            """
            fun test(param: String) {
                println("TestTag: test(param=${'$'}{param})")
                val y = 0
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinMethodLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.PRINTLN)
        }

        myFixture.checkResult(after)
    }

    fun testRemoveKotlinLogs() {
        val before =
            """
            fun test() {
                println("TestTag: some log")
                var x = 1
                println("OtherTag: other log")
            }
            """.trimIndent()

        val after =
            """
            fun test() {
                var x = 1
                println("OtherTag: other log")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.PRINTLN)
        }

        myFixture.checkResult(after)
    }

    fun testRemoveJavaLogs() {
        val before =
            """
            public class Test {
                public void test() {
                    System.out.println("TestTag: log");
                    int x = 1;
                }
            }
            """.trimIndent()

        val after =
            """
            public class Test {
                public void test() {
                    int x = 1;
                }
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.java", before)

        WriteCommandAction.runWriteCommandAction(project) {
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.PRINTLN)
        }

        myFixture.checkResult(after)
    }

    fun testInsertKotlinAssignmentAndroidLogLogs() {
        val before =
            """
            fun test() {
                var x = 1
                x = 2
            }
            """.trimIndent()

        val after =
            """
            import android.util.Log

            fun test() {
                var x = 1
                x = 2
                Log.d("TestTag", "x assigned new value: ${'$'}{x}")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinAssignmentLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.ANDROID_LOG)
        }

        myFixture.checkResult(after)
    }

    fun testInsertKotlinMethodAndroidLogLogs() {
        val before =
            """
            fun test(param: String) {
                val y = 0
            }
            """.trimIndent()

        val after =
            """
            import android.util.Log

            fun test(param: String) {
                Log.d("TestTag", "test(param=${'$'}{param})")
                val y = 0
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinMethodLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.ANDROID_LOG)
        }

        myFixture.checkResult(after)
    }

    fun testRemoveKotlinAndroidLogLogs() {
        val before =
            """
            fun test() {
                Log.d("TestTag", "some log")
                var x = 1
                Log.d("OtherTag", "other log")
            }
            """.trimIndent()

        val after =
            """
            fun test() {
                var x = 1
                Log.d("OtherTag", "other log")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.ANDROID_LOG)
        }

        myFixture.checkResult(after)
    }

    fun testRemoveAndroidLogInsideScopeFunctionKeepsBlock() {
        val before =
            """
            fun test() {
                args.productUUID?.apply {
                    productUUID = this
                    Log.d("TestTag", "productUUID assigned new value: ${'$'}{productUUID}")
                }
            }
            """.trimIndent()

        val after =
            """
            fun test() {
                args.productUUID?.apply {
                    productUUID = this
                }
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.ANDROID_LOG)
        }

        myFixture.checkResult(after)
    }

    fun testRemoveKotlinAndroidLogLogsAlsoRemovesImport() {
        val before =
            """
            import android.util.Log

            fun test() {
                Log.d("TestTag", "some log")
                var x = 1
            }
            """.trimIndent()

        val after =
            """
            fun test() {
                var x = 1
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.ANDROID_LOG)
        }

        myFixture.checkResult(after)
    }

    fun testRemoveKotlinAndroidLogLogsRemovesImportWhenClassNameAppearsAsSubstring() {
        val before =
            """
            import android.util.Log

            fun test() {
                Log.d("TestTag", "some log")
                val logging = "Logger"
            }
            """.trimIndent()

        val after =
            """
            fun test() {
                val logging = "Logger"
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.ANDROID_LOG)
        }

        myFixture.checkResult(after)
    }

    fun testRemoveKotlinAndroidLogLogsKeepsImportWhenOtherLogsRemain() {
        val before =
            """
            import android.util.Log

            fun test() {
                Log.d("TestTag", "some log")
                Log.d("OtherTag", "other log")
            }
            """.trimIndent()

        val after =
            """
            import android.util.Log

            fun test() {
                Log.d("OtherTag", "other log")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.ANDROID_LOG)
        }

        myFixture.checkResult(after)
    }

    fun testInsertAndroidLogLogsWithImport() {
        val before =
            """
            package com.example

            fun test() {
                var x = 1
                x = 2
            }
            """.trimIndent()

        val after =
            """
            package com.example

            import android.util.Log

            fun test() {
                var x = 1
                x = 2
                Log.d("TestTag", "x assigned new value: ${'$'}{x}")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinAssignmentLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.ANDROID_LOG)
        }

        myFixture.checkResult(after)
    }

    fun testInsertAndroidLogLogsWithExistingImport() {
        val content =
            """
            package com.example

            import android.util.Log

            fun test() {
                var x = 1
                x = 2
                Log.d("TestTag", "x assigned new value: ${'$'}{x}")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", content) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinAssignmentLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.ANDROID_LOG)
        }

        myFixture.checkResult(content)
    }

    fun testInsertKotlinAssignmentTimberLogs() {
        val before =
            """
            fun test() {
                var x = 1
                x = 2
            }
            """.trimIndent()

        val after =
            """
            import timber.log.Timber

            fun test() {
                var x = 1
                x = 2
                Timber.tag("TestTag").d("x assigned new value: ${'$'}{x}")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinAssignmentLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.TIMBER)
        }

        myFixture.checkResult(after)
    }

    fun testInsertKotlinMethodTimberLogs() {
        val before =
            """
            fun test(param: String) {
                val y = 0
            }
            """.trimIndent()

        val after =
            """
            import timber.log.Timber

            fun test(param: String) {
                Timber.tag("TestTag").d("test(param=${'$'}{param})")
                val y = 0
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinMethodLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.TIMBER)
        }

        myFixture.checkResult(after)
    }

    fun testRemoveKotlinTimberLogs() {
        val before =
            """
            fun test() {
                Timber.tag("TestTag").d("some log")
                var x = 1
                Timber.tag("OtherTag").d("other log")
            }
            """.trimIndent()

        val after =
            """
            fun test() {
                var x = 1
                Timber.tag("OtherTag").d("other log")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.TIMBER)
        }

        myFixture.checkResult(after)
    }

    fun testRemoveTimberLogInsideScopeFunctionKeepsBlock() {
        val before =
            """
            fun test() {
                args.productUUID?.apply {
                    productUUID = this
                    Timber.tag("TestTag").d("productUUID assigned new value: ${'$'}{productUUID}")
                }
            }
            """.trimIndent()

        val after =
            """
            fun test() {
                args.productUUID?.apply {
                    productUUID = this
                }
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.TIMBER)
        }

        myFixture.checkResult(after)
    }

    fun testRemoveJavaTimberLogs() {
        val before =
            """
            public class Test {
                public void test() {
                    Timber.tag("TestTag").d("log");
                    int x = 1;
                }
            }
            """.trimIndent()

        val after =
            """
            public class Test {
                public void test() {
                    int x = 1;
                }
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.java", before)

        WriteCommandAction.runWriteCommandAction(project) {
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.TIMBER)
        }

        myFixture.checkResult(after)
    }

    fun testRemoveKotlinTimberLogsAlsoRemovesImport() {
        val before =
            """
            import timber.log.Timber

            fun test() {
                Timber.tag("TestTag").d("some log")
                var x = 1
            }
            """.trimIndent()

        val after =
            """
            fun test() {
                var x = 1
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.TIMBER)
        }

        myFixture.checkResult(after)
    }

    fun testRemoveKotlinTimberLogsKeepsImportWhenOtherTimberLogsRemain() {
        val before =
            """
            import timber.log.Timber

            fun test() {
                Timber.tag("TestTag").d("some log")
                Timber.tag("OtherTag").d("other log")
            }
            """.trimIndent()

        val after =
            """
            import timber.log.Timber

            fun test() {
                Timber.tag("OtherTag").d("other log")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.TIMBER)
        }

        myFixture.checkResult(after)
    }

    fun testRemoveJavaTimberLogsAlsoRemovesImport() {
        val before =
            """
            import timber.log.Timber;

            public class Test {
                public void test() {
                    Timber.tag("TestTag").d("log");
                    int x = 1;
                }
            }
            """.trimIndent()

        val after =
            """
            public class Test {
                public void test() {
                    int x = 1;
                }
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.java", before)

        WriteCommandAction.runWriteCommandAction(project) {
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.TIMBER)
        }

        myFixture.checkResult(after)
    }

    fun testInsertKotlinAssignmentNapierLogs() {
        val before =
            """
            fun test() {
                var x = 1
                x = 2
            }
            """.trimIndent()

        val after =
            """
            import io.github.aakira.napier.Napier

            fun test() {
                var x = 1
                x = 2
                Napier.d("x assigned new value: ${'$'}{x}", tag = "TestTag")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinAssignmentLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.NAPIER)
        }

        myFixture.checkResult(after)
    }

    fun testInsertKotlinMethodNapierLogs() {
        val before =
            """
            fun test(param: String) {
                val y = 0
            }
            """.trimIndent()

        val after =
            """
            import io.github.aakira.napier.Napier

            fun test(param: String) {
                Napier.d("test(param=${'$'}{param})", tag = "TestTag")
                val y = 0
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinMethodLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.NAPIER)
        }

        myFixture.checkResult(after)
    }

    fun testInsertNapierLogsWithImport() {
        val before =
            """
            package com.example

            fun test() {
                var x = 1
                x = 2
            }
            """.trimIndent()

        val after =
            """
            package com.example

            import io.github.aakira.napier.Napier

            fun test() {
                var x = 1
                x = 2
                Napier.d("x assigned new value: ${'$'}{x}", tag = "TestTag")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinAssignmentLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.NAPIER)
        }

        myFixture.checkResult(after)
    }

    fun testInsertNapierLogsWithExistingImport() {
        val content =
            """
            package com.example

            import io.github.aakira.napier.Napier

            fun test() {
                var x = 1
                x = 2
                Napier.d("x assigned new value: ${'$'}{x}", tag = "TestTag")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", content) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinAssignmentLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.NAPIER)
        }

        myFixture.checkResult(content)
    }

    fun testRemoveKotlinNapierLogs() {
        val before =
            """
            fun test() {
                Napier.d("some log", tag = "TestTag")
                var x = 1
                Napier.d("other log", tag = "OtherTag")
            }
            """.trimIndent()

        val after =
            """
            fun test() {
                var x = 1
                Napier.d("other log", tag = "OtherTag")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.NAPIER)
        }

        myFixture.checkResult(after)
    }

    fun testRemoveNapierLogInsideScopeFunctionKeepsBlock() {
        val before =
            """
            fun test() {
                args.productUUID?.apply {
                    productUUID = this
                    Napier.d("productUUID assigned new value: ${'$'}{productUUID}", tag = "TestTag")
                }
            }
            """.trimIndent()

        val after =
            """
            fun test() {
                args.productUUID?.apply {
                    productUUID = this
                }
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.NAPIER)
        }

        myFixture.checkResult(after)
    }

    fun testRemoveKotlinNapierLogsAlsoRemovesImport() {
        val before =
            """
            import io.github.aakira.napier.Napier

            fun test() {
                Napier.d("some log", tag = "TestTag")
                var x = 1
            }
            """.trimIndent()

        val after =
            """
            fun test() {
                var x = 1
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.NAPIER)
        }

        myFixture.checkResult(after)
    }

    fun testRemoveKotlinNapierLogsKeepsImportWhenOtherNapierLogsRemain() {
        val before =
            """
            import io.github.aakira.napier.Napier

            fun test() {
                Napier.d("some log", tag = "TestTag")
                Napier.d("other log", tag = "OtherTag")
            }
            """.trimIndent()

        val after =
            """
            import io.github.aakira.napier.Napier

            fun test() {
                Napier.d("other log", tag = "OtherTag")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.NAPIER)
        }

        myFixture.checkResult(after)
    }

    fun testInsertTimberLogsWithImport() {
        val before =
            """
            package com.example

            fun test() {
                var x = 1
                x = 2
            }
            """.trimIndent()

        val after =
            """
            package com.example

            import timber.log.Timber

            fun test() {
                var x = 1
                x = 2
                Timber.tag("TestTag").d("x assigned new value: ${'$'}{x}")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinAssignmentLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.TIMBER)
        }

        myFixture.checkResult(after)
    }

    fun testInsertTimberLogsWithExistingImport() {
        val content =
            """
            package com.example

            import timber.log.Timber

            fun test() {
                var x = 1
                x = 2
                Timber.tag("TestTag").d("x assigned new value: ${'$'}{x}")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", content) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinAssignmentLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.TIMBER)
        }

        myFixture.checkResult(content)
    }
}
