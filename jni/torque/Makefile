DRMAA_INCLUDE=-I /usr/local/include
DRMAA_LIB=-L /usr/local/lib
JAVA_INCLUDE=-I $(JAVA_HOME)/include/ -I $(JAVA_HOME)/include/linux

CFLAGS = -ggdb -O2 -shared -fPIE $(DRMAA_INCLUDE) $(JAVA_INCLUDE)
LDFLAGS = $(DRMAA_LIB) -ldrmaa
CP = cp
CC = cc

jnilib: basis_types.h msg_drmaa.h ro_cs_tao_execution_drmaa_AbstractSession.h ro_cs_tao_execution_drmaa_AbstractSession.c
	$(CC) ro_cs_tao_execution_drmaa_AbstractSession.c -o libdrmaa-jni-torque.so $(CFLAGS) $(LDFLAGS)  	


all: jnilib

clean:
	rm -f libdrmaa-jni-torque.so
