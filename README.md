RamDog
======

A graphical user interface program to interactively analyze 2D Diffraction Images. Initial project setup; bugs throughout the software exist. Development in progress, use at own risk! Contact: (Developer) Dr. Eric Dill eddill@ncsu.edu; (Principal Investigator) Dr. James Martin martinjd@ncsu.edu

Currently requires https://github.com/ericdill/GlobalPackages 

See "src/Ramdog Help&Tutorial.docx" for usage instructions.

Run the program via command line "java -jar Ramdog.jar" or by double clicking on the downloaded file.

Alternatively, you can load this into an instance of the Eclipse IDE with the EGit addon.  Once synchronized, run the "ImageDisplay.java" file to pull up the GUI for 2D X-ray image analysis.  

*** The 2D Fourier Transform currently only works on windows with a CUDA-capable GPU installed.  You may also need to compile the *.dll ***


What can this software do?<br>
-Image Viewing<br>
&nbsp;&nbsp;-Load any binary file<br>
&nbsp;&nbsp;-Load common images (.png, .gif, .jpg, etc.)<br>
&nbsp;&nbsp;-Load calculated 2D diffraction images<br>
&nbsp;&nbsp;-Zoom in/out with mouse scroll wheel<br> 
-Image Analysis<br> 
&nbsp;&nbsp;-Load and subtract an image background file scaled by a user-controllable scaling factor <br> 
&nbsp;&nbsp;-Automatically find diffraction spots in a 2D image<br> 
&nbsp;&nbsp;-Allows the user to select regions of 2D diffraction images:<br> 
&nbsp;&nbsp;&nbsp;&nbsp;-Along a single pixel wide path (multiple straight line segments)<br> 
&nbsp;&nbsp;&nbsp;&nbsp;-All pixels inside a region bounded by an ellipse or rectangle<br> 
&nbsp;&nbsp;&nbsp;&nbsp;-Automatically find diffraction spots in the entire image or near the user’s mouse click<br> 
&nbsp;&nbsp;&nbsp;&nbsp;-Compute 2D Fourier transforms of the 2D diffraction image (Accelerated with CUDA in the<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;presence of a CUDA-capable graphics card)<br> 
&nbsp;&nbsp;-Logarithmic or linear intensity scaling<br> 
&nbsp;&nbsp;-2D Image Filtering: Median, min, max<br> 
&nbsp;&nbsp;-View numerically computed 2nd derivatives: (d2/dx2), (d2/dy2) and (d2/dx2+ d2/dy2)<br> 
&nbsp;&nbsp;-Image false coloring with multiple user-controllable color levels (intensity and color are<br>   &nbsp;&nbsp;&nbsp;
-Image I/O<br> 
&nbsp;&nbsp;-Output diffraction image<br> 
&nbsp;&nbsp;-Output the data from the selected regions of the 2D diffraction images (spot, path, region, etc.)<br> 
-Automated Features<br> 
&nbsp;&nbsp;-Convert a series of 2D binary X-ray diffraction images to .png images<br> 
&nbsp;&nbsp;&nbsp;&nbsp;-The entire image<br> 
&nbsp;&nbsp;&nbsp;&nbsp;-A specific region bounded by two pixel coordinates<br> 
&nbsp;&nbsp;&nbsp;&nbsp;-A specific region centered on the image center<br> 
&nbsp;&nbsp;-Compute 2D Fourier transforms of a series of diffraction images and output as .png images<br> 
&nbsp;&nbsp;-Output the data from the selected regions of a 2D diffraction image for a series of 2D diffraction<br>
&nbsp;&nbsp;&nbsp;images.<br> 

