package com.example.loggingplugin.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile

class RemoveLogsAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val elementAtCaret = psiFile.findElementAt(editor.caretModel.offset)

        WriteCommandAction.runWriteCommandAction(project) {
            if (psiFile is PsiJavaFile) {
                val targetClass = PsiTreeUtil.getParentOfType(elementAtCaret, PsiClass::class.java)
                val searchScope = targetClass ?: psiFile
                
                // In Java, System.out.println is usually wrapped in a PsiExpressionStatement
                val statements = PsiTreeUtil.findChildrenOfType(searchScope, PsiExpressionStatement::class.java)
                statements.filter { it.text.contains("Myfancy log") }.forEach { it.delete() }
            } else if (psiFile is KtFile) {
                val targetClass = PsiTreeUtil.getParentOfType(elementAtCaret, KtClass::class.java)
                val searchScope = targetClass ?: psiFile
                
                // In Kotlin, println is a KtCallExpression
                val calls = PsiTreeUtil.findChildrenOfType(searchScope, KtCallExpression::class.java)
                calls.filter { it.text.contains("Myfancy log") }.forEach { it.delete() }
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
