Haedus Toolbox SCA, Manual

##Introduction##
The Haedus Toolbox SCA was designed to address one specific and pernicous problem with other sound-change applier programs in common use withing the conlang community, namely that single sounds often need to be represented by two or mor characters, whether that is 'ts', 'tʰ', or 'qʷʰ'. This relieves the user from having to artificially re-order commands because 'p > b' happens to also affect 'pʰ' even when the intent of the user is for these to be distinct and segments.
The SCA can infer the what sequences should be treated as unitary by attaching diacritics and modifier letters to a preceding base character. The user can also manually specify sequences that should be treated as atomic. 

##Running The SCA##
To use the SCA, the user must provide a lexicon and commands file. In stand-alone operation, it can be run using the command

> `java -jar sca.jar LEXICON RULES OUTPUT`

with the user providing paths for the worlist, commands, and output file. This can, of course, be placed into a batch or shell script for your convenience. However, running SCA through a terminal is recommended, so that any errors can be printed to the terminal. Later versions will permit the user to provide only the script, in which the input and output paths are specified

##Scripts##
Operation of the SCA is controlled through a script file while primarily contains rule definitions, but which also allows the user to define variables, reserve character sequences, and control segmentaton and normalization. Lists of things, like sources and targets in commands, the contents of sets inside conditions, variable definitions, and commands to reserve character sequences are all delimited by whitespace (the space character, or tab) and is quantity-insensitive, so you can use extra spaces, or tabs to make columns align, as you will see throughout the examples.

While whitespace is used to separate items in lists, padding around operators and delimiters is optional. As elsewhere the quantity is not important.

The following characters have special meanings in the SCA script language and cannot be used elsewhere: `>, /, _, #, $, %, *, ?, +, !, (, ), {, }, ., =`

Script files may contain comments, starting with `%`, either at the beginning of a line, or in-line.

##Command##
Apart from commands and variables, there are additional commands used to control segmenation and normalziation and reserve sequences to be treated as atomic. This is controlled by the following command, plus one of the listed flags

NORMALIZATION:
> * `NFD ` Canonical decomposition (default)
> * `NFC ` Canonical decomposition, followed by canonical composition
> * `NFKD` Compatibility decomposition
> * `NFKC` Compatibility decomposition followed by compatibility composition
> * `NONE` No normalization; input is not modified

SEGMENTATION:
> * `TRUE ` By default, automatic segmentation is used
> * `FALSE` Treats each input character as atomic, except where characters are reserved by the use

##Variables##
The SCA allows for the definition of variables (and re-definition) on-the-fly, anywhere in the script. Variables definitions consist of a label, the assignment operator = and a space-separated list of values. For example:
```
TH = pʰ tʰ kʰ
T  = p  t  k
D  = b  d  g
W  = w  y  ɰ
N  = m  n
C  = TH T D W N r s
```

The values may contain other variable labels. There are no restrictions on variable naming - it is up to the user to avoid conflicts. However, when SCA parses a rule or variable definition, it searches for variables by finding the longest matching lable. If you have `T`, `H`, and `TH` defined as variables, a rule containing `TH` will always be understood to represent the variable `TH`, and not `T` followed by `H`.

If you wish to define longer variable names, you can use a non-reseved prefix like `&` or `@`, or wrap the name in square brackets.

##The Rule Format##
The syntax for commands is desinged to be similar to that used to describe sound changes in linguistics generally, and to support pattern matching using regular expressions.

This SCA uses `>` as the transformation operator, and separates the transformation and condition using `/`. The condition is not required and commands lacking a condition do not require the the `/` symbol. When the `/` symbol is present, the precondition-postcondition separator `_` must appear exactly once.

Some basic commands are:
```
pʰ tʰ kʰ > f  θ  x
p  t  k  > b  d  g  / N_
p  t  k  > pʰ tʰ kʰ / _V

[Aspirata] > [Anaspirata]
```
The first part of the rule (before `/`) is the *transform* which may contain literals and variables, as well as sequences of both.

Note that if mutliple sounds converge (or are deleted), such as a merger of e and o with a, then the following are equivalent:
```
e o > a a
e o > a
```

###Deletion###
Segments can be deleted by transforming them to `0` (the character zero). This can be written as follows:
```
x h > 0
```

Just as with other transformations, if all segments are to be deleted, then the rule can  be written as shown above. However, one can also write a zero along with other sounds as follows:
```
w s h > 0 h 0 / #_
```

###Indices and Backreferences###
The SCA permits the transform of commands to contain backreferences which can be very useful in writing commands for metathesis or total assimilation. For example
```
C = p t k
N = n m
CN > $2$1
```
allows us to easily represent metathesis, swapping `N` and `C` wherever `N` is found following `C`.

When SCA parses a rule, it keeps track of each variable in the source part of the transform and knows in the above example, that `C`  is at index `1` and `N` is at index `2`. The target part of the transform lets us refer back to this using the `$` symbol and the index of the variable we wish to refer to.

We can actually go slightly further, however and use the indices on a *different* variable. In a slighly variation on the previous example, we can write
```
C = p t k
G = b d g
N = n m
CN > $2$G1
```
which does the same as the above, but also replaces any element of `C` with the corresponding element of `G`. So, if a word is *atna*, the rule will change it to *anda*.

This can also be used for some kinds of assimilation and dissimilation, such as simplifying clusters of plosives by changing the second to be the same as the first:
```
C = p t k
CC > $1$1
```
This will change a word like *akpa* to *akka*; in this case, it is actually equivalent to write `CC > C$1`

###Condition Format###
Most of the power of the Toolbox condition format lies in it's ability to use ad-hoc sets, and regular expressions. The underscore character `_` separates the precondition from the postcondition, so that the rule will be applied only when both sides of the condition match.

The ``/`` part of the rule can be left out if there is no condition: `a > b` is equivalent to `a > b / _` and both are valid. A rule can have `/` and `_` or neither, but `a > b _` and `a > b /` are both invalid.

Regular Expression metacharacters
> - `+`  matches the previous expression one or more times
> - `*`  matches the previous expression zero or more times
> - `?`  matches the previous expression zero or one times
> - `{}` matches any of the list of expressions inside it
> - `()` used to group expressions
> - `!`  ~~matches anything that is NOT the following expression~~ (NB: not implemented)
> - `.`  ~~matches any character~~ (NB: not implemented)

Sets, delimited by curly braces `{}`, contain a list of space-separated subexpressions. These can be single characters, variables, or other regular expressions - anything allowed elsewhere in the condition. It's not clear that this capability is of any real use, but it remains avaible if you happen to find a use for it. Some examples
```
{ a b c }
{ a b+ }
{ C a }
{ C(a)? x }
{ C{a b} x }
```

Take some care when writing commands that use `?` or `*`. Because these allow a condition to match zero times, any condition consisting solely of `_X?` or `X*_` will match no matter what. This is because, logically, `_X?` is equivalent to `_X OR _`. Further, `X?_Y?` does not just allow the rule to match both `X_` or `_Y`, but also `_`, and any rule matching `_` will be applied everywhere

###Joint Conditions###
Another piece of advanced functionality supported by this SCA is the capacity to combine mulitple conditions in one rule using OR. For example, if the same transformation occurs under multiple conditions, they can be joined together:
```
o e > a / X_ OR _Y % change e and o to a when preceded by X or followed by Y
```
