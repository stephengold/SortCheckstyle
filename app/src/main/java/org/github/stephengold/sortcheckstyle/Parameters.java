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

import com.beust.jcommander.Parameter;

/**
 * Command-line parameters of the SortCheckstyle application.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class Parameters {
    // *************************************************************************
    // fields

    /**
     * whether to compress whitespace in values
     */
    @Parameter(names = {"-c", "--compress"},
            description = "Compress whitespace in values.")
    private boolean compressWhitespace;
    /**
     * whether to simply display the usage message and then exit
     */
    @Parameter(names = {"-h", "--help"},
            description = "Display this usage message and exit.")
    private boolean helpOnly;
    /**
     * filename for XML input
     */
    @Parameter(names = {"-f", "--file", "-i", "--input"},
            description = "Specify the input file.")
    private String inputFilename;
    /**
     * URI for XML input
     */
    @Parameter(names = {"-u", "--uri"}, description = "Specify the input URI.")
    private String inputUri;
    /**
     * whether to disable attribute sorting
     */
    @Parameter(names = "--noSortAttributes",
            description = "Disable attribute sorting.")
    private boolean noSortAttributes;
    /**
     * whether to disable child sorting
     */
    @Parameter(names = "--noSortChildren",
            description = "Disable child sorting.")
    private boolean noSortChildren;
    /**
     * filename for XML output (default="checkstyle-out.xml")
     */
    @Parameter(names = {"-o", "--output"},
            description = "Specify the output file.")
    private String outputFilename = "checkstyle-out.xml";
    /**
     * whether log output should be verbose
     */
    @Parameter(names = {"-v", "--verbose"},
            description = "Generate additional log output.")
    private boolean verboseLogging = false;
    // *************************************************************************
    // new methods exposed

    /**
     * Test whether to compress whitespace in message/property values.
     *
     * @return {@code true} to compress, otherwise {@code false}
     */
    boolean compressWhitespace() {
        return compressWhitespace;
    }

    /**
     * Describe how the document was/will be processed.
     *
     * @return text in English (not null)
     */
    String describeProcessing() {
        if (sortAttributes() && sortChildren()) {
            if (compressWhitespace()) {
                return "compressed and sorted";
            } else {
                return "sorted";
            }

        } else if (sortAttributes() || sortChildren()) {
            if (compressWhitespace()) {
                return "compressed and partly sorted";
            } else {
                return "partly sorted";
            }

        } else {
            if (compressWhitespace()) {
                return "compressed";
            } else {
                return "unsorted";
            }
        }
    }

    /**
     * Test whether to display the usage message and then exit.
     *
     * @return {@code true} to display the message, otherwise {@code false}
     */
    boolean helpOnly() {
        return helpOnly;
    }

    /**
     * Return the input filename that was specified.
     *
     * @return the filename, or {@code null} if none specified
     */
    String inputFilename() {
        return inputFilename;
    }

    /**
     * Return the input URI that was specified.
     *
     * @return the URI or {@code null} if none specified
     */
    String inputUri() {
        return inputUri;
    }

    /**
     * Test whether to sort attributes.
     *
     * @return {@code true} for sorting or {@code false} to preserve attribute
     * order
     */
    boolean sortAttributes() {
        return !noSortAttributes;
    }

    /**
     * Test whether to sort children.
     *
     * @return {@code true} for sorting or {@code false} to preserve child order
     */
    boolean sortChildren() {
        return !noSortChildren;
    }

    /**
     * Return the output file that was specified.
     *
     * @return the filename, or {@code null} if none specified
     */
    String outputFilename() {
        return outputFilename;
    }

    /**
     * Test whether the verbose-logging option was specified.
     *
     * @return true if specified, otherwise false
     */
    boolean verboseLogging() {
        return verboseLogging;
    }
}
