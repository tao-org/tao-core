#!/bin/bash

PRINT_HELP="no"

while [[ $# -gt 0 ]]; do
    key="$1"
    case $key in
        -s|--hostname)
        shift
        NODE_HOST_NAME="$1"
        ;;
        -n|--nbprocs)
        shift
        HOST_NB_PROCS="$1"
        ;;
        -u|--user)
        shift
        HOST_USER="$1"
        ;;
        -p|--pass)
        shift
        HOST_PASS="$1"
        ;;

        -h|--help)
        PRINT_HELP="yes"
        ;;

        *)
                # unknown option
        ;;
    esac
    shift # past argument or value
done

function print_help() {
    if [ $PRINT_HELP = "yes" ] ; then
        echo "Script for updating the hosts file on the master node whenever a new node is added"
        echo "optional arguments:"
        echo "  -h, --help              show this help message and exit"
        echo "  -s, --hostname          execution node hostname to be added added to torque"
        echo "  -n, --nbprocs           number of processors of execution node hostname to be added added to torque"
        echo "  -u, --user              the user to be used for execution"
        echo "  -p, --pass              the password of the user to be used for execution"

        exit
    fi
}

function addNode() {
    TARGET_NODE_NAME=$1
    TARGET_NODE_NB_PROCS=$2
    USER=$3
    USER_PASS=$4

    if [ -z ${TARGET_NODE_NAME} ] ; then
        echo "Error: Execution node name not set. Exiting ..."
        exit -1
    fi

    echo "Executing env \"PATH=$PATH:/usr/local/bin/\" qmgr -c \"delete node ${TARGET_NODE_NAME}\""
    runCmd "env \"PATH=$PATH:/usr/local/bin/\" qmgr -c \"delete node ${TARGET_NODE_NAME}\"" $USER $USER_PASS

    if [ -z ${TARGET_NODE_NB_PROCS} ] ; then
       echo "Warning: Number of processors for the execution node not set"
       runCmd "env \"PATH=$PATH:/usr/local/bin/\" qmgr -c \"create node ${TARGET_NODE_NAME}\"" $USER $USER_PASS
    else
        echo "Executing env \"PATH=$PATH:/usr/local/bin/\" qmgr -c \"create node ${TARGET_NODE_NAME} np=${TARGET_NODE_NB_PROCS}\""
	runCmd "env \"PATH=$PATH:/usr/local/bin/\" qmgr -c \"create node ${TARGET_NODE_NAME} np=${TARGET_NODE_NB_PROCS}\"" $USER $USER_PASS
    fi
}

function runCmd() {
    CMD="$1"
    USER="$2"
    USER_PASS="$3"
    if [ -z ${USER} ] ; then
        ${CMD}
    else
        if [ -z ${USER_PASS} ] ; then
            su ${USER} -c "${CMD}"
        else
            echo ${USER_PASS} | su ${USER} -c "echo ${USER_PASS} | sudo -S ${CMD}"
        fi
    fi

}

print_help
addNode ${NODE_HOST_NAME} ${HOST_NB_PROCS} ${HOST_USER} ${HOST_PASS}
