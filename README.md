RamDog
======

A graphical user interface program to interactively analyze 2D Diffraction Images. Initial project setup; bugs throughout the software exist. Development in progress, use at own risk! Contact: (Developer) Dr. Eric Dill eddill@ncsu.edu; (Principal Investigator) Dr. James Martin martinjd@ncsu.edu

Currently requires https://github.com/ericdill/GlobalPackages 

See "src/Ramdog Help&Tutorial.docx" for usage instructions.

Run the program via command line "java -jar Ramdog.jar" or by double clicking on the downloaded file.

What can this software do?
•	Image Viewing<br>
  o	Load any binary file
  o	Load common images (.png, .gif, .jpg, etc.)
  o	Load calculated 2D diffraction images
  o	Zoom in/out with mouse scroll wheel 
•	Image Analysis
  o	Load and subtract an image background file scaled by a user-controllable scaling factor 
  o	Automatically find diffraction spots in a 2D image
  o	Allows the user to select regions of 2D diffraction images:
    	Along a single pixel wide path (multiple straight line segments)
    	All pixels inside a region bounded by an ellipse or rectangle
    	Automatically find diffraction spots in the entire image or near the user’s mouse click
  o	Compute 2D Fourier transforms of the 2D diffraction image (Accelerated with CUDA in the presence 
    of a CUDA-capable graphics card)
  o	Logarithmic or linear intensity scaling
  o	2D Image Filtering: Median, min, max
  o	View numerically computed 2nd derivatives: (d2/dx2), (d2/dy2) and (d2/dx2+ d2/dy2)
  o	Image false coloring with multiple user-controllable color levels (intensity and color are changeable)
•	Image I/O
  o	Output diffraction image
  o	Output the data from the selected regions of the 2D diffraction images (spot, path, region, etc.)
•	Automated Features
  o	Convert a series of 2D binary X-ray diffraction images to .png images
    	The entire image
    	A specific region bounded by two pixel coordinates
    	A specific region centered on the image center
  o	Compute 2D Fourier transforms of a series of diffraction images and output as .png images
  o	Output the data from the selected regions of a 2D diffraction image for a series of 2D diffraction images.

