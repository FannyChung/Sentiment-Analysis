/**
 * 
 */
package gui;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import spider.FileDealer;

/**
 * 表格帮助类，用于表格自动设置长宽，从xls中读取内容、表格筛选
 * 
 * @author ZhongFang
 *
 */
public class TableHelper {
	/**
	 * 使表格自动适应列宽度
	 * 
	 * @param myTable
	 *            待调整的表格
	 */
	public static void FitTableColumns(JTable myTable) {
		JTableHeader header = myTable.getTableHeader();
		int rowCount = myTable.getRowCount();
		Enumeration columns = myTable.getColumnModel().getColumns();
		while (columns.hasMoreElements()) {
			TableColumn column = (TableColumn) columns.nextElement();
			int col = header.getColumnModel().getColumnIndex(
					column.getIdentifier());
			int width = (int) myTable
					.getTableHeader()
					.getDefaultRenderer()
					.getTableCellRendererComponent(myTable,
							column.getIdentifier(), false, false, -1, col)
					.getPreferredSize().getWidth();
			for (int row = 0; row < rowCount; row++) {
				int preferedWidth = (int) myTable
						.getCellRenderer(row, col)
						.getTableCellRendererComponent(myTable,
								myTable.getValueAt(row, col), false, false,
								row, col).getPreferredSize().getWidth();
				width = Math.max(width, preferedWidth);
			}
			header.setResizingColumn(column);
			column.setWidth(width + myTable.getIntercellSpacing().width);
		}
	}

	/**
	 * 读取xls文件并返回DefaultTableModel对象
	 * 
	 * @param fileName
	 *            待读取的xls文件名
	 * @return 包含文件内容的DefaultTableModel对象
	 */
	public static DefaultTableModel loadTable(String fileName) {
		FileDealer fileDealer = new FileDealer();
		int sheetNum = fileDealer.openReadFile(fileName);
		String[][] data = fileDealer.readBook();
		fileDealer.closeReadFile();
		System.out.println(data.length);
		int col = data[0].length;
		String[] names = new String[col];
		for (int i = 0; i < col; i++) {
			names[i] = (i + "");
		}
		DefaultTableModel dtModel = new DefaultTableModel(data, names);
		return dtModel;
	}

	/**
	 * 使得表格只显示是指定星级并且包含指定关键字的评论
	 * 
	 * @param selectedLevel
	 *            选中的星级集合
	 * @param text
	 *            用于筛选的字符串
	 * @param sorter
	 *            用于表格筛选的对象
	 */
	public static void filt(ArrayList<Integer> selectedLevel, String text,
			TableRowSorter<TableModel> sorter) {
		ArrayList<RowFilter<Object, Object>> filters = new ArrayList<RowFilter<Object, Object>>(
				2);
		RowFilter<Object, Object> textFilter = null;
		RowFilter<Object, Object> levelFilter = null;

		if (text.length() != 0) {
			textFilter = RowFilter.regexFilter(text);
		}
		if (!selectedLevel.isEmpty()) {
			String levels = "[";
			for (Integer integer : selectedLevel) {
				levels += ((integer + 1) + "");
			}
			levels += "]";
			levelFilter = RowFilter.regexFilter(levels, 1);
		}
		if (levelFilter != null) {
			filters.add(levelFilter);
		}
		if (textFilter != null) {
			filters.add(textFilter);
		}
		sorter.setRowFilter(RowFilter.andFilter(filters));
	}
}
