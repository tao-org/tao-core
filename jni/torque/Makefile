DRMAA_INCLUDE=-I /usr/local/include
DRMAA_LIB=-L /usr/local/lib
JAVA_INCLUDE=-I $(JAVA_HOME)/include/ -I $(JAVA_HOME)/include/linux

CFLAGS = -ggdb -O2 -shared -fPIE $(DRMAA_INCLUDE) $(JAVA_INCLUDE)
LDFLAGS = $(DRMAA_LIB) -ldrmaa
CP = cp

jnilib: basis_types.h msg_drmaa.h com_sun_grid_drmaa_SessionImpl.h com_sun_grid_drmaa_SessionImpl.c
	$(CC) com_sun_grid_drmaa_SessionImpl.c -o libdrmaa-jni-torque.so $(CFLAGS) $(LDFLAGS)
    	$(CP) libdrmaa-jni-torque.so ../
	$(CP) libdrmaa-jni-torque.so ../../tao-drmaa-impl-torque/jni

all: jnilib

clean:
	rm -f libdrmaa-jni-torque.so
    	rm -f ../../tao-drmaa-impl-torque/jni/libdrmaa-jni-torque.so
