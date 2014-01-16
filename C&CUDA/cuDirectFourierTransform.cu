// CUDA-C includes
#include <cuda.h>
#include <cuda_runtime.h>
#include <helper_cuda.h>
#include <math.h>
#include "cuprintf.cu"

/* DEFINITIONS */
/* STRUCTURES */

/* FUNCTION PROTOTYPES */
void initDevice();
__global__ void byCol(double *dev_input_data, double *dev_ft_data, int nRows, int nCols);
__global__ void byRow(double *dev_input_data, double *dev_ft_data, int nRows, int nCols);

/* GLOBAL VARS */

/* FUNCTION IMPLEMENTATION */
/////////////////////////////////////////////////////////////////////////
// get cuda info to set the device as the one with the most cuda cores //
/////////////////////////////////////////////////////////////////////////
void initDevice() {

	int cudaDeviceCount, 
		cudaDeviceToUse, 
		*numCudaCores, 
		maxNumCudaCores;
	
	cudaDeviceProp deviceProp;
	cudaDeviceCount = cudaGetDeviceCount(&cudaDeviceCount);
	cudaDeviceToUse = 0;
	numCudaCores = (int *) malloc(sizeof(int) * cudaDeviceCount);
	maxNumCudaCores = 0;
	for(int i = 0; i < cudaDeviceCount; i++) {
		cudaGetDeviceProperties(&deviceProp, i);	// get the device properties with a specific cuda call
		numCudaCores[i] = _ConvertSMVer2Cores(deviceProp.major, deviceProp.minor) * deviceProp.multiProcessorCount;	// get the number of cuda cores for device "i"
		if(numCudaCores[i] > maxNumCudaCores) {
			cudaDeviceToUse = i;
			maxNumCudaCores = numCudaCores[i];
		}
	}
	cudaSetDevice(cudaDeviceToUse);
}

/* DEVICE CALLS */

__global__ void byCol(double *dev_input_data_re, double *dev_input_data_im, 
						double *dev_ft_data_re, double *dev_ft_data_im, 
						int nRows, int nCols) {
	int kx = (blockIdx.x * blockDim.x) + threadIdx.x;
	int ky = (blockIdx.y * blockDim.y) + threadIdx.y;
	int ft_idx = kx + ky * nCols;
	cuPrintf("\nx y ft_idx: %d %d", kx, ky, ft_idx);
	int data_idx = 0;
	double PI = 3.14159265359;
	if(kx < nCols && ky < nRows) {
		for(int t = 0; t < nRows; t++) {
			data_idx = t * nCols + kx;
			double arg = 2*PI*t*ky/((double) nRows);
			dev_ft_data_re[ft_idx] +=    dev_input_data_re[data_idx] * cos(arg) + dev_input_data_im[data_idx] * sin(arg);
			dev_ft_data_im[ft_idx] += -1*dev_input_data_re[data_idx] * sin(arg) + dev_input_data_im[data_idx] * cos(arg);
		}
	}
}

__global__ void byRow(double *dev_input_data_re, double *dev_input_data_im, 
						double *dev_ft_data_re, double *dev_ft_data_im, 
						int nRows, int nCols) {
	int kx = (blockIdx.x * blockDim.x) + threadIdx.x;
	int ky = (blockIdx.y * blockDim.y) + threadIdx.y;
	int ft_idx = kx + ky * nCols;
	cuPrintf("\nx y ft_idx: %d %d", kx, ky, ft_idx);
	int data_idx = 0;
	double PI = 3.14159265359;
	if(kx < nCols && ky < nRows) {
		for(int t = 0; t < nCols; t++) {
			data_idx = ky * nCols + t;
			double arg = 2*PI*t*kx/((double) nCols);
			dev_ft_data_re[ft_idx] +=    dev_input_data_re[data_idx] * cos(arg) + dev_input_data_im[data_idx] * sin(arg);
			dev_ft_data_im[ft_idx] += -1*dev_input_data_re[data_idx] * sin(arg) + dev_input_data_im[data_idx] * cos(arg);
		}
	}
}

void emulateByRow(double *input_data, double *ft_data, int nRows, int nCols, int kx, int ky) {
	int ft_idx = kx + ky * nCols;
	int t;
	int data_idx = 0;
	double PI = 3.14159265359;
	if(kx < nCols && ky < nRows) {
		//printf("\nx y ft_idx: %d %d %d: dataIdx: ", x, y, ft_idx);
		double sumReal = 0, sumImag = 0;
		for(t = 0; t < nCols; t++) {
			data_idx = t + ky * nCols;
			//printf(" %d", data_idx);
			sumReal +=    input_data[data_idx] * cos(((double) 2*PI * t * kx) / ((double) nCols));
			sumImag += -1*input_data[data_idx] * sin(((double) 2*PI * t * kx) / ((double) nCols));
		}
		//printf("\nsumReal %lf sumImag %lf", sumReal, sumImag);
		ft_data[ft_idx] = sqrt(pow(sumReal, 2) + pow(sumImag, 2));
	}
}

void emulate(dim3 numBlocks, dim3 threadsPerBlock, double *data, double *host_ft_data, int nRows, int nCols) {
	int kx, ky, x1, y1, x2, y2;
	for(x1 = 0; x1 < numBlocks.x; x1++) {
		for(y1 = 0; y1 < numBlocks.y; y1++) {
			for(x2 = 0; x2 < threadsPerBlock.x; x2++) {
				for(y2 = 0; y2 < threadsPerBlock.y; y2++) {
					kx = (x1 * threadsPerBlock.x) + x2;
					ky = (y1 * threadsPerBlock.y) + y2;
					emulateByRow(data, host_ft_data, nRows, nCols, kx, ky);
				}
			}
		}
	}
}
void printArray(double *data, int nRows, int nCols) {
	int i, j, idx;
	for(i = 0; i < nRows; i++) {
		for(j = 0; j < nCols; j++) {
			idx = j + i * nCols;
			printf("%lf\t", data[idx]);
		}
		printf("\n");
	}
}
/* HOST CALLS */
#ifdef __cplusplus
extern "C"
{
#endif
void runDFT(double *data, double *host_ft_data_re, double *host_ft_data_im, int nRows, int nCols, int colFirst) {
	dim3 threadsPerBlock(8, 8);
	dim3 numBlocks(nCols / threadsPerBlock.x+1,
					nRows / threadsPerBlock.y+1);
	
	double *dev_input_data_re, *dev_input_data_im, *dev_ft_data_re, *dev_ft_data_im;

	size_t array_size = sizeof(double) * nRows * nCols;
	int idx;
	
	/* ALLOCATE DEVICE MEMORY */
	cudaMalloc((void **) &dev_input_data_re, array_size);
	cudaMalloc((void **) &dev_input_data_im, array_size);
	cudaMalloc((void **) &dev_ft_data_re, array_size);
	cudaMalloc((void **) &dev_ft_data_im, array_size);
	
	/* INITIALIZE DEVICE MEMORY */
	cudaMemset(dev_ft_data_re, 0, array_size);
	cudaMemset(dev_ft_data_im, 0, array_size);
	cudaMemset(dev_input_data_im, 0, array_size);
	memset(host_ft_data_re, 0, array_size);
	memset(host_ft_data_im, 0, array_size);
	
	printf("\nthreadsPerBlock (%d, %d), numBlocks (%d, %d)", threadsPerBlock.x, threadsPerBlock.y, numBlocks.x, numBlocks.y);
	printf("\nCommencing Print before copy to device");
	printArray(data, 1, nCols);
	/* COPY DATA TO DEVICE */	
	cudaMemcpy(dev_input_data_re, data, array_size, cudaMemcpyHostToDevice);
	if(colFirst == 0) {
		byRow<<<numBlocks, threadsPerBlock>>>(dev_input_data_re, dev_input_data_im, dev_ft_data_re, dev_ft_data_im, nRows, nCols);
		cudaMemcpy(dev_input_data_re, dev_ft_data_re, array_size, cudaMemcpyDeviceToDevice);
		cudaMemcpy(dev_input_data_im, dev_ft_data_im, array_size, cudaMemcpyDeviceToDevice);
		cudaMemset(dev_ft_data_re, 0, array_size);
		cudaMemset(dev_ft_data_im, 0, array_size);
		byCol<<<numBlocks, threadsPerBlock>>>(dev_input_data_re, dev_input_data_im, dev_ft_data_re, dev_ft_data_im, nRows, nCols);
	} else if(colFirst == 1){
		byCol<<<numBlocks, threadsPerBlock>>>(dev_input_data_re, dev_input_data_im, dev_ft_data_re, dev_ft_data_im, nRows, nCols);
		cudaMemcpy(dev_input_data_re, dev_ft_data_re, array_size, cudaMemcpyDeviceToDevice);
		cudaMemcpy(dev_input_data_im, dev_ft_data_im, array_size, cudaMemcpyDeviceToDevice);
		cudaMemset(dev_ft_data_re, 0, array_size);
		cudaMemset(dev_ft_data_im, 0, array_size);
		byRow<<<numBlocks, threadsPerBlock>>>(dev_input_data_re, dev_input_data_im, dev_ft_data_re, dev_ft_data_im, nRows, nCols);
	} else {
		cudaMemcpy(dev_ft_data_re, dev_input_data_re, array_size, cudaMemcpyDeviceToDevice);
	}
	cudaThreadSynchronize();
	/* COPY DATA FROM DEVICE */
	cudaMemcpy(host_ft_data_re, dev_ft_data_re, array_size, cudaMemcpyDeviceToHost);
	cudaMemcpy(host_ft_data_im, dev_ft_data_im, array_size, cudaMemcpyDeviceToHost);
	
	printf("\nCommencing Print after data has been copied from device");
	printf("\nre:\n");
	printArray(host_ft_data_re, 1, nCols);
	printf("\nim:\n");
	printArray(host_ft_data_im, 1, nCols);

	/* FREE DEVICE MEMORY */
	cudaFree(dev_input_data_re);
	cudaFree(dev_input_data_im);
	cudaFree(dev_ft_data_re);
	cudaFree(dev_ft_data_im);
}
#ifdef __cplusplus
}
#endif