@echo off

set PATH=%PATH%;"c:\Program Files\java\jdk1.8.0_45\bin"
set JAVA_HOME="c:\Program Files\java\jdk1.8.0_45"

gradlew setupDecompWorkspace --refresh-dependencies