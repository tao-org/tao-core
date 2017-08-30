#!/bin/bash

PRINT_HELP="no"

while [[ $# -gt 0 ]]; do
    key="$1"
    case $key in
        -s|--hostname)
        shift
        NODE_HOST_NAME="$1"
        ;;
        -p|--procs_cnt)
        shift
        NODE_PROCESSORS_CNT="$1"
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
        echo "  -s, --hostname          the hostname of the execution node to be added in /var/spool/torque/server_priv/nodes"
        echo "  -p, --procs_cnt         the number of processors for the node to be added in /var/spool/torque/server_priv/nodes"

        exit
    fi

}

function addTorqueNode() {
    NODE_HOST_NAME=$1
    NODE_PROCESSORS_CNT=$2
    # Update the server_priv/nodes file
    SERVER_PRIV_CUR_NODE_LINE=`sudo grep '/var/spool/torque/server_priv/nodes' -e "$NODE_HOST_NAME"`
    if [ -z "${SERVER_PRIV_CUR_NODE_LINE}" ] ; then
        echo "Adding line $NODE_HOST_NAME np=$NODE_PROCESSORS_CNT to /var/spool/torque/server_priv/nodes"
        qmgr -c "create node $NODE_HOST_NAME np=$NODE_PROCESSORS_CNT"
#            if [ -f "/var/spool/torque/server_priv/nodes" ] ; then
#                sudo sh -c "echo \"$NODE_HOST_NAME np=$NODE_PROCESSORS_CNT\" >> /var/spool/torque/server_priv/nodes"
#            else
#                sudo sh -c "echo \"$NODE_HOST_NAME np=$NODE_PROCESSORS_CNT\" > /var/spool/torque/server_priv/nodes"
#            fi

        #systemctl stop pbs_mom
        systemctl restart pbs_server
        systemctl restart pbs_sched
        #systemctl start pbs_mom

    else
        echo "Nothing is added to /var/spool/torque/server_priv/nodes for node $NODE_HOST_NAME as it already exists."
        #qmgr -c "set node $NODE_HOST_NAME np=$NODE_PROCESSORS_CNT"
        fi
}

print_help
addTorqueNode ${NODE_HOST_NAME} ${NODE_PROCESSORS_CNT}