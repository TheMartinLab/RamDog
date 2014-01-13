del *.class
del *.obj
cd ..
cd javaToC
javac *.java -Xlint:unchecked
cd ..
javah.exe -jni -classpath . javaToC.DirectFourierTransform
copy javaToC_DirectFourierTransform.h native
del javaToC_DirectFourierTransform.h
cd native
cl -c -Ox -I"C:\Program Files\Java\jdk1.7.0_25\include" -I"C:\Program Files\Java\jdk1.7.0_25\include\win32" DirectFourierTransform.c
nvcc -arch=sm_30 cuDirectFourierTransform.cu -c -m64 -I"C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\v5.0\include" -I"C:\ProgramData\NVIDIA Corporation\CUDA Samples\v5.0\common\inc"
link /OUT:DirectFourierTransform.dll *.obj /DLL /LIBPATH:"C:\Program Files\NVIDIA GPU Computing Toolkit\CUDA\v5.0\lib\x64" /LIBPATH:"C:\ProgramData\NVIDIA Corporation\CUDA Samples\v5.0\common\lib" cudart.lib cuda.lib 

del "..\DirectFourierTransform.dll"
copy DirectFourierTransform.dll "..\..\..\RamdogPrototype1\"
copy DirectFourierTransform.dll "..\..\"