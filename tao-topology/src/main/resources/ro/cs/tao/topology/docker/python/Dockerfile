FROM centos:centos7

LABEL maintainer="tao@c-s.ro" \
       description="Prepare an python 2.7 environment with common used scientifique library."

# The versions of stuff
ENV PYTHON_VERSION 2.7.15

WORKDIR /opt/

RUN yum -y install epel-release; yum -y update && yum -y install \
        curl \
        bzip2 \
        file \
        which \
        gd \
        libxslt \
        libxml2 \
        python-devel \
        python-pip \
        gcc \
        gcc-c++ \
        git \
        && yum -y clean all
#RUN echo pip stage
#RUN pip install -U pip
RUN pip install lxml \
        matplotlib \
        numpy \
        scipy \
        scikit-image \
        numexpr
        #pip install gdal==2.2.1
