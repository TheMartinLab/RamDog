package image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import dataFileTypes.ASC;
import dataFileTypes.BIN;
import dataFileTypes.ImageFile;
import dataFileTypes.ImageProperties;

public class XrayImage extends JPanel {

	private BufferedImage image;
	private BufferedImage overlay;
	private File curFile;
	private double[][] imageData;
	private Dimension dimension;
	private MouseListener mouseListener;
	public XrayImage() {
		super();
		image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
		overlay = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
		dimension = new Dimension(1000, 1000);
		createTestImage(1000, 1000);
	}
	
	public void loadNewFile(File aFile) {
		curFile = aFile;
		String fileName = aFile.toString().toLowerCase();
		ImageFile.types type = null;
		for(ImageFile.types tempType: ImageFile.types.values()) {
			if(fileName.contains("cor")) { type = ImageFile.types.BIN; }
			if(fileName.contains(tempType.toString().toLowerCase())) {
				type = tempType;
			}
		}
		if(type == null) {
			JOptionPane.showMessageDialog(null, "The file you selected cannot be loaded because the file type" +
					" cannot be read. Please select another file");
			return;
		}
		ImageProperties prop;
		switch(type) {
		case ASC:
			System.out.println("ASC read not implemented yet.");
			break;
		case BIN:
			prop = new ImageProperties(2, 4096, BIN.LITTLE_ENDIAN, new int[] {2048, 2048});
			imageData = (new BIN(prop)).readFile(aFile);
			break;
		case SPR:
			break;
		}
		image = new BufferedImage(imageData.length, imageData[0].length, BufferedImage.TYPE_INT_RGB);
		setImage();
	}
	
	public void setColor() {
		
	}
	public void setFilter() {
		
	}
	private Color iToColor(double i, double min, double max) {
		min = Math.log(min);
		max = Math.log(max);
		i = Math.log(i);
		i = (i-min) / (max-min) * 255;
		int defaultColor = (int) Math.rint(i);
		return new Color(defaultColor, defaultColor, defaultColor);
	}
	public void setImage() {
		WritableRaster imageRaster = image.getRaster();
		ColorModel model = image.getColorModel();
		Color color;
		Object colorData;
		// find min and max
		double min = imageData[0][0], max = imageData[0][0];
		for(int i = 0; i < imageData.length; i++) {
			for(int j = 0; j < imageData[i].length; j++) {
				if(imageData[i][j] < min) { min = imageData[i][j]; }
				if(imageData[i][j] > max) { max = imageData[i][j]; }
			}
		}
		if(min < 0) { min = 0; }
		for(int i = 0; i < imageData.length; i++) {
			for(int j = 0; j < imageData[i].length; j++) {
				color = iToColor(imageData[i][j], min, max);
				colorData = model.getDataElements(color.getRGB(), null);
				imageRaster.setDataElements(i, j, colorData);
			}
		}
		paint(getGraphics());
	}
	
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		Rectangle bounds = this.getBounds();
		g2.drawImage(image, bounds.x, bounds.y, bounds.width, bounds.height, null);
	}
	public BufferedImage createTestImage (int iWidth, int iHeight)
	{
		System.out.println("createTestImage entered \n");
		int displayWidth = iWidth; 
		int displayHeight = iHeight;
		image = new BufferedImage(displayWidth, displayHeight, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = image.getRaster();
		ColorModel model = image.getColorModel();
		Color fractalColor = Color.red;
		int argb = fractalColor.getRGB();
		
		Object colorData = model.getDataElements(argb, null);
		for (int i = 0; i < displayWidth; i++)
		{
			for (int j = 0; j < displayHeight ; j++)
			{
				//fractalColor = new Color(((i)%256), ((j)%256), ((i*j)%256));
				fractalColor = new Color((((j%(i+1)) - (i%(j+1)))+displayHeight)%128, 
						(((j%(i+1)) + (i%(j+1)))+displayHeight)%128, (((j%(i+1)) + (i%(j+1)))+displayHeight)%128);
				argb = fractalColor.getRGB();
				colorData = model.getDataElements(argb, null);
				raster.setDataElements(i,j,colorData);

			}
		}
		ImageIcon imageIcon = new ImageIcon(image);
		JLabel imageWindow = new JLabel(imageIcon);
		this.add(imageWindow, BorderLayout.CENTER);
		//imageWindow.addMouseListener(this);
		//imageWindow.addMouseMotionListener(this);
		//addMouseMotionListener(this);
        //addMouseListener(this);
		return image;
	}
	class AMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
