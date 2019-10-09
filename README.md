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

Where text.in is assumed to be pre-tokenized text.
And text.out is output in "factored" format for use by translation systems like Moses-SMT.

See [examples/] for some example input/output files.
