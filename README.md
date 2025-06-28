SortCheckstyle is a tool to organize the contents of
[Checkstyle](https://checkstyle.org/) configuration files.

Organizing file content according to a standard scheme
facilitates comparisons between files.

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
