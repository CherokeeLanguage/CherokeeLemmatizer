#!/bin/bash

set -e
set -o pipefail

cd "$(dirname "$0")"

EXAMPLES="$(pwd)"
FATJAR="../build/libs/CherokeeLemmatizer.jar"
cd ..
./gradlew clean build FATJAR
cd "$EXAMPLES"

for x in *.tok-factored.txt; do
    rm "$x"
done

for x in *.tok.txt; do
    y="${x/.tok.txt/.tok-factored.txt}"
    cat "$x" | java -jar "$FATJAR" > "$y"
done

echo "DONE"
exit 0