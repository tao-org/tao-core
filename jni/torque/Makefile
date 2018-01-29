DRMAA_INCLUDE=-I /usr/local/include
DRMAA_LIB=-L /usr/local/lib
JAVA_INCLUDE=-I $(JAVA_HOME)/include/ -I $(JAVA_HOME)/include/linux

CFLAGS = -ggdb -O2 -shared -fPIE $(DRMAA_INCLUDE) $(JAVA_INCLUDE)
LDFLAGS = $(DRMAA_LIB) -ldrmaa
CP = cp
CC = cc

jnilib: basis_types.h msg_drmaa.h com_sun_grid_drmaa_SessionImpl.h com_sun_grid_drmaa_SessionImpl.c
	$(CC) com_sun_grid_drmaa_SessionImpl.c -o libdrmaa-jni-torque.so $(CFLAGS) $(LDFLAGS)  	


all: jnilib

clean:
	rm -f libdrmaa-jni-torque.so
