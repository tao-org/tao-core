-- Processing components visibility
INSERT INTO component.component_visibility (visibility) VALUES
('SYSTEM'), ('USER'), ('CONTRIBUTOR');

-- Processing components type
INSERT INTO component.processing_component_type (id, type) VALUES
(1, 'EXECUTABLE'), (2, 'SCRIPT'), (3, 'AGGREGATE'), (4, 'EXTERNAL'), (5, 'UTILITY');

-- Processing components category
INSERT INTO component.component_category (id, category) VALUES
(1, 'RASTER'), (2, 'VECTOR'), (3, 'OPTICAL'), (4, 'RADAR'), (5, 'MISC');

-- Tag type
INSERT INTO common.tag_type (id, description) VALUES
(1, 'TOPOLOGY_NODE'), (2, 'COMPONENT'), (3, 'DATASOURCE'), (4, 'WORKFLOW');

-- Condition
INSERT INTO component.condition (id, description) VALUES
(1, 'Equals'), (2, 'Not equals'), (3, 'Less than'), (4, 'Less than or equal'), (5, 'Greater than'), (6, 'Greater than or equals'), (7, 'In'), (8, 'Not in');

-- Container type
INSERT INTO component.container_type (id, description) VALUES
(1, 'DOCKER'), (2, 'WPS'), (3, 'UTILITY'), (4, 'STAC');

-- Data formats
INSERT INTO product.data_format (type) VALUES
('RASTER'), ('VECTOR'), ('OTHER'), ('DB_CONNECTION'), ('FOLDER'),('JSON');

-- Data types
INSERT INTO product.data_type (type) VALUES
('String'), ('Integer'), ('Double'), ('Short'), ('Date'), ('Polygon2D');

-- Data formats
INSERT INTO product.product_status (status) VALUES
('QUERIED'), ('DOWNLOADING'), ('DOWNLOADED'), ('FAILED'), ('PRODUCED');

-- Execution status
INSERT INTO execution.status (description) VALUES
('UNDETERMINED'), ('QUEUED_ACTIVE'), ('RUNNING'), ('SUSPENDED'), ('DONE'), ('FAILED'), ('CANCELLED'), ('PENDING_FINALISATION');

-- Fetch mode
INSERT INTO component.fetch_mode (fetch_mode) VALUES
('OVERWRITE'), ('RESUME'), ('COPY'), ('SYMLINK'), ('CHECK');

-- Orbit direction
INSERT INTO product.orbit_direction (direction) VALUES
('ASCENDING'), ('DESCENDING');

-- Parameter type
INSERT INTO component.parameter_type (type) VALUES
('RegularParameter'), ('TemplateParameter');

-- Pixel types
INSERT INTO product.pixel_type (type) VALUES
('UINT8'), ('INT8'), ('UINT16'), ('INT16'), ('UINT32'), ('INT32'), ('FLOAT32'), ('FLOAT64');

-- Sensor types
INSERT INTO product.sensor_type (type) VALUES
('OPTICAL'), ('RADAR'), ('ALTIMETRIC'), ('ATMOSPHERIC'), ('UNKNOWN'), ('PASSIVE_MICROWAVE');

-- Service status
INSERT INTO  topology.service_status (status) VALUES
('NOT_FOUND'), ('INSTALLED'), ('UNINSTALLED'), ('ERROR');

-- Data Query parameters
-- common parameters
INSERT INTO workflow.query_parameter (data_type_id, name) VALUES
(1, 'platformName'), (5, 'startDate'), (5, 'endDate'), (6, 'footprint'), (1, 'productType'), (1, 'relativeOrbit'),
-- Radar parameters
(1, 'polarisation'), (1, 'sensorOperationalMode'),
-- Optical parameters
(3, 'cloudCover');

-- Template type
INSERT INTO component.template_type (type) VALUES
('VELOCITY'), ('JAVASCRIPT'), ('XSLT');

-- Component type
INSERT INTO workflow.component_type (id, description) VALUES
(1, 'DATASOURCE'), (2, 'PROCESSING'), (3, 'GROUP'), (4, 'DATASOURCE_GROUP');

-- Behavior
INSERT INTO workflow.node_behavior (id, description) VALUES
(1, 'FAIL_ON_ERROR'), (2, 'CONTINUE_ON_ERROR');

-- Workflow graph status
INSERT INTO workflow.status (description) VALUES
('DRAFT'), ('READY'), ('PUBLISHED');

-- Workflow graph visibility
INSERT INTO common.visibility (visibility) VALUES
('PUBLIC'), ('PRIVATE');

-- Groups
-- admin group
INSERT INTO usr.grp (name, input_quota, processing_quota) VALUES ('ADMIN', -1, -1);
-- operator group
INSERT INTO usr.grp (name, input_quota, processing_quota) VALUES ('USER', 100, 100);

-- User status
INSERT INTO usr.user_status (status) VALUES
('PENDING'), ('ACTIVE'), ('DISABLED');
-- User type
INSERT INTO usr.user_type (id, type) VALUES
(1, 'LOCAL'), (2, 'LDAP'), (3, 'KEYCLOAK');

-- Users
INSERT INTO usr."user" (username, password, email, alternative_email, last_name, first_name, input_quota, actual_input_quota, processing_quota, actual_processing_quota, cpu_quota, memory_quota, organization, status_id, user_type_id, created)
VALUES ('admin', 'QxdxrJkXAQMN5nT9+dBWLg==', 'admin@tao.org', null, 'Administrator', 'Account', -1, -1, -1, -1, -1, -1, 'TAO', 2, 1, CURRENT_TIMESTAMP);

INSERT INTO usr."user" (username, password, email, alternative_email, last_name, first_name, input_quota, actual_input_quota, processing_quota, actual_processing_quota, cpu_quota, memory_quota, organization, status_id, user_type_id, created)
VALUES ('SystemAccount', 'TSEBHrrH9Uo+O69A5nzq9A==', 'admin@tao.org', null, 'System', 'Account', -1, -1, -1, -1, -1, -1, 'TAO', 2, 1, CURRENT_TIMESTAMP);

-- User groups
INSERT INTO usr.user_group (user_id, group_id) VALUES (1, 1);
INSERT INTO usr.user_group (user_id, group_id) VALUES (2, 1);

-- Node flavor
INSERT INTO topology.node_flavor (id, cpu, memory, disk, swap, rxtx)
VALUES ('master', 8, 32, 1024, 32, 1.0);

-- localhost execution node
INSERT INTO topology.node (id, username, password, flavor_id, description, role)
VALUES ('localhost', '', '', 'master', 'Master Node', 'master');

-- Naming rules
INSERT INTO product.naming_rule (id, sensor, regex, description) VALUES
(1, 'Sentinel2', '(S2[A-B])_(MSIL1C|MSIL2A)_(\d{8})T(\d{6})_(N\d{4})_R(\d{3})_T(\d{2}\w{3})_(\d{8}T\d{6})(?:.SAFE)?', 'Sentinel-2 L1C and L2A product naming'),
(2, 'Sentinel1', '(S1[A-B])_(S[1-6]|IW|EW|WV)_(SLC|GRD|RAW|OCN)([FHM_])_([0-2])([AS])(SH|SV|DH|DV)_(\d{8})T(\d{6})_(\d{8})T(\d{6})_(\d{6})_([0-9A-F]{6})_([0-9A-F]{4})(?:.SAFE)?', 'Sentinel-1 L1 product naming'),
(3, 'Landsat8', '(L\w\d{2})_(L[1-2]\w{2})_(\d{3})(\d{3})_(\d{8})_(\d{8})_(\d{2})_(\w{2})', 'Landsat-8 L1 product naming'),
(4, 'GeoTIFF', '((?:[A-Za-z0-9_]*?(?=\d{8}))((\d{4})(\d{2})(\d{2}))?(?:[A-Za-z0-9_]*))\.(tif|TIF|tiff|TIFF+)', 'Generic GeoTIFF product'),
(5, 'Generic', '((?:[A-Za-z0-9_]*?(?=\d{8}))((\d{4})(\d{2})(\d{2}))?(?:[A-Za-z0-9_]*))\.([A-Za-z.]+)', 'Generic product');

INSERT INTO product.naming_rule_token (naming_rule_id, token_name, matching_group_number, description) VALUES
(1, 'MISSION', 1, 'Mission identifier'),
(1, 'LEVEL', 2, 'Product level'),
(1, 'ADATE', 3, 'Acquisition date'),
(1, 'ATIME', 4, 'Acquisition time'),
(1, 'BASELINE', 5, 'Processing baseline'),
(1, 'ORBIT', 6, 'Relative orbit'),
(1, 'TILE', 7, 'UTM tile'),
(1, 'DISCRIMINATOR', 8, 'Product discriminator');

INSERT INTO product.naming_rule_token (naming_rule_id, token_name, matching_group_number, description) VALUES
(2, 'MISSION', 1, 'Mission identifier'),
(2, 'MODE', 2, 'Mode'),
(2, 'TYPE', 3, 'Product type'),
(2, 'RESOLUTION', 4, 'Resolution class'),
(2, 'LEVEL', 5, 'Processing level'),
(2, 'CLASS', 6, 'Product class'),
(2, 'POLARISATION', 7, 'Polarisation'),
(2, 'STARTDATE', 8, 'Acquisition start date'),
(2, 'STARTTIME', 9, 'Acquisition start time'),
(2, 'STOPDATE', 10, 'Acquisition stop date'),
(2, 'STOPTIME', 11, 'Acquisition stop time'),
(2, 'ORBIT', 12, 'Absolute orbit'),
(2, 'DATATAKE', 13, 'Datatake identifier'),
(2, 'DISCRIMINATOR', 14, 'Product discriminator');

INSERT INTO product.naming_rule_token (naming_rule_id, token_name, matching_group_number, description) VALUES
(3, 'MISSION', 1, 'Mission identifier'),
(3, 'LEVEL', 2, 'Processing level'),
(3, 'PATH', 3, 'Acquisition path'),
(3, 'ROW', 4, 'Acquisition row'),
(3, 'ADATE', 5, 'Acquisition date'),
(3, 'PDATE', 6, 'Processing date'),
(3, 'COLLECTION', 7, 'Collection number'),
(3, 'CATEGORY', 8, 'Collection category');

INSERT INTO product.naming_rule_token (naming_rule_id, token_name, matching_group_number, description) VALUES
(4, 'NAME', 1, 'Product name'),
(4, 'DATE', 2, 'Product date'),
(4, 'YEAR', 3, 'Product year'),
(4, 'MONTH', 4, 'Product month'),
(4, 'DAY', 5, 'Product day'),
(4, 'EXT', 6, 'Product file extension');

INSERT INTO product.naming_rule_token (naming_rule_id, token_name, matching_group_number, description) VALUES
(5, 'NAME', 1, 'Product name'),
(5, 'DATE', 2, 'Product date'),
(5, 'YEAR', 3, 'Product year'),
(5, 'MONTH', 4, 'Product month'),
(5, 'DAY', 5, 'Product day'),
(5, 'EXT', 6, 'Product file extension');

-- Repository types
INSERT INTO workspace.repository_type (id, name, params) VALUES
(1, 'Local repository', '{"root":true}'),
(2, 'AWS S3 repository', '{"aws.region":true,"aws.access.key":false,"aws.secret.key":false,"aws.bucket":true}'),
(3, 'OpenStack Swift S3 repository', '{"openstack.auth.url":true,"openstack.tenantId":true,"openstack.domain":true,"openstack.user":true,"openstack.password":true,"openstack.bucket":true}'),
(4, 'STAC repository', '{"stac.url":true, "page.size":false}'),
(5, 'Network share repository', '{"smb.server":true,"smb.domain":false,"smb.share":true,"smb.user":false,"smb.password":false}'),
(6, 'FTP repository', '{"ftp.server":true,"ftp.user":false,"ftp.password":false}'),
(7, 'FTPS repository', '{"ftps.server":true,"ftps.user":false,"ftps.password":false}');

-- Repository templates
INSERT INTO workspace.repository_template (name, description, url_prefix, repository_type_id, params) VALUES
('CCI', 'CCI LandCover dataset', 'ftp', 6, '{"ftp.server":"geo10.elie.ucl.ac.be","ftp.user":"anonymous"}'),
('S2_L2A_COG', 'This dataset is the same as the Sentinel-2 dataset, except the JP2K files were converted into Cloud-Optimized GeoTIFFs (COGs).', 's3', '2', '{"aws.region":"us-west-2","aws.access.key":null,"aws.secret.key":null,"aws.bucket":"sentinel-cogs"}'),
('GeoMAD', 'GeoMAD is the Digital Earth Africa (DE Africa) surface reflectance geomedian and triple Median Absolute Deviation data service. It is a cloud-free composite of satellite data compiled over specific timeframes.', 's3', '2', '{"aws.region":"af-south-1","aws.access.key":null,"aws.secret.key":null,"aws.bucket":"deafrica-services/gm_s2_annual"}'),
('DE_Africa_S1', 'DE Africa provides Sentinel-1 backscatter as Radiometrically Terrain Corrected (RTC) gamma-0 (γ0) where variation due to changing observation geometries has been mitigated.', 's3', '2', '{"aws.region":"af-south-1","aws.access.key":null,"aws.secret.key":null,"aws.bucket":"deafrica-sentinel-1"}'),
('DE_Africa_S2_L2A', 'The Digital Earth Africa Sentinel-2 dataset contains Level-2A data of the African continent. Digital Earth Africa does not host any lower-level Sentinel-2 data. Note that this data is a subset of the Sentinel-2 COGs dataset.', 's3', '2', '{"aws.region":"af-south-1","aws.access.key":null,"aws.secret.key":null,"aws.bucket":"deafrica-sentinel-2"}'),
('DE_Africa_Landsat_L2', 'DE Africa provides data from Landsat 5, 7 and 8 satellites, including historical observations dating back to late 1980s and regularly updated new acquisitions. New Level-2 Landsat 7 and Landsat 8 data are available after 15 to 27 days from acquisition.', 's3', '2', '{"aws.region":"af-south-1","aws.access.key":null,"aws.secret.key":null,"aws.bucket":"deafrica-landsat"}'),
('DE_Africa_Crop', 'Digital Earth Africa cropland extent map (2019) shows the estimated location of croplands in Africa for the period January to December 2019.', 's3', '2', '{"aws.region":"af-south-1","aws.access.key":null,"aws.secret.key":null,"aws.bucket":"deafrica-services/crop_mask"}'),
('WorldCover', 'The European Space Agency (ESA) WorldCover is a global land cover map with 11 different land cover classes produced at 10m resolution based on combination of both Sentinel-1 and Sentinel-2 data.', 's3', '2', '{"aws.region":"eu-central-1","aws.access.key":null,"aws.secret.key":null,"aws.bucket":"esa-worldcover"}'),
('EBD_S1_BCK_COHE', 'This data set is the first-of-its-kind spatial representation of multi-seasonal, global SAR repeat-pass interferometric coherence and backscatter signatures. Global coverage comprises all land masses and ice sheets from 82 degrees northern to 79 degress southern latitude. The data set is derived from high-resolution multi-temporal repeat-pass interferometric processing of about 205,000 Sentinel-1 Single-Look-Complex data acquired in Interferometric Wide-Swath mode (Sentinel-1 IW mode) from 1-Dec-2019 to 30-Nov-2020.', 's3', '2', '{"aws.region":"us-west-2","aws.access.key":null,"aws.secret.key":null,"aws.bucket":"sentinel-1-global-coherence-earthbigdata/data/tiles"}'),
('EO_Cubes_Brazil', 'Earth observation (EO) data cubes produced from analysis-ready data (ARD) of CBERS-4, Sentinel-2 A/B and Landsat-8 satellite images for Brazil. The datacubes are regular in time and use a hierarchical tiling system.', 's3', '2', '{"aws.region":"us-west-2","aws.access.key":null,"aws.secret.key":null,"aws.bucket":"bdc-sentinel-2"}'),
('S3', 'This data set consists of observations from the Sentinel-3 satellite of the European Commission’s Copernicus Earth Observation Programme.', 's3', '2', '{"aws.region":"eu-central-1","aws.access.key":null,"aws.secret.key":null,"aws.bucket":"meeo-s3/NRT"}'),
('S5P_L2', 'This data set consists of observations from the Sentinel-5 Precursor (Sentinel-5P) satellite of the European Commission’s Copernicus Earth Observation Programme.', 's3', '2', '{"aws.region":"eu-central-1","aws.access.key":null,"aws.secret.key":null,"aws.bucket":"meeo-s5p/NRTI"}'),
('S1_SLC_Germany', 'In its current first stage, the dataset covers the entirety of Germany and is being updated continuously. As a next stage, the dataset will provide up-to-date coverage of the sentinel-1 SLC data over Europe.', 's3', '2', '{"aws.region":"eu-west-1","aws.access.key":null,"aws.secret.key":null,"aws.bucket":"sentinel1-slc"}'),
('DE_Africa_WOfS', 'Water Observations from Space (WOfS) is a service that draws on satellite imagery to provide historical surface water observations of the whole African continent. WOfS allows users to understand the location and movement of inland and coastal water present in the African landscape. It shows where water is usually present; where it is seldom observed; and where inundation of the surface has been observed by satellite. They are generated using the WOfS classification algorithm on Landsat satellite data.', 's3', '2', '{"aws.region":"af-south-1","aws.access.key":null,"aws.secret.key":null,"aws.bucket":"deafrica-services/wofs_ls"}'),
('DE_Africa_FC', 'Fractional cover (FC) describes the landscape in terms of coverage by green vegetation, non-green vegetation (including deciduous trees during autumn, dry grass, etc.) and bare soil. It provides insight into how areas of dry vegetation and/or bare soil and green vegetation are changing over time. The product is derived from Landsat satellite data, using an algorithm developed by the Joint Remote Sensing Research Program.', 's3', '2', '{"aws.region":"af-south-1","aws.access.key":null,"aws.secret.key":null,"aws.bucket":"deafrica-services/fc_ls"}'),
('NDUI_2000', 'NDUI is combined with cloud shadow-free Landsat Normalized Difference Vegetation Index (NDVI) composite and DMSP/OLS Night Time Light (NTL) to characterize global urban areas at a 30 m resolution,and it can greatly enhance urban areas, which can then be easily distinguished from bare lands including fallows and deserts.', 's3', '2', '{"aws.region":"us-west-2","aws.access.key":null,"aws.secret.key":null,"aws.bucket":"qinglinglab-ndui-2000"}'),
('NASA_NEX', 'A collection of Earth science datasets maintained by NASA, including climate change projections and satellite images of the Earth surface.', 's3', '2', '{"aws.region":"us-west-2","aws.access.key":null,"aws.secret.key":null,"aws.bucket":"nasanex/NEX-DCP30"}'),
('RADARSAT-1', 'Developed and operated by the Canadian Space Agency, it is Canada''s first commercial Earth observation satellite.', 's3', '2', '{"aws.region":"ca-central-1","aws.access.key":null,"aws.secret.key":null,"aws.bucket":"radarsat-r1-l1-cog"}'),
('DE_Africa_SAR', 'The ALOS/PALSAR annual mosaic is a global 25 m resolution dataset that combines data from many images captured by JAXA’s PALSAR and PALSAR-2 sensors on ALOS-1 and ALOS-2 satellites respectively. This product contains radar measurement in L-band and in HH and HV polarizations. It has a spatial resolution of 25 m and is available annually for 2007 to 2010 (ALOS/PALSAR) and 2015 to 2020 (ALOS-2/PALSAR-2). The JERS annual mosaic is generated from images acquired by the SAR sensor on the Japanese Earth Resources Satellite-1 (JERS-1) satellite. This product contains radar measurement in L-band and HH polarization. It has a spatial resolution of 25 m and is available for 1996. This mosaic data is part of a global dataset provided by the Japan Aerospace Exploration Agency (JAXA) Earth Observation Research Center.', 's3', '2', '{"aws.region":"af-south-1","aws.access.key":null,"aws.secret.key":null,"aws.bucket":"deafrica-input-datasets/alos_palsar_mosaic"}'),
('CAPELLA_SAR', 'Capella Space Open Data in COG format', 's3', '2', '{"aws.region":"us-west-2","aws.access.key":null,"aws.secret.key":null,"aws.bucket":"capella-open-data/data"}'),
('MSPC', 'Microsoft Planetary Computer catalog of open data', 'stac', '4', '{"stac.url":"https://planetarycomputer.microsoft.com/api/stac/v1", "page.size":"1000"}');
