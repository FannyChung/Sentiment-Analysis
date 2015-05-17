/**
 * 
 */
package gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowFilter.ComparisonType;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import spider.FileDeal;
import spider.ReivewWebDriver;

public class SpiderPan extends JPanel implements ActionListener {
	JMenuBar mainMenu = new JMenuBar();
	JMenu menuSystem = new JMenu();
	JMenuItem itemExit = new JMenuItem();
	JMenu menuStu = new JMenu();
	JMenuItem itemAddS = new JMenuItem();
	JMenu itemSearchGrd = new JMenu();// 成绩查询
	JMenuItem itemSearchMutGrd = new JMenuItem();

	JTextField textField = new JTextField();
	JTextField textField2 = new JTextField();
	JTextField filterText = new JTextField();
	JButton spiderButton = new JButton("重新爬取");
	JButton button2 = new JButton("刷新");

	JCheckBox[] levelChecks = new JCheckBox[5];

	TableRowSorter<TableModel> sorter;
	JTable table = new JTable();

	public SpiderPan() {
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		this.setLayout(new BorderLayout());
		JPanel jpanel = new JPanel(new BorderLayout());
		JPanel panel1 = new JPanel(new GridLayout(1, 5));
		JPanel panel2 = new JPanel();

		
		JLabel label = new JLabel("关键字");
		JLabel label2 = new JLabel("商品数目");

		panel1.add(label);
		panel1.add(textField);
		panel1.add(label2);
		panel1.add(textField2);
		panel1.add(spiderButton);

		FileDeal fileDeal = new FileDeal();
		int sheetNum = fileDeal.openReadFile("t.xls");
		String[][] data = fileDeal.readBook();
		System.out.println(data.length);
		int col = data[0].length;
		String[] names = new String[col];
		for (int i = 0; i < col; i++) {
			names[i] = (i + "");
		}
		DefaultTableModel dtModel = new DefaultTableModel(data, names);
		table.setModel(dtModel);
		FitTableColumns(table);
		sorter = new TableRowSorter<TableModel>(dtModel);
		table.setRowSorter(sorter);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.setColumnSelectionAllowed(true);
		table.setRowSelectionAllowed(true);
		panel2.add(scrollPane);
		
		JPanel checkJPanel = new JPanel();
		for (int i = 0; i < levelChecks.length; i++) {
			levelChecks[i] = new JCheckBox((i + 1) + "");
			checkJPanel.add(levelChecks[i]);
		}
		JPanel panel3 = new JPanel(new GridLayout(1, 2));
		panel3.add(checkJPanel);
		panel3.add(filterText);
		panel3.add(button2);

		// jpanel.add(panel1);
		this.add(panel1, BorderLayout.NORTH);
		this.add(panel3, BorderLayout.SOUTH);
		this.add(scrollPane);
		// jpanel.add(table);
		// this.add(jpanel);

		spiderButton.addActionListener(this);
		button2.addActionListener(this);

	}

	public void refreshTable() {
		table.removeAll();

		FileDeal fileDeal = new FileDeal();
		int sheetNum = fileDeal.openReadFile("t.xls");
		String[][] data = fileDeal.readBook();
		System.out.println(data.length);
		int col = data[0].length;
		String[] names = new String[col];
		for (int i = 0; i < col; i++) {
			names[i] = (i + "");
		}
		DefaultTableModel dtModel = new DefaultTableModel(data, names);
		table.setModel(dtModel);
		table.repaint();
		table.updateUI();
	}

	public void FitTableColumns(JTable myTable) {
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

	public void filt(ArrayList<Integer> selectedLevel) {
		ArrayList<RowFilter<Object,Object>> filters = new ArrayList<RowFilter<Object,Object>>(2);  
		RowFilter<Object, Object> textFilter=null;
		RowFilter<Object, Object> levelFilter=null;

		String text = filterText.getText().trim();
		if (text.length() != 0) {
			textFilter=RowFilter.regexFilter(text);
		}
		if(!selectedLevel.isEmpty()){
			String levels="[";
			for (Integer integer : selectedLevel) {
				levels+=((integer+1)+"");
			}
			levels+="]";
			levelFilter=RowFilter.regexFilter(levels, 1);
		}
		if(levelFilter!=null){
			filters.add(levelFilter);	
		}
		if(textFilter!=null){
			filters.add(textFilter);
		}
		sorter.setRowFilter(RowFilter.andFilter(filters));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == spiderButton) {
			String searchStr = textField.getText();
			int n = Integer.parseInt(textField2.getText());
			ReivewWebDriver reivewWebDriver = new ReivewWebDriver();
			reivewWebDriver.runSpider(searchStr, n);
			JOptionPane.showMessageDialog(null, "已保存到excel文件t.xls", "爬取成功",
					JOptionPane.ERROR_MESSAGE);
			refreshTable();
		} else {
			ArrayList<Integer> selectedLevel = new ArrayList<Integer>(5);
			for (int i = 0; i < levelChecks.length; i++) {
				if (levelChecks[i].isSelected()) {
					selectedLevel.add(i);
				}
			}
			filt(selectedLevel);
		}

	}

}