package com.github.kiolk.loggingplugin.actions

import com.github.kiolk.loggingplugin.settings.LoggingSettings
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*

class InsertLogsAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val elementAtCaret = psiFile.findElementAt(editor.caretModel.offset)
        val settings = LoggingSettings.getInstance(project).state

        WriteCommandAction.runWriteCommandAction(project) {
            when (psiFile) {
                is PsiJavaFile -> handleJavaFile(psiFile, elementAtCaret, settings)
                is KtFile -> handleKotlinFile(psiFile, elementAtCaret, settings)
            }
        }
    }

    private fun handleJavaFile(psiFile: PsiJavaFile, elementAtCaret: PsiElement?, settings: LoggingSettings.State) {
        val targetClass = PsiTreeUtil.getParentOfType(elementAtCaret, PsiClass::class.java)
        val searchScope = targetClass ?: psiFile
        val factory = JavaPsiFacade.getElementFactory(psiFile.project)
        val logTag = settings.logTag

        if (settings.trackMethodExecution) {
            insertJavaMethodLogs(searchScope, factory, logTag)
        }
        if (settings.trackAssignments) {
            insertJavaAssignmentLogs(searchScope, factory, logTag)
        }
    }

    private fun handleKotlinFile(psiFile: KtFile, elementAtCaret: PsiElement?, settings: LoggingSettings.State) {
        val targetClass = PsiTreeUtil.getParentOfType(elementAtCaret, KtClass::class.java)
        val searchScope = targetClass ?: psiFile
        val factory = KtPsiFactory(psiFile.project)
        val logTag = settings.logTag

        if (settings.trackMethodExecution) {
            insertKotlinMethodLogs(searchScope, factory, logTag)
        }
        if (settings.trackAssignments) {
            insertKotlinAssignmentLogs(searchScope, factory, logTag)
        }
    }

    private fun insertJavaMethodLogs(searchScope: PsiElement, factory: PsiElementFactory, logTag: String) {
        val methods = PsiTreeUtil.findChildrenOfType(searchScope, PsiMethod::class.java)
        methods.forEach { method ->
            val body = method.body ?: return@forEach
            if (body.text.contains("$logTag: ${method.name}")) return@forEach
            val lBrace = body.lBrace ?: return@forEach

            val paramsText = method.parameterList.parameters.joinToString(", ") {
                "${it.name}=\" + ${it.name} + \""
            }
            val logMessage = "$logTag: ${method.name}($paramsText)"
            val statement = factory.createStatementFromText("System.out.println(\"$logMessage\");", method)
            body.addAfter(statement, lBrace)
        }
    }

    private fun insertJavaAssignmentLogs(searchScope: PsiElement, factory: PsiElementFactory, logTag: String) {
        val assignments = PsiTreeUtil.findChildrenOfType(searchScope, PsiAssignmentExpression::class.java)
        assignments.forEach { assignment ->
            val varName = assignment.lExpression.text
            if (assignment.parent.parent.text.contains("$logTag: $varName assigned")) return@forEach

            val logStatement = "System.out.println(\"$logTag: $varName assigned new value: \" + $varName);"
            val statement = factory.createStatementFromText(logStatement, assignment)

            var current: PsiElement = assignment
            while (current.parent !is PsiCodeBlock && current.parent != null) {
                current = current.parent
            }
            current.parent?.addAfter(statement, current)
        }
    }

    private fun insertKotlinMethodLogs(searchScope: PsiElement, factory: KtPsiFactory, logTag: String) {
        val functions = PsiTreeUtil.findChildrenOfType(searchScope, KtNamedFunction::class.java)
        functions.forEach { function ->
            val body = function.bodyBlockExpression ?: return@forEach
            if (body.text.contains("$logTag: ${function.name}")) return@forEach
            val lBrace = body.lBrace ?: return@forEach

            val paramsText = function.valueParameters.joinToString(", ") {
                "${it.name}=\${${it.name}}"
            }
            val logMessage = "$logTag: ${function.name}($paramsText)"
            val expression = factory.createExpression("println(\"$logMessage\")")
            body.addAfter(expression, lBrace)
            body.addAfter(factory.createNewLine(), lBrace)
        }
    }

    private fun insertKotlinAssignmentLogs(searchScope: PsiElement, factory: KtPsiFactory, logTag: String) {
        val assignments = PsiTreeUtil.findChildrenOfType(searchScope, KtBinaryExpression::class.java)
            .filter { it.operationToken in listOf(KtTokens.EQ, KtTokens.PLUSEQ, KtTokens.MINUSEQ, KtTokens.MULTEQ, KtTokens.DIVEQ, KtTokens.PERCEQ) }

        assignments.forEach { assignment ->
            val left = assignment.left ?: return@forEach
            val varName = left.text

            val logStatement = "println(\"$logTag: $varName assigned new value: \${$varName}\")"
            val expression = factory.createExpression(logStatement)

            var current: PsiElement = assignment
            while (current.parent !is KtBlockExpression && current.parent != null) {
                current = current.parent
            }

            if (current.parent is KtBlockExpression) {
                val block = current.parent as KtBlockExpression
                block.addAfter(expression, current)
                block.addAfter(factory.createNewLine(), current)
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)

        val isSupportedFile = psiFile is PsiJavaFile || psiFile is KtFile
        e.presentation.isEnabledAndVisible = project != null && editor != null && isSupportedFile
    }
}
