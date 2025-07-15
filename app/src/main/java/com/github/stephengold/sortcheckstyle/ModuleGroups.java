/*
Copyright (c) 2025 Stephen Gold

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.github.stephengold.sortcheckstyle;

/**
 * Encode module names into groups.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class ModuleGroups {
    // *************************************************************************
    // constants

    /**
     * index of the group to which non-file suppression modules belong
     */
    final private static int suppressionGroup = 15;
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private ModuleGroups() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Test whether the specified module is in the non-file suppression group.
     *
     * @param moduleName the name of a Checkstyle module
     * @return {@code true} if it's in the group, otherwise {@code false}
     */
    static boolean isInSuppressionGroup(String moduleName) {
        int groupIndex = moduleGroup(moduleName);
        if (groupIndex == suppressionGroup) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return the group to which the specified module belongs.
     *
     * @param moduleName the name of a Checkstyle module
     * @return the module's group index
     */
    static int moduleGroup(String moduleName) {
        switch (moduleName) {
            case "Checker":
                return 0;

            case "AnnotationLocation":
            case "AnnotationOnSameLine":
            case "AnnotationUseStyle":
            case "MissingDeprecated":
            case "MissingOverride":
            case "PackageAnnotation":
            case "SuppressWarnings":
            case "SuppressWarningsHolder":
                return 1; // annotations

            case "AvoidNestedBlocks":
            case "EmptyBlock":
            case "EmptyCatchBlock":
            case "LeftCurly":
            case "NeedBraces":
            case "RightCurly":
                return 2; // block checks

            case "DesignForExtension":
            case "FinalClass":
            case "HideUtilityClassConstructor":
            case "InnerTypeLast":
            case "InterfaceIsType":
            case "MutableException":
            case "OneTopLevelClass":
            case "SealedShouldHavePermitsList":
            case "ThrowsCount":
            case "VisibilityModifier":
                return 3; // class design

            case "ArrayTrailingComma":
            case "AvoidDoubleBraceInitialization":
            case "AvoidInlineConditionals":
            case "AvoidNoArgumentSuperConstructorCall":
            case "ConstructorsDeclarationGrouping":
            case "CovariantEquals":
            case "DeclarationOrder":
            case "DefaultComesLast":
            case "EmptyStatement":
            case "EqualsAvoidNull":
            case "EqualsHashCode":
            case "ExplicitInitialization":
            case "FallThrough":
            case "FinalLocalVariable":
            case "HiddenField":
            case "IllegalCatch":
            case "IllegalInstantiation":
            case "IllegalThrows":
            case "IllegalToken":
            case "IllegalTokenText":
            case "IllegalType":
            case "InnerAssignment":
            case "MagicNumber":
            case "MatchXpath":
            case "MissingCtor":
            case "MissingNullCaseInSwitch":
            case "MissingSwitchDefault":
            case "ModifiedControlVariable":
            case "MultipleStringLiterals":
            case "MultipleVariableDeclarations":
            case "NestedForDepth":
            case "NestedIfDepth":
            case "NestedTryDepth":
            case "NoArrayTrailingComma":
            case "NoFinalizer":
            case "OneStatementPerLine":
            case "OverloadMethodsDeclarationOrder":
            case "PackageDeclaration":
            case "ParameterAssignment":
            case "PatternVariableAssignment":
            case "RequireThis":
            case "ReturnCount":
            case "SimplifyBooleanExpression":
            case "SimplifyBooleanReturn":
            case "StringLiteralEquality":
            case "SuperClone":
            case "SuperFinalize":
            case "UnnecessaryNullCheckWithInstanceOf":
            case "UnnecessaryParentheses":
            case "UnnecessarySemicolonAfterOuterTypeDeclaration":
            case "UnnecessarySemicolonAfterTypeMemberDeclaration":
            case "UnnecessarySemicolonInEnumeration":
            case "UnnecessarySemicolonInTryWithResources":
            case "UnusedCatchParameterShouldBeUnnamed":
            case "UnusedLambdaParameterShouldBeUnnamed":
            case "UnusedLocalVariable":
            case "VariableDeclarationUsageDistance":
            case "WhenShouldBeUsed":
                return 4; // coding

            case "Header":
            case "MultiFileRegexpHeader":
            case "RegexpHeader":
                return 5; // headers

            case "AvoidStarImport":
            case "AvoidStaticImport":
            case "CustomImportOrder":
            case "IllegalImport":
            case "ImportControl":
            case "ImportOrder":
            case "RedundantImport":
            case "UnusedImports":
                return 6; // imports

            case "AtclauseOrder":
            case "InvalidJavadocPosition":
            case "JavadocBlockTagLocation":
            case "JavadocContentLocation":
            case "JavadocLeadingAsteriskAlign":
            case "JavadocMethod":
            case "JavadocMissingLeadingAsterisk":
            case "JavadocMissingWhitespaceAfterAsterisk":
            case "JavadocPackage":
            case "JavadocParagraph":
            case "JavadocStyle":
            case "JavadocTagContinuationIndentation":
            case "JavadocType":
            case "JavadocVariable":
            case "MissingJavadocMethod":
            case "MissingJavadocPackage":
            case "MissingJavadocType":
            case "NonEmptyAtclauseDescription":
            case "RequireEmptyLineBeforeBlockTagGroup":
            case "SingleLineJavadoc":
            case "SummaryJavadoc":
            case "WriteTag":
                return 7; // javadoc comments

            case "BooleanExpressionComplexity":
            case "ClassDataAbstractionCoupling":
            case "ClassFanOutComplexity":
            case "CyclomaticComplexity":
            case "JavaNCSS":
            case "NPathComplexity":
                return 8; // metrics

            case "ArrayTypeStyle":
            case "AvoidEscapedUnicodeCharacters":
            case "CommentsIndentation":
            case "DescendantToken":
            case "FinalParameters":
            case "Indentation":
            case "NewlineAtEndOfFile":
            case "NoCodeInFile":
            case "OrderedProperties":
            case "OuterTypeFilename":
            case "TodoComment":
            case "TrailingComment":
            case "Translation":
            case "UncommentedMain":
            case "UniqueProperties":
            case "UpperEll":
                return 9; // miscellaneous

            case "ClassMemberImpliedModifier":
            case "InterfaceMemberImpliedModifier":
            case "ModifierOrder":
            case "RedundantModifier":
                return 10; // modifiers

            case "AbbreviationAsWordInName":
            case "AbstractClassName":
            case "CatchParameterName":
            case "ClassTypeParameterName":
            case "ConstantName":
            case "IllegalIdentifierName":
            case "InterfaceTypeParameterName":
            case "LambdaParameterName":
            case "LocalFinalVariableName":
            case "LocalVariableName":
            case "MemberName":
            case "MethodName":
            case "MethodTypeParameterName":
            case "PackageName":
            case "ParameterName":
            case "PatternVariableName":
            case "RecordComponentName":
            case "RecordTypeParameterName":
            case "StaticVariableName":
            case "TypeName":
                return 11; // naming conventions

            case "Regexp":
            case "RegexpMultiline":
            case "RegexpOnFilename":
            case "RegexpSingleline":
            case "RegexpSinglelineJava":
                return 12; // regexp checks

            case "AnonInnerLength":
            case "ExecutableStatementCount":
            case "FileLength":
            case "LambdaBodyLength":
            case "LineLength":
            case "MethodCount":
            case "MethodLength":
            case "OuterTypeNumber":
            case "ParameterNumber":
            case "RecordComponentNumber":
                return 13; // size violations

            case "EmptyForInitializerPad":
            case "EmptyForIteratorPad":
            case "EmptyLineSeparator":
            case "FileTabCharacter":
            case "GenericWhitespace":
            case "MethodParamPad":
            case "NoLineWrap":
            case "NoWhitespaceAfter":
            case "NoWhitespaceBefore":
            case "NoWhitespaceBeforeCaseDefaultColon":
            case "OperatorWrap":
            case "ParenPad":
            case "SeparatorWrap":
            case "SingleSpaceSeparator":
            case "TypecastParenPad":
            case "WhitespaceAfter":
            case "WhitespaceAround":
                return 14; // whitespace

            case "SeverityMatchFilter":
            case "SuppressWarningsFilter":
            case "SuppressWithNearbyCommentFilter":
            case "SuppressWithNearbyTextFilter":
            case "SuppressWithPlainTextCommentFilter":
            case "SuppressionCommentFilter":
            case "SuppressionFilter":
            case "SuppressionSingleFilter":
            case "SuppressionXpathFilter":
            case "SuppressionXpathSingleFilter":
                return suppressionGroup; // non-file filters

            case "BeforeExecutionExclusionFileFilter":
                return suppressionGroup + 1; // file filters

            case "TreeWalker":
                return 99;

            default:
                String message
                        = String.format("Unknown module \"%s\"", moduleName);
                throw new IllegalArgumentException(message);
        }
    }
}
