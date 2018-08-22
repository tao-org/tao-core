#!/bin/bash

MYPWD=`pwd`
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"

cd ${SCRIPTPATH}/..
java -cp 'config/*:services/*:modules/*:plugins/*:lib/*:static/*' ro.cs.tao.services.TaoServicesStartup

cd ${MYPWD}


