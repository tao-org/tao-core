SHELL:= /bin/bash

SLURM_DRMAA_INCLUDE=-I $(SLURM_DRMAA_INCLUDE_PATH)
#SLURM_DRMAA_LIB=-L $(SLURM_DRMAA_LIBRARY_PATH)
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
	

	
jnilib: basis_types.h msg_drmaa.h com_sun_grid_drmaa_SessionImpl.h com_sun_grid_drmaa_SessionImpl.c
	$(CC) com_sun_grid_drmaa_SessionImpl.c -o libdrmaa-jni-slurm.so $(CFLAGS) $(LDFLAGS)
	#$(CP) libdrmaa-jni-slurm.so $(DRMAA_JAVA_JNI_LIBRARY_PATH)
	$(CP) libdrmaa-jni-slurm.so /home/jcoravu/TAOWorkspace/Slurm/drmaa-libraries/
		

all: jnilib

clean:
	rm -f libdrmaa-jni-slurm.so
	#rm -f $(DRMAA_JAVA_JNI_LIBRARY_PATH)/libdrmaa-jni-slurm.so
	rm -f /home/jcoravu/TAOWorkspace/Slurm/drmaa-libraries/libdrmaa-jni-slurm.so

