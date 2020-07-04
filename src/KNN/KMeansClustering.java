package KNN;

import java.util.Set;
import java.util.HashSet;
import java.util.Random;

public class KMeansClustering {
	public static void main(String[] args) {
		int K = 500;
		int maxIters = 20;

		// TODO: Adaptez les parcours
		byte[][][] images = KNN.parseIDXimages(Helpers.readBinaryFile("datasets/1000-per-digit_images_train"));
		byte[] labels = KNN.parseIDXlabels(Helpers.readBinaryFile("datasets/1000-per-digit_labels_train"));

		byte[][][] reducedImages = KMeansReduce(images, K, maxIters);

		byte[] reducedLabels = new byte[reducedImages.length];
		for (int i = 0; i < reducedLabels.length; i++) {
			reducedLabels[i] = KNN.knnClassify(reducedImages[i], images, labels, 5);
			System.out.println("Classified " + (i + 1) + " / " + reducedImages.length);
		}

		Helpers.writeBinaryFile("datasets/reduced10Kto1K_images", encodeIDXimages(reducedImages));
		Helpers.writeBinaryFile("datasets/reduced10Kto1K_labels", encodeIDXlabels(reducedLabels));

	}

	/**
	 * @brief Encodes a tensor of images into an array of data ready to be written
	 *        on a file
	 * 
	 * @param images the tensor of image to encode
	 * 
	 * @return the array of byte ready to be written to an IDX file
	 */
	public static byte[] encodeIDXimages(byte[][][] images) {
		// TODO: Implémenter

		byte[] imagesInBytes = new byte[images.length * images[0].length * images[0][0].length + 16];

		encodeInt(2051, imagesInBytes, 0);
		encodeInt(images.length, imagesInBytes, 4);
		encodeInt(images[0].length, imagesInBytes, 8);
		encodeInt(images[0][0].length, imagesInBytes, 12);

		int indice = 16;
		for (int i = 0; i < images.length; ++i) {
			for (int j = 0; j < images[0].length; ++j) {
				for (int k = 0; k < images[0][0].length; ++k) {
					imagesInBytes[indice] = (byte) ((images[i][j][k] & 0xFF) + 128);
					++indice;
				}
			}
		}
		return imagesInBytes;
	}

	/**
	 * @brief Prepares the array of labels to be written on a binary file
	 * 
	 * @param labels the array of labels to encode
	 * 
	 * @return the array of bytes ready to be written to an IDX file
	 */
	public static byte[] encodeIDXlabels(byte[] labels) {
		// TODO: Implémenter
		byte[] labelsInBytes = new byte[labels.length + 8];

		encodeInt(2049, labelsInBytes, 0);
		encodeInt(labels.length, labelsInBytes, 4);
		int indice = 8;
		for (int i = 0; i < labels.length; ++i) {
			labelsInBytes[indice] = labels[i];
			++indice;
		}
		return labelsInBytes;

	}

	/**
	 * @brief Decomposes an integer into 4 bytes stored consecutively in the
	 *        destination array starting at position offset
	 * 
	 * @param n           the integer number to encode
	 * @param destination the array where to write the encoded int
	 * @param offset      the position where to store the most significant byte of
	 *                    the integer, the others will follow at offset + 1, offset
	 *                    + 2, offset + 3
	 */
	public static void encodeInt(int n, byte[] destination, int offset) {
		// TODO: Implémenter
		byte b1 = (byte) (n & 0xFF);
		byte b2 = (byte) (n >> 8 & 0xFF);
		byte b3 = (byte) (n >> 16 & 0xFF);
		byte b4 = (byte) (n >> 24 & 0xFF);

		destination[offset] = b4;
		destination[offset + 1] = b3;
		destination[offset + 2] = b2;
		destination[offset + 3] = b1;
	}

	/**
	 * @brief Runs the KMeans algorithm on the provided tensor to return size
	 *        elements.
	 * 
	 * @param tensor   the tensor of images to reduce
	 * @param size     the number of images in the reduced dataset
	 * @param maxIters the number of iterations of the KMeans algorithm to perform
	 * 
	 * @return the tensor containing the reduced dataset
	 */
	public static byte[][][] KMeansReduce(byte[][][] tensor, int size, int maxIters) {
		int[] assignments = new Random().ints(tensor.length, 0, size).toArray();
		byte[][][] centroids = new byte[size][][];
		initialize(tensor, assignments, centroids);

		int nIter = 0;
		while (nIter < maxIters) {
			// Step 1: Assign points to closest centroid
			recomputeAssignments(tensor, centroids, assignments);
			System.out.println("Recomputed assignments");
			// Step 2: Recompute centroids as average of points
			recomputeCentroids(tensor, centroids, assignments);
			System.out.println("Recomputed centroids");

			System.out.println("KMeans completed iteration " + (nIter + 1) + " / " + maxIters);

			nIter++;
		}

		return centroids;
	}

	/**
	 * @brief Assigns each image to the cluster whose centroid is the closest. It
	 *        modifies.
	 * 
	 * @param tensor      the tensor of images to cluster
	 * @param centroids   the tensor of centroids that represent the cluster of
	 *                    images
	 * @param assignments the vector indicating to what cluster each image belongs
	 *                    to. if j is at position i, then image i belongs to cluster
	 *                    j
	 */
	public static void recomputeAssignments(byte[][][] tensor, byte[][][] centroids, int[] assignments) {
		float dist[] = new float[centroids.length];

		for (int i = 0; i < tensor.length; ++i) {
			for (int j = 0; j < centroids.length; ++j) {

				dist[j] = KNN.squaredEuclideanDistance(tensor[i], centroids[j]);

			}
			assignments[i] = indexOfMin(dist);

		}

	}

	public static int indexOfMin(float[] array) {
		int indiceOfMin = 0;
		float valueOfMin = array[0];
		for (int j = 0; j < array.length; ++j) {
			if (array[j] < valueOfMin) {
				valueOfMin = array[j];
				indiceOfMin = j;
			}
		}
		return indiceOfMin;
	}

	/**
	 * @brief Computes the centroid of each cluster by averaging the images in the
	 *        cluster
	 * 
	 * @param tensor      the tensor of images to cluster
	 * @param centroids   the tensor of centroids that represent the cluster of
	 *                    images
	 * @param assignments the vector indicating to what cluster each image belongs
	 *                    to. if j is at position i, then image i belongs to cluster
	 *                    j
	 */
	public static void recomputeCentroids(byte[][][] tensor, byte[][][] centroids, int[] assignments) {
		int[] count = new int[centroids.length];
		float[][][] sum = new float[centroids.length][tensor[0].length][tensor[0][0].length];

		for (int i = 0; i < tensor.length; ++i) {
			int cluster = assignments[i];
			++count[cluster];
			byte[][] image = tensor[i];
			for (int j = 0; j < image.length; ++j) {
				for (int k = 0; k < image[0].length; ++k) {
					sum[cluster][j][k] += image[j][k];
				}
			}
		}
		for (int i = 0; i < centroids.length; ++i) {
			for (int j = 0; j < centroids[0].length; ++j) {
				for (int k = 0; k < centroids[0][0].length; ++k) {
					centroids[i][j][k] = (byte) (sum[i][j][k] / count[i]);
				}
			}
		}
	}

	/**
	 * Initializes the centroids and assignments for the algorithm. The assignments
	 * are initialized randomly and the centroids are initialized by randomly
	 * choosing images in the tensor.
	 * 
	 * @param tensor      the tensor of images to cluster
	 * @param assignments the vector indicating to what cluster each image belongs
	 *                    to.
	 * @param centroids   the tensor of centroids that represent the cluster of
	 *                    images if j is at position i, then image i belongs to
	 *                    cluster j
	 */
	public static void initialize(byte[][][] tensor, int[] assignments, byte[][][] centroids) {
		Set<Integer> centroidIds = new HashSet<>();
		Random r = new Random("cs107-2018".hashCode());
		while (centroidIds.size() != centroids.length)
			centroidIds.add(r.nextInt(tensor.length));
		Integer[] cids = centroidIds.toArray(new Integer[] {});
		for (int i = 0; i < centroids.length; i++)
			centroids[i] = tensor[cids[i]];
		for (int i = 0; i < assignments.length; i++)
			assignments[i] = cids[r.nextInt(cids.length)];
	}
}
