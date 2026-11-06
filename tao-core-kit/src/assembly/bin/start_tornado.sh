#!/bin/bash

MYPWD=`pwd`
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
TORNADOVM_HOME=../tornadovm
cd ${SCRIPTPATH}
java -server -cp '../services/*:../modules/*:../plugins/*:../lib/*:../static/*' -XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI --enable-preview -Djava.library.path=$TORNADOVM_HOME/lib --module-path '.:$TORNADOVM_HOME/share/java/tornado' -Dtornado.load.api.implementation=uk.ac.manchester.tornado.runtime.tasks.TornadoTaskGraph -Dtornado.load.runtime.implementation=uk.ac.manchester.tornado.runtime.TornadoCoreRuntime -Dtornado.load.tornado.implementation=uk.ac.manchester.tornado.runtime.common.Tornado -Dtornado.load.annotation.implementation=uk.ac.manchester.tornado.annotation.ASMClassVisitor -Dtornado.load.annotation.parallel=uk.ac.manchester.tornado.api.annotations.Parallel -XX:+UseParallelGC $(<TORNADOVM_HOME/etc/exportLists/common-exports) $(<TORNADOVM_HOME/etc/exportLists/opencl-exports) --add-modules ALL-SYSTEM,tornado.runtime,tornado.annotation,tornado.drivers.common,tornado.drivers.opencl -Dsun.lang.ClassLoader.allowArraySyntax=true -Djavax.xml.parsers.SAXParserFactory=com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl --add-opens=java.base/java.lang=ALL-UNNAMED -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 ro.cs.tao.services.TaoServicesStartup

cd ${MYPWD}


