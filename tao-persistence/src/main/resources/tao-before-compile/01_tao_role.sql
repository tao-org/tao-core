-- Role: tao
CREATE ROLE "{tao_install_user}" WITH LOGIN PASSWORD '{tao_install_password}'
NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOREPLICATION;

