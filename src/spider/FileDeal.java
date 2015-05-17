/**
 * 
 */
package spider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * @author ZhongFang
 *
 */
public class FileDeal {
	private WritableWorkbook writeBook;
	private Workbook readBook;

	public int openReadFile(String fileName) {
		int sheetNum = 0;
		try {
			InputStream stream = new FileInputStream(fileName);
			readBook = Workbook.getWorkbook(stream);
			sheetNum = readBook.getNumberOfSheets();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sheetNum;
	}

	/**
	 * @param sheetNum
	 *            表单编号
	 * @return 二维String数组，表单的内容
	 */
	public String[][] readSheet(int sheetNum) {
		Sheet sheet = readBook.getSheet(sheetNum);
		int rowC = sheet.getRows();
		int colC = sheet.getColumns();
		String[][] data = new String[rowC][colC];
		for (int i = 0; i < rowC; i++) {
			for (int j = 0; j < colC; j++) {
				data[i][j] = sheet.getCell(j, i).getContents();
			}
		}
		return data;
	}

	public String[][] readBook() {
		Sheet sheet = readBook.getSheet(0);

		int sheetCount = readBook.getNumberOfSheets();
		int colC = sheet.getColumns();
		int rowSum = 0;
		int rowC[] = new int[sheetCount];
		for (int i = 0; i < sheetCount; i++) {
			rowC[i] = readBook.getSheet(i).getRows();
			rowSum += rowC[i];
		}

		int rowNow = 0;
		String[][] data = new String[rowSum][colC];
		for (int k = 0; k < sheetCount; k++) {
			sheet = readBook.getSheet(k);
			for (int i = 0; i < rowC[k]; i++) {
				for (int j = 0; j < colC; j++) {
					data[i+rowNow][j] = sheet.getCell(j, i).getContents();
				}
			}
			rowNow+=rowC[k];
		}
		return data;
	}

	/**
	 * 打印所有的评论
	 *
	 * @param reviews
	 * @param sheet
	 *            xls文件中的一个sheet表
	 * @throws RowsExceededException
	 * @throws WriteException
	 */
	public void wirteReviews(Vector<Review> reviews, WritableSheet sheet)
			throws RowsExceededException, WriteException {
		int col = 0;
		Label newLabel;
		// newLabel=new Label(0,0,"文本");
		for (Review review : reviews) {
			newLabel = new Label(0, col, review.getText());
			sheet.addCell(newLabel);
			newLabel = new Label(1, col, review.getLevel() + "");
			sheet.addCell(newLabel);
			newLabel = new Label(2, col, review.getReTitle());
			sheet.addCell(newLabel);
			newLabel = new Label(3, col, review.getTime().toString());
			sheet.addCell(newLabel);
			newLabel = new Label(4, col, review.getUserName());
			sheet.addCell(newLabel);
			col++;
			System.out.println("excel--------------------------------" + col);
		}
	}

	/**
	 * 打开excel文件
	 *
	 * @param fileName
	 */
	public void openWriteFile(String fileName) {
		File file = new File(fileName);
		try {
			writeBook = Workbook.createWorkbook(file);
		} catch (IOException e) {
			System.err.println("excel表打开失败");
			e.printStackTrace();
		}
	}

	/**
	 * 关闭excel文件
	 */
	public void closeWriteFile() {
		try {
			writeBook.write();
			writeBook.close();
		} catch (IOException e) {
			System.err.println("excel表写入失败");
			e.printStackTrace();
		} catch (WriteException e) {
			System.err.println("excel表关闭失败");
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			System.err.println("没有创建表单");
		}
	}

	/**
	 * 获取excel表对象
	 *
	 * @return
	 */
	public WritableWorkbook getBook() {
		return writeBook;
	}
}
