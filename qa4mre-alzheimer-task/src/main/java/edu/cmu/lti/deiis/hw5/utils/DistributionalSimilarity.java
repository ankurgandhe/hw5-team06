
package edu.cmu.lti.deiis.hw5.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.cmu.lti.deiis.hw5.constants.DistributionalSimilarityConstants;

public class DistributionalSimilarity {

	public Map<String, double[]> wordVectorMap;
	public ArrayList<String> vocab;
	double vocabSize;
	int vectorSize;
	static DistributionalSimilarity DS;
	
	public static DistributionalSimilarity getInstance(){
		
		String filename = DistributionalSimilarityConstants.DIR_PATH;
		if (DS==null){
			DS = new DistributionalSimilarity(filename);
			
		}
		return DS;
	}
	
	private DistributionalSimilarity(String filename){
		try {
			readModel(filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void readModel(String filename) throws IOException {

		wordVectorMap = new HashMap<String, double[]>();
		vocab = new ArrayList<String>();

		BufferedReader br;
		double count = 0.0;
		System.out.print("Reading Words Vectors from"+filename+" ... ");
		try {
			br = new BufferedReader(new FileReader(filename));
			String line = br.readLine();
			vocabSize = Double.parseDouble(line.split(" ")[0]);
			vectorSize = Integer.parseInt(line.split(" ")[1]);

			String[] vectorStr;
			String word;

			while (line != null) {
				line = br.readLine();
				if (line == null)
					break;
				vectorStr = line.split(" ");
				word = vectorStr[0];
				double[] wVector = new double[vectorSize];
				int i;
				for (i = 1; i < vectorStr.length; i++) {
					wVector[i - 1] = Double.parseDouble(vectorStr[i]);
				}
				normalize(wVector);
				// Add to vocabulary and vector Hash Map
				vocab.add(word);
				wordVectorMap.put(word, wVector);
				count++;
				if (count%50000==0)
					System.out.print(count+"...");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("Read " + count + " words");
		

		// testModel("amino acid");
	}

	// Vector Normalization
	public void normalize(double[] wVector) {
		double len = 0.0;
		for (int a = 0; a < vectorSize; a++)
			len += wVector[a] * wVector[a];
		len = Math.sqrt(len);
		for (int a = 0; a < vectorSize; a++)
			wVector[a] /= len;

	}

	public double[] getSentenceVector(String query) {

		double[] queryVector = new double[vectorSize];
		String queryArray[] = query.split(" ");
		int a;
		int count = 0;
		for (String word : queryArray) {

			if (!wordVectorMap.containsKey(word)) {
				// System.out.println("Word " + word + " not in vocabulary\n");
				continue;
			}
			count++;
			double[] wordVector = wordVectorMap.get(word);
			for (a = 0; a < vectorSize; a++)
				queryVector[a] += wordVector[a];
		}
		if (count > 0) {
			normalize(queryVector);
		
		}
		//else
		//	queryVector[0]=-100;
		return queryVector;
	}

	public double getDistance(String word1, String word2) {

		double[] word1Vector = getSentenceVector(word1);
		double[] word2Vector = getSentenceVector(word2);
		if (word1Vector[0]==-100 || word2Vector[0]==-100)
			return -1;
		return getDistance(word1Vector, word2Vector);
	}

	public double getDistance(double[] w1Vector, double[] w2Vector) {
		double score = 0.0;
		for (int a = 0; a < vectorSize; a++)
			score += w1Vector[a] * w2Vector[a];
		/*if (score==0.0)
			score=0.5;*/
		/*else
			score=1;*/
		return score;

	}

	public String[] testModel(String query) {

        //        if(query==null || )
                
                String queryArray[] = query.split(" ");

                int a;
                double[] queryVector;
                queryVector = getSentenceVector(query);
                int topN = 20;
                String[] bestw = new String[topN];
                double[] bestd = new double[topN];

                for (a = 0; a < topN; a++)
                        bestw[a] = "";

                for (String word : vocab) {
                        int check = 1;

                        for (String qWord : queryArray) {
                                if (word.contentEquals(qWord)) {
                                        check = 0;
                                        break;
                                }
                        }
                        if (check == 0) {
                                continue;
                        }

                        double[] wordVector = wordVectorMap.get(word);
                        double dist = getDistance(wordVector, queryVector);

                        for (a = 0; a < topN; a++) {
                                if (dist > bestd[a]) {
                                        for (int d = topN - 1; d > a; d--) {
                                                bestd[d] = bestd[d - 1];
                                                bestw[d] = bestw[d - 1];
                                        }
                                        bestd[a] = dist;
                                        bestw[a] = word;
                                        break;
                                }
                        }
                }

        //        for (a = 0; a < topN; a++)
        //                System.out.println(bestw[a] + "\t" + bestd[a]);

        return bestw;
        }
	public boolean isOOV(String word){
		if (vocab.contains(word))
			return true;
		else
			return false;
				
	}
	public static void main(String[] args) throws Exception {
		String filename = DistributionalSimilarityConstants.DIR_PATH;
		DistributionalSimilarity DS = new DistributionalSimilarity(filename);
		
		DS.readModel(filename);
	}
}