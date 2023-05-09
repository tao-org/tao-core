#!/bin/bash
MyPwd=`pwd`

#pushd `dirname $0` > /dev/null
DB_CONF_DIR="."
#popd > /dev/null

function echoColored() {
    local txtToColor=""
    local color="black"
    NC='\033[0m' # No Color
    BOLD='\033[1m'
    local OPTIND
    while getopts 't:c:' arg
    do
        case ${arg} in
            t) txtToColor=${OPTARG};;
            c) color=${OPTARG};;
            *) echo "Invalid option ${arg}" ; return 1 # illegal option
        esac
    done

    case "$color" in
        black) color='\033[0;30m';;
        red) color='\033[0;31m';;
        blue) color='\033[0;34m' ;;
        green) color='\033[0;32m' ;;
        magenta) color='\033[0;35m' ;;
        *)
            echo $"Usage: $0 {black|red|blue|green|magenta}"
            color='\033[0;31m'
    esac
    
    if [ -n "${txtToColor}" ] ; then
        echo -e "${color}${BOLD}${txtToColor}${NC}"
    fi
}

function EnsurePsqlAccess() {
    # Check if we need to overwrite the port
#    local host port user password
#    
#    echo "Entering in EnsurePsqlAccess ..."
#    while getopts 'h:p:u:s:' arg
#    do
#        echo "Arg is ${arg}"
#        case ${arg} in
#            h) host=${OPTARG};;
#            p) port=${OPTARG};;
#            u) user=${OPTARG};;
#            s) password=${OPTARG};;            
#            *) echo "Invalid option ${arg}" ; return 1 # illegal option
#        esac
#    done
    
    echo "Reading file ./db.conf ..."
    readarray dbConfLines < "${DB_CONF_DIR}/db.conf"
    # write an empty line and create/reset the file
    echo "" > ~/temp_db.conf

    shopt -s extglob    
    dbConfHost=""
    dbConfPort=""
    dbConfUser=""
    dbConfPassFile=""
    for var in "${dbConfLines[@]}"
    do
        var=$(echo "${var}"|tr '\t\r\n' ' ')
        var="${var##*( )}"
        var="${var%%*( )}"
        
        echo "${var}"

        # Check if the line contains "PGHOST="
        searchstring="PGHOST="
        if [[ ${var} == *$searchstring* ]]; then
            value=${var#*$searchstring}
            echo "The value is $value"
            if [ "$host" != "" -a "$value" != "$host" ] ; then
                dbConfHost="$host"
            else
                dbConfHost="$value"
            fi
        fi
        
        # Check if the line contains "PGPORT="
        searchstring="PGPORT="
        if [[ ${var} == *$searchstring* ]]; then
            value=${var#*$searchstring}
            echo "The value is $value"
            if [ "$port" != "" -a "$value" != "$port" ] ; then
                dbConfPort="$port"
            else
                dbConfPort="$value"
            fi            
        fi

        # Check if the line contains "PGUSER="
        searchstring="PGUSER="
        if [[ ${var} == *$searchstring* ]]; then
            value=${var#*$searchstring}
            echo "The value is $value"
            if [ "$user" != "" -a "$value" != "$user" ] ; then
                dbConfUser="$user"
            else
                dbConfUser="$value"
            fi
        fi

        # Check if the line contains "PGPASSFILE="
        searchstring="PGPASSFILE="
        if [[ ${var} == *$searchstring* ]]; then
            value=${var#*$searchstring}
            echo "The value is $value"
            value=$(eval "echo $value")
            echo "Actual value is $value"
            if [ -f "$value" ] ; then
                echo "$value found."
                dbConfPassFile="$value"
            else
                echo "$value not found. Creating it in ${HOME}/.pgpass..."
                touch "${HOME}/.pgpass"
                chmod 0600 "${HOME}/.pgpass"
                dbConfPassFile="$value"
            fi
        fi

        # write the line into file        
        echo "${var}" >> ~/temp_db.conf
        # do something on $var
    done
    echo "PGHOST=${dbConfHost}" > ~/temp_db.conf
    echo "PGPORT=${dbConfPort}" >> ~/temp_db.conf
    echo "PGUSER=${dbConfUser}" >> ~/temp_db.conf
    echo "PGPASSFILE=${dbConfPassFile}" >> ~/temp_db.conf

    #strToFind="${dbConfHost}:${dbConfPort}:*:${dbConfUser}:${password}"
    strToFind="${dbConfHost}:${dbConfPort}:*:${dbConfUser}:"
    if grep -Fq "${strToFind}" "${dbConfPassFile}"; then
        echo "Line ${strToFind} found in file ... nothing to do"
    else
        echo "Line ${strToFind} not found in file. Adding it ..."
        while [[ $password == '' ]] # While empty...
        do
            
            echoColored -t "Please provide the ${dbConfUser} password, followed by [ENTER]:" "green"
            read password
        done         
        strToWrite="${dbConfHost}:${dbConfPort}:*:${dbConfUser}:${password}"
        echo "${strToWrite}" >> "${dbConfPassFile}"
        if grep -Fq "${strToWrite}" "${dbConfPassFile}"; then
            echo "The line ${strToWrite} was successfully added to file ${dbConfPassFile}"
        else
            echo "Error writing line ${strToWrite} into file ${dbConfPassFile}. Postgresql commands might not work or will request password at each externalStep!!!"
        fi
    fi
}

function exportPGVariables() {
    EnsurePsqlAccess 
     
    . ~/temp_db.conf
    export PGHOST PGPORT PGUSER PGPASSFILE
}

function executeSqlScript {
    SCRIPT_NAME=$1 
    DB_NAME=$2 

    #echo "Port number is : $PSQL_PORT_NO, Database name: $DB_NAME, Script name: $SCRIPT_NAME"
    if [ "$DB_NAME" != "" ]; then
        #cat "$SCRIPT_NAME" | sudo su - postgres -c "psql --dbname=$DB_NAME --port=$PSQL_PORT_NO"
        echo "Executing psql -f $SCRIPT_NAME -d $DB_NAME"
        psql -f "$SCRIPT_NAME" -d "$DB_NAME"
    else 
        #cat "$SCRIPT_NAME" | sudo su - postgres -c "psql --port=$PSQL_PORT_NO"
        echo "Executing psql -f $SCRIPT_NAME"
        psql -f "$SCRIPT_NAME"
    fi
}  

function executeSqlCommand {
    CMD=$1 
    DB_NAME=$2 
    if [ -z "$2" ] || [ $DB_NAME == "" ]; then
        #echo "$CMD" | sudo su - postgres -c "psql --port=$PSQL_PORT_NO"
        echo "Executing echo $CMD | psql"
        echo "$CMD" | psql
    else 
        #echo "$CMD" | sudo su - postgres -c "psql --dbname=$DB_NAME --port=$PSQL_PORT_NO"
        echo "Executing echo $CMD | psql -d $DB_NAME"
        echo "$CMD" | psql -d "$DB_NAME"
    fi
}  

function executeSqlScriptsFromFile {  
    FILE_NAME=$1
    SQL_SCRIPTS_ROOT=$2
    
    while IFS=';' read -r col1 col2
    do 
        TRIMMED_COL1=$(echo $col1 | tr -d '\r')
        TRIMMED_COL2=$(echo $col2 | tr -d '\r')

        ### Trim leading whitespaces ###
        TRIMMED_COL1="${TRIMMED_COL1##*( )}"
        ### trim trailing whitespaces  ##
        TRIMMED_COL1="${TRIMMED_COL1%%*( )}"
    
        ### Trim leading whitespaces ###
        TRIMMED_COL2="${TRIMMED_COL2##*( )}"
        ### trim trailing whitespaces  ##
        TRIMMED_COL2="${TRIMMED_COL2%%*( )}"
        
        if [ ! -z "$TRIMMED_COL1" -a "$TRIMMED_COL1" != "" ]; then
            if [ -z "$TRIMMED_COL2" ] || [ "$TRIMMED_COL2" == "" ]; then
                #echo "Executing 1 : $SQL_SCRIPTS_ROOT/${TRIMMED_COL1}"
                executeSqlScript "$SQL_SCRIPTS_ROOT/${TRIMMED_COL1}" ""
            else
                #echo "Executing 2 : $SQL_SCRIPTS_ROOT/${TRIMMED_COL1} and ${TRIMMED_COL2}"
                executeSqlScript "$SQL_SCRIPTS_ROOT/${TRIMMED_COL1}" "${TRIMMED_COL2}"
            fi
        fi
    done <"$FILE_NAME"
}
