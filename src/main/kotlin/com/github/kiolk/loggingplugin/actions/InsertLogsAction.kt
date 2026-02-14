package com.github.kiolk.loggingplugin.actions

import com.github.kiolk.loggingplugin.services.LogInserterService
import com.github.kiolk.loggingplugin.settings.LoggingSettings
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*

class InsertLogsAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val elementAtCaret = psiFile.findElementAt(editor.caretModel.offset)
        val settings = LoggingSettings.getInstance(project).state
        val inserterService = LogInserterService.getInstance(project)

        WriteCommandAction.runWriteCommandAction(project) {
            when (psiFile) {
                is PsiJavaFile -> handleJavaFile(psiFile, elementAtCaret, settings, inserterService)
                is KtFile -> handleKotlinFile(psiFile, elementAtCaret, settings, inserterService)
            }
        }
    }

    private fun handleJavaFile(
        psiFile: PsiJavaFile,
        elementAtCaret: PsiElement?,
        settings: LoggingSettings.State,
        inserterService: LogInserterService
    ) {
        val targetClass = PsiTreeUtil.getParentOfType(elementAtCaret, PsiClass::class.java)
        val searchScope = targetClass ?: psiFile
        val logTag = settings.logTag

        if (settings.trackMethodExecution) {
            inserterService.insertJavaMethodLogs(searchScope, logTag)
        }
        if (settings.trackAssignments) {
            inserterService.insertJavaAssignmentLogs(searchScope, logTag)
        }
    }

    private fun handleKotlinFile(
        psiFile: KtFile,
        elementAtCaret: PsiElement?,
        settings: LoggingSettings.State,
        inserterService: LogInserterService
    ) {
        val targetClass = PsiTreeUtil.getParentOfType(elementAtCaret, KtClass::class.java)
        val searchScope = targetClass ?: psiFile
        val logTag = settings.logTag

        if (settings.trackMethodExecution) {
            inserterService.insertKotlinMethodLogs(searchScope, logTag)
        }
        if (settings.trackAssignments) {
            inserterService.insertKotlinAssignmentLogs(searchScope, logTag)
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
