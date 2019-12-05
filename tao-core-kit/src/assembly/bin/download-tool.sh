#!/bin/bash

MYPWD=`pwd`
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"

cd ${SCRIPTPATH}
java -cp '../modules/*:../plugins/*:../lib/*' ro.cs.tao.datasource.cli.DownloadTool "$@"

cd ${MYPWD}
