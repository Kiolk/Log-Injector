package com.github.kiolk.loggingplugin.services

import com.github.kiolk.loggingplugin.settings.LoggingSettings
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.kotlin.psi.KtFile

class CustomLogInserterServiceTest : BasePlatformTestCase() {
    private lateinit var service: LogInserterService

    override fun setUp() {
        super.setUp()
        service = LogInserterService.getInstance(project)
        val state = LoggingSettings.getInstance(project).state
        state.customKotlinTemplate = "MyLogger.log(\"{tag}\", \"{message}\")"
        state.customJavaTemplate = "MyLogger.log(\"{tag}\", \"{message}\");"
        state.customImport = "com.example.MyLogger"
    }

    // region Kotlin — Assignment

    fun testInsertKotlinAssignmentCustomLogs() {
        val before =
            """
            fun test() {
                var x = 1
                x = 2
            }
            """.trimIndent()

        val after =
            """
            import com.example.MyLogger

            fun test() {
                var x = 1
                x = 2
                MyLogger.log("TestTag", "x assigned new value: ${'$'}{x}")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinAssignmentLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.CUSTOM)
        }

        myFixture.checkResult(after)
    }

    fun testInsertKotlinAssignmentCustomLogsWithPackage() {
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

            import com.example.MyLogger

            fun test() {
                var x = 1
                x = 2
                MyLogger.log("TestTag", "x assigned new value: ${'$'}{x}")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinAssignmentLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.CUSTOM)
        }

        myFixture.checkResult(after)
    }

    fun testInsertKotlinAssignmentCustomLogsIdempotency() {
        val content =
            """
            import com.example.MyLogger

            fun test() {
                var x = 1
                x = 2
                MyLogger.log("TestTag", "x assigned new value: ${'$'}{x}")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", content) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinAssignmentLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.CUSTOM)
        }

        myFixture.checkResult(content)
    }

    fun testInsertKotlinAssignmentCustomLogsWithExistingImport() {
        val content =
            """
            import com.example.MyLogger

            fun test() {
                var x = 1
                x = 2
                MyLogger.log("TestTag", "x assigned new value: ${'$'}{x}")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", content) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinAssignmentLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.CUSTOM)
        }

        myFixture.checkResult(content)
    }

    // endregion

    // region Kotlin — Method

    fun testInsertKotlinMethodCustomLogs() {
        val before =
            """
            fun test(param: String) {
                val y = 0
            }
            """.trimIndent()

        val after =
            """
            import com.example.MyLogger

            fun test(param: String) {
                MyLogger.log("TestTag", "test(param=${'$'}{param})")
                val y = 0
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinMethodLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.CUSTOM)
        }

        myFixture.checkResult(after)
    }

    // endregion

    // region Kotlin — Custom template without import

    fun testInsertKotlinCustomLogsWithoutImport() {
        LoggingSettings.getInstance(project).state.customImport = ""

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
                MyLogger.log("TestTag", "x assigned new value: ${'$'}{x}")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinAssignmentLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.CUSTOM)
        }

        myFixture.checkResult(after)
    }

    // endregion

    // region Kotlin — Custom template with only {message} (e.g. Crashlytics style)

    fun testInsertKotlinCrashlyticsStyleCustomLogs() {
        val state = LoggingSettings.getInstance(project).state
        state.customKotlinTemplate = "Crashlytics.log(\"{message}\")"
        state.customImport = ""

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
                Crashlytics.log("x assigned new value: ${'$'}{x}")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertKotlinAssignmentLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.CUSTOM)
        }

        myFixture.checkResult(after)
    }

    // endregion

    // region Kotlin — Removal

    fun testRemoveKotlinCustomLogs() {
        val before =
            """
            fun test() {
                MyLogger.log("TestTag", "some log")
                var x = 1
                MyLogger.log("OtherTag", "other log")
            }
            """.trimIndent()

        val after =
            """
            fun test() {
                var x = 1
                MyLogger.log("OtherTag", "other log")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.CUSTOM)
        }

        myFixture.checkResult(after)
    }

    fun testRemoveKotlinCustomLogsAlsoRemovesImport() {
        val before =
            """
            import com.example.MyLogger

            fun test() {
                MyLogger.log("TestTag", "some log")
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
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.CUSTOM)
        }

        myFixture.checkResult(after)
    }

    fun testRemoveKotlinCustomLogsKeepsImportWhenOtherLogsRemain() {
        val before =
            """
            import com.example.MyLogger

            fun test() {
                MyLogger.log("TestTag", "some log")
                MyLogger.log("OtherTag", "other log")
            }
            """.trimIndent()

        val after =
            """
            import com.example.MyLogger

            fun test() {
                MyLogger.log("OtherTag", "other log")
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.kt", before) as KtFile

        WriteCommandAction.runWriteCommandAction(project) {
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.CUSTOM)
        }

        myFixture.checkResult(after)
    }

    fun testRemoveCustomLogInsideScopeFunctionKeepsBlock() {
        val before =
            """
            fun test() {
                args.productUUID?.apply {
                    productUUID = this
                    MyLogger.log("TestTag", "productUUID assigned new value: ${'$'}{productUUID}")
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
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.CUSTOM)
        }

        myFixture.checkResult(after)
    }

    // endregion

    // region Java — Assignment

    fun testInsertJavaAssignmentCustomLogs() {
        val before =
            """
            public class Test {
                public void test() {
                    int x = 1;
                    x = 2;
                }
            }
            """.trimIndent()

        val after =
            """
            import com.example.MyLogger;

            public class Test {
                public void test() {
                    int x = 1;
                    x = 2;
                    MyLogger.log("TestTag", "x assigned new value: " + x);
                }
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.java", before)

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertJavaAssignmentLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.CUSTOM)
        }

        myFixture.checkResult(after)
    }

    // endregion

    // region Java — Method

    fun testInsertJavaMethodCustomLogs() {
        val before =
            """
            public class Test {
                public void test(String param) {
                    int y = 0;
                }
            }
            """.trimIndent()

        val after =
            """
            import com.example.MyLogger;

            public class Test {
                public void test(String param) {
                    MyLogger.log("TestTag", "test(param=" + param + ")");
                    int y = 0;
                }
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.java", before)

        WriteCommandAction.runWriteCommandAction(project) {
            service.insertJavaMethodLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.CUSTOM)
        }

        myFixture.checkResult(after)
    }

    // endregion

    // region Java — Removal

    fun testRemoveJavaCustomLogs() {
        val before =
            """
            public class Test {
                public void test() {
                    MyLogger.log("TestTag", "log");
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
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.CUSTOM)
        }

        myFixture.checkResult(after)
    }

    fun testRemoveJavaCustomLogsKeepsOtherTags() {
        val before =
            """
            public class Test {
                public void test() {
                    MyLogger.log("TestTag", "log");
                    MyLogger.log("OtherTag", "other log");
                    int x = 1;
                }
            }
            """.trimIndent()

        val after =
            """
            public class Test {
                public void test() {
                    MyLogger.log("OtherTag", "other log");
                    int x = 1;
                }
            }
            """.trimIndent()

        val psiFile = myFixture.configureByText("Test.java", before)

        WriteCommandAction.runWriteCommandAction(project) {
            service.removeLogs(psiFile, "TestTag", LoggingSettings.LoggingFramework.CUSTOM)
        }

        myFixture.checkResult(after)
    }

    // endregion
}
