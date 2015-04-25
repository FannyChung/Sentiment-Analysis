package textManager;

import java.util.ArrayList;
import java.util.HashMap;

import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * 利用已经计算出来的各种概率，来进行分类
 * 
 * @author hp
 *
 */
public class Predict {
	private ArrayList<ArrayList<Double>> pOfWordInDifCate;// P(w|c)

	private ArrayList<ArrayList<Integer>> countOfWordsDifCate;
	private HashMap<String, Integer> featureCode;
	MyLogger logger=new MyLogger("评论以及其特征的概率.txt");

	public Predict(TrainSet trainSet, CalculateP calculateP) {
		countOfWordsDifCate = trainSet.getCountOfWordsDifCate();
		featureCode = trainSet.getFeatureCode();
		pOfWordInDifCate = calculateP.getpOfWordInDifCate();
	}

	/**
	 * 预测一条评论的类别
	 * 
	 * @param review
	 *            要预测的评论
	 * @param features
	 *            作为特征的词序列
	 * @return 类别的编号
	 */
	private int predict(AnalReview review, ArrayList<String> features) {
		int n = countOfWordsDifCate.size();//类别数
		int resultIndex = 0;
		double finalP = -Double.MAX_VALUE;
		logger.info(review.getText().substring(0, 1)+"\t");
		for (int i = 0; i < n; i++) {
			double p = 1;
			HashMap<String, Integer> reviewWords = review.getFrequency();
			for (String string : features) {
				int index=featureCode.get(string);
				if (reviewWords.containsKey(string)) {
					p+=Math.log(pOfWordInDifCate.get(i).get(index));
//					p*=pOfWordInDifCate.get(i).get(index);
				} else {
					p+=Math.log(1-pOfWordInDifCate.get(i).get(index));
//					p*=(1-pOfWordInDifCate.get(i).get(index));
				}
			}
			if (finalP < p) {
				finalP = p;
				resultIndex = i;
			}
			logger.info("ca"+i+"\t"+p+"\t");
		}
		logger.info("\r\n");
		return resultIndex;
	}

	/**
	 * 预测多个评论的结果
	 * 
	 * @param reviews
	 *            待分类的评论集合
	 * @param features
	 *            给定的特征词集合
	 * @return 分类结果编号的序列
	 */
	public ArrayList<Integer> predictRevs(ArrayList<AnalReview> reviews,
			ArrayList<String> features) {
		ArrayList<Integer> results = new ArrayList<Integer>();
		for (AnalReview analReview : reviews) {
			results.add(predict(analReview, features));
		}
		return results;
	}

	/**
	 * 预测给定评论集合，并打印结果
	 * 
	 * @param reviews
	 * @param sheet
	 * @param results
	 * @param a
	 * @throws RowsExceededException
	 * @throws WriteException
	 */
	public void writePredResult(ArrayList<AnalReview> reviews,
			WritableSheet sheet, ArrayList<Integer> results, int a[])
			throws RowsExceededException, WriteException {
		int i = 0;
		Label label;
		for (AnalReview analReview : reviews) {
			label = new Label(0, i, analReview.getText());
			sheet.addCell(label);

			int oriLevel = analReview.getLevel();
			label = new Label(1, i, oriLevel + "");
			sheet.addCell(label);

			int predLevel = a[results.get(i)];
			label = new Label(2, i, predLevel + "");
			sheet.addCell(label);

			int diff = Math.abs(oriLevel - predLevel);
			label = new Label(3, i, diff + "");
			sheet.addCell(label);

			i++;
		}
	}
}
