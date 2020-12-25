# Changelog:
<!---
#### [Unreleased]
#### Added 
#### Changed 
#### Deprecated
#### Removed 
#### Fixed 
#### Security 
-->

## Didelphis Sound Change Applier
The changes and versions relate to the current version of the software, which
also reset the version numbers. This was done because several parts of the older
Toolbox project were moved to the `didelphis-common` project, allowing this
`didelphis-sca` project to only concern sound-change rule processing.

Unfortunately the changelog was not properly maintained during this process, and
had to be re-created retroactively. It should be considered generally reliable,
but it cannot be guaranteed (if it matters to you) that every work item is 
associated with the correct version. That said, milestones were more often used,
which reduces the liklihood of a change appearing with the wrong version.

It's actually very confusing. At some point some of the code was separated, and
the 

### 0.4.0
#### Added 
#### Changed 
#### Deprecated
#### Removed 
#### Fixed 
#### Security 

### 0.3.0
#### Added 
#### Changed 
 + Transitioned to Java 8 and reset version to `0.3.0`
   [[didelphis-sca-130]](https://github.com/samanthamccabe/didelphis-Sca/issues/130)
#### Deprecated
#### Removed 
#### Fixed 
#### Security 

## Haedus Toolbox 
The changes and versions below apply to 	early development of the program when it
was still known as the Haedus Toolbox.

### 1.0.0
#### Added 
#### Changed 
* Refactored the feature model into a specification containing the feature 
	definitions and a mapping, containing the mapping of symbols to feature
	arrays
	[tooblox-sca-106]
#### Deprecated
#### Removed 
#### Fixed 
#### Security 

### 0.8.0
#### Added
* Change rule parsing to permit commands to be split across multiple lines for
	readability
	[tooblox-sca-117]
* Added support for compound rules, where several rules are applied sequentially
	at each position in a word, rather than each rule reaching the end of a word
	before the next rule is applied
	[tooblox-sca-57]
* Added logger instrumentation to `StandardScript` and rule commands
	[toolbox-sca-118], [toolbox-sca-127]

#### Changed
* Updated artifact definitions and package names
	[tooblox-sca-113]
* Refactored script parsing to use new `ScriptParser`
	[toolbox-sca-120]
* Changed import and export functions to extend `AbstractIoCommand`
* Updated project for transition to Java 8 and changes to Didelphis Common
	[didelphis-sca-130], [didelphis-sca-140]

#### Fixed 
*  A number of rule conditions were failing
	[toolbox-sca-125]

#### Removed
* Removed `obj == null` checks in the .equals() methods of some objects and used
	`instanceof`
	[tooblox-sca-107]

### 0.7.0
#### Added 
* Support for phonetic features
	[toolbox-sca-47; toolbox-sca-71]
* Support for negation/complementation in state machines 
	[toolbox-sca-3; tooblox-sca-79]
* Add support for feature constraints [toolbox-sca-87] and aliases
	[toolbox-sca-90]

#### Changed 
* Ensured that lexicons were re-normalized before being written out
	[toolbox-sca-50]
* Fixed bug  preventing `EXECUTE` command from working correctly
	[toolbox-sca-74]
* Made `Segment` object implement `Comparable` interface
	[toolbox-sca-72]
* Corrected behavior of `NOT` chains in rule conditions
	[toolbox-sca-81]
* Fixed but where `IMPORT` did not load variable definitions
	[toolbox-sca-84]
* Refactored feature arrays to an interface

<!---
#### Deprecated
#### Removed 
#### Fixed 
#### Security 
-->

### 0.6.0 (2015-08-16)
#### Added 
* Allows insertion / epenthesis of segments
* Logging to file + console
* Now attempts to log all compilation errors encountered in all loaded scripts
	before quitting

#### Changed
* Improved logging, including line number and file name for the script in which
	the error was found.
* Added some clarifications to the manual where users indicated some sections
	were unclear.

#### Removed
* "Basic mode" for loading a single rules file and lexicon; all lexicon IO is
	specified in the rules file

<!---
#### Deprecated 
#### Removed 
#### Fixed 
#### Security 
-->

### 0.5.0 (2015-07-25)
#### Added
* Runnable using shell scripts or batch scripts
* User Manual

#### Changed
* Improved normalization mode operation
* Rewrote regular expression engine, including support for the `.` dot 
	metacharacter
* Numerous bug fixes

### 0.1.0 (2014-10-25)
#### Added
* Allow user to change segmentation and normalization modes
* Added support for scripting language (`IMPORT`, `EXECUTE` commands)
* Added file IO commands (`OPEN`, `CLOSE`, `WRITE`) and file-handles;
* Support for metathesis and total assimilation through use of back-references
* Added `OR` condition chaining
* Added `NOT` for excluding conditions
* Improved back-end handling of compiled rules
