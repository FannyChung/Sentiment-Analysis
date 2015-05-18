package sentiAna;

import java.util.ArrayList;
import java.util.HashMap;

import utils.AnalReview;
import utils.MyLogger;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * 利用已经计算出来的各种概率，来进行分类
 * 
 * @author ZhongFang
 *
 */
public class Prediction {
	/**
	 * 
	 */
	private ArrayList<ArrayList<Double>> pOfWordInDifCate;// P(f|c)
	/**
	 * 
	 */
	private ArrayList<Double> pOfACate;// P(c)

	MyLogger logger = new MyLogger("评论以及其特征的概率.txt");

	/**
	 * 
	 */
	public Prediction() {
	}

	/**
	 * @param calculateP
	 */
	public Prediction(Model model) {
		pOfWordInDifCate = model.getpOfWordInDifCate();
		pOfACate = model.getpOfACate();
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
		int c = pOfWordInDifCate.size();// 类别数
		int n = features.size();
		int resultIndex = 0;
		double finalP = -Double.MAX_VALUE;
		logger.info(review.getText().substring(0, 1) + "\t");
		for (int cateIndex = 0; cateIndex < c; cateIndex++) {
			double p = Math.log(pOfACate.get(cateIndex));
			HashMap<String, Integer> reviewWords = review.getFrequency();// 该评论的词集
			for (int featureIndex = 0; featureIndex < n; featureIndex++) {
				if (reviewWords.containsKey(features.get(featureIndex))) {
					p += Math.log(pOfWordInDifCate.get(cateIndex).get(
							featureIndex));
				} else {
					p += Math.log(1 - pOfWordInDifCate.get(cateIndex).get(
							featureIndex));
				}
			}
			if (finalP < p) {
				finalP = p;
				resultIndex = cateIndex;
			}
			logger.info("ca" + cateIndex + "\t" + p + "\t");
		}
		logger.info("\r\n");
		return resultIndex;
	}

	public int predict(AnalReview review) {
		int resultIndex = 0;
		int c = pOfWordInDifCate.size();// 类别数
		double finalP = -Double.MAX_VALUE;
		for (int cateIndex = 0; cateIndex < c; cateIndex++) {
			double p = Math.log(pOfACate.get(cateIndex));
			boolean[] feVec = review.getFeatureVector();
			for (int featureIndex = 0; featureIndex < feVec.length; featureIndex++) {
				if (feVec[featureIndex]) {
					p += Math.log(pOfWordInDifCate.get(cateIndex).get(
							featureIndex));
				} else {
					p += Math.log(1 - pOfWordInDifCate.get(cateIndex).get(
							featureIndex));
				}
			}
			if (finalP < p) {
				finalP = p;
				resultIndex = cateIndex;
			}
			logger.info("ca" + cateIndex + "\t" + p + "\t");
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

	public ArrayList<Integer> predictRevs(ArrayList<AnalReview> reviews) {
		ArrayList<Integer> results = new ArrayList<Integer>();
		for (AnalReview analReview : reviews) {
			results.add(predict(analReview));// 向量化后的预测方法
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
			WritableSheet sheet, ArrayList<Integer> results, int a[][])
			throws RowsExceededException, WriteException {
		int i = 0;
		Label label;
		for (AnalReview analReview : reviews) {
			label = new Label(0, i, analReview.getText());
			sheet.addCell(label);

			int oriLevel = analReview.getLevel();
			label = new Label(1, i, oriLevel + "");
			sheet.addCell(label);

			int predLevel = a[results.get(i)][0];
			label = new Label(2, i, predLevel + "");
			sheet.addCell(label);

			int diff = Math.abs(oriLevel - predLevel);
			label = new Label(3, i, diff + "");
			sheet.addCell(label);

			i++;
		}
	}

	/**
	 * 计算混淆矩阵
	 * 
	 * @param reviews
	 *            被预测的评论集合
	 * @param results
	 *            预测出的评论结果
	 * @param a
	 *            标记的分类数组
	 * @param b
	 *            预测的分类数组
	 * @return 混淆矩阵
	 */
	public int[][] genConfuMatrix(ArrayList<AnalReview> reviews,
			ArrayList<Integer> results, int a[], int b[][]) {// a实际分类（5）；b预测分类
		int size = results.size();
		int n = a.length;
		int n2 = b.length;// 预测分类个数
		int[][] ConfuMtr = new int[n][n2];// 默认每个元素为0
		HashMap<Integer, Integer> catecode2cateNum_act = new HashMap<Integer, Integer>(
				n);
		for (int i = 0; i < n; i++) {
			catecode2cateNum_act.put(a[i], i);
		}
		// 统计个数
		for (int i = 0; i < size; i++) {// 所有的真实结果
			int level = reviews.get(i).getLevel();
			int level_code = catecode2cateNum_act.get(level);
			int result = results.get(i);
			ConfuMtr[level_code][result] += 1;
		}
		return ConfuMtr;
	}

	/**
	 * 根据混淆矩阵计算precision、accuracy、recall、F1等值
	 * 
	 * @param a
	 *            标记的分类数组
	 * @param b
	 *            预测的分类数组
	 * @param ConfuMtr
	 *            混淆矩阵
	 */
	public double[] statisticalRate(int k, int a[], int b[][], int ConfuMtr[][]) {
		int n = a.length;
		int n2 = b.length;// 预测分类个数
		double[] precision = new double[n2];
		double[] recall = new double[n2];
		double[] f1 = new double[n2];
		double accuracy;
		int correctSum = 0;
		int allSum = 0;// 评论总个数
		double precisionAvg = 0;
		double recallAvg = 0;
		double f1Avg = 0;
		MyLogger logger2 = new MyLogger("结果" + k + ".txt");

		int[] correctNum = new int[n2];
		int[] sumPre = new int[n2];
		int[] sumAct = new int[n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n2; j++) {
				boolean corTag=false;
				for (int j2 = 0; j2 < b[j].length; j2++) {
					if(b[j][j2]==a[i]){
						corTag=true;
						break;
					}
				}
				if (corTag)
					correctNum[j] += ConfuMtr[i][j];
				sumAct[i] += ConfuMtr[i][j];
				sumPre[j] += ConfuMtr[i][j];
				allSum += ConfuMtr[i][j];
				logger2.info(ConfuMtr[i][j] + "\t");
			}
			logger2.info("\r\n");
		}
		logger2.info("\r\n");
		logger2.info("precision\trecall\tf1\r\n");

		for (int j = 0; j < n2; j++) {
			precision[j] = (double) correctNum[j]
					/ (sumPre[j] + Double.MIN_VALUE);// 避免分母为0
			int sumActi=0;
			for (int j2 = 0; j2 < b[j].length; j2++) {
				for (int i = 0; i < sumAct.length; i++) {
					if(a[i]==b[j][j2]){
						sumActi+=sumAct[i];
					}
				}
			}
			recall[j] = (double) correctNum[j] / ( sumActi+ Double.MIN_VALUE);// 避免分母为0
			f1[j] = 2 * precision[j] * recall[j]
					/ (precision[j] + recall[j] + Double.MIN_VALUE);// 避免分母为0
			logger2.info(precision[j] + "\t" + recall[j] + "\t" + f1[j]
					+ "\r\n");
			correctSum += correctNum[j];

			double weight = pOfACate.get(j);// 第i个类别的权重
			precisionAvg += precision[j] * weight;
			recallAvg += recall[j] * weight;
			f1Avg += f1[j] * weight;
		}
		logger2.info("\r\n");
		logger2.info(precisionAvg + "\t" + recallAvg + "\t" + f1Avg);
		logger2.info("\r\n");
		logger2.info("\r\n");
		accuracy = (double) correctSum / allSum;
		logger2.info("correctSum\taccuracy\r\n");
		logger2.info(correctSum + "\t" + accuracy);
		logger2.info("\r\n");
		logger2.info("\r\n");
		logger2.info("\r\n");

		double prfa[] = new double[4];
		prfa[0] = precisionAvg;
		prfa[1] = recallAvg;
		prfa[2] = f1Avg;
		prfa[3] = accuracy;
		return prfa;
	}

	/**
	 * @param pOfWordInDifCate the pOfWordInDifCate to set
	 */
	public void setpOfWordInDifCate(ArrayList<ArrayList<Double>> pOfWordInDifCate) {
		this.pOfWordInDifCate = pOfWordInDifCate;
	}

	/**
	 * @param pOfACate the pOfACate to set
	 */
	public void setpOfACate(ArrayList<Double> pOfACate) {
		this.pOfACate = pOfACate;
	}
}
