package textManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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

	public void openExcel() throws BiffException, IOException {
		InputStream stream = new FileInputStream(
				"C:\\Users\\hp\\Desktop\\MyData.xls");
		Workbook wb = Workbook.getWorkbook(stream);
		book = Workbook.createWorkbook(new File("result.xls"), wb);
	}

	public void closeExcel() {
		try {
			book.write();
			book.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
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

	public void featureSel() throws BiffException, IOException,
			RowsExceededException, WriteException {
		featureSelection.sortByFreq(textAnal.getFrequency());
		featureSelection.delLessThan(2);
		featureSelection.delTopK(10);
		WritableSheet sheet = book.createSheet("筛选后的特征", 6);
		featureSelection.writeFeature(sheet);
	}

	public void seleTrainSet() throws RowsExceededException, WriteException {
		TrainSet trainSet = new TrainSet();
		int a[] = { 1, 3, 5 };
		trainSet.calCategory(a, textAnal.getReviews());
		trainSet.seleTrain(a, 200, textAnal.getReviews());
		WritableSheet sheet = book.createSheet("选择的训练集", 7);
		textAnal.writeReviews(sheet, trainSet.getTrainRev());
	}

	public static void main(String[] args) {
		Controller controller = new Controller();

		try {
			controller.openExcel();
			controller.wordSegmentation();

			controller.featureSel();

			controller.seleTrainSet();
			
			controller.closeExcel();
		} catch (BiffException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
