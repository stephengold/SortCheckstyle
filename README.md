SortCheckstyle is a tool to organize the contents of
[Checkstyle](https://checkstyle.org/) configuration files.

Organizing file content according to a standard scheme
exposes redundancy and facilitates comparisons between files.

Characteristics of the SortCheckstyle scheme:

+ Modules generally appear in the same order
  as they do in Checkstyle's online documentation:
  grouped into "annotations", "block checks", etcetera
  and then alphabetized by name within each group.
+ Suppression modules that specify an ID
  appear with the modules they suppress.
+ Within each module:
  + properties appear before modules
  + modules appear before messages
  + properties are sorted lexicographically by name, and
  + messages are sorted lexicographically by key.

### Usage

<pre>
Usage: SortCheckstyle [options]
  Options:
    -c, --compress
      Compress whitespace in values.
      Default: false
    -h, --help
      Display this usage message and exit.
      Default: false
    -f, --file, -i, --input
      Specify the input file.
    --noSortAttributes
      Disable attribute sorting.
      Default: false
    --noSortChildren
      Disable child sorting.
      Default: false
    -o, --output
      Specify the output file.
      Default: checkstyle-out.xml
    -u, --uri
      Specify the input URI.
    -v, --verbose
      Generate additional log output.
      Default: false
</pre>

If no input file or URI is specified,
the tool will attempt to read the file "checkstyle-in.xml".
