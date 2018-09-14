#prepare role for tao DB
#run:
  ./prepare_tao_role.sh -u tao_DB_install_role -p tao_DB_install_role_password -f -d

# update where required with new user/password
#../build_scripts/db.conf
#../persistence/scripts/01-role-and-database/01_tao_role.sql

# create superuser role for taodata DB and tao user creation 

