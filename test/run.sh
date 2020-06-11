#!/bin/sh
cd $(dirname $0)

cd ../sspts-batch
./gradlew build
ret=$?
if [ $ret -ne 0 ]; then
  exit $ret
fi
rm -rf build

cd ../initial
../sspts-batch/gradlew -b ../sspts-batch/build.gradle wrapper
./gradlew compileJava
ret=$?
if [ $ret -ne 0 ]; then
  exit $ret
fi
rm -rf build
rm -rf gradle
rm gradlew*

exit
