#!/bin/bash

while [[ $# -gt 0 ]]; do
    key="$1"
    case $key in
        -d|--dir)
        shift 
        PACKAGE_DIR_NAME="$1"
        ;;
        -s|--server_hostname)
        shift 
        SERVER_HOST_NAME="$1"
        ;;
        -a|--server_ip_addr)
        shift 
        SERVER_IP_ADDR="$1"
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
    echo "Script for installing Torque on the master node or on the execution nodes"
    echo "optional arguments:"
    echo "  -h, --help              show this help message and exit"
    echo "  -d, --dir               directory where the Torque client packages are located"
    
    exit
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
            sudo sh -c "echo \"${TARGET_IP_ADDR} ${TARGET_NODE_NAME}\" >> /etc/hosts"
        fi
    else 
        echo "Host name for node ${TARGET_NODE_NAME} is correctly set."
    fi
}

function install_torque_client() {
    if [ -z ${PACKAGE_DIR_NAME+x} ]
    then 
        echo "Please provide location of the Torque installation package!"
        return
    fi
    if [ -z ${SERVER_HOST_NAME+x} ]
    then 
        echo "Please provide master node host name!"
        return
    fi

    yum install openssl-devel -y
     yum install hwloc -y
    
    echo "Installing torque-package-mom-linux ..."
    ${PACKAGE_DIR_NAME}/torque-package-mom-linux-*.sh --install
    echo "Installing torque-package-clients-linux ..."
    ${PACKAGE_DIR_NAME}/torque-package-clients-linux-*.sh --install
    
    #CURRENT_HOST_NAME=`hostname`
    #echo "$CURRENT_HOST_NAME" > /var/spool/torque/server_name
    echo "$SERVER_HOST_NAME" > /var/spool/torque/server_name
    
    echo "Echoing to /etc/ld.so.conf.d/torque.conf ..."
    echo /usr/local/lib > /etc/ld.so.conf.d/torque.conf
    ldconfig

    echo "Copying files to /etc/init.d/ ..."
    chmod a+x ${PACKAGE_DIR_NAME}/contrib/init.d/pbs_mom
    chmod a+x ${PACKAGE_DIR_NAME}/contrib/init.d/trqauthd
    
    cp ${PACKAGE_DIR_NAME}/contrib/init.d/pbs_mom /etc/init.d/
    cp ${PACKAGE_DIR_NAME}/contrib/init.d/trqauthd /etc/init.d/
    chkconfig trqauthd on
    chkconfig pbs_mom on

    echo "Restarting trqauthd ..."
    service trqauthd restart

    echo "Updating file /var/spool/torque/mom_priv/config ..."
    
    echo '$'pbsserver $SERVER_HOST_NAME > /var/spool/torque/mom_priv/config
    echo '$'logevent 255 >> /var/spool/torque/mom_priv/config
    #echo '$'usercp *:/home /home >> /var/spool/torque/mom_priv/config
    
    service pbs_mom restart
}

updateHosts ${SERVER_HOST_NAME} ${SERVER_IP_ADDR}

install_torque_client