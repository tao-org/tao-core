#!/bin/bash

MYPWD=`pwd`
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"

cd ${SCRIPTPATH}
java -Dsun.lang.ClassLoader.allowArraySyntax=true -Djavax.xml.parsers.SAXParserFactory=com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl -cp '../config/*:../services/*:../modules/*:../plugins/*:../lib/*:../static/*' -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 ro.cs.tao.services.TaoServicesStartup

cd ${MYPWD}


