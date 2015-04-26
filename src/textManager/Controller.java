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
	private FeatureSelection featureSelection ;
	private TrainSet trainSet = new TrainSet();
	CalculateP calculateP;
	CountNum countNum;
	ArrayList<String> aftFt;
	int a[] = { 1, 2, 3, 4, 5 };
	ArrayList<String> features;

	private WritableWorkbook book;
	private int sheetNum;
	private int dataSheetNum = 5;

	public void openExcel() {
		InputStream stream;
		try {
			stream = new FileInputStream("t.xls");
			Workbook wb = Workbook.getWorkbook(stream);
			book = Workbook.createWorkbook(new File("result.xls"), wb);
			sheetNum = dataSheetNum - 1;
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
	 * 选择训练集
	 */
	public void seleTrainSet() {

		trainSet.seleTrain(a, 200, textAnal.getReviews());
		// trainSet.seleTrain(a, 0.8, textAnal.getReviews());
		WritableSheet sheet;
		for (int i = 0; i < a.length; i++) {
			sheet = book.createSheet("选择的训练集" + (i + 1),
					sheetNum++);
			try {
				textAnal.writeReviews(sheet, trainSet.getDiffCateTrainSet()
						.get(i));
			} catch (WriteException e) {
				e.printStackTrace();
			}
		}

		countNum = new CountNum(trainSet);
		// ArrayList<String> features = featureSelection.getFeatureString();
		features = countNum.getFeatureString();
		countNum.countAll(features);
		countNum.analAll(countNum.getFrequency(),textAnal.getReviews());
		try {
		// 将词频信息写入表单中
		sheet = book.createSheet("总词频", sheetNum++);
		countNum.writeFrequecy(sheet);
		
		sheet = book.createSheet("词在不同类别中出现的次数", sheetNum++);
		
			countNum.writeCount(sheet, features);
		} catch (WriteException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 特征选择
	 */
	public void featureSel() {
		featureSelection= new FeatureSelection(trainSet, countNum);
		featureSelection.sortByFreq(textAnal.getFrequency());
		featureSelection.delLessThan(2);
		featureSelection.delTopK(10);
		
		WritableSheet sheet = book.createSheet("筛选后的特征", sheetNum++);
		try {
			featureSelection.writeFeature(sheet);
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void training() {
		calculateP = new CalculateP(trainSet, countNum);
		// ArrayList<String> aftFt=calculateP.IGSelection(features, 4000);
		aftFt = features;

		calculateP.calcPc();
		calculateP.calcPfc(aftFt);
	}

	public void predict() {
		Predict predict = new Predict(trainSet, calculateP);
		WritableSheet sheet;
		try {
			ArrayList<Integer> results = predict.predictRevs(
					trainSet.getTestSet(), aftFt);
			sheet = book.createSheet("预测_测试集", sheetNum++);
			predict.writePredResult(trainSet.getTestSet(), sheet, results, a);

			results = predict.predictRevs(trainSet.getAllTrainSet(), aftFt);
			sheet = book.createSheet("预测_训练集", sheetNum++);
			predict.writePredResult(trainSet.getAllTrainSet(), sheet, results,
					a);
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
		controller.seleTrainSet();
		controller.featureSel();
		controller.training();
		controller.predict();

		controller.closeExcel();
	}
}
