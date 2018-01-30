#!/bin/bash
MyPwd=`pwd`

exit 0

local host port user password
while getopts 'h:p:u:s:' arg
do
    echo "Arg is ${arg}"
    case ${arg} in
        h) host=${OPTARG};;
        p) port=${OPTARG};;
        u) user=${OPTARG};;
        s) password=${OPTARG};;            
        *) echo "Invalid option ${arg}" ; return 1 # illegal option
    esac
done

SCRIPTS_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo $SCRIPTS_ROOT
cd $SCRIPTS_ROOT

. common_functions.sh
exportPGVariables

executeSqlCommand "DROP DATABASE IF EXISTS taodata" ""
executeSqlCommand "DROP ROLE IF EXISTS tao" ""
