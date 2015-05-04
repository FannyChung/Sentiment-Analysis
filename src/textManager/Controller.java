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

	private WritableWorkbook book;
	private int sheetNum;
	private int dataSheetNum = 5;

	/**
	 * 打开excel文件
	 */
	public void openExcel() {
		InputStream stream;
		try {
			stream = new FileInputStream("t.xls");
			Workbook wb = Workbook.getWorkbook(stream);
			book = Workbook.createWorkbook(new File("result.xls"), wb);
			sheetNum = dataSheetNum - 1;
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
		featureSelection.delLessThan(3);//删除高于
		featureSelection.delTopK(10);
		features = featureSelection.getFeatureString();
		sheet = book.createSheet("第一次特征筛选后", sheetNum++);
		try {
			featureSelection.writeFeature(sheet);

			countNum.splitReviewsByLev(textAnal.getReviews(), a);
			countNum.countFeatureInCates(features);
			features = featureSelection.IGSelection(features, 1000, countNum);

			// 将词频信息写入表单中
			sheet = book.createSheet("总词频", sheetNum++);
			countNum.writeFrequecy(sheet);
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 选择训练集
	 */
	public void seleTrainSet() {
		// trainSet.seleTrain(a, 300, textAnal.getReviews());
		trainSet.seleTrain(a, 0.8, textAnal.getReviews());

		WritableSheet sheet;
		for (int i = 0; i < a.length; i++) {
			sheet = book.createSheet("选择的训练集" + (i + 1), sheetNum++);
			try {
				textAnal.writeReviews(sheet, trainSet.getDiffCateTrainSet()
						.get(i));
			} catch (WriteException e) {
				e.printStackTrace();
			}
		}
	}

	public void training() {
		countNum.splitReviewsByLev(trainSet.getAllTrainSet(), a);
		countNum.countFeatureInCates(features);

		calculateP = new CalculateP(trainSet, countNum);
		calculateP.calcPc();
		calculateP.calcPfc();
		WritableSheet sheet;

		sheet = book.createSheet("词在不同类别中出现的次数", sheetNum++);
		try {
			countNum.writeCount(sheet, features,
					calculateP.getpOfWordInDifCate());
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	public void predict() {
		Predict predict = new Predict(calculateP);
		WritableSheet sheet;
		ArrayList<Integer> results;
		try {
			results = predict.predictRevs(trainSet.getTestSet(), features);
			sheet = book.createSheet("预测_测试集", sheetNum++);
			predict.writePredResult(trainSet.getTestSet(), sheet, results, a);
			predict.statisticalRate(a, a, predict.genConfuMatrix(
					trainSet.getTestSet(), results, a, a));

			results = predict.predictRevs(trainSet.getAllTrainSet(), features);
			sheet = book.createSheet("预测_训练集", sheetNum++);
			predict.writePredResult(trainSet.getAllTrainSet(), sheet, results,
					a);
			predict.statisticalRate(a, a, predict.genConfuMatrix(
					trainSet.getAllTrainSet(), results, a, a));

		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
		System.out.println("featureSize" + features.size());
	}

	public static void main(String[] args) {
		Controller controller = new Controller();
		controller.openExcel();

		controller.wordSegmentation();
		controller.featureSel();
		controller.seleTrainSet();
		controller.training();
		controller.predict();

		controller.closeExcel();
	}
}
