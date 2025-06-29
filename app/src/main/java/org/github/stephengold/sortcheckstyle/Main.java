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
package org.github.stephengold.sortcheckstyle;

import com.beust.jcommander.JCommander;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Console application to organize the content of a Checkstyle configuration
 * file in accordance with Stephen Gold's preferences.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class Main {
    // *************************************************************************
    // constants

    /**
     * index of the group to which non-file suppression modules belong
     */
    final private static int suppressionGroup = 15;
    /**
     * global map from module IDs to (non-suppression) DOM nodes
     */
    final private static Map<String, Node> moduleIdToNode = new TreeMap<>();
    /**
     * command-line parameters
     */
    final private static Parameters parameters = new Parameters();
    /**
     * default input file
     */
    final private static String defaultInputFilename = "checkstyle-in.xml";
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private Main() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the SortCheckstyle console application.
     *
     * @param arguments the command-line arguments
     * @throws IOException if an error occurs while reading the input document
     * @throws ParserConfigurationException if the requested DocumentBuilder
     * cannot be created
     * @throws SAXException if a parse error occurs
     * @throws TransformerException if an unrecoverable error occurs while
     * writing the modified document
     */
    public static void main(String[] arguments)
            throws IOException, ParserConfigurationException,
            SAXException, TransformerException {
        JCommander jCommander = new JCommander(parameters);
        jCommander.parse(arguments);
        jCommander.setProgramName("SortCheckstyle");
        if (parameters.helpOnly()) {
            jCommander.usage();
            System.exit(0);
        }

        // Create a DocumentBuilder:
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Read and parse the document:
        Document document;
        if (parameters.inputUri() != null) {
            String inputUri = parameters.inputUri();
            System.out.printf(
                    "Reading XML from URI \"%s\" ...", inputUri);
            document = builder.parse(inputUri);

        } else {
            String inputFilename = parameters.inputFilename();
            if (inputFilename == null) {
                inputFilename = defaultInputFilename;
            }
            System.out.printf(
                    "Reading XML from file \"%s\" ...", inputFilename);
            File inputFile = new File(inputFilename);
            document = builder.parse(inputFile);
        }
        System.out.println(" done.");

        // Process the document, making changes as we go:
        processDocument(document);

        // Write the modified document to the output file:
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        DOMSource source = new DOMSource(document);

        String outputFilename = parameters.outputFilename();
        String description = parameters.describeProcessing();
        System.out.printf("Writing %s XML to file \"%s\" ...",
                description, outputFilename);
        StreamResult result = new StreamResult(outputFilename);
        transformer.transform(source, result);
        System.out.println(" done.");
    }

    /**
     * Compare the specified elements, for sorting.
     *
     * @param a the DOM node of the first element (not null, unaffected)
     * @param b the DOM node of the 2nd element (not null, unaffected)
     * @return -1, 0, or +1
     */
    private static int compareElements(Node a, Node b) {
        short aType = a.getNodeType();
        short bType = b.getNodeType();
        assert aType == Node.ELEMENT_NODE : aType;
        assert bType == Node.ELEMENT_NODE : bType;

        // <property> then <module> then <message>:
        String aTag = a.getNodeName();
        String bTag = b.getNodeName();
        int orderA = tagOrder(aTag);
        int orderB = tagOrder(bTag);
        if (orderA > orderB) {
            return 1;
        } else if (orderA < orderB) {
            return -1;
        }

        if (aTag.equals("module")) {
            assert bTag.equals("module") : bTag;

            String aId = getModuleId(a);
            String aName = getElementName(a);
            int groupA = moduleGroup(aName);
            if (groupA == suppressionGroup && aId != null) {
                a = moduleIdToNode.get(aId);
                aName = getElementName(a);
                groupA = moduleGroup(aName);
            }

            String bId = getModuleId(b);
            String bName = getElementName(b);
            int groupB = moduleGroup(bName);
            if (groupB == suppressionGroup && bId != null) {
                b = moduleIdToNode.get(bId);
                bName = getElementName(b);
                groupB = moduleGroup(bName);
            }

            if (groupA > groupB) {
                return 1;
            } else if (groupA < groupB) {
                return -1;
            }

            // Within groups, sort by module name:
            int result = aName.compareTo(bName);
            if (result != 0) {
                return result;
            }

            // If 2 modules have the same name, sort by ID:
            if (aId != null && bId != null) {
                result = aId.compareTo(bId);
            }
            return result;

        } else if (aTag.equals("message")) {
            assert bTag.equals("message") : bTag;

            // Sort by message key:
            String aKey = DomUtils.getElementAttribute(a, "key");
            String bKey = DomUtils.getElementAttribute(b, "key");
            return aKey.compareTo(bKey);

        } else {
            assert aTag.equals("property") : aTag;
            assert bTag.equals("property") : bTag;

            // Sort by property name:
            String aName = getElementName(a);
            String bName = getElementName(b);
            return aName.compareTo(bName);
        }
    }

    /**
     * Return the "name" attribute of the specified element.
     *
     * @param element the element's DOM node (not null, unaffected)
     * @return the attribute value, or {@code null} if the element lacks the
     * "name" attribute
     */
    private static String getElementName(Node element) {
        String result = DomUtils.getElementAttribute(element, "name");
        return result;
    }

    /**
     * Return the ID of the specified module.
     *
     * @param module the module's DOM node (not null, unaffected)
     * @return the "id" property value, or {@code null} if the module lacks an
     * "id" property
     */
    private static String getModuleId(Node module) {
        int nodeType = module.getNodeType();
        assert nodeType == Node.ELEMENT_NODE : "nodeType = " + nodeType;

        NodeList children = module.getChildNodes();
        int numChildren = children.getLength();

        for (int i = 0; i < numChildren; ++i) {
            Node child = children.item(i);
            short childType = child.getNodeType();
            if (childType == Node.ELEMENT_NODE) {
                String tagName = child.getNodeName();
                if (tagName.equals("property")) {
                    String elementName = getElementName(child);
                    if (elementName.equals("id")) {
                        String result
                                = DomUtils.getElementAttribute(child, "value");
                        return result;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Initialize the global map from module IDs to DOM nodes.
     *
     * @param document the document to process (not null, unaffected)
     */
    private static void initializeIdMap(Document document) {
        NodeList allModules = document.getElementsByTagName("module");
        int numModules = allModules.getLength();
        for (int i = 0; i < numModules; ++i) {
            Node module = allModules.item(i);
            String moduleName = getElementName(module);

            // Ignore any IDs found in suppression modules:
            int moduleGroup = moduleGroup(moduleName);
            if (moduleGroup != suppressionGroup) {

                String moduleId = getModuleId(module);
                if (moduleId != null) {
                    Node previousNode = moduleIdToNode.put(moduleId, module);
                    assert previousNode == null : "Duplicate id: " + moduleId;
                }
            }

        }
    }

    /**
     * Return the group to which the specified module belongs.
     *
     * @param moduleName the name of a Checkstyle module
     * @return the module's group index
     */
    private static int moduleGroup(String moduleName) {
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
                throw new IllegalArgumentException(
                        "moduleName = " + moduleName);
        }
    }

    /**
     * Return the sort index of the specified node type, when sorting a module's
     * children.
     *
     * @param nodeType the type of the child node
     * @return the index
     */
    private static int nodeTypeOrder(int nodeType) {
        switch (nodeType) {
            // element before text
            case Node.ELEMENT_NODE:
                return 1;
            case Node.TEXT_NODE:
                return 2;
            default:
                throw new IllegalArgumentException("nodeType = " + nodeType);
        }
    }

    /**
     * Dump the specified NodeList to the standard output, to assist with
     * debugging.
     *
     * @param list the list to dump (not null, unaffected)
     */
    private static void printNodeList(NodeList list) {
        System.out.println("NodeList:");
        int numNodes = list.getLength();
        for (int i = 0; i < numNodes; ++i) {
            Node node = list.item(i);
            short nodeType = node.getNodeType();
            if (nodeType == Node.COMMENT_NODE) {
                String comment = node.getNodeValue();
                System.out.println("  comment: " + comment);

            } else if (nodeType == Node.ELEMENT_NODE) {
                String tagName = node.getNodeName();
                switch (tagName) {
                    case "module":
                    case "property":
                        String elementName = getElementName(node);
                        if (tagName.equals("module")) {
                            int group = moduleGroup(elementName);
                            System.out.printf(" [group %d]", group);
                            String id = getModuleId(node);
                            if (id != null) {
                                System.out.printf(" [id=%s]", id);
                            }
                        }
                        System.out.printf(
                                " <%s name=%s>%n", tagName, elementName);
                        break;
                    default:
                        throw new RuntimeException(
                                "Unexpected element, tagName = " + tagName);
                }

            } else if (nodeType == Node.TEXT_NODE) {
                System.out.println("  text");

            } else {
                throw new RuntimeException(
                        "Unexpected node, type = " + nodeType);
            }
        }
    }

    /**
     * Process the specified document in memory.
     *
     * @param document the document to process (not null)
     */
    private static void processDocument(Document document) {
        initializeIdMap(document);

        // Re-order the children of each module:
        NodeList allModules = document.getElementsByTagName("module");
        int numModules = allModules.getLength();
        for (int i = 0; i < numModules; ++i) {
            Node module = allModules.item(i);
            sortModuleChildren(module);
        }
    }

    /**
     * Re-order the attributes and children of the specified module.
     *
     * @param module the DOM node of the module (not null)
     */
    private static void sortModuleChildren(Node module) {
        Comparator<Node> comparator = (Node a, Node b) -> {

            // Keep each comment/text with its following module/property:
            short aType = a.getNodeType();
            while ((aType == Node.COMMENT_NODE || aType == Node.TEXT_NODE)
                    && a.getNextSibling() != null) {
                a = a.getNextSibling();
                aType = a.getNodeType();
            }

            short bType = b.getNodeType();
            while ((bType == Node.COMMENT_NODE || bType == Node.TEXT_NODE)
                    && b.getNextSibling() != null) {
                b = b.getNextSibling();
                bType = b.getNodeType();
            }

            // element before text:
            int orderA = nodeTypeOrder(aType);
            int orderB = nodeTypeOrder(bType);
            if (orderA > orderB) {
                return 1;
            } else if (orderA < orderB) {
                return -1;
            }

            // same type
            if (aType == Node.ELEMENT_NODE) {
                assert bType == Node.ELEMENT_NODE : bType;

                int result = compareElements(a, b);
                return result;

            } else {
                assert aType == Node.TEXT_NODE : aType;
                assert bType == Node.TEXT_NODE : bType;

                // Preserve the original order:
                return 0;
            }
        };

        if (parameters.sortAttributes()) {
            // Sort the module's attributes:
            NamedNodeMap attributeMap = module.getAttributes();
            Node[] attributeArray = DomUtils.toArray(attributeMap);
            Arrays.sort(attributeArray, comparator);
            DomUtils.setAttributesFromArray(module, attributeArray);
        }

        if (parameters.sortAttributes()) {
            // Sort the module's children:
            NodeList childList = module.getChildNodes();
            Node[] childArray = DomUtils.toArray(childList);
            Arrays.sort(childArray, comparator);
            DomUtils.setChildrenFromArray(module, childArray);
        }
    }

    /**
     * Return the sort index of the specified tag when sorting a module's
     * children.
     *
     * @param tag the tag of the child node
     * @return the index
     */
    private static int tagOrder(String tag) {
        switch (tag) {
            // <property> then <module> then <message>:
            case "property":
                return 1;
            case "module":
                return 2;
            case "message":
                return 3;
            default:
                throw new IllegalArgumentException("tag=" + tag);
        }
    }
}
