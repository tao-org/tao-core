

-- Processing components visibility

INSERT INTO tao.component_visibility (visibility) VALUES ('SYSTEM');

INSERT INTO tao.component_visibility (visibility) VALUES ('USER');

INSERT INTO tao.component_visibility (visibility) VALUES ('CONTRIBUTOR');



-- Data formats

INSERT INTO tao.data_format (type) VALUES ('RASTER');

INSERT INTO tao.data_format (type) VALUES ('VECTOR');

INSERT INTO tao.data_format (type) VALUES ('OTHER');



-- Data types

INSERT INTO tao.data_type (type) VALUES ('String');

INSERT INTO tao.data_type (type) VALUES ('Integer');

INSERT INTO tao.data_type (type) VALUES ('Double');

INSERT INTO tao.data_type (type) VALUES ('Short');

INSERT INTO tao.data_type (type) VALUES ('Date');

INSERT INTO tao.data_type (type) VALUES ('Polygon2D');



-- Execution status

INSERT INTO tao.execution_status (status) VALUES ('UNDETERMINED');

INSERT INTO tao.execution_status (status) VALUES ('QUEUED_ACTIVE');

INSERT INTO tao.execution_status (status) VALUES ('RUNNING');

INSERT INTO tao.execution_status (status) VALUES ('SUSPENDED');

INSERT INTO tao.execution_status (status) VALUES ('DONE');

INSERT INTO tao.execution_status (status) VALUES ('FAILED');

INSERT INTO tao.execution_status (status) VALUES ('CANCELLED');



-- Fetch mode

INSERT INTO tao.fetch_mode (fetch_mode) VALUES ('OVERWRITE');

INSERT INTO tao.fetch_mode (fetch_mode) VALUES ('RESUME');

INSERT INTO tao.fetch_mode (fetch_mode) VALUES ('COPY');

INSERT INTO tao.fetch_mode (fetch_mode) VALUES ('SYMLINK');



-- Orbit direction

INSERT INTO tao.orbit_direction (direction) VALUES ('ASCENDING');

INSERT INTO tao.orbit_direction (direction) VALUES ('DESCENDING');



-- Parameter type

INSERT INTO tao.parameter_type (type) VALUES ('RegularParameter');

INSERT INTO tao.parameter_type (type) VALUES ('TemplateParameter');



-- Pixel types

INSERT INTO tao.pixel_type (type) VALUES ('UINT8');

INSERT INTO tao.pixel_type (type) VALUES ('INT8');

INSERT INTO tao.pixel_type (type) VALUES ('UINT16');

INSERT INTO tao.pixel_type (type) VALUES ('INT16');

INSERT INTO tao.pixel_type (type) VALUES ('UINT32');

INSERT INTO tao.pixel_type (type) VALUES ('INT32');

INSERT INTO tao.pixel_type (type) VALUES ('FLOAT32');

INSERT INTO tao.pixel_type (type) VALUES ('FLOAT64');


-- Sensor types

INSERT INTO tao.sensor_type (type) VALUES ('OPTICAL');

INSERT INTO tao.sensor_type (type) VALUES ('RADAR');

INSERT INTO tao.sensor_type (type) VALUES ('ALTIMETRIC');

INSERT INTO tao.sensor_type (type) VALUES ('ATMOSPHERIC');

INSERT INTO tao.sensor_type (type) VALUES ('UNKNOWN');


-- Service status

INSERT INTO  tao.service_status (status) VALUES ('NOT_FOUND');

INSERT INTO  tao.service_status (status) VALUES ('INSTALLED');

INSERT INTO  tao.service_status (status) VALUES ('UNINSTALLED');

INSERT INTO  tao.service_status (status) VALUES ('ERROR');


-- Data Query parameters
-- common parameters
INSERT INTO tao.query_parameter (data_type_id, name)
VALUES (1, 'platformName');

INSERT INTO tao.query_parameter (data_type_id, name)
VALUES (5, 'beginPosition');

INSERT INTO tao.query_parameter (data_type_id, name)
VALUES (5, 'endPosition');

INSERT INTO tao.query_parameter (data_type_id, name)
VALUES (6, 'footprint');

INSERT INTO tao.query_parameter (data_type_id, name)
VALUES (1, 'productType');

-- Sentinel-1 parameters
INSERT INTO tao.query_parameter (data_type_id, name)
VALUES (1, 'polarisationMode');

INSERT INTO tao.query_parameter (data_type_id, name)
VALUES (1, 'sensorOperationalMode');

INSERT INTO tao.query_parameter (data_type_id, name)
VALUES (1, 'relativeOrbitNumber');

-- Sentinel-2 parameters
INSERT INTO tao.query_parameter (data_type_id, name)
VALUES (3, 'cloudcoverpercentage');

INSERT INTO tao.query_parameter (data_type_id, name)
VALUES (4, 'relativeOrbitNumber');



-- Template type

INSERT INTO tao.template_type (type) VALUES ('VELOCITY');
INSERT INTO tao.template_type (type) VALUES ('JAVASCRIPT');
INSERT INTO tao.template_type (type) VALUES ('XSLT');



-- Workflow graph status

INSERT INTO tao.workflow_graph_status (status) VALUES ('DRAFT');

INSERT INTO tao.workflow_graph_status (status) VALUES ('READY');

INSERT INTO tao.workflow_graph_status (status) VALUES ('PUBLISHED');



-- Workflow graph visibility

INSERT INTO tao.workflow_graph_visibility (visibility) VALUES ('PUBLIC');

INSERT INTO tao.workflow_graph_visibility (visibility) VALUES ('PRIVATE');



-- Groups
-- admin group
INSERT INTO tao."group" (name)
VALUES ('admin');


-- Users
-- admin user
INSERT INTO tao."user" (username, password, email, last_name, first_name, quota, group_id, created, active)
VALUES ('admin', md5('admin'), 'kraftek@c-s.ro', 'Cosmin', 'Cara', 1000, 1, CURRENT_TIMESTAMP, true);

-- System Account
INSERT INTO tao."user" (username, password, email, last_name, first_name, quota, group_id, created, active)
VALUES ('System Account', md5('system'), 'kraftek@c-s.ro', 'Cosmin', 'Cara', 1000, 1, CURRENT_TIMESTAMP, true);



-- localhost execution node
INSERT INTO tao.execution_node (host_name, username, password, total_cpu, total_ram, total_hdd, description)
VALUES ('localhost', 'admin', 'admin', 8, 32, 1024, 'Master Node');

