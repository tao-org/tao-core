CREATE OR REPLACE FUNCTION usr.login(IN usr text, IN pwd text) RETURNS integer AS
$func$
    SELECT id FROM usr.user WHERE username = USR and password = crypt(pwd, usr);
$func$
LANGUAGE sql VOLATILE NOT LEAKPROOF;

ALTER FUNCTION usr.login(IN text, IN text) OWNER TO tao;