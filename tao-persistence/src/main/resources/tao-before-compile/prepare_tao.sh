#prepare role for tao DB
#default user:tao_install_user
#default passwd:tao_install_passwod
myPh=$(pwd)


if [ $# -le 0 ]; then 
echo minimum 1 arg required; 
echo -u user; 
echo -p password; 
echo -f create file ~/.pgpass;
echo -r create role;
echo -d create DB; 

exit 1; 
fi

while getopts ":u:p:d f r" opt; do
  case $opt in
    u)
	user=$OPTARG
      ;;
    p)
	passwd=$OPTARG
      ;;
    f)
	filename=".pgpass"
	if [ -f "$HOME/$filename" ];then
		echo I will try to use what is set in "$HOME/$filename"
		user=$(awk -F":" '{print $(NF-1)}' "$HOME/$filename")
		passwd=$(awk -F":" '{print $(NF)}' "$HOME/$filename")
	else
		##!!!
		echo "localhost:5432:*:$user:$passwd" > "$HOME/$filename"
	fi
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      exit 2
      ;;
    :)
      echo "Option -$OPTARG requires an argument." >&2
      exit 2
      ;;
    r)
	userExist=$(sudo -u postgres psql -c "SELECT 1 FROM pg_roles WHERE rolname='${user}'" 2>/dev/null)
	if [ "$userExist" != "1" ]; then
	               #prepare_tao_role.sql
                sed -i "s/{tao_install_user}/${user}/g" prepare_tao_role.sql
                sed -i "s/{tao_install_password}/${passwd}/g" prepare_tao_role.sql
                ### !!! sudo password required
                cat prepare_tao_role.sql|sudo -u postgres psql 
	fi
	;;

    d)
        cat tao_db.sql|sudo -u postgres psql 
	;;
  esac
done
shift $((OPTIND -1 ))

#echo "$user:$passwd"
### update where required with new user/password
sed -i "s/{tao_install_user}/${user}/g" ../build_scripts/db.conf
#sed -i "s/{tao_install_user}/${user}/g" ../build_scripts/uninstall_db.sh
sed -i "s/{tao_install_user}/${user}/g" ../persistence/scripts/01-role-and-database/01_tao_role.sql
sed -i "s/{tao_install_password}/${passwd}/g" ../persistence/scripts/01-role-and-database/01_tao_role.sql

