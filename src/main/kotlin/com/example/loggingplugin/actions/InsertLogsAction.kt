package com.example.loggingplugin.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
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

        WriteCommandAction.runWriteCommandAction(project) {
            if (psiFile is PsiJavaFile) {
                val targetClass = PsiTreeUtil.getParentOfType(elementAtCaret, PsiClass::class.java)
                val searchScope = targetClass ?: psiFile
                val factory = JavaPsiFacade.getElementFactory(project)

                // 1. Insert logs at method start
                val methods = PsiTreeUtil.findChildrenOfType(searchScope, PsiMethod::class.java)
                methods.forEach { method ->
                    val body = method.body ?: return@forEach
                    if (body.text.contains("Myfancy log: ${method.name}")) return@forEach
                    val lBrace = body.lBrace ?: return@forEach
                    
                    val paramsText = method.parameterList.parameters.joinToString(", ") { 
                        "${it.name}=\" + ${it.name} + \"" 
                    }
                    val logMessage = "Myfancy log: ${method.name}($paramsText)"
                    val statement = factory.createStatementFromText("System.out.println(\"$logMessage\");", method)
                    body.addAfter(statement, lBrace)
                }

                // 2. Insert logs after assignments
                val assignments = PsiTreeUtil.findChildrenOfType(searchScope, PsiAssignmentExpression::class.java)
                assignments.forEach { assignment ->
                    val varName = assignment.lExpression.text
                    if (assignment.parent.parent.text.contains("Myfancy log: $varName assigned")) return@forEach
                    
                    val logStatement = "System.out.println(\"Myfancy log: $varName assigned new value: \" + $varName);"
                    val statement = factory.createStatementFromText(logStatement, assignment)
                    
                    // Find the actual statement to insert after
                    var current: PsiElement = assignment
                    while (current.parent !is PsiCodeBlock && current.parent != null) {
                        current = current.parent
                    }
                    current.parent?.addAfter(statement, current)
                }

            } else if (psiFile is KtFile) {
                val targetClass = PsiTreeUtil.getParentOfType(elementAtCaret, KtClass::class.java)
                val searchScope = targetClass ?: psiFile
                val factory = KtPsiFactory(project)

                // 1. Insert logs at function start
                val functions = PsiTreeUtil.findChildrenOfType(searchScope, KtNamedFunction::class.java)
                functions.forEach { function ->
                    val body = function.bodyBlockExpression ?: return@forEach
                    if (body.text.contains("Myfancy log: ${function.name}")) return@forEach
                    val lBrace = body.lBrace ?: return@forEach
                    
                    val paramsText = function.valueParameters.joinToString(", ") { 
                        "${it.name}=\${${it.name}}" 
                    }
                    val logMessage = "Myfancy log: ${function.name}($paramsText)"
                    val expression = factory.createExpression("println(\"$logMessage\")")
                    body.addAfter(expression, lBrace)
                    body.addAfter(factory.createNewLine(), lBrace)
                }

                // 2. Insert logs after assignments
                val assignments = PsiTreeUtil.findChildrenOfType(searchScope, KtBinaryExpression::class.java)
                    .filter { it.operationToken in listOf(KtTokens.EQ, KtTokens.PLUSEQ, KtTokens.MINUSEQ, KtTokens.MULTEQ, KtTokens.DIVEQ, KtTokens.PERCEQ) }
                
                assignments.forEach { assignment ->
                    val left = assignment.left ?: return@forEach
                    val varName = left.text
                    
                    val logStatement = "println(\"Myfancy log: $varName assigned new value: \${$varName}\")"
                    val expression = factory.createExpression(logStatement)
                    
                    // Find the statement/expression to insert after in the block
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
