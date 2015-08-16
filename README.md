Haedus Toolbox SCA

Samantha F McCabe, 2015

The Haedus Toolbox SCA is a powerful, script-driven sound-change applier program implemented in Java. It supports UTF-8 natively and has a rule syntax designed for clarity and similarity to sound change notation in linguistics.

The Haedus SCA supports capabilities like multiple rule conditions, regular expressions, metathesis, unrestricted variable naming, and scripting but also allows novice users to ignore advanced functionality they do not need.

Changelog:

Version 0.6.0
* Allows insertion / epenthesis of segments
* Logging to file + console
* Improved logging, including line number and file name for the script in which the error was found.
* Now attempts to log all compilation errors encountered in all loaded scripts before quitting
* Added some clarifications to the manual where users indicated some sections were unclear.

Version 0.5.0
* Improved normalization mode operation
* Greatly expanded manual
* Runnable using shell scripts or batch scripts
* Rewritten regular expression engine
* Numerous bug fixes

Version 0.1.0 (First Official Release)
* Allow user to change segmentation and normalization modes
* Added support for scripting language (IMPORT, EXECUTE commands)
* Added file IO commands (OPEN, CLOSE, WRITE) and file-handles;
* Support for metathesis and total assimilation through use of back-references
* Added OR condition chaining
* Added NOT for excluding conditions
* Improved back-end handling of compiled rules

Version 0.0.2
* Support control of segmentation and normalization modes

Version 0.0.1
* Initial test version, limited release
