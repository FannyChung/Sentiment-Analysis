package textManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
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
	private FeatureSelection featureSelection = new FeatureSelection();
	private WritableWorkbook book;

	public void openExcel() {
		InputStream stream;
		try {
			stream = new FileInputStream("MyData.xls");
			Workbook wb = Workbook.getWorkbook(stream);
			book = Workbook.createWorkbook(new File("result.xls"), wb);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

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
			for (int i = 0; i < 4; i++) {
				sheet = book.getSheet(i);
				textAnal.dealSheetRev(sheet);
			}
			// test.printRes();
			textAnal.analAll(textAnal.getFrequency());

			// 将词频信息写入表单中
			sheet = book.createSheet("总词频", 4);
			textAnal.writeFrequecy(sheet);

			// 写每条评论的词频和星级等信息
			sheet = book.createSheet("所有评论", 5);
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
		featureSelection.sortByFreq(textAnal.getFrequency());
		featureSelection.delLessThan(2);
		featureSelection.delTopK(10);
		WritableSheet sheet = book.createSheet("筛选后的特征", 6);
		try {
			featureSelection.writeFeature(sheet);
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 选择训练集
	 */
	public void seleTrainSet() {
		TrainSet trainSet = new TrainSet();
		int a[] = { 1,  3,  5 };
		// trainSet.calCategory(a,
		// textAnal.getReviews());//无需统计每个类别的个数，因为已经指定是200
		trainSet.seleTrain(a, 200, textAnal.getReviews());
		int i = 0;
		for (; i < a.length; i++) {
			WritableSheet sheet = book.createSheet("选择的训练集" + (i + 1), 7 + i);
			try {
				textAnal.writeReviews(sheet, trainSet.getDiffCateTrainSet()
						.get(i));
			} catch (WriteException e) {
				e.printStackTrace();
			}
		}
		ArrayList<String> features = featureSelection.getFeatureString();
		trainSet.countAll(features);
		WritableSheet sheet = book.createSheet("词在不同类别中出现的次数", 7 + i);
		i++;
		try {
			trainSet.writeCount(sheet, features);
			trainSet.makefeatureCode(features);
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		CalculateP calculateP = new CalculateP(trainSet, features);
		Predict predict = new Predict(trainSet, calculateP);
		ArrayList<Integer> results = predict.predictRevs(trainSet.getTestSet(),
				features);
		try {
			sheet = book.createSheet("预测结果", 7 + i);
			i++;
			predict.writePredResult(trainSet.getTestSet(), sheet, results, a);
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Controller controller = new Controller();
		controller.openExcel();

		controller.wordSegmentation();
		controller.featureSel();
		controller.seleTrainSet();

		controller.closeExcel();
	}
}
