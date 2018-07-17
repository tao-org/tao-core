FROM centos:centos7

LABEL maintainer="tao@c-s.ro" \
       description="Prepare an EO processing environment with commonly used tools."

# The versions of stuff
ENV SNAP_VERSION=6.0 

WORKDIR /opt/

RUN	yum -y install wget which && \
	yum -y update && yum clean all


RUN wget http://step.esa.int/downloads/6.0/installers/esa-snap_sentinel_unix_6_0.sh && \
     chmod +x esa-snap_sentinel_unix_6_0.sh && \
     ./esa-snap_sentinel_unix_6_0.sh -q && \
     ### update snap
     #snap --nosplash --nogui --modules --update-all
     rm -f esa-snap_sentinel_unix_6_0.sh

 
ENV LD_LIBRARY_PATH=/opt/snap/jre/lib/

RUN ln -s /opt/snap/bin/gpt /usr/local/bin/gpt


#RUN mkdir -p /mnt/store
#
#VOLUME /mnt/store

#RUN yum -y remove file which python-devel gcc gcc-c++ && \
#    yum -y clean all


###   SNAP-6.0.0-Linux64
# wget http://step.esa.int/downloads/6.0/installers/esa-snap_sentinel_unix_6_0.sh
# chmod +x esa-snap_sentinel_unix_6_0.sh
#### install SNAP in console mode and quiet:  ./esa-snap_sentinel_unix_6_0.sh -q -dir "/opt/SNAP-6.0-Linux64/"

#master.tao.c-s.ro:5000/snap-v6.0   latest