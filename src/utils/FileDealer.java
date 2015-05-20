/**
 * 
 */
package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

import spider.Review;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * 处理表格的帮助函数
 * 
 * @author ZhongFang
 *
 */
public class FileDealer {
	/**
	 * 要写入的表格文件
	 */
	private WritableWorkbook writeBook;
	/**
	 * 要读的表格文件
	 */
	private Workbook readBook;

	/**
	 * 打开要读取的表格
	 * 
	 * @param fileName
	 *            表格名称
	 * @return 表格文件中的表单数目
	 */
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
	 * 关闭读取的表格文件
	 */
	public void closeReadFile() {
		readBook.close();
	}

	/**
	 * 读取整个表格文件
	 * 
	 * @return 二维String数组，表格所有内容
	 */
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
					data[i + rowNow][j] = sheet.getCell(j, i).getContents();
				}
			}
			rowNow += rowC[k];
		}
		return data;
	}

	/**
	 * 向指定表单中写评论文本，预测结果
	 * 
	 * @param text
	 *            所有评论文本
	 * @param result
	 *            所有预测结果
	 * @param sheet
	 *            指定表单
	 * @throws RowsExceededException
	 *             行错误
	 * @throws WriteException
	 *             写错误
	 */
	public void writeResult(String[] text, int[] result, WritableSheet sheet)
			throws RowsExceededException, WriteException {
		int row = 0;
		Label label;
		for (int i : result) {
			label = new Label(0, row, text[row]);// 写文本
			sheet.addCell(label);
			label = new Label(1, row, (i + 1) + "");// 写结果
			sheet.addCell(label);
			row++;
		}
	}

	/**
	 * 向指定表格写所有的评论
	 *
	 * @param reviews
	 *            所有评论集合
	 * @param sheet
	 *            xls文件中的一个sheet表
	 * @throws RowsExceededException
	 *             行错误
	 * @throws WriteException
	 *             写错误
	 */
	public void wirteReviews(ArrayList<Review> reviews, WritableSheet sheet)
			throws RowsExceededException, WriteException {
		int row = 0;
		Label newLabel;
		for (Review review : reviews) {// 所有评论
			newLabel = new Label(0, row, review.getText());// 写文本
			sheet.addCell(newLabel);
			newLabel = new Label(1, row, review.getLevel() + "");// 写标记类别
			sheet.addCell(newLabel);
			newLabel = new Label(2, row, review.getReTitle());// 写评论题目
			sheet.addCell(newLabel);
			newLabel = new Label(3, row, review.getTime().toString());// 写评论时间
			sheet.addCell(newLabel);
			row++;
			System.out.println("excel--------------------------------" + row);
		}
	}

	/**
	 * 打开要写的excel文件
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
	 * 关闭要写的excel文件
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
			System.err.println("表单没有可以写入的内容");
		}
	}

	/**
	 * 获取要写的excel表对象
	 *
	 * @return
	 */
	public WritableWorkbook getBook() {
		return writeBook;
	}
}
