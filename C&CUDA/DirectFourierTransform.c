#include <stdio.h>
#include <stdlib.h>
#include "javaToC_DirectFourierTransform.h"

/* DEFINITIONS */

/* STRUCTURES */

/* FUNCTION PROTOTYPES */
//extern void runDFT(double *data, int nRows, int nCols, int colFirst);
  
 /* FUNCTIONS */
 #ifdef __cplusplus
extern "C" {
#endif
 JNIEXPORT jdoubleArray JNICALL Java_javaToC_DirectFourierTransform_direct
  (JNIEnv *env, jclass cls, jdoubleArray myArray, jint colFirst, jint nRows, jint nCols) {
	int len1;
	int idx;
	double *localArray, *ft_re, *ft_im;
	jdouble *dim, *returnElements;
	jdoubleArray returnArray;
	
	len1 = (*env)->GetArrayLength(env, myArray);
	//printf("\nLine: %d", __LINE__);
	dim = (*env)->GetDoubleArrayElements(env, myArray, 0);
	//printf("\nLine: %d", __LINE__);
	// allocate local array using len1
	localArray = (double *) malloc(sizeof(double) * len1);
	ft_re = (double *) malloc(sizeof(double) * len1);
	ft_im = (double *) malloc(sizeof(double) * len1);
	//printf("\nLine: %d", __LINE__);
	
	for(idx = 0; idx < len1; ++idx) {
		localArray[idx] = dim[idx];
//		printf("\n%d: %lf %lf", idx, dim[idx], localArray[idx]);
	}
	
	(*env)->ReleaseDoubleArrayElements(env, myArray, dim, 0);
//	printf("\nLine: %d", __LINE__);
	
	// TODO stuff with the localArray
	runDFT(localArray, ft_re, ft_im, nRows, nCols, (int) colFirst);

//	printf("\nLine: %d", __LINE__);
	
	free(localArray);
	localArray = (double *) malloc(sizeof(double) * 2 * len1);
	
	returnArray = (*env)->NewDoubleArray(env, len1*2);
	for(idx = 0; idx < len1; idx++) {
		localArray[idx] = ft_re[idx];
		localArray[idx+len1] = ft_im[idx];
	}
	//(*env)->ReleaseDoubleArrayElements(env, returnArray, returnElements, 0);
	// TODO return array of doubles to java
	(*env)->SetDoubleArrayRegion(env, returnArray, (jsize) 0, (jsize) len1*2, localArray);
	free(localArray);
	free(ft_re);
	free(ft_im);
	return returnArray;
  }
  
#ifdef __cplusplus
}
#endif