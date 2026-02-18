package com.github.kiolk.loggingplugin.services

import com.github.kiolk.loggingplugin.settings.LoggingSettings
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAssignmentExpression
import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiExpressionStatement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtSafeQualifiedExpression

@Service(Service.Level.PROJECT)
class LogInserterService(private val project: Project) {
    fun insertKotlinAssignmentLogs(
        searchScope: PsiElement,
        logTag: String,
        framework: LoggingSettings.LoggingFramework = LoggingSettings.LoggingFramework.PRINTLN,
    ) {
        val strategy = LogStrategyFactory.getStrategy(framework)
        val factory = KtPsiFactory(project)
        val assignments =
            PsiTreeUtil.findChildrenOfType(
                searchScope,
                org.jetbrains.kotlin.psi.KtBinaryExpression::class.java,
            )
                .filter {
                    it.operationToken in
                        listOf(
                            org.jetbrains.kotlin.lexer.KtTokens.EQ,
                            org.jetbrains.kotlin.lexer.KtTokens.PLUSEQ,
                            org.jetbrains.kotlin.lexer.KtTokens.MINUSEQ,
                            org.jetbrains.kotlin.lexer.KtTokens.MULTEQ,
                            org.jetbrains.kotlin.lexer.KtTokens.DIVEQ,
                            org.jetbrains.kotlin.lexer.KtTokens.PERCEQ,
                        )
                }

        if (assignments.isNotEmpty()) {
            addKotlinImport(
                searchScope.containingFile as? KtFile,
                strategy.getKotlinImport(),
            )
        }

        assignments.forEach { assignment ->
            val left = assignment.left ?: return@forEach
            val varName = left.text
            val logMessage = "$varName assigned new value: \${$varName}"
            val fullLog = strategy.createKotlinLog(factory, logTag, logMessage)

            if (isLogAlreadyPresent(assignment, logTag, varName)) return@forEach

            val expression = factory.createExpression(fullLog)
            insertAfterStatement(assignment, expression, factory)
        }
    }

    fun insertKotlinMethodLogs(
        searchScope: PsiElement,
        logTag: String,
        framework: LoggingSettings.LoggingFramework = LoggingSettings.LoggingFramework.PRINTLN,
    ) {
        val strategy = LogStrategyFactory.getStrategy(framework)
        val factory = KtPsiFactory(project)
        val functions =
            PsiTreeUtil.findChildrenOfType(
                searchScope,
                KtNamedFunction::class.java,
            )

        if (functions.isNotEmpty()) {
            addKotlinImport(
                searchScope.containingFile as? KtFile,
                strategy.getKotlinImport(),
            )
        }

        functions.forEach { function ->
            val body = function.bodyBlockExpression ?: return@forEach
            val paramsText =
                function.valueParameters.joinToString(", ") { "${it.name}=\${${it.name}}" }
            val logMessage = "${function.name}($paramsText)"
            val fullLog = strategy.createKotlinLog(factory, logTag, logMessage)

            if (body.text.contains(logTag) &&
                body.text.contains(
                    function.name ?: "",
                )
            ) {
                return@forEach
            }

            val lBrace = body.lBrace ?: return@forEach
            val expression = factory.createExpression(fullLog)
            body.addAfter(expression, lBrace)
            body.addAfter(factory.createNewLine(), lBrace)
        }
    }

    fun insertJavaMethodLogs(
        searchScope: PsiElement,
        logTag: String,
        framework: LoggingSettings.LoggingFramework = LoggingSettings.LoggingFramework.PRINTLN,
    ) {
        val strategy = LogStrategyFactory.getStrategy(framework)
        val factory = JavaPsiFacade.getElementFactory(project)
        val methods =
            PsiTreeUtil.findChildrenOfType(searchScope, PsiMethod::class.java)

        if (methods.isNotEmpty()) {
            addJavaImport(
                searchScope.containingFile as? PsiJavaFile,
                strategy.getJavaImport(),
            )
        }

        methods.forEach { method ->
            val body = method.body ?: return@forEach
            val paramsText =
                method.parameterList.parameters.joinToString(", ") { "${it.name}=\" + ${it.name} + \"" }
            val logMessage = "${method.name}($paramsText)"
            val fullLog = strategy.createJavaLog(factory, logTag, logMessage)

            if (body.text.contains(logTag) && body.text.contains(method.name)) return@forEach

            val lBrace = body.lBrace ?: return@forEach
            val statement = factory.createStatementFromText(fullLog, method)
            body.addAfter(statement, lBrace)
        }
    }

    fun insertJavaAssignmentLogs(
        searchScope: PsiElement,
        logTag: String,
        framework: LoggingSettings.LoggingFramework = LoggingSettings.LoggingFramework.PRINTLN,
    ) {
        val strategy = LogStrategyFactory.getStrategy(framework)
        val factory = JavaPsiFacade.getElementFactory(project)
        val assignments =
            PsiTreeUtil.findChildrenOfType(
                searchScope,
                PsiAssignmentExpression::class.java,
            )

        if (assignments.isNotEmpty()) {
            addJavaImport(
                searchScope.containingFile as? PsiJavaFile,
                strategy.getJavaImport(),
            )
        }

        assignments.forEach { assignment ->
            val varName = assignment.lExpression.text
            val logMessage = "$varName assigned new value: \" + $varName"
            val fullLog = strategy.createJavaLog(factory, logTag, logMessage)

            if (isLogAlreadyPresent(assignment, logTag, varName)) return@forEach

            val statement = factory.createStatementFromText(fullLog, assignment)
            insertAfterStatement(assignment, statement)
        }
    }

    private fun addKotlinImport(
        file: KtFile?,
        importPath: String?,
    ) {
        if (file == null || importPath == null) return
        val factory = KtPsiFactory(project)
        val importList = file.importList
        if (importList?.imports?.any { it.importPath?.pathStr == importPath } == true) return

        val newImport =
            factory.createImportDirective(
                org.jetbrains.kotlin.resolve.ImportPath.fromString(importPath),
            )
        if (importList == null) {
            val packageDirective = file.packageDirective
            file.addAfter(newImport, packageDirective)
        } else {
            importList.add(newImport)
        }
    }

    private fun addJavaImport(
        file: PsiJavaFile?,
        importPath: String?,
    ) {
        if (file == null || importPath == null) return
        val factory = JavaPsiFacade.getElementFactory(project)
        val importList = file.importList ?: return
        if (importList.findSingleClassImportStatement(importPath) != null) return

        val psiClass =
            JavaPsiFacade.getInstance(project)
                .findClass(importPath, file.resolveScope) ?: return
        val importStatement = factory.createImportStatement(psiClass)
        importList.add(importStatement)
    }

    fun removeLogs(
        searchScope: PsiElement,
        logTag: String,
        framework: LoggingSettings.LoggingFramework = LoggingSettings.LoggingFramework.PRINTLN,
    ) {
        val strategy = LogStrategyFactory.getStrategy(framework)
        val patterns = strategy.getRemovalPatterns(logTag)

        if (searchScope.containingFile is PsiJavaFile) {
            val file = searchScope.containingFile as PsiJavaFile
            val statements =
                PsiTreeUtil.findChildrenOfType(
                    searchScope,
                    PsiExpressionStatement::class.java,
                )
            statements.filter { stmt -> patterns.any { stmt.text.contains(it) } }
                .forEach { it.delete() }
            removeJavaImportIfUnused(file, strategy.getJavaImport())
        } else if (searchScope.containingFile is KtFile) {
            val file = searchScope.containingFile as KtFile
            val calls =
                PsiTreeUtil.findChildrenOfType(
                    searchScope,
                    KtCallExpression::class.java,
                )
            val toDelete = mutableSetOf<PsiElement>()
            calls.forEach { call ->
                if (patterns.any { call.text.contains(it) }) {
                    var top: PsiElement = call
                    while (true) {
                        val parent = top.parent
                        if (parent is KtBlockExpression || parent is KtNamedFunction) break
                        if (parent is KtDotQualifiedExpression || parent is KtSafeQualifiedExpression) {
                            top = parent
                        } else {
                            break
                        }
                    }
                    if (top.parent is KtBlockExpression || top.parent is KtNamedFunction) {
                        toDelete.add(top)
                    }
                }
            }
            toDelete
                .filter { candidate ->
                    toDelete.none { other ->
                        other !== candidate && PsiTreeUtil.isAncestor(candidate, other, true)
                    }
                }
                .forEach { it.delete() }
            removeKotlinImportIfUnused(file, strategy.getKotlinImport())
        }
    }

    private fun removeKotlinImportIfUnused(
        file: KtFile,
        importPath: String?,
    ) {
        if (importPath == null) return
        val className = importPath.substringAfterLast('.')
        val hasRemainingUsage = file.text
            .lines()
            .filter { !it.trimStart().startsWith("import ") }
            .any { it.contains(className) }
        if (hasRemainingUsage) return
        file.importList?.imports
            ?.find { it.importPath?.pathStr == importPath }
            ?.delete()
    }

    private fun removeJavaImportIfUnused(
        file: PsiJavaFile,
        importPath: String?,
    ) {
        if (importPath == null) return
        val className = importPath.substringAfterLast('.')
        val hasRemainingUsage = file.text
            .lines()
            .filter { !it.trimStart().startsWith("import ") }
            .any { it.contains(className) }
        if (hasRemainingUsage) return
        file.importList
            ?.findSingleClassImportStatement(importPath)
            ?.delete()
    }

    private fun isLogAlreadyPresent(
        element: PsiElement,
        logTag: String,
        varName: String,
    ): Boolean {
        var current = element
        while (current.parent != null && current.parent !is KtBlockExpression && current.parent !is PsiCodeBlock) {
            current = current.parent
        }

        var next = current.nextSibling
        while (next != null && (next is PsiWhiteSpace || next is PsiComment)) {
            next = next.nextSibling
        }
        val text = next?.text ?: ""
        return text.contains(logTag) && text.contains(varName)
    }

    private fun insertAfterStatement(
        statement: PsiElement,
        newElement: PsiElement,
        ktFactory: KtPsiFactory? = null,
    ) {
        var current = statement
        while (current.parent != null && current.parent !is KtBlockExpression && current.parent !is PsiCodeBlock) {
            current = current.parent
        }

        val parent = current.parent
        if (parent is KtBlockExpression && ktFactory != null) {
            parent.addAfter(newElement, current)
            parent.addAfter(ktFactory.createNewLine(), current)
        } else if (parent is PsiCodeBlock) {
            parent.addAfter(newElement, current)
        }
    }

    companion object {
        fun getInstance(project: Project): LogInserterService = project.getService(LogInserterService::class.java)
    }
}
