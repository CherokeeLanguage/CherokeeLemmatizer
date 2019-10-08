# CherokeeLemmatizer

Utility to analyze Cherokee language texts.

* Generate likely lemmas.
* Show likely affixes.

Developed for use with the Moses Machine Translation software.

build:
```
./gradlew clean build fatjar
```

run: 
cat text.in | java -jar build/libs/CherokeeLemmatizer.jar > text.out

[TODO] 

Flags:

```
--moses-lemma
```
Output in pipe format for use in the Moses MT system. [surface form|lemma]
```
--moses-prefixes
```
Output in pipe format for use in the Moses MT system. [surface form|prefixes]

```
--moses-suffixes
```
Output in pipe format for use in the Moses MT system. [surface form|suffixes]

```
--moses-full
```
Output in pipe format for use in the Moses MT system. [surface form|lemma|prefixes|suffixes]

 
Only one flag may be specified.

