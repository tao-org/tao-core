-- Role: install user (build)
--CREATE ROLE tao_install_user WITH LOGIN PASSWORD 'tao_install_password' SUPERUSER;

--#-- Role: tao-service
CREATE ROLE "tao" WITH LOGIN PASSWORD 'tao';

-- DataBase Create: taodata
CREATE DATABASE taodata
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'en_US.UTF-8'
       LC_CTYPE = 'en_US.UTF-8'
       TEMPLATE = template0
       CONNECTION LIMIT = -1;

