#!/bin/sh
mvn dependency:copy-dependencies -DoutputDirectory=war/WEB-INF/lib
mvn dependency:copy-dependencies -DoutputDirectory=sources -Dclassifier=sources
