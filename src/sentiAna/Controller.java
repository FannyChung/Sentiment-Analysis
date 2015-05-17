package sentiAna;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import utils.AnalReview;
import utils.LoadEmotionRelated;
import utils.LoadStopWords;
import utils.MyLogger;

public class Controller {
	private AnalysisText textAnal = new AnalysisText();
	private Feature feature;
	private DateSet dateSet = new DateSet();
	private Model model;
	private int a[] = { 0, 1 };
	// {0,1}
	// { 1, 2, 3, 4, 5 }
	private int b[][] = { { 0 }, { 1 } };
	// { { 1 }, { 2 }, { 3 },{ 4 }, { 5 } } 0.566489925768823
	// { { 1,2 }, { 3}, { 4, 5 } } 0.7654294803817603
	// {{1,2},{3,4},{5}} 0.6294803817603394
	// {{1,2,3},{4,5}}
	// {{0},{1}}
	private ArrayList<String> features;
	private LoadEmotionRelated loadEmotionRelated = new LoadEmotionRelated();
	private LoadStopWords loadStopWords = new LoadStopWords();
	private WritableWorkbook book;
	private int sheetNum;// 当前表单编号
	private int dataSheetNum;

	private boolean DF_on;// 是否使用DF来过滤特征
	private boolean Stop_on;// 是否使用停用词过滤
	private boolean IG_on;// 是否使用信息增益过滤
	private int IG_Num;// 设置的信息增益过滤后的大小

	/**
	 * 打开excel文件
	 */
	public void openExcel() {
		InputStream stream;
		try {
			stream = new FileInputStream("tan.xls");// t.xls是收集到的评论信息所在文件
			Workbook wb = Workbook.getWorkbook(stream);
			dataSheetNum = wb.getNumberOfSheets();
			book = Workbook.createWorkbook(new File("result.xls"), wb);
			sheetNum = dataSheetNum;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 关闭excel文件
	 */
	public void closeExcel() {
		try {
			book.write();
			book.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 分词
	 */
	public void wordSegmentation() {
		getTextAnal().setAllEmotionRelatedWords(loadEmotionRelated
				.getAllEmotionRelated());
		getTextAnal().initNlpri();
		try {
			WritableSheet sheet;
			// 读表格中的信息
			for (int i = 0; i < dataSheetNum; i++) {
				sheet = book.getSheet(i);
				getTextAnal().dealSheetRev(sheet);
			}
			// test.printRes();

			// 写每条评论的词频和星级等信息
			sheet = book.createSheet("所有评论", sheetNum++);
			getTextAnal().writeReviews(sheet, getTextAnal().getReviews());
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
		getTextAnal().exitNlpir();
	}

	/**
	 * 特征选择
	 */
	public void featureSel() {
		WritableSheet sheet;
		model = new Model();
		model.analReviews(getTextAnal().getReviews());

		feature = new Feature();
		feature.sortByFreq(model.getFrequency());
		// featureSelection.delLessThan(3);// 删除出现低于3次的词
		// featureSelection.delTopK(20);
		features = feature.getFeatureString();
		System.out.println("initial featureSize: " + features.size());

		try {
			// 将词频信息写入表单中
			sheet = book.createSheet("总词频", sheetNum++);
			feature.writeFeature(sheet);

			// 删除停用词
			if (Stop_on) {
				loadEmotionRelated.regenWordLists(features);
				loadStopWords.filtEmotionWords(loadEmotionRelated
						.getAllEmotionRelated());// 停用词不包含情感词
				feature.removeStopWords(loadStopWords.getStopWords());
				System.out.println("after StopWords: featureSize "
						+ features.size());
				sheet = book.createSheet("StopWords特征筛选后", sheetNum++);
				feature.writeFeature(sheet);
			}

			model.separateReviewsByLevel(getTextAnal().getReviews(), b);
			model.countFeatureInCates(features);// 还没有向量化

			// 文档频率过滤
			if (DF_on) {
				feature.removeByDF(model.getFeatureCount(), 3);
				System.out.println("after DF: featureSize "
						+ feature.getFeatureString().size());

				sheet = book.createSheet("DF特征筛选后", sheetNum++);
				feature.writeFeature(sheet);
			}

			// 信息增益过滤
			if (IG_on) {
				features = feature.IGSelection(features, IG_Num,
						model);
				System.out.println("after IG: featureSize " + features.size());

				sheet = book.createSheet("IG特征筛选后", sheetNum++);
				feature.writeFeature(sheet);
			}

		} catch (WriteException e) {
			e.printStackTrace();
		}

		dateSet.genFeatureVectors(getTextAnal().getReviews(), features);
	}

	/**
	 * 选择训练集
	 */
	public void seleTrainSet(int k) {
		dateSet.seleTrain(k, getTextAnal().getReviews());
	}

	public void training(ArrayList<AnalReview> trainData) {
		model.separateReviewsByLevel(trainData, b);
		// countNum.countFeatureInCates(features);
		model.countFeatureInCates();// 向量化后的计数

		model.calcPc();
		model.calcPfc();
	}

	public double[] predict(int k, ArrayList<AnalReview> testData) {
		Predict predict = new Predict(model);
		WritableSheet sheet;
		// ArrayList<Integer> results = predict.predictRevs(testData, features);
		ArrayList<Integer> results = predict.predictRevs(testData);// 向量化后

		try {
			sheet = book.createSheet("预测_测试集" + k, sheetNum++);
			predict.writePredResult(testData, sheet, results, b);
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
		double[] prfa = predict.statisticalRate(k, a, b,
				predict.genConfuMatrix(testData, results, a, b));
		return prfa;
	}

	/**
	 * @param dF_on
	 *            the dF_on to set
	 */
	public void setDF_on(boolean dF_on) {
		DF_on = dF_on;
	}

	/**
	 * @param stop_on
	 *            the stop_on to set
	 */
	public void setStop_on(boolean stop_on) {
		Stop_on = stop_on;
	}

	/**
	 * @param iG_on
	 *            the iG_on to set
	 */
	public void setIG_on(boolean iG_on) {
		IG_on = iG_on;
	}

	/**
	 * @param iG_Num
	 *            the iG_Num to set
	 */
	public void setIG_Num(int iG_Num) {
		IG_Num = iG_Num;
	}


	/**
	 * @return the trainSet
	 */
	public DateSet getDataSet() {
		return dateSet;
	}

	public static void main(String[] args) {
		int repeatTimes = 5;
		double total[] = new double[5];
		for (int p = 0; p < repeatTimes; p++) {
			long a = System.currentTimeMillis();
			Controller controller = new Controller();
			controller.openExcel();

			controller.setStop_on(true);// 停用词
			controller.setDF_on(false);// DF
			controller.setIG_on(false);// IG
			controller.setIG_Num(2000);

			controller.wordSegmentation();
			controller.featureSel();

			int k = 5;
			controller.seleTrainSet(k);
			double sumPre = 0;
			double sumRecal = 0;
			double sumF1 = 0;
			double sumAccu = 0;
			for (int i = 0; i < k; i++) {
				ArrayList<AnalReview> trainAnalReviews = new ArrayList<AnalReview>(
						k - 1);
				for (int j = 0; j < k; j++) {
					if (i == j)
						continue;
					trainAnalReviews.addAll(controller.getDataSet()
							.getkLists().get(j));
				}
				controller.training(trainAnalReviews);
				double[] prfa = controller.predict(i, controller.getDataSet()
						.getkLists().get(i));
				sumPre += prfa[0];
				sumRecal += prfa[1];
				sumF1 += prfa[2];
				sumAccu += prfa[3];
			}
			DecimalFormat decimalFormat = new DecimalFormat("#.00");
			MyLogger logger = new MyLogger("结果.txt");
			logger.info("precision\t\trecall\t\tF1\t\taccuracy\r\n");
			logger.info(decimalFormat.format(sumPre / k * 100) + "%\t"
					+ decimalFormat.format(sumRecal / k * 100) + "%\t"
					+ decimalFormat.format(sumF1 / k * 100) + "%\t"
					+ decimalFormat.format(sumAccu / k * 100) + "%\t");

			controller.closeExcel();
			logger.info((System.currentTimeMillis() - a) + " ms ");
			total[0] += (sumPre / k);
			total[1] += (sumRecal / k);
			total[2] += (sumF1 / k);
			total[3] += (sumAccu / k);
			total[4] += (System.currentTimeMillis() - a);
		}

		for (int i = 0; i < total.length; i++) {
			System.out.print(total[i] / repeatTimes + "\t");
		}

	}

	/**
	 * @return the textAnal
	 */
	public AnalysisText getTextAnal() {
		return textAnal;
	}

	/**
	 * @param textAnal the textAnal to set
	 */
	public void setTextAnal(AnalysisText textAnal) {
		this.textAnal = textAnal;
	}

	/**
	 * @param a the a to set
	 */
	public void setA(int[] a) {
		this.a = a;
	}

	/**
	 * @param b the b to set
	 */
	public void setB(int[][] b) {
		this.b = b;
	}

	/**
	 * @return the feature
	 */
	public Feature getFeature() {
		return feature;
	}

	/**
	 * @return the model
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * @param model the model to set
	 */
	public void setModel(Model model) {
		this.model = model;
	}
}
