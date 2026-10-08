package com.github.kiolk.loggingplugin.actions

import com.github.kiolk.loggingplugin.settings.LoggingSettings
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Invokes the actions through the action system (update + actionPerformed),
 * the same way the IDE does when the user picks them from the editor popup menu.
 */
class LogsActionTest : BasePlatformTestCase() {
    private lateinit var originalState: LoggingSettings.State

    override fun setUp() {
        super.setUp()
        val settings = LoggingSettings.getInstance(project)
        originalState = settings.state.copy()
        settings.loadState(
            LoggingSettings.State(
                logTag = "TestTag",
                loggingFramework = LoggingSettings.LoggingFramework.PRINTLN,
            ),
        )
    }

    override fun tearDown() {
        try {
            LoggingSettings.getInstance(project).loadState(originalState)
        } finally {
            super.tearDown()
        }
    }

    fun testActionsAreRegistered() {
        assertInstanceOf(action(INSERT_ACTION_ID), InsertLogsAction::class.java)
        assertInstanceOf(action(REMOVE_ACTION_ID), RemoveLogsAction::class.java)
    }

    fun testInsertLogsActionKotlin() {
        myFixture.configureByText(
            "Test.kt",
            """
            class Test {
                fun test(param: String) {
                    <caret>var x = 1
                    x = 2
                }
            }
            """.trimIndent(),
        )

        val presentation = myFixture.testAction(action(INSERT_ACTION_ID))

        assertTrue(presentation.isEnabledAndVisible)
        myFixture.checkResult(
            """
            class Test {
                fun test(param: String) {
                    println("TestTag: test(param=${'$'}{param})")
                    var x = 1
                    x = 2
                    println("TestTag: x assigned new value: ${'$'}{x}")
                }
            }
            """.trimIndent(),
        )
    }

    fun testInsertLogsActionKotlinOnlyAffectsClassAtCaret() {
        myFixture.configureByText(
            "Test.kt",
            """
            class First {
                fun first() {
                    <caret>val a = 1
                }
            }

            class Second {
                fun second() {
                    val b = 2
                }
            }
            """.trimIndent(),
        )

        myFixture.testAction(action(INSERT_ACTION_ID))

        myFixture.checkResult(
            """
            class First {
                fun first() {
                    println("TestTag: first()")
                    val a = 1
                }
            }

            class Second {
                fun second() {
                    val b = 2
                }
            }
            """.trimIndent(),
        )
    }

    fun testInsertLogsActionJava() {
        myFixture.configureByText(
            "Test.java",
            """
            public class Test {
                public void test(String param) {
                    <caret>int x = 1;
                    x = 2;
                }
            }
            """.trimIndent(),
        )

        myFixture.testAction(action(INSERT_ACTION_ID))

        myFixture.checkResult(
            """
            public class Test {
                public void test(String param) {
                    System.out.println("TestTag: test(param=" + param + ")");
                    int x = 1;
                    x = 2;
                    System.out.println("TestTag: x assigned new value: " + x);
                }
            }
            """.trimIndent(),
        )
    }

    fun testInsertThenRemoveActionsRestoreOriginalCode() {
        val original =
            """
            class Test {
                fun test(param: String) {
                    var x = 1
                    x = 2
                }
            }
            """.trimIndent()
        myFixture.configureByText("Test.kt", original.replace("var x", "<caret>var x"))

        myFixture.testAction(action(INSERT_ACTION_ID))
        myFixture.testAction(action(REMOVE_ACTION_ID))

        myFixture.checkResult(original)
    }

    fun testActionsAreHiddenForUnsupportedFiles() {
        myFixture.configureByText("notes.txt", "x = 2<caret>")

        assertFalse(myFixture.testAction(action(INSERT_ACTION_ID)).isEnabledAndVisible)
        assertFalse(myFixture.testAction(action(REMOVE_ACTION_ID)).isEnabledAndVisible)
        myFixture.checkResult("x = 2")
    }

    private fun action(id: String): AnAction =
        requireNotNull(ActionManager.getInstance().getAction(id)) {
            "Action '$id' is not registered"
        }

    private companion object {
        const val INSERT_ACTION_ID = "InsertLogsAction"
        const val REMOVE_ACTION_ID = "RemoveLogsAction"
    }
}
