#!/bin/bash

PRINT_HELP="no"

while [[ $# -gt 0 ]]; do
    key="$1"
    case $key in
        -s|--hostname)
        shift
        NODE_HOST_NAME="$1"
        ;;
        -p|--ip)
        shift
        NODE_IP_ADDR="$1"
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
        echo "  -s, --hostname          execution node hostname to be added to /etc/hosts"
        echo "  -p, --ip                execution node ip address to be added to /etc/hosts"

        exit
    fi
}

function updateHosts() {
    TARGET_NODE_NAME=$1
    TARGET_IP_ADDR=$2
    # Check the /etc/hosts file
    echo "Checking host for the node $TARGET_NODE_NAME ..."
    HOSTNAME_LINES=`grep '/etc/hosts' -e "$TARGET_NODE_NAME"`
    echo "UpdateHosts:      ${HOSTNAME_LINES}"
    if [ -z "${HOSTNAME_LINES}" ] ; then
        if [ -z ${TARGET_IP_ADDR} ] ; then
            echo "Host name for node ${TARGET_NODE_NAME} is not set in the /etc/hosts file and no IP address is also provided. Please add it manually!"
        else
            echo "Adding to /etc/hosts the host ${TARGET_NODE_NAME} with IP ${TARGET_IP_ADDR}!"
            echo "${TARGET_IP_ADDR} ${TARGET_NODE_NAME}" >> /etc/hosts
        fi
    else
        echo "Host name for node ${TARGET_NODE_NAME} is correctly set."
    fi
}

print_help
updateHosts ${NODE_HOST_NAME} ${NODE_IP_ADDR}