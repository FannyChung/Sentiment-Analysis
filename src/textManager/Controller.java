package textManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import utils.AnalReview;
import utils.LoadEmotionRelated;
import utils.LoadStopWords;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class Controller {
	private AnalysisText textAnal = new AnalysisText();
	private FeatureSelection featureSelection;
	private TrainSet trainSet = new TrainSet();
	private CalculateP calculateP;
	private CountNum countNum;
	private int a[] = { 1, 2, 3, 4, 5 };
	private int b[] = { 1, 3, 5 };
	private ArrayList<String> features;
	private LoadEmotionRelated loadEmotionRelated = new LoadEmotionRelated();
	private LoadStopWords loadStopWords = new LoadStopWords();
	private WritableWorkbook book;
	private int sheetNum;// 当前表单编号
	private final int dataSheetNum = 5;

	/**
	 * 打开excel文件
	 */
	public void openExcel() {
		InputStream stream;
		try {
			stream = new FileInputStream("t.xls");
			Workbook wb = Workbook.getWorkbook(stream);
			book = Workbook.createWorkbook(new File("result.xls"), wb);// result.xls是收集到的评论信息所在文件
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
		textAnal.initNlpri();
		try {
			WritableSheet sheet;
			// 读表格中的信息
			for (int i = 0; i < dataSheetNum; i++) {
				sheet = book.getSheet(i);
				textAnal.dealSheetRev(sheet);
			}
			// test.printRes();

			// 写每条评论的词频和星级等信息
			sheet = book.createSheet("所有评论", sheetNum++);
			textAnal.writeReviews(sheet, textAnal.getReviews());
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
		textAnal.exitNlpir();
	}

	/**
	 * 特征选择
	 */
	public void featureSel() {
		WritableSheet sheet;
		countNum = new CountNum();
		countNum.analReviews(textAnal.getReviews());

		featureSelection = new FeatureSelection();
		featureSelection.sortByFreq(countNum.getFrequency());
//		featureSelection.delLessThan(3);// 删除出现低于3次的词
		// featureSelection.delTopK(20);
		features = featureSelection.getFeatureString();
		System.out.println("featureSize " + features.size());

		// 删除停用词
		loadEmotionRelated.regenWordLists(features);
		loadStopWords.filtEmotionWords(loadEmotionRelated
				.getAllEmotionRelated());// 停用词不包含情感词
		featureSelection.removeStopWords(loadStopWords.getStopWords());
		System.out.println("after StopWords: featureSize " + features.size());

		sheet = book.createSheet("第一次特征筛选后", sheetNum++);
		try {
			featureSelection.writeFeature(sheet);

			
			countNum.separateReviewsByLevel(textAnal.getReviews(), a);
			countNum.countFeatureInCates(features);
			
			featureSelection.removeByDF(countNum.getFeatureCount(), 3);
			System.out.println("after DF: featureSize " + featureSelection.getFeatureString().size());
			// 信息增益过滤
			features = featureSelection.IGSelection(features, 2000, countNum);
			System.out.println("after IG: featureSize " + features.size());

			// 将词频信息写入表单中
			sheet = book.createSheet("总词频", sheetNum++);
			countNum.writeFrequecy(sheet);
		} catch (WriteException e) {
			e.printStackTrace();
		}

		// TODO 向量化，为每个review添加向量，boolean

	}

	/**
	 * 选择训练集
	 */
	public void seleTrainSet(int k) {
		// trainSet.seleTrain(a, 300, textAnal.getReviews());
		// trainSet.seleTrain(a, 0.8, textAnal.getReviews());
		trainSet.seleTrain(k, a, textAnal.getReviews());
	}

	public void training(ArrayList<AnalReview> trainData) {
		countNum.separateReviewsByLevel(trainData, a);
		countNum.countFeatureInCates(features);

		calculateP = new CalculateP(trainData.size(), countNum);
		calculateP.calcPc();
		calculateP.calcPfc();
	}

	public double[] predict(int k, ArrayList<AnalReview> testData) {
		Predict predict = new Predict(calculateP);
		WritableSheet sheet;
		ArrayList<Integer> results = predict.predictRevs(testData, features);
		;
		try {
			sheet = book.createSheet("预测_测试集" + k, sheetNum++);
			predict.writePredResult(testData, sheet, results, a);
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
		double[] prfa = predict.statisticalRate(k, a, a,
				predict.genConfuMatrix(testData, results, a, a));
		return prfa;
	}

	public static void main(String[] args) {
		long a=System.currentTimeMillis();
		Controller controller = new Controller();
		controller.openExcel();

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
				trainAnalReviews.addAll(controller.trainSet.getkLists().get(j));
			}
			controller.training(trainAnalReviews);
			double[] prfa = controller.predict(i, controller.trainSet
					.getkLists().get(i));
			sumPre += prfa[0];
			sumRecal += prfa[1];
			sumF1 += prfa[2];
			sumAccu += prfa[3];
		}
		System.out.println("precision\t\trecall\t\tF1\t\taccuracy");
		System.out.println(sumPre / k + "\t" + sumRecal / k + "\t" + sumF1 / k
				+ "\t" + sumAccu / k);

		controller.closeExcel();
		System.out.println("\r执行耗时 : "+(System.currentTimeMillis()-a)+" ms ");
	}
}
