import numpy as np
import os
import tifffile

if __name__ == "__main__":
    # folder where the tiffs are located
    tiff_folder = 'D:\\Data\\aps 15\\d10\\130\\tiffs\\'

    # output folder for binary files
    bin_folder = 'D:\\Data\\aps 15\\d10\\130\\bin\\'

    if not os.path.exists(bin_folder):
        os.mkdir(bin_folder)

    files = os.listdir(tiff_folder)
    files = [f for f in files if f.endswith(".tif")]
    for f in files:
        I = tifffile.imread(os.path.join(tiff_folder, f))
        # set values that are less than 0 to 0
        I[I<0] = 0
        I = I.astype(np.int32)
        bin_name = f[:-4] + ".bin"
        print("Saving file {}".format(bin_name))
        I.tofile(os.path.join(bin_folder, bin_name))