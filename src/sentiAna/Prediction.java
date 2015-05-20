package sentiAna;

import java.util.ArrayList;
import java.util.HashMap;

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
	 * 每个特征在各个类中的概率P(f|c)
	 */
	private ArrayList<ArrayList<Double>> pOfWordInDifCate;
	/**
	 * 每个类的概率P(c)
	 */
	private ArrayList<Double> pOfACate;

	private MyLogger logger = new MyLogger("评论以及其特征的概率.txt");

	
	/**
	 * 无参数的构造函数
	 */
	public Prediction() {
	}

	/**
	 * 以前面的模型为参数的构造函数
	 * 
	 * @param model
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
	 * @return 类别的编号
	 */
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
	 * 预测多个评论的类别
	 * 
	 * @param reviews
	 *            待分类的评论集合
	 * @return 分类结果编号的序列
	 */
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
	 * 根据标记类别和预测类别来计算混淆矩阵
	 * 
	 * @param reviews
	 *            被预测的评论集合
	 * @param results
	 *            预测出的评论结果
	 * @param a
	 *            标记类别的数组
	 * @param b
	 *            预测类别的数组
	 * @return 混淆矩阵
	 */
	public int[][] genConfuMatrix(ArrayList<AnalReview> reviews,
			ArrayList<Integer> results, int a[], int b[][]) {// a实际分类（5）；b预测分类
		int size = results.size();
		int n = a.length;// 标记类别个数
		int n2 = b.length;// 预测分类个数
		int[][] ConfuMtr = new int[n][n2];// 每个元素自动初始化为0
		HashMap<Integer, Integer> catecode2cateNum_act = new HashMap<Integer, Integer>(
				n);// 建立标记类别和预测类别的对应关系，前者是标记类别，后者是预测类别编号
		for (int i = 0; i < n; i++) {
			catecode2cateNum_act.put(a[i], i);
		}
		// 统计标记类别和预测类别的混淆矩阵
		for (int i = 0; i < size; i++) {// 所有的评论
			int level = reviews.get(i).getLevel();// 标记类别
			int level_code = catecode2cateNum_act.get(level);// 标记类别对应的预测类别
			int result = results.get(i);// 预测类别的结果
			ConfuMtr[level_code][result] += 1;
		}
		return ConfuMtr;
	}

	/**
	 * 根据混淆矩阵计算precision、recall、F1、accuracy等值
	 * 
	 * @param k
	 *            第k次预测
	 * @param a
	 *            标记的分类数组
	 * @param b
	 *            预测的分类数组
	 * @param ConfuMtr
	 *            混淆矩阵
	 * @return
	 */
	public double[] statisticalRate(int k, int a[], int b[][], int ConfuMtr[][]) {
		int n = a.length;// 实际类别个数
		int n2 = b.length;// 预测分类个数
		double[] precision = new double[n2];
		double[] recall = new double[n2];
		double[] f1 = new double[n2];
		double accuracy;
		int correctSum = 0;// 预测正确的总个数
		int allSum = 0;// 评论总个数
		double precisionAvg = 0;// 多个类别的平均precision
		double recallAvg = 0;// 多个类别的平均recall
		double f1Avg = 0;// 多个类别的平均f1
		MyLogger logger = new MyLogger("结果" + k + ".txt");

		int[] correctNum = new int[n2];// 多个类别正确的个数
		int[] sumPre = new int[n2];// 多个类别预测的个数
		int[] sumAct = new int[n];// 多个类别实际标记的个数
		// 通过标记类别和预测类别的对应关系，统计是否预测正确，并统计预测和标记对应的类别的个数以及总个数
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n2; j++) {
				boolean corTag = false;
				for (int j2 = 0; j2 < b[j].length; j2++) {
					if (b[j][j2] == a[i]) {
						corTag = true;
						break;
					}
				}
				if (corTag)
					correctNum[j] += ConfuMtr[i][j];
				sumAct[i] += ConfuMtr[i][j];
				sumPre[j] += ConfuMtr[i][j];
				allSum += ConfuMtr[i][j];
				logger.info(ConfuMtr[i][j] + "\t");
			}
			logger.info("\r\n");
		}
		logger.info("\r\n");
		logger.info("precision\trecall\tf1\r\n");

		// 统计precision、recall、accuracy
		for (int j = 0; j < n2; j++) {
			precision[j] = (double) correctNum[j]
					/ (sumPre[j] + Double.MIN_VALUE);// 计算该类别的precision，避免分母为0
			int sumActi = 0;// 计算该预测类别的对应的标记类别的总文档数
			for (int j2 = 0; j2 < b[j].length; j2++) {
				for (int i = 0; i < sumAct.length; i++) {
					if (a[i] == b[j][j2]) {
						sumActi += sumAct[i];
					}
				}
			}
			recall[j] = (double) correctNum[j] / (sumActi + Double.MIN_VALUE);// 计算该类别的recall，避免分母为0
			f1[j] = 2 * precision[j] * recall[j]
					/ (precision[j] + recall[j] + Double.MIN_VALUE);// 计算该类别的F1，避免分母为0
			logger.info(precision[j] + "\t" + recall[j] + "\t" + f1[j] + "\r\n");

			correctSum += correctNum[j];
			double weight = pOfACate.get(j);// 第j个类别的权重

			// 计算加权平均的precision、recall、F1
			precisionAvg += precision[j] * weight;
			recallAvg += recall[j] * weight;
			f1Avg += f1[j] * weight;
		}
		accuracy = (double) correctSum / allSum;// 统计accuracy

		logger.info("\r\n");
		logger.info(precisionAvg + "\t" + recallAvg + "\t" + f1Avg);
		logger.info("\r\n");
		logger.info("\r\n");
		logger.info("correctSum\taccuracy\r\n");
		logger.info(correctSum + "\t" + accuracy);
		logger.info("\r\n");
		logger.info("\r\n");
		logger.info("\r\n");

		// 返回由precision、recall、F1和accuracy组成的数组
		double prfa[] = new double[4];
		prfa[0] = precisionAvg;
		prfa[1] = recallAvg;
		prfa[2] = f1Avg;
		prfa[3] = accuracy;
		return prfa;
	}

	/**
	 * @param pOfWordInDifCate
	 *            the pOfWordInDifCate to set
	 */
	public void setpOfWordInDifCate(
			ArrayList<ArrayList<Double>> pOfWordInDifCate) {
		this.pOfWordInDifCate = pOfWordInDifCate;
	}

	/**
	 * @param pOfACate
	 *            the pOfACate to set
	 */
	public void setpOfACate(ArrayList<Double> pOfACate) {
		this.pOfACate = pOfACate;
	}
}
