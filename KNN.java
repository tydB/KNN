/*
	Tyler deBoon
	120030
	CMPT 450
*/

import java.util.ArrayList;
import java.io.File;
import java.util.Scanner;
import java.net.URL;
import javax.imageio.*;
import java.awt.image.*;
public class KNN {
	public static void main(String[] args) {
		// get the file into a scanner
		boolean verb = false;
		Scanner s;
		try {
			File f = new File(args[0]);
			s = new Scanner(f);
		} catch (Exception e) {
			System.out.println("Error opening file.");
			return;
		}
		ArrayList<String> imageURLs = new ArrayList<String>();
		int groupCount = Integer.parseInt(s.nextLine());
		String[] groupNames = new String[groupCount];
		for (int i = 0; i < groupCount; ++i) {
			groupNames[i] = s.nextLine();
		}
		// get urls
		while (s.hasNextLine()) {
			imageURLs.add(s.nextLine());
		}
		// create all the images
		// BufferedImage[] imgs = new BufferedImage[imageURLs.size()];
		ArrayList<BufferedImage> imgs = new ArrayList<BufferedImage>();
		for (int i = 0; i < imageURLs.size(); ++i) {
			try {	
				URL u = new URL(imageURLs.get(i));
				BufferedImage temp = ImageIO.read(u);
				imgs.add(temp);
				// imgs[i] = ImageIO.read(u);
			} catch (Exception e) {
				System.out.println("Error getting image" + imageURLs.get(i));
			}
		}
		double[][] data = new double[imgs.size()][96];
		// histogram those images
		for (int i = 0; i < imgs.size(); ++i) {
			if (verb) System.out.println(imageURLs.get(i));
			data[i] = histogram(imgs.get(i), verb);
		}
		KNN k = new KNN(data, imageURLs.toArray(new String[imageURLs.size()]), groupNames, 1);
		// KNN k = new KNN(randomData(10, 2), randomLabels(10), groupNames, 2);
	}
	public static double[] histogram(BufferedImage img, boolean verb) {
		double[] counts = new double[32 * 3];
		for(int j=0; j<img.getHeight(); j++) {
			for(int i=0; i<img.getWidth(); i++) {
				int p = img.getRGB(i,j);
				++counts[getRed(p) / 8];
				++counts[getGreen(p) / 8 + 32];
				++counts[getBlue(p) / 8 + 64];
			}
		}
		double totalRed = 0;
		double totalGreen = 0;
		double totalBlue = 0;
		for (int c = 0; c < 32; ++c) {
			counts[c] /= 8;
			// System.out.println(counts[c]);
			totalRed += counts[c];
			counts[c + 32] /= 8;
			totalGreen += counts[c + 32];
			counts[c + 64] /= 8;
			totalBlue += counts[c + 64];
		}
		double sum = 0;
		String out = "Red:";
		for (int c = 0; c < counts.length; ++c) {
			if (verb) {
				if (c == 31) {
					System.out.println(out + "\nTotal Red = " + (sum * 100) + "%");
					out = "Green:";
					sum = 0;
				}
				else if (c == 63) {
					System.out.println(out + "\nTotal Green = " + (sum * 100) + "%");
					out = "Blue:";
					sum = 0;
				}
				else if (c == 95) {
					System.out.println(out + "\nTotal Blue = " + (sum * 100) + "%");
				}
			}
			if (c < 32) {
				counts[c] /= totalRed;
			}
			else if (c < 64) {
				counts[c] /= totalGreen;
			}
			else {
				counts[c] /= totalBlue;
			}
			if (verb) {
				sum += counts[c];
				out += " " + (counts[c] * 100) + "%";
			}
		}
		return counts;
	}
	public static int getRed(int pixelColour) { return   (0x00FF0000 & pixelColour)>>>16;}
	public static int getGreen(int pixelColour) { return (0x0000FF00 & pixelColour)>>>8;}
	public static int getBlue(int pixelColour) { return  (0x000000FF & pixelColour);}
	public static String[] randomLabels(int numOf) {
		String[] labels = new String[numOf];
		for (int i = 0; i < numOf; ++i) {
			labels[i] = "Image URL: ";
			for (int j = 0; j < 5; ++j) {
				labels[i] += (char)((int)'a' + (int)(Math.random() * 26));
			}
		}
		return labels;
	}
	public static double[][] randomData(int numOf, int dims) {
		double[][] d = new double[numOf][dims];
		for (int i = 0; i < numOf; ++i) {
			for (int j = 0; j < dims; ++j) {
				d[i][j] = Math.random() * 50;
			}
		}
		return d;
	}
	public KNN(double[][] data, String[] dataLabels, String[] groupNames, int passes) {
		solveKNN(data, dataLabels, groupNames, passes);
	}
	private double distance(double[] a, double[] b) {
		double sum = 0;
		if (a.length != b.length) {
			System.out.println("Mismatched Dimensions");
			return -1;
		}
		for (int i = 0; i < a.length; ++i) {
			sum += Math.pow(a[i] - b[i], 2);
		}
		return Math.sqrt(sum);
	}
	public double[] average(ArrayList<double[]> cluster) {
		double[] average = new double[cluster.get(0).length];
		for (int i = 0; i < average.length; i++) {
			average[i] = 0;
		}
		for (int i = 0; i < cluster.size(); ++i) {
			for (int j = 0; j < cluster.get(i).length; ++j) {
				average[j] += cluster.get(i)[j];
			}
		}
		for (int i = 0; i < average.length; i++) {
			average[i] /= cluster.size();
		}
		return average;
	}
	private void solveKNN(double[][] data, String[] dataLabels, String[] groupNames, int passes) {
		int index = 0;
		int groups = groupNames.length;
		double[][] averages = new double[groups][data[0].length];
		ArrayList<ArrayList<double[]>> clusters = new ArrayList<ArrayList<double[]>>();
		for (int p = 0; p < passes; ++p) {
			System.out.println("Pass: " + p);
			// for the first pass set set the exemplars in each cluster
			if (p == 0) {
				for (int g = 0; g < groups; ++g) {
					// create clusters
					clusters.add(new ArrayList<double[]>());
					clusters.get(g).add(data[g]);
					averages[g] = data[g];
					System.out.println("Image URL: " + dataLabels[g]);
					System.out.println("Add this sample to cluster " + g + " (" + groupNames[g] + ")\n");
				}
			}
			else {
				for (int g = 0; g < groups; ++g) {
					// clear clusters
					clusters.get(g).clear();
				}
			}
			int start = 0;
			if (p == 0) {
				start += groups;
			}
			for (int d = start; d < data.length; ++d) {
				System.out.println("Image URL: " + dataLabels[d]);
				double distance = -1;
				int selected = 0;
				for (int a = 0; a < averages.length; ++a) {
					double temp = distance(averages[a], data[d]);
					System.out.println("Distance (" + a + ") is : " + temp);
					if (temp  < distance || distance == -1) {
						distance = temp;
						selected = a;
					}
				}
				clusters.get(selected).add(data[d]);
				averages[selected] = average(clusters.get(selected));
				System.out.println("Add this sample to cluster " + selected + " (" + groupNames[selected] + ")\n");
				// System.out.println("New Average " + printArray(averages[selected]));
			}
		}
		for (int c = 0; c < clusters.size(); ++c) {
			System.out.println("Count [" + c + "] = " + clusters.get(c).size());
		}
	}
	private String printArray(double[] list) {
		String a = "[" + list[0];
		for (int i = 1; i < list.length; ++i) {
			a += ", " + list[i];
		}
		return a + "]";
	}
}