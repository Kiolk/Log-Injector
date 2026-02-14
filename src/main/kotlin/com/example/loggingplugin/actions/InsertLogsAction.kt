package com.example.loggingplugin.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory

class InsertLogsAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val elementAtCaret = psiFile.findElementAt(editor.caretModel.offset)

        WriteCommandAction.runWriteCommandAction(project) {
            if (psiFile is PsiJavaFile) {
                val targetClass = PsiTreeUtil.getParentOfType(elementAtCaret, PsiClass::class.java)
                val methods = if (targetClass != null) {
                    targetClass.methods.toList()
                } else {
                    PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod::class.java).toList()
                }

                val factory = JavaPsiFacade.getElementFactory(project)
                methods.forEach { method ->
                    val body = method.body ?: return@forEach
                    if (body.text.contains("Myfancy log")) return@forEach
                    val lBrace = body.lBrace ?: return@forEach
                    
                    val paramsText = method.parameterList.parameters.joinToString(", ") { 
                        "${it.name}=\" + ${it.name} + \"" 
                    }
                    val logMessage = "Myfancy log: ${method.name}($paramsText)"
                    val statement = factory.createStatementFromText("System.out.println(\"$logMessage\");", method)
                    body.addAfter(statement, lBrace)
                }
            } else if (psiFile is KtFile) {
                val targetClass = PsiTreeUtil.getParentOfType(elementAtCaret, KtClass::class.java)
                val functions = if (targetClass != null) {
                    targetClass.declarations.filterIsInstance<KtNamedFunction>()
                } else {
                    PsiTreeUtil.findChildrenOfType(psiFile, KtNamedFunction::class.java)
                }

                val factory = KtPsiFactory(project)
                functions.forEach { function ->
                    val body = function.bodyBlockExpression ?: return@forEach
                    if (body.text.contains("Myfancy log")) return@forEach
                    val lBrace = body.lBrace ?: return@forEach
                    
                    val paramsText = function.valueParameters.joinToString(", ") { 
                        "${it.name}=\${${it.name}}" 
                    }
                    val logMessage = "Myfancy log: ${function.name}($paramsText)"
                    val expression = factory.createExpression("println(\"$logMessage\")")
                    body.addAfter(expression, lBrace)
                    body.addAfter(factory.createNewLine(), lBrace)
                }
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
