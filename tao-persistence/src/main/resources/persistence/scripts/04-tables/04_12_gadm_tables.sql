-- Table: gadm.countries
DROP TABLE IF EXISTS gadm.countries;
CREATE TABLE IF NOT EXISTS gadm.countries
(
    iso3 character varying(3) NOT NULL,
    name character varying(100) NOT NULL,
    lon double precision NOT NULL,
    lat double precision NOT NULL,
    geom geography(Geometry, 4326) NOT NULL,
    enabled boolean NOT NULL DEFAULT true
);
ALTER TABLE gadm.countries ADD CONSTRAINT PK_countries PRIMARY KEY (iso3);

-- Table: gadm.adm_l1
DROP TABLE IF EXISTS gadm.adm_l1;
CREATE TABLE IF NOT EXISTS gadm.adm_l1
(
    id character varying(80) NOT NULL,
    name character varying(100) NOT NULL,
    country_iso3 character varying(3) NOT NULL,
    geom geography(Geometry, 4326) NOT NULL
);
ALTER TABLE gadm.adm_l1 ADD CONSTRAINT u_gid_1 PRIMARY KEY (id);
ALTER TABLE gadm.adm_l1 ADD CONSTRAINT fk_adm_l1_countries FOREIGN KEY (country_iso3)
    REFERENCES gadm.countries (iso3) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;

-- Table: gadm.adm_l2
DROP TABLE IF EXISTS gadm.adm_l2;
CREATE TABLE IF NOT EXISTS gadm.adm_l2
(
    id character varying(80) NOT NULL,
    name character varying(100) NOT NULL,
    l1_id character varying(80) NOT NULL,
    geom geography(Geometry, 4326) NOT NULL
);
ALTER TABLE gadm.adm_l2 ADD CONSTRAINT u_gid_2 PRIMARY KEY (id);
ALTER TABLE gadm.adm_l2 ADD CONSTRAINT fk_adm_l2_adm_l1 FOREIGN KEY (l1_id)
    REFERENCES gadm.adm_l1 (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;