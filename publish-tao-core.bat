@ECHO OFF
SET MODULES=tao-config tao-data-model tao-data-sources tao-drmaa-api tao-execution tao-execution-model tao-notification tao-orchestration tao-persistence tao-scheduling tao-service-api tao-service-registry tao-spring-bridge tao-topology tao-utils
(FOR %%M IN (%MODULES%) DO (
	mvn install:install-file -DgroupId=ro.cs.tao -DartifactId=%%M -Dversion=1.0-SNAPSHOT -DpomFile=C:\Dev\tao\tao-core\%%M\pom.xml -Dfile=C:\Dev\tao\tao-core\%%M\target\%%M-1.0-SNAPSHOT.jar -Dpackaging=jar -DlocalRepositoryPath=. -DcreateChecksum=true
))
