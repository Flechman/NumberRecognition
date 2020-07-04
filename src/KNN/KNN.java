package KNN;

import java.util.Arrays;

public class KNN {
	public static void main(String[] args) {
	    //Attention à faire tourner le programme en Java 1.10
		int TESTS = 100;
		int K = 7;
		byte[][][] trainImages = parseIDXimages(Helpers.readBinaryFile("datasets/100-per-digit_images_train"));
		byte[] trainLabels = parseIDXlabels(Helpers.readBinaryFile("datasets/100-per-digit_labels_train"));
		byte[][][] testImages = parseIDXimages(Helpers.readBinaryFile("datasets/10k_images_test"));
		byte[] testLabels = parseIDXlabels(Helpers.readBinaryFile("datasets/10k_labels_test"));
		byte[] predictions = new byte[TESTS];
		long start = System.currentTimeMillis();
		for (int i = 0; i < TESTS; i++) {
			predictions[i] = knnClassify(testImages[i], trainImages, trainLabels, K);
		}
		long end = System.currentTimeMillis();
		double time = (end - start) / 1000d;
		System.out.println("Accuracy = " + accuracy(predictions, Arrays.copyOfRange(testLabels, 0, TESTS)) + " %");
		System.out.println("Time = " + time + " seconds");
		System.out.println("Time per test image = " + (time / TESTS));
		Helpers.show("Test", testImages, predictions, testLabels, 10, 10); // (int)Maths.sqrt(TESTS),
		// (int)Maths.sqrt(TESTS)
	}

	/**
	 * Composes four bytes into an integer using big endian convention.
	 *
	 * @param bXToBY The byte containing the bits to store between positions X and Y
	 * 
	 * @return the integer having form [ b31ToB24 | b23ToB16 | b15ToB8 | b7ToB0 ]
	 */
	public static int extractInt(byte b31ToB24, byte b23ToB16, byte b15ToB8, byte b7ToB0) {
		int n1 = (b7ToB0 & 0xFF);
		int n2 = (b15ToB8 & 0xFF) << 8;
		int n3 = (b23ToB16 & 0xFF) << 16;
		int n4 = (b31ToB24 & 0xFF) << 24;

		int nombre = n4 | n3 | n2 | n1;

		return nombre;
	}

	/**
	 * Parses an IDX file containing images
	 *
	 * @param data the binary content of the file
	 *
	 * @return A tensor of images
	 */
	public static byte[][][] parseIDXimages(byte[] data) {

		// Réception du magic number, qui est sensé être 2051

		int magicNumberImg = extractInt(data[0], data[1], data[2], data[3]);
		if (magicNumberImg != 2051) {
			return null;
		}

		// Récupération du nombre d'images, du nombre de pixels en longueur et en
		// largeur

		int nbImages = extractInt(data[4], data[5], data[6], data[7]);
		int nbRows = extractInt(data[8], data[9], data[10], data[11]);
		int nbColums = extractInt(data[12], data[13], data[14], data[15]);
		if ((nbColums == 0) || (nbRows == 0) || (nbImages == 0)) {
			return null;
		}

		// Création de notre tableau à trois dimensions composé de nos images

		byte[][][] tenseurImg = new byte[nbImages][nbRows][nbColums];

		// System.out.println("Images : "+nbImages);
		// System.out.println("Rows : "+nbRows);
		// System.out.println("Colums : "+nbColums);

		// Remplissage de notre tableau

		int nbPixel = 16;

		for (int idxImages = 0; idxImages < nbImages; ++idxImages) {

			for (int idxRows = 0; idxRows < nbRows; ++idxRows) {

				for (int idxColums = 0; idxColums < nbColums; ++idxColums) {

					tenseurImg[idxImages][idxRows][idxColums] = (byte) ((data[nbPixel] & 0xFF) - 128);
					++nbPixel;
				}
			}
		}

		return tenseurImg;
	}

	/**
	 * Parses an idx images containing labels
	 *
	 * @param data the binary content of the file
	 *
	 * @return the parsed labels
	 */
	public static byte[] parseIDXlabels(byte[] data) {
		// TODO: Implémenter

		// Réception de notre magic number, qui est sensé être 2049

		int magicNumberLbl = extractInt(data[0], data[1], data[2], data[3]);
		if (magicNumberLbl != 2049) {
			return null;
		}

		// Récupération de nos étiquettes

		int nbLabels = extractInt(data[4], data[5], data[6], data[7]);
		if (nbLabels == 0) {
			return null;
		}

		// Création de notre tableau composé de nos étiquettes

		byte[] tenseurLbl = new byte[nbLabels];

		// Remplissage de notre tableau

		int numeroDuByte = 8;
		for (int idxLabels = 0; idxLabels < nbLabels; ++idxLabels) {
			tenseurLbl[idxLabels] = data[numeroDuByte];
			++numeroDuByte;
		}
		return tenseurLbl;

	}

	/**
	 * @brief Computes the squared L2 distance of two images
	 * 
	 * @param a, b two images of same dimensions
	 * 
	 * @return the squared euclidean distance between the two images
	 */
	public static float squaredEuclideanDistance(byte[][] a, byte[][] b) {
		// TODO: Implémenter

		// Comparaison de deux images avec la méthode de distance euclidienne (formule
		// ci-dessous)

		float distance = 0;
		for (int h = 0; h < a.length; ++h) {
			for (int l = 0; l < a[h].length; ++l) {
				distance += ((a[h][l]) - (b[h][l])) * ((a[h][l]) - (b[h][l]));

			}
		}
		// if (distance == 0) {
		// System.out.println("Les images a et b sont identiques.");
		// }
		return distance;
	}

	/**
	 * @brief Computes the inverted similarity between 2 images.
	 * 
	 * @param a, b two images of same dimensions
	 * 
	 * @return the inverted similarity between the two images
	 */
	public static float invertedSimilarity(byte[][] a, byte[][] b) {
		// TODO: Implémenter

		// Comparaison de deux images avec la méthode de similarité inversée
		// Calcul de la moyenne de couleur des images a et b

		float moyA = 0;
		float moyB = 0;
		for (int h = 0; h < b.length; ++h) {
			for (int l = 0; l < b[h].length; ++l) {
				moyB += (b[h][l]);
				moyA += (a[h][l]);
			}
		}
		moyB = moyB / (float) (b.length * b[0].length);
		moyA = moyA / (float) (a.length * a[0].length);

		// Calcul du numérateur dans notre expression
		// Calcul de la première partie de notre dénominateur
		// Calcul de la deuxième partie de notre dénominateur

		float denomB = 0;
		float denomA = 0;
		float numerateur = 0;
		if (a.length != b.length || a[0].length != b[0].length) {
			return 2;
		}
		for (int h = 0; h < a.length; ++h) {
			for (int l = 0; l < a[h].length; ++l) {
				numerateur += (a[h][l] - moyA) * (b[h][l] - moyB);
				denomA += (a[h][l] - moyA) * (a[h][l] - moyA);
				denomB += (b[h][l] - moyB) * (b[h][l] - moyB);
			}
		}

		// Calcul du dénominateur

		float denominateur = (float) Math.sqrt(denomA * denomB);
		if (denominateur == 0) {
			return 2;
		}

		// Calcul de notre expression finale

		float similInv = 1 - (numerateur / denominateur);
		// if (similInv == 1) {
		// System.out.println("Les images a et b sont identiques.");
		// }
		return similInv;
	}

	/**
	 * @brief Quicksorts and returns the new indices of each value.
	 * 
	 * @param values the values whose indices have to be sorted in non decreasing
	 *               order
	 * 
	 * @return the array of sorted indices
	 * 
	 *         Example: values = quicksortIndices([3, 7, 0, 9]) gives [2, 0, 1, 3]
	 */
	public static int[] quicksortIndices(float[] values) {
		// TODO: Implémenter
		int[] indiceImg = new int[values.length];
		for (int i = 0; i < values.length; ++i) {
			indiceImg[i] = i;
		}
		quicksortIndices(values, indiceImg, 0, values.length - 1);
		return indiceImg;
	}

	/**
	 * @brief Sorts the provided values between two indices while applying the same
	 *        transformations to the array of indices
	 * 
	 * @param values  the values to sort
	 * @param indices the indices to sort according to the corresponding values
	 * @param         low, high are the **inclusive** bounds of the portion of array
	 *                to sort
	 */
	public static void quicksortIndices(float[] values, int[] indices, int low, int high) {
		// TODO: Implémenter
		int l = low;
		int h = high;
		float pivot = values[l];
		while (l <= h) {
			if (values[l] < pivot) {
				++l;
			} else if (values[h] > pivot) {
				--h;
			} else {
				swap(l, h, values, indices);
				++l;
				--h;
			}
		}
		if (low < h) {
			quicksortIndices(values, indices, low, h);
		}
		if (high > l) {
			quicksortIndices(values, indices, l, high);
		}
	}

	/**
	 * @brief Swaps the elements of the given arrays at the provided positions
	 * 
	 * @param         i, j the indices of the elements to swap
	 * @param values  the array floats whose values are to be swapped
	 * @param indices the array of ints whose values are to be swapped
	 */
	public static void swap(int i, int j, float[] values, int[] indices) {
		// TODO: Implémenter
		float swap = values[i];
		values[i] = values[j];
		values[j] = swap;

		int swapIndices = indices[i];
		indices[i] = indices[j];
		indices[j] = swapIndices;
	}

	/**
	 * @brief Returns the index of the largest element in the array
	 * 
	 * @param array an array of integers
	 * 
	 * @return the index of the largest integer
	 */
	public static int indexOfMax(int[] array) {
		// TODO: Implémenter
		int indiceOfMax = 0;
		int valueOfMax = array[0];
		for (int j = 0; j < array.length; ++j) {
			if (array[j] > valueOfMax) {
				valueOfMax = array[j];
				indiceOfMax = j;
			}
		}
		return indiceOfMax;
	}

	/**
	 * The k first elements of the provided array vote for a label
	 *
	 * @param sortedIndices the indices sorted by non-decreasing distance
	 * @param labels        the labels corresponding to the indices
	 * @param k             the number of labels asked to vote
	 *
	 * @return the winner of the election
	 */
	public static byte electLabel(int[] sortedIndices, byte[] labels, int k) {
		// TODO: Implémenter
		int[] vote = new int[10];
		byte valeurElue;
		for (int i = 0; i < k; ++i) {
			int numeroImage = sortedIndices[i];
			byte valeurLabel = labels[numeroImage];
			++vote[valeurLabel];
		}
		valeurElue = (byte) indexOfMax(vote);
		return valeurElue;
	}

	/**
	 * Classifies the symbol drawn on the provided image
	 *
	 * @param image       the image to classify
	 * @param trainImages the tensor of training images
	 * @param trainLabels the list of labels corresponding to the training images
	 * @param k           the number of voters in the election process
	 *
	 * @return the label of the image
	 */
	public static byte knnClassify(byte[][] image, byte[][][] trainImages, byte[] trainLabels, int k) {
		// TODO: Implémenter
		float[] e = new float[trainImages.length];
		for (int i = 0; i < trainImages.length; ++i) {
			e[i] = invertedSimilarity(image, trainImages[i]);

		}
		int[] sortedE = quicksortIndices(e);
		byte votedE = electLabel(sortedE, trainLabels, k);
		return votedE;
	}

	/**
	 * Computes accuracy between two arrays of predictions
	 * 
	 * @param predictedLabels the array of labels predicted by the algorithm
	 * @param trueLabels      the array of true labels
	 * 
	 * @return the accuracy of the predictions. Its value is in [0, 1]
	 */
	public static double accuracy(byte[] predictedLabels, byte[] trueLabels) {
		// TODO: Implémenter
		double a = 0;
		for (int i = 0; i < trueLabels.length; ++i) {
			if (predictedLabels[i] != trueLabels[i]) {
				a += 0;
			} else {
				a += 1;
			}
		}
		int n = trueLabels.length;
		double precision = (a / n) * 100;
		return precision;
	}
}
