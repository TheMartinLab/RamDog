RamDog
======

A graphical user interface program to interactively analyze 2D Diffraction Images. 

What can this software do?
--------------------------

- Image Viewing
  - Load any binary file
  - Load common images (.png, .gif, .jpg, etc.)
  - Load calculated 2D diffraction images
  - Zoom in/out with mouse scroll wheel

- Image Analysis
  - Load and subtract an image background file scaled by a user-controllable scaling factor <br> 
  - Automatically find diffraction spots in a 2D image
  - Allows the user to select regions of 2D diffraction images:
    - Along a single pixel wide path (multiple straight line segments) 
    - All pixels inside a region bounded by an ellipse or rectangle
    - Automatically find diffraction spots in the entire image or near the user’s mouse click 
  - Compute 2D Fourier transforms of the 2D diffraction image (Accelerated with CUDA in the 
    presence of a CUDA-capable graphics card) 
  - Logarithmic or linear intensity scaling
  - 2D Image Filtering: Median, min, max 
  - View numerically computed 2nd derivatives: (d2/dx2), (d2/dy2) and (d2/dx2+ d2/dy2)
  - Image false coloring with multiple user-controllable color levels (intensity and color)

- Image I/O
  - Output diffraction image
  - Output the data from the selected regions of the 2D diffraction images (spot, path, region, etc.)

- Automated Features
  - Convert a series of 2D binary X-ray diffraction images to .png images
    - The entire image
    - A specific region bounded by two pixel coordinates
    - A specific region centered on the image center
  - Compute 2D Fourier transforms of a series of diffraction images and output as .png images
  - Output the data from the selected regions of a 2D diffraction image for a series of 2D diffraction images

Disclaimer
----------
Critical bugs throughout the software may exist, please submit a [Pull Request](https://github.com/TheMartinLab/RamDog/pulls) or a [bug report](https://github.com/TheMartinLab/RamDog/issues) if you find any!

Please submit [new features](https://github.com/TheMartinLab/RamDog/issues) that you wish to see

Development in progress, use at own risk! Contact: (Developer) Dr. Eric Dill edill@bnl.gov; 
(Principal Investigator) Dr. James Martin martinjd@ncsu.edu

Installation
------------
Requires Java Runtime Environment v1.7

See the [releases page](https://github.com/TheMartinLab/RamDog/releases) for an executable (jar).

Run the program via command line `java -jar Ramdog.jar` or by double clicking on the downloaded file.

**The 2D Fourier Transform currently only works on windows with a CUDA-capable GPU installed.  You may also need to compile the .dll**

Development
-----------
Currently requires https://github.com/ericdill/GlobalPackages 

This source code can be loaded into the Eclipse IDE with the EGit addon.  Once synchronized, run 
`src/gui/ImageDisplay.java` to pull up the GUI for 2D X-ray image analysis.  

Further reading
---------------
See the [manual](https://github.com/TheMartinLab/RamDog/raw/master/doc/Manual.pdf) for usage instructions.

