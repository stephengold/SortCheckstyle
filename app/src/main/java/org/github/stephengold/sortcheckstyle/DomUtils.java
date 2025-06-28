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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utilities to facilitate manipulation of generic DOM documents.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class DomUtils {
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private DomUtils() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Return the value of the specified attribute of the specified element.
     *
     * @param element the element's DOM node (not null, unaffected)
     * @param attributeName the name of the attribute to get (not null)
     * @return the attribute value, or {@code null} if the element lacks the
     * specified attribute
     */
    static String getElementAttribute(Node element, String attributeName) {
        int nodeType = element.getNodeType();
        assert nodeType == Node.ELEMENT_NODE : "nodeType = " + nodeType;

        String result;
        NamedNodeMap attributes = element.getAttributes();
        Node item = attributes.getNamedItem(attributeName);
        result = item.getNodeValue();

        return result;
    }

    /**
     * Replace all attributes of the specified DOM node.
     *
     * @param node the node to modify (not null)
     * @param attributes the desired attributes, in order (not null)
     */
    static void setAttributesFromArray(Node node, Node[] attributes) {
        clearAttributes(node);
        NamedNodeMap map = node.getAttributes();
        for (Node attribute : attributes) {
            map.setNamedItem(attribute);
        }
    }

    /**
     * Replace all children of the specified DOM node.
     *
     * @param parent the node to modify (not null)
     * @param children the desired children, in order (not null)
     */
    static void setChildrenFromArray(Node parent, Node[] children) {
        clearChildren(parent);
        for (Node child : children) {
            parent.appendChild(child);
        }
    }

    /**
     * Convert the specified NamedNodeMap to an array, to facilitate sorting.
     *
     * @param map the map to convert (not null)
     * @return a new array of pre-existing nodes
     */
    static Node[] toArray(NamedNodeMap map) {
        int length = map.getLength();
        Node[] result = new Node[length];
        for (int i = 0; i < length; ++i) {
            result[i] = map.item(i);
        }

        return result;
    }

    /**
     * Convert the specified NodeList to an array, to facilitate sorting.
     *
     * @param list the list to convert (not null)
     * @return a new array of pre-existing nodes
     */
    static Node[] toArray(NodeList list) {
        int length = list.getLength();
        Node[] result = new Node[length];
        for (int i = 0; i < length; ++i) {
            result[i] = list.item(i);
        }

        return result;
    }
    // *************************************************************************
    // private methods

    /**
     * Remove all attributes from the specified DOM node.
     *
     * @param node the node to modify (not null)
     */
    private static void clearAttributes(Node node) {
        NamedNodeMap map = node.getAttributes();
        int length = map.getLength();
        for (int i = length - 1; i >= 0; --i) {
            Node item = map.item(i);
            String name = item.getNodeName();
            map.removeNamedItem(name);
        }
    }

    /**
     * Remove all children from the specified DOM node.
     *
     * @param parent the node to modify (not null)
     */
    private static void clearChildren(Node parent) {
        NodeList list = parent.getChildNodes();
        int length = list.getLength();
        for (int i = length - 1; i >= 0; --i) {
            Node child = list.item(i);
            parent.removeChild(child);
        }
    }
}
