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
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile

class RemoveLogsAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val elementAtCaret = psiFile.findElementAt(editor.caretModel.offset)
        val settings = LoggingSettings.getInstance(project).state
        val logTag = settings.logTag
        val framework = settings.loggingFramework
        val inserterService = LogInserterService.getInstance(project)

        WriteCommandAction.runWriteCommandAction(project) {
            val searchScope = if (psiFile is PsiJavaFile) {
                PsiTreeUtil.getParentOfType(elementAtCaret, PsiClass::class.java) ?: psiFile
            } else if (psiFile is KtFile) {
                PsiTreeUtil.getParentOfType(elementAtCaret, KtClass::class.java) ?: psiFile
            } else {
                return@runWriteCommandAction
            }
            
            inserterService.removeLogs(searchScope, logTag, framework)
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
