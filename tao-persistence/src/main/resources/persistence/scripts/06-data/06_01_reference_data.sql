-- Processing components visibility
INSERT INTO component.component_visibility (visibility) VALUES ('SYSTEM');
INSERT INTO component.component_visibility (visibility) VALUES ('USER');
INSERT INTO component.component_visibility (visibility) VALUES ('CONTRIBUTOR');

-- Processing components type
INSERT INTO component.processing_component_type (id, type) VALUES (1, 'EXECUTABLE');
INSERT INTO component.processing_component_type (id, type) VALUES (2, 'SCRIPT');

-- Tag type
INSERT INTO common.tag_type (id, description) VALUES (1, 'TOPOLOGY_NODE');
INSERT INTO common.tag_type (id, description) VALUES (2, 'COMPONENT');
INSERT INTO common.tag_type (id, description) VALUES (3, 'DATASOURCE');
INSERT INTO common.tag_type (id, description) VALUES (4, 'WORKFLOW');

-- Condition
INSERT INTO component.condition (id, description) VALUES (1, 'Equals');
INSERT INTO component.condition (id, description) VALUES (2, 'Not equals');
INSERT INTO component.condition (id, description) VALUES (3, 'Less than');
INSERT INTO component.condition (id, description) VALUES (4, 'Less than or equal');
INSERT INTO component.condition (id, description) VALUES (5, 'Greater than');
INSERT INTO component.condition (id, description) VALUES (6, 'Greater than or equals');
INSERT INTO component.condition (id, description) VALUES (7, 'In');
INSERT INTO component.condition (id, description) VALUES (8, 'Not in');

-- Data formats
INSERT INTO product.data_format (type) VALUES ('RASTER');
INSERT INTO product.data_format (type) VALUES ('VECTOR');
INSERT INTO product.data_format (type) VALUES ('OTHER');

-- Data types
INSERT INTO product.data_type (type) VALUES ('String');
INSERT INTO product.data_type (type) VALUES ('Integer');
INSERT INTO product.data_type (type) VALUES ('Double');
INSERT INTO product.data_type (type) VALUES ('Short');
INSERT INTO product.data_type (type) VALUES ('Date');
INSERT INTO product.data_type (type) VALUES ('Polygon2D');

-- Execution status
INSERT INTO execution.status (description) VALUES ('UNDETERMINED');
INSERT INTO execution.status (description) VALUES ('QUEUED_ACTIVE');
INSERT INTO execution.status (description) VALUES ('RUNNING');
INSERT INTO execution.status (description) VALUES ('SUSPENDED');
INSERT INTO execution.status (description) VALUES ('DONE');
INSERT INTO execution.status (description) VALUES ('FAILED');
INSERT INTO execution.status (description) VALUES ('CANCELLED');

-- Fetch mode
INSERT INTO component.fetch_mode (fetch_mode) VALUES ('OVERWRITE');
INSERT INTO component.fetch_mode (fetch_mode) VALUES ('RESUME');
INSERT INTO component.fetch_mode (fetch_mode) VALUES ('COPY');
INSERT INTO component.fetch_mode (fetch_mode) VALUES ('SYMLINK');

-- Orbit direction
INSERT INTO product.orbit_direction (direction) VALUES ('ASCENDING');
INSERT INTO product.orbit_direction (direction) VALUES ('DESCENDING');

-- Parameter type
INSERT INTO component.parameter_type (type) VALUES ('RegularParameter');
INSERT INTO component.parameter_type (type) VALUES ('TemplateParameter');

-- Pixel types
INSERT INTO product.pixel_type (type) VALUES ('UINT8');
INSERT INTO product.pixel_type (type) VALUES ('INT8');
INSERT INTO product.pixel_type (type) VALUES ('UINT16');
INSERT INTO product.pixel_type (type) VALUES ('INT16');
INSERT INTO product.pixel_type (type) VALUES ('UINT32');
INSERT INTO product.pixel_type (type) VALUES ('INT32');
INSERT INTO product.pixel_type (type) VALUES ('FLOAT32');
INSERT INTO product.pixel_type (type) VALUES ('FLOAT64');

-- Sensor types
INSERT INTO product.sensor_type (type) VALUES ('OPTICAL');
INSERT INTO product.sensor_type (type) VALUES ('RADAR');
INSERT INTO product.sensor_type (type) VALUES ('ALTIMETRIC');
INSERT INTO product.sensor_type (type) VALUES ('ATMOSPHERIC');
INSERT INTO product.sensor_type (type) VALUES ('UNKNOWN');

-- Service status
INSERT INTO  topology.service_status (status) VALUES ('NOT_FOUND');
INSERT INTO  topology.service_status (status) VALUES ('INSTALLED');
INSERT INTO  topology.service_status (status) VALUES ('UNINSTALLED');
INSERT INTO  topology.service_status (status) VALUES ('ERROR');

-- Data Query parameters
-- common parameters
INSERT INTO workflow.query_parameter (data_type_id, name) VALUES (1, 'platformName');
INSERT INTO workflow.query_parameter (data_type_id, name) VALUES (5, 'startDate');
INSERT INTO workflow.query_parameter (data_type_id, name) VALUES (5, 'endDate');
INSERT INTO workflow.query_parameter (data_type_id, name) VALUES (6, 'footprint');
INSERT INTO workflow.query_parameter (data_type_id, name) VALUES (1, 'productType');
INSERT INTO workflow.query_parameter (data_type_id, name) VALUES (1, 'relativeOrbit');

-- Radar parameters
INSERT INTO workflow.query_parameter (data_type_id, name) VALUES (1, 'polarisation');
INSERT INTO workflow.query_parameter (data_type_id, name) VALUES (1, 'sensorOperationalMode');

-- Optical parameters
INSERT INTO workflow.query_parameter (data_type_id, name) VALUES (3, 'cloudCover');

-- Template type
INSERT INTO component.template_type (type) VALUES ('VELOCITY');
INSERT INTO component.template_type (type) VALUES ('JAVASCRIPT');
INSERT INTO component.template_type (type) VALUES ('XSLT');

-- Component type
INSERT INTO workflow.component_type (id, description) VALUES (1, 'DATASOURCE');
INSERT INTO workflow.component_type (id, description) VALUES (2, 'PROCESSING');
INSERT INTO workflow.component_type (id, description) VALUES (3, 'GROUP');
INSERT INTO workflow.component_type (id, description) VALUES (4, 'DATASOURCE_GROUP');

-- Behavior
INSERT INTO workflow.node_behavior (id, description) VALUES (1, 'FAIL_ON_ERROR');
INSERT INTO workflow.node_behavior (id, description) VALUES (2, 'CONTINUE_ON_ERROR');

-- Workflow graph status
INSERT INTO workflow.status (description) VALUES ('DRAFT');
INSERT INTO workflow.status (description) VALUES ('READY');
INSERT INTO workflow.status (description) VALUES ('PUBLISHED');

-- Workflow graph visibility
INSERT INTO common.visibility (visibility) VALUES ('PUBLIC');
INSERT INTO common.visibility (visibility) VALUES ('PRIVATE');

-- Groups
-- admin group
INSERT INTO usr."group" (name) VALUES ('ADMIN');
-- operator group
INSERT INTO usr."group" (name) VALUES ('USER');

-- User status
INSERT INTO usr.user_status (status) VALUES ('PENDING');
INSERT INTO usr.user_status (status) VALUES ('ACTIVE');
INSERT INTO usr.user_status (status) VALUES ('DISABLED');

-- Users
-- admin user (password "admin" - "$2a$08$wU07f1hSVkTO7321eVi0quF8If7d23Ly66dkwGAOzbb6xrZFeo69m")
INSERT INTO usr."user" (username, password, email, alternative_email, last_name, first_name, quota, organization, status_id, created)
VALUES ('admin', 'QxdxrJkXAQMN5nT9+dBWLg==', 'kraftek@c-s.ro', 'oana.hogoiu@c-s.ro', 'Administrator', 'Account', 1000, 'TAO', 2, CURRENT_TIMESTAMP);

-- System Account (password "system" - "$2a$12$mXbtZweZmmFjcKIA0/NmF.uxcHik8rTKGdkkyrelKiZWe3bhgR.aC")
INSERT INTO usr."user" (username, password, email, alternative_email, last_name, first_name, quota, organization, status_id, created)
VALUES ('SystemAccount', 'TSEBHrrH9Uo+O69A5nzq9A==', 'kraftek@c-s.ro', 'oana.hogoiu@c-s.ro', 'Cosmin', 'Cara', 1000, 'CSRO', 2, CURRENT_TIMESTAMP);

-- operator user (password "operator" - "$2a$09$AvkhkrwRHqQIdEfslOLB8ulHzKo89AUIuuANhHmrE5RxzGUsMFGs.")
INSERT INTO usr."user" (username, password, email, alternative_email, last_name, first_name, quota, organization, status_id, created)
VALUES ('operator', 'dzkzdsvxRSrhlNLJdvThzg==', 'kraftek@c-s.ro', 'oana.hogoiu@c-s.ro', 'Cosmin', 'Cara', 1000, 'CSRO', 2, CURRENT_TIMESTAMP);

-- operator anonymousUser
INSERT INTO usr."user" (username, password, email, alternative_email, last_name, first_name, quota, organization, status_id, created)
VALUES ('anonymous', 'Gm5jHka7JxkbhZW60GoQFw==', 'kraftek@c-s.ro', 'oana.hogoiu@c-s.ro', 'Anonymous', 'Anonymous', 1000, 'CSRO', 2, CURRENT_TIMESTAMP);

-- User groups
INSERT INTO usr.user_group (user_id, group_id) VALUES (1, 1);
INSERT INTO usr.user_group (user_id, group_id) VALUES (2, 1);
INSERT INTO usr.user_group (user_id, group_id) VALUES (3, 2);
INSERT INTO usr.user_group (user_id, group_id) VALUES (4, 2);

-- localhost execution node
INSERT INTO topology.node (id, username, password, total_cpu, total_ram, total_hdd, description)
VALUES ('localhost', '', '', 8, 32, 1024, 'Master Node');

