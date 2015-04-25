/**
 * 
 */
package textManager;

import java.util.ArrayList;
import java.util.HashMap;

/**计算各种概率
 * @author hp
 *
 */
public class CalculateP {
	private ArrayList<ArrayList<Integer>> countOfWordsDifCate;
	private HashMap<String, Integer> featureCode;
	
	private ArrayList<ArrayList<Double>> pOfWordInDifCate;//P(w|c)
	
	public CalculateP(TrainSet trainSet,ArrayList<String> features) {
		countOfWordsDifCate=trainSet.getCountOfWordsDifCate();
		featureCode=trainSet.getFeatureCode();
		calcP(features);
	}
	/**计算所有特征在所有类别中的概率，形成矩阵,必须在预测评论之前执行
	 * @param features
	 */
	private void calcP(ArrayList<String> features) {
		int c = countOfWordsDifCate.size();// 类别数
		int n = features.size();//特征数
		pOfWordInDifCate = new ArrayList<ArrayList<Double>>(c);
		for (int i = 0; i < c; i++) {
			ArrayList<Double> resList = new ArrayList<Double>(n);
			for (String string : features) {
				int index = featureCode.get(string);
				ArrayList<Integer> tmpCounts = countOfWordsDifCate.get(i);
				double p = (double) (tmpCounts.get(index) + 1)
						/ (tmpCounts.get(tmpCounts.size() - 1) + 1);
				resList.add(p);
			}
			pOfWordInDifCate.add(resList);
		}
	}
	
	/**
	 * @return the pOfWordInDifCate
	 */
	public ArrayList<ArrayList<Double>> getpOfWordInDifCate() {
		return pOfWordInDifCate;
	}
}
