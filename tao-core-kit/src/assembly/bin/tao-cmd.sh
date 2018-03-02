#!/bin/bash

MYPWD=`pwd`
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"

cd ${SCRIPTPATH}
java -cp '../modules/*:../lib/*' ro.cs.tao.TAO "$@"

cd ${MYPWD}
