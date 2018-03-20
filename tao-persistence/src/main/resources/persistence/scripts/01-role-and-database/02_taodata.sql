-- Create DB: taodata
CREATE DATABASE taodata
  WITH OWNER = tao
       ENCODING = 'UTF-8'
       TABLESPACE = pg_default
       CONNECTION LIMIT = -1;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";