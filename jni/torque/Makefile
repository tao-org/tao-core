SHELL:= /bin/bash

SLURM_DRMAA_INCLUDE=-I $(SLURM_DRMAA_INCLUDE_PATH)
JAVA_HOME=/usr/lib/jvm/java-openjdk
JAVA_INCLUDE=-I $(JAVA_HOME)/include/ -I $(JAVA_HOME)/include/linux/
CP = cp
CFLAGS = -ggdb -O2 -shared -fPIE $(SLURM_DRMAA_INCLUDE) $(JAVA_INCLUDE)

LDFLAGS = -L/usr/lib64/slurm/ -lslurm -ldrmaa \
      -l:auth_munge.so -l:crypto_munge.so \
      -l:select_cray.so \
      -l:select_linear.so \
      -l:select_serial.so \
      -l:select_alps.so \
      -l:select_cons_res.so
	
	
jnilib: basis_types.h msg_drmaa.h ro_cs_tao_execution_drmaa_AbstractSession.h ro_cs_tao_execution_drmaa_AbstractSession.c
	$(CC) ro_cs_tao_execution_drmaa_AbstractSession.c -o libdrmaa-jni-slurm.so $(CFLAGS) $(LDFLAGS)
		

all: jnilib

clean:
	rm -f libdrmaa-jni-slurm.so


