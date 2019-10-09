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

The output format looks like:

```
ᏃᏊᏍ|ᎣᏊᏍ|Ꮒ-| ᏗᏥᏲᏟ|ᏗᏥᏲᏟ|| ,|,|| ᏛᏨᏃᏎᎵ|ᎥᏨᏃᏒᎢ|Ꮥ-|-ᎡᎵ ᏥᏔᎦ|ᏔᎦ|Ꮵ-| ᏄᏛᏂᏗ|ᎤᏛᏂᏗ|Ꮒ-| ᎦᏅᏅᎢ|ᎦᏅᏅᎢ|| ᏧᏂᏗᏫᏍᏗᎢ|ᏧᏂᏗᏫᏍᏗᎢ|| .|.||
```

Where each entry on a line corresponds to a word in the source text.
Each entry consists of:

```
surface-form|lemma|prefixes|suffixes
```

Where:
- surface-form = original word in text
- lemma = guessed lemma for the original word.
- prefixes = guessed sequence of prefixes applied to the lemma
- suffixes = guessed sequence of suffixes applied to the lemma

Note:
- The program uses heuristics via regex pattern matches to extract the likely prefixes and suffixes. The program will generated incorrect lemma|prefix|suffix results for some words which match the lookup patterns, but are already in their "lemma" form.


 



See [examples/] for some example input/output files.
