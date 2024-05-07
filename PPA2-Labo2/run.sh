#!/bin/bash

mvn package || exit $?

target=$(find . -name "*.jar" -print -quit)

java -jar $target &
pid=$!

jconsole -interval=1 -notile $pid
kill $pid