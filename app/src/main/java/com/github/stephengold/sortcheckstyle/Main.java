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

import com.beust.jcommander.JCommander;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
     * global map from module IDs to (non-suppression) DOM nodes
     */
    final private static Map<String, Node> moduleIdToNode = new TreeMap<>();
    /**
     * command-line parameters
     */
    final private static Parameters parameters = new Parameters();
    /**
     * pattern that matches one or more whitespace characters
     */
    final private static Pattern whitespacePattern = Pattern.compile("\\s+");
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
            if (ModuleGroups.isInSuppressionGroup(aName) && aId != null) {
                a = moduleIdToNode.get(aId);
                aName = getElementName(a);
            }

            String bId = getModuleId(b);
            String bName = getElementName(b);
            if (ModuleGroups.isInSuppressionGroup(bName) && bId != null) {
                b = moduleIdToNode.get(bId);
                bName = getElementName(b);
            }

            int groupA = ModuleGroups.moduleGroup(aName);
            int groupB = ModuleGroups.moduleGroup(bName);
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
     * Compress any whitespace in the message/property values of the specified
     * module.
     *
     * @param module the DOM node of the module (not null)
     */
    private static void compressWhitespace(Node module) {
        short nodeType = module.getNodeType();
        assert nodeType == Node.ELEMENT_NODE : nodeType;
        String tag = module.getNodeName();
        assert tag.equals("module") : tag;

        NodeList list = module.getChildNodes();
        int length = list.getLength();
        for (int i = 0; i < length; ++i) {
            Node child = list.item(i);
            String childTag = child.getNodeName();
            if (childTag.equals("message") || childTag.equals("property")) {
                String value = DomUtils.getElementAttribute(child, "value");
                if (value != null) {
                    Matcher m = whitespacePattern.matcher(value);
                    if (m.find()) {
                        value = m.replaceAll(" ");
                        DomUtils.setElementAttribute(child, "value", value);
                    }
                }
            }
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
            if (!ModuleGroups.isInSuppressionGroup(moduleName)) {

                String moduleId = getModuleId(module);
                if (moduleId != null) {
                    Node previousNode = moduleIdToNode.put(moduleId, module);
                    assert previousNode == null : "Duplicate id: " + moduleId;
                }
            }

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
                            int group = ModuleGroups.moduleGroup(elementName);
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

        NodeList allModules = document.getElementsByTagName("module");
        int numModules = allModules.getLength();

        if (parameters.compressWhitespace()) {
            for (int i = 0; i < numModules; ++i) {
                Node module = allModules.item(i);
                compressWhitespace(module);
            }
        }

        // Re-order the children of each module:
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
        short nodeType = module.getNodeType();
        assert nodeType == Node.ELEMENT_NODE : nodeType;
        String tag = module.getNodeName();
        assert tag.equals("module") : tag;

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
