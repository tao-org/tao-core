FROM centos:7

LABEL maintainer="tao@c-s.ro" \
       description="Prepare an EO processing environment with commonly used tools."

# The versions of stuff
ENV OTB_VERSION=6.4.0 

WORKDIR /opt/

RUN yum -y install epel-release && \
     yum -y update && \
     yum -y install curl bzip2 file which gd libxslt libxml2 python-devel python-pip gcc gcc-c++ git && \
     yum -y clean all

RUN pip install -U pip && \
     pip install lxml matplotlib numpy scipy scikit-image numexpr

RUN curl -L https://www.orfeo-toolbox.org/packages/archives/OTB-${OTB_VERSION}-Linux64.run -o OTB-${OTB_VERSION}-Linux64.run && \
     chmod +x OTB-${OTB_VERSION}-Linux64.run && \
     ./OTB-${OTB_VERSION}-Linux64.run --target /opt/OTB-${OTB_VERSION}-Linux64 && \
     rm -f OTB-${OTB_VERSION}-Linux64.run && \
     chmod a+rx /opt/OTB-${OTB_VERSION}-Linux64/otbenv.profile && \
     ln -s /opt/OTB-${OTB_VERSION}-Linux64/otbenv.profile /etc/profile.d/OTB.sh

COPY dist/gdal-config /usr/bin/
RUN export C_INCLUDE_PATH=/opt/OTB-${OTB_VERSION}-Linux64/include && \
     export CPLUS_INCLUDE_PATH=/opt/OTB-${OTB_VERSION}-Linux64/include && \
     pip install gdal==2.2.1
 
ENV LD_LIBRARY_PATH=/opt/OTB-${OTB_VERSION}-Linux64/lib/


