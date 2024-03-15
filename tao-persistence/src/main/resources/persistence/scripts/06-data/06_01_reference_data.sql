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
(1, 'Equals'), (2, 'Not equals'), (3, 'Less than'), (4, 'Less than or equal'), (5, 'Greater than'), (6, 'Greater than or equals'), (7, 'In'), (8, 'Not in'), (9, 'Not set'), (10, 'Set');

-- Container type
INSERT INTO component.container_type (id, description) VALUES
(1, 'DOCKER'), (2, 'WPS'), (3, 'UTILITY'), (4, 'STAC'), (5, 'WMS');

-- Container visibility
INSERT INTO component.container_visibility (id, description) VALUES
(1, 'UNDEFINED'), (2, 'PRIVATE'), (3, 'PUBLIC');

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
('PUBLIC'), ('PRIVATE'), ('SUBSCRIPTION');

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
VALUES
('admin', 'QxdxrJkXAQMN5nT9+dBWLg==', 'admin@tao.org', null, 'Administrator', 'Account', -1, -1, -1, -1, -1, -1, 'TAO', 2, 1, CURRENT_TIMESTAMP),
('SystemAccount', 'TSEBHrrH9Uo+O69A5nzq9A==', 'system@tao.org', null, 'System', 'Account', -1, -1, -1, -1, -1, -1, 'TAO', 2, 1, CURRENT_TIMESTAMP);

-- User groups
INSERT INTO usr.user_group (user_id, group_id) VALUES ((SELECT id FROM usr."user" WHERE username='admin'), 1);
INSERT INTO usr.user_group (user_id, group_id) VALUES ((SELECT id FROM usr."user" WHERE username='SystemAccount'), 1);

-- Node flavor
INSERT INTO topology.node_flavor (id, cpu, memory, disk, swap, rxtx)
VALUES ('master', 8, 32, 1024, 32, 1.0);

-- localhost execution node
INSERT INTO topology.node (id, username, password, flavor_id, description, role)
VALUES ('localhost', '', '', 'master', 'Master Node', 'master');

-- Naming rules
INSERT INTO product.naming_rule (id, sensor, synonyms, regex, description) VALUES
(1, 'Sentinel2', 'S2MSI1C,S2MSI2A,L1C,L2A' , '(S2[A-B])_(MSIL1C|MSIL2A)_(\d{8})T(\d{6})_(N\d{4})_R(\d{3})_T(\d{2}\w{3})_(\d{8}T\d{6})(?:.SAFE)?', 'Sentinel-2 L1C and L2A product naming'),
(2, 'Sentinel1', 'GRD,SLC', '(S1[A-B])_(S[1-6]|IW|EW|WV)_(SLC|GRD|RAW|OCN)([FHM_])_([0-2])([AS])(SH|SV|DH|DV)_(\d{8})T(\d{6})_(\d{8})T(\d{6})_(\d{6})_([0-9A-F]{6})_([0-9A-F]{4})(?:.SAFE)?', 'Sentinel-1 L1 product naming'),
(3, 'Landsat8', 'L1T,L1GT,L1TP,L2SP', '(L\w\d{2})_(L[1-2]\w{2})_(\d{3})(\d{3})_(\d{8})_(\d{8})_(\d{2})_(\w{2})', 'Landsat-8 L1 product naming'),
(4, 'GeoTIFF', null, '((?:[A-Za-z0-9_]*?(?=\d{8}))((\d{4})(\d{2})(\d{2}))?(?:[A-Za-z0-9_]*))\.(tif|TIF|tiff|TIFF+)', 'Generic GeoTIFF product'),
(5, 'Generic', null, '((?:[A-Za-z0-9_]*?(?=\d{8}))((\d{4})(\d{2})(\d{2}))?(?:[A-Za-z0-9_]*))\.([A-Za-z.]+)', 'Generic product'),
(6, 'Sentinel5P', 'TROPOMI', '(S5P)_(NRTI|OFFL|RPRO)_(L1B_RA_BD1|L1B_RA_BD2|L1B_RA_BD3|L1B_RA_BD4|L1B_RA_BD5|L1B_RA_BD6|L1B_RA_BD7|L1B_RA_BD8|L1B_IR_UVN|L1B_IR_SIR|L2__AER_AI|L2__AER_LH|L2__CH4___|L2__CLOUD_|L2__CO____|L2__HCHO__|L2__NO2___|L2__NP_BD3|L2__NP_BD6|L2__NP_BD7|L2__O3____|L2__O3_TCL|L2__O3__PR|L2__SO2___|AUX_CTMANA|AUX_CTMFCT)_(\d{8})T(\d{6})_(\d{8})T(\d{6})_(\d{5})_(\d{2})_(\d{6})_(\d{8})T(\d{6})', 'Sentinel-5P product naming');

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

INSERT INTO product.naming_rule_token (naming_rule_id, token_name, matching_group_number, description) VALUES
(6, 'MISSION', 1, 'Mission identifier'),
(6, 'CLASS', 2, 'Product class'),
(6, 'TYPE', 3, 'Product type'),
(6, 'ADATESTART', 4, 'Sensing start date'),
(6, 'ATIMESTART', 5, 'Sensing start time'),
(6, 'ADATESTOP', 6, 'Sensing stop date'),
(6, 'ATIMESTOP', 7, 'Sensing stop time'),
(6, 'ORBIT', 8, 'Absolute orbit'),
(6, 'COLLECTION', 9, 'Collection number'),
(6, 'TOOLKIT', 10, 'Toolkit version number'),
(6, 'PDATE', 11, 'Processing date'),
(6, 'PTIME', 12, 'Processing time');

-- Repository types
INSERT INTO workspace.repository_type (id, name, params) VALUES
(1, 'Local repository', '{"root":true}'),
(2, 'AWS S3 repository', '{"aws.region":true,"aws.access.key":false,"aws.secret.key":false,"aws.bucket":true,"aws.endpoint":false}'),
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

-- Configuration categories
INSERT INTO config.category (id, name, display_order) VALUES
(1, 'General', 1),
(2, 'Topology', 2),
(3, 'Containers', 3),
(4, 'Execution', 4),
(5, 'Email', 5),
(6, 'OpenStack', 6),
(7, 'Workspaces', 7),
(8, 'Data Sources', 8),
(9, 'Kubernetes', 9);

-- Configuration items
INSERT INTO config.config (category_id, id, value, friendly_name, type, label, "values", last_updated) VALUES
(1, 'allow.resumable.transfers', 'true', 'Enables persistence of transfer state', 'bool', 'Transfers can be resumed ?', null, now()),
(1, 'quicklook.extension', '.png', 'Extension for product quicklooks', 'string', 'Quicklook extension', null, now()),
(1, 'extract.statistics', 'true', 'Include raster statistics in produced metadata', 'bool', 'Extract statistics', null, now()),
(1, 'api.token', '13cd8418-0b76-4346-b89b-2f5d7f5a5959', 'API token for development purposes', 'string', 'API token', null, now()),
(1, 'serialize.downloads', 'true', 'Perform downloads sequentially', 'bool', 'Sequential downloads', null, now()),
(1, 'replace.local.symlinks', 'true', 'Replace symlinks to products with actual products', 'bool', 'Replace symlinks when processing', null, now()),
(2, 'topology.node.poll.interval', '15', 'Time (in seconds) between node polling requests', 'int', 'Node polling interval (s)', null, now()),
(2, 'topology.master.user', 'taouser', 'User with privileges on the master node', 'string', 'Master node user', null, now()),
(2, 'topology.master.password', 'taopassword', 'Password of the user with privileges on the master node', 'string', 'Master node user password', null, now()),
(2, 'topology.node.name.prefix', 'TAO-', 'Prefix for new node names', 'string', 'Node prefix', null, now()),
(2, 'topology.node.default.description', 'Worker node', 'Default description for new nodes', 'string', 'Node default description', null, now()),
(2, 'topology.node.limit', '0', 'Maximum worker nodes (0 for unlimited)', 'int', 'Maximum worker nodes', null, now()),
(2, 'topology.node.user.limit', '1', 'Maximum worker nodes per user (0 for unlimited)', 'int', 'Maximum user worker nodes', null, now()),
(2, 'topology.node.create.wait', '60', 'Time in seconds to wait for a node to be alive', 'int', 'Node startup wait time (s)', null, now()),
(2, 'topology.node.connection.retries', '-1', 'How many times to retry the connection with a node (-1 for each time)', 'int', 'Node connection retries', null, now()),
(2, 'topology.dedicated.user.nodes', 'true', 'Use dedicated nodes for users', 'bool', 'Use dedicated nodes for users', null, now()),
(3, 'tao.docker.images', '/mnt/tao/docker', 'Location for extracting container files for new Docker plugins', 'string', 'Dockerfiles location', null, now()),
(3, 'tao.docker.registry', 'sen2agri-prod:5000', 'Docker registry', 'string', 'Docker registry', null, now()),
(3, 'tao.docker.base.port', '8888', 'Start port for standalone user containers', 'int', 'Start port for standalone containers', null, now()),
(3, 'master.docker.mappings', '{ "workspace.mount": "/mnt/tao/workspaces:/mnt/tao", "temp.mount": "/home/taouser/workdir:/mnt/workdir", "cfg.mount": "/mnt/tao/cfg:/mnt/cfg", "eodata.mount": "/eodata:/eodata", "additional.mount": "/mnt/tao/cfg:/home/.snap/:z" }', 'Volume mappings for docker execution on the master node', 'string', 'Master node volume map', null, now()),
(3, 'node.docker.mappings', '{ "workspace.mount": "/mnt/tao:/mnt/tao", "temp.mount": "/home/tao/workdir:/mnt/workdir", "cfg.mount": "/mnt/cfg:/mnt/cfg", "eodata.mount": "/eodata:/eodata", "additional.mount": "/mnt/tao/cfg:/home/.snap/:z" }', 'Volume mappings for docker execution on worker nodes', 'string', 'Worker node volume map', null, now()),
(3, 'docker.volume.map.style', 'docker', 'Docker host volume map style', 'string', 'Docker host volume map style', null, now()),
(3, 'enable.tunnel.filters', 'false', 'Enable tunneling for accessing private containers', 'bool', 'Enable tunnel filters', null, now()),
(4, 'tao.drmaa.polling.interval', '10', 'Time (in seconds) between inspections of the DRMAA system', 'int', 'DRMAA polling interval (s)', null, now()),
(4, 'tao.force.memory.requirements', 'true', 'Enforce memory constraints for applications', 'bool', 'Enforce memory constraints', null, now()),
(4, 'tao.ssh.async', 'true', 'Enable asynchronous mode for SSH invocations', 'bool', 'Enable SSH asynchronous mode', null, now()),
(4, 'tao.user.jobs.limit', '20', 'Maximum number of jobs a user can submit', 'int', 'Job limit per user', null, now()),
(4, 'tao.remove.intermediate.files', 'false', 'Remove the intermediate files when the execution finished successfuly', 'bool', 'Remove intermediate files', null, now()),
(4, 'tao.upload.to.bucket', 'false', 'Move the execution results to the object storage', 'bool', 'Move results to bucket', null, now()),
(4, 'tao.remove.invalid.products', 'false', 'Delete local products with invalid location', 'bool', 'Delete invalid products', null, now()),
(5, 'mail.smtp.auth', 'true', 'Enable SMTP authentication', 'bool', 'Enable SMTP authentication', null, now()),
(5, 'mail.smtp.starttls.enable', 'true', 'SMTP uses TLS', 'bool', 'SMTP uses TLS', null, now()),
(5, 'mail.smtp.host', 'smtp.gmail.com', 'SMTP server', 'string', 'SMTP server', null, now()),
(5, 'mail.smtp.port', '587', 'SMTP server port', 'int', 'SMTP server port', null, now()),
(5, 'mail.smtp.username', 'sen2agri.system@gmail.com', 'SMTP user', 'string', 'SMTP user', null, now()),
(5, 'mail.smtp.password', 'esa-sen2agri2019', 'SMTP password', 'string', 'SMTP password', null, now()),
(5, 'mail.from', 'sen2agri.system@gmail.com', 'Sender', 'string', 'Sender', null, now()),
(5, 'mail.to', 'kraftek@c-s.ro', 'Additional email receiving emails', 'string', 'Additional receiving email', null, now()),
(6, 'openstack.auth.url', 'https://cf2.cloudferro.com:5000/v3', 'OpenStack authentication endpoint', 'string', 'Authentication endpoint', null, now()),
(6, 'openstack.tenantId', 'e31d0ad8bb8b45968ac5d0167b0b32a7', 'OpenStack tenant (or project) ID', 'string', 'Project ID', null, now()),
(6, 'openstack.domain', 'cloud_01569', 'OpenStack domain', 'string', 'Domain', null, now()),
(6, 'openstack.user', 'kraftek@c-s.ro', 'OpenStack user', 'string', 'User', null, now()),
(6, 'openstack.password', 'cei7pitici.', 'OpenStack password', 'string', 'Password', null, now()),
(6, 'openstack.default.security.group', 'alow_ssh', 'VM security group', 'string', 'VM security group', null, now()),
(6, 'openstack.private.network', 'private_network_01569', 'Internal network', 'string', 'Internal network', null, now()),
(6, 'openstack.eodata.network', 'eodata', 'EO Data network', 'string', 'EO Data network', null, now()),
(6, 'openstack.floating.ip.pool', 'external', 'External IP address pool', 'string', 'External IP pool', null, now()),
(6, 'openstack.default.image', 'CentOS 7', 'Base OS image', 'string', 'Base OS image', null, now()),
(6, 'openstack.keypair.name', 'dunia-tao', 'Key Pair name', 'string', 'Key pair name', null, now()),
(6, 'openstack.volume.type.hdd', 'hdd', 'Label for HDD storage', 'string', 'HDD storage label', null, now()),
(6, 'openstack.volume.type.ssd', 'ssd', 'Label for SSD storage', 'string', 'SSD storage label', null, now()),
(6, 'openstack.volume.hdd.size', '4096', 'Default HDD storage size (GB)', 'int', 'HDD storage size (GB)', null, now()),
(6, 'openstack.volume.hdd.device', '/dev/sdh', 'Linux device for HDD', 'string', 'HDD device', null, now()),
(6, 'openstack.volume.ssd.size', '480', 'Default SDD storage size (GB)', 'int', 'SSD storage size (GB)', null, now()),
(6, 'openstack.volume.ssd.device', '/dev/sds', 'Linux device for SSD', 'string', 'SSD device', null, now()),
(6, 'openstack.execute.installers', 'false', 'Run post-install steps', 'bool', 'Run post-install steps', null, now()),
(6, 'openstack.node.user', 'eouser', 'Default account on a new node', 'string', 'Default account', null, now()),
(7, 'aws.region', 'eu-central-1', 'AWS region', 'string', 'AWS region', null, now()),
(7, 'aws.access.key', null, 'AWS access key', 'string', 'Access key', null, now()),
(7, 'aws.secret.key', null, 'AWS secret key', 'string', 'Secret key', null, now()),
(7, 'aws.bucket', null, 'AWS S3 bucket', 'string', 'S3 bucket', null, now()),
(7, 'aws.use.username.as.root', 'false', 'Use user name as bucket root', 'bool', 'Use user name as bucket root', null, now()),
(8, 'datasource.filter.category', null, 'Category filter', 'string', 'Category filter', null, now()),
(8, 'datasource.filter.collection', null, 'Collection filter', 'string', 'Collection filter', null, now()),
(8, 'datasource.filter.provider', null, 'Provider filter', 'string', 'Provider filter', null, now()),
(9, 'tao.kubernetes.master.url', 'https://127.0.0.1:6443', 'Cluster URL', 'string', 'Cluster URL', null, now()),
(9, 'tao.kubernetes.token', null, 'Service account token', 'string', 'Service account token', null, now()),
(9, 'tao.kubernetes.ca.cert.file', null, 'Service account certificate filepath', 'string', 'Service account certificate filepath', null, now()),
(9, 'tao.kubernetes.pvc.mappings', '{"/mnt/tao":"tao-pvc","/home/taouser":"tao-user-pvc","/eodata":"tao-eodata-pvc"}', 'Cluster PVC mappings', 'string', 'Cluster PVC mappings', null, now());

-- Subscriptions
INSERT INTO subscription.subscription_type VALUES
(1, 'Fixed resources'),
(2, 'Pay per use');
