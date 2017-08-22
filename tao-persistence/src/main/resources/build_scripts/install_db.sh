#!/bin/bash
MyPwd=`pwd`

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

common_functions.sh
exportPGVariables

executeSqlScriptsFromFile "install_db_sql_files.txt" $SCRIPTS_ROOT

cd ${MyPwd}