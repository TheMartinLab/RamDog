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
link /DLL /OUT:DirectFourierTransform.dll *.obj
del *.class
del *.obj
del *.lib
del *.exp
del "..\DirectFourierTransform.dll"
copy DirectFourierTransform.dll "..\"