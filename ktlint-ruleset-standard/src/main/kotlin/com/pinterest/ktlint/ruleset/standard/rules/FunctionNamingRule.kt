package com.pinterest.ktlint.ruleset.standard.rules

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.ANNOTATION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CONSTRUCTOR_CALLEE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IDENTIFIER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IMPORT_DIRECTIVE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.OVERRIDE_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.USER_TYPE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.SinceKtlint
import com.pinterest.ktlint.rule.engine.core.api.SinceKtlint.Status.EXPERIMENTAL
import com.pinterest.ktlint.rule.engine.core.api.SinceKtlint.Status.STABLE
import com.pinterest.ktlint.rule.engine.core.api.children20
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.CommaSeparatedListValueParser
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfig
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfigProperty
import com.pinterest.ktlint.rule.engine.core.api.nextCodeSibling20
import com.pinterest.ktlint.ruleset.standard.StandardRule
import com.pinterest.ktlint.ruleset.standard.rules.internal.regExIgnoringDiacriticsAndStrokesOnLetters
import org.ec4j.core.model.PropertyType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtImportDirective

/**
 * https://kotlinlang.org/docs/coding-conventions.html#function-names
 */
@SinceKtlint("0.48", EXPERIMENTAL)
@SinceKtlint("1.0", STABLE)
public class FunctionNamingRule :
    StandardRule(
        id = "function-naming",
        usesEditorConfigProperties = setOf(IGNORE_WHEN_ANNOTATED_WITH_PROPERTY),
    ) {
    private var isTestClass = false
    private var ignoreWhenAnnotatedWith = IGNORE_WHEN_ANNOTATED_WITH_PROPERTY.defaultValue

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        ignoreWhenAnnotatedWith = editorConfig[IGNORE_WHEN_ANNOTATED_WITH_PROPERTY]
    }

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (!isTestClass && node.elementType == IMPORT_DIRECTIVE) {
            (node.psi as KtImportDirective)
                .importPath
                ?.pathStr
                ?.takeIf { importPathString -> TEST_LIBRARIES_SET.any { importPathString.startsWith(it) } }
                ?.let { isTestClass = true }
        }

        node
            .takeIf { node.elementType == FUN }
            ?.takeUnless {
                node.isFactoryMethod() ||
                    node.isMethodInTestClass() ||
                    node.hasValidFunctionName() ||
                    node.isAnonymousFunction() ||
                    node.isOverrideFunction() ||
                    node.isAnnotatedWithAnyOf(ignoreWhenAnnotatedWith)
            }?.findChildByType(IDENTIFIER)
            ?.takeUnless { it.isTokenKeywordBetweenBackticks() }
            ?.let { identifier ->
                emit(
                    identifier.startOffset,
                    "Function name should start with a lowercase letter (except factory methods) and use camel case",
                    false,
                )
            }
    }

    private fun ASTNode.isFactoryMethod() =
        (this.psi as KtFunction)
            .let { ktFunction ->
                if (ktFunction.hasDeclaredReturnType()) {
                    // Allow:
                    //     fun Foo(): Foo = ..
                    //     fun <T> Foo(action: () -> T): Foo<T> = ..
                    ktFunction.name == ktFunction.typeReferenceNameWithoutGenerics()
                } else {
                    // Allow factory methods to overload another factory method or class constructor without specifying the type like:
                    //     fun Foo(value: Bar) = Foo(value.baz())
                    ktFunction.name == callExpressionReferenceIdentifier(ktFunction)
                }
            }

    private fun KtFunction.typeReferenceNameWithoutGenerics() =
        typeReference
            ?.node
            ?.findChildByType(USER_TYPE)
            ?.findChildByType(REFERENCE_EXPRESSION)
            ?.text

    private fun callExpressionReferenceIdentifier(ktFunction: KtFunction) =
        ktFunction
            .bodyExpression
            ?.node
            ?.takeIf { it.elementType == CALL_EXPRESSION }
            ?.findChildByType(REFERENCE_EXPRESSION)
            ?.findChildByType(IDENTIFIER)
            ?.text

    private fun ASTNode.isMethodInTestClass() = isTestClass && hasValidTestFunctionName()

    private fun ASTNode.hasValidTestFunctionName() =
        findChildByType(IDENTIFIER)
            ?.text
            .orEmpty()
            .matches(VALID_TEST_FUNCTION_NAME_REGEXP)

    private fun ASTNode.hasValidFunctionName() =
        findChildByType(IDENTIFIER)
            ?.text
            .orEmpty()
            .matches(VALID_FUNCTION_NAME_REGEXP)

    private fun ASTNode.isAnonymousFunction() =
        VALUE_PARAMETER_LIST ==
            findChildByType(FUN_KEYWORD)
                ?.nextCodeSibling20
                ?.elementType

    /*
     * A function override should not be reported as the interface of class that defines the function might be out of scope of the project
     * in which case the function name can not be changed. Note that the function will still be reported at the interface or class itself
     * whenever that interface or class is defined inside the scope of the project.
     */
    private fun ASTNode.isOverrideFunction() =
        findChildByType(MODIFIER_LIST)
            ?.children20
            .orEmpty()
            .any { it.elementType == OVERRIDE_KEYWORD }

    private fun ASTNode.isAnnotatedWithAnyOf(excludeWhenAnnotatedWith: Set<String>) =
        findChildByType(MODIFIER_LIST).containsAnnotationEntryWithIdentifierIn(excludeWhenAnnotatedWith)

    private fun ASTNode?.containsAnnotationEntryWithIdentifierIn(excludeWhenAnnotatedWith: Set<String>): Boolean =
        this
            ?.children20
            ?.any {
                when (it.elementType) {
                    ANNOTATION -> {
                        it.containsAnnotationEntryWithIdentifierIn(excludeWhenAnnotatedWith)
                    }

                    ANNOTATION_ENTRY -> {
                        it.annotationEntryName() in excludeWhenAnnotatedWith
                    }

                    else -> {
                        false
                    }
                }
            }
            ?: false

    private fun ASTNode.annotationEntryName() =
        findChildByType(CONSTRUCTOR_CALLEE)
            ?.findChildByType(TYPE_REFERENCE)
            ?.findChildByType(USER_TYPE)
            ?.findChildByType(REFERENCE_EXPRESSION)
            ?.findChildByType(IDENTIFIER)
            ?.text

    private fun ASTNode.isTokenKeywordBetweenBackticks() =
        this
            .takeIf { it.elementType == IDENTIFIER }
            ?.text
            .orEmpty()
            .removeSurrounding("`")
            .let { KEYWORDS.contains(it) }

    public companion object {
        public val IGNORE_WHEN_ANNOTATED_WITH_PROPERTY: EditorConfigProperty<Set<String>> =
            EditorConfigProperty(
                type =
                    PropertyType.LowerCasingPropertyType(
                        "ktlint_function_naming_ignore_when_annotated_with",
                        "Ignore functions that are annotated with. Value is a comma separated list of name without the '@' prefix.",
                        CommaSeparatedListValueParser(),
                    ),
                defaultValue = setOf("unset"),
            )

        private val VALID_FUNCTION_NAME_REGEXP = "[a-z][A-Za-z\\d]*".regExIgnoringDiacriticsAndStrokesOnLetters()
        private val VALID_TEST_FUNCTION_NAME_REGEXP = "(`.*`)|([a-z][A-Za-z\\d_]*)".regExIgnoringDiacriticsAndStrokesOnLetters()
        private val KEYWORDS =
            setOf(KtTokens.KEYWORDS, KtTokens.SOFT_KEYWORDS)
                .flatMap { tokenSet -> tokenSet.types.mapNotNull { it.debugName } }
                .filterNot { keyword ->
                    // The keyword sets contain a few 'keywords' which should be ignored. All valid keywords only contain lowercase
                    // characters
                    keyword.any { it.isUpperCase() }
                }.toSet()
        private val TEST_LIBRARIES_SET =
            setOf(
                "io.kotest",
                "junit.framework",
                "kotlin.test",
                "org.junit",
                "org.testng",
            )
    }
}

public val FUNCTION_NAMING_RULE_ID: RuleId = FunctionNamingRule().ruleId
