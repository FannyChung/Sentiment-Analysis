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

import spider.ReivewWebDriver;
import utils.FileDealer;

public class SpiderPan extends JPanel implements ActionListener {
	/**
	 * 要搜索的关键字的输入框
	 */
	private JTextField spiderWord = new JTextField();
	/**
	 * 要搜索的商品个数的输入框
	 */
	private JTextField spiderNum = new JTextField();
	/**
	 * 要筛选的文本的输入框
	 */
	private JTextField filterText = new JTextField();
	/**
	 * 爬取评论的按钮
	 */
	private JButton spiderButton = new JButton("重新爬取");
	/**
	 * 筛选表格的按钮
	 */
	private JButton filtButton = new JButton("筛选");

	/**
	 * 要筛选的五个星级的单选框
	 */
	private JCheckBox[] levelChecks = new JCheckBox[5];

	/**
	 * 用于实现表格排序和过滤的对象
	 */
	private TableRowSorter<TableModel> sorter;
	/**
	 * 显示文件的表格
	 */
	private JTable table = new JTable();

	/**
	 * 构造函数，布置面板内容
	 */
	public SpiderPan() {
		this.setLayout(new BorderLayout());
		JPanel jpanel = new JPanel(new BorderLayout());
		JPanel panel1 = new JPanel(new GridLayout(1, 5));
		JPanel panel2 = new JPanel();

		JLabel label = new JLabel("关键字");
		JLabel label2 = new JLabel("商品数目");

		panel1.add(label);
		panel1.add(spiderWord);
		panel1.add(label2);
		panel1.add(spiderNum);
		JPanel spJPanel = new JPanel();
		spJPanel.add(spiderButton);
		panel1.add(spJPanel);

		DefaultTableModel dtModel = TableHelper.loadTable("t.xls");
		table.setModel(dtModel);
		TableHelper.FitTableColumns(table);
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
		JPanel but2Pane = new JPanel();
		but2Pane.add(filtButton);
		panel3.add(but2Pane);

		this.add(panel1, BorderLayout.NORTH);
		this.add(panel3, BorderLayout.SOUTH);
		this.add(scrollPane);

		spiderButton.addActionListener(this);
		filtButton.addActionListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == spiderButton) {// 从网络上爬取评论
			if (!(spiderWord.getText().trim().length() == 0 || spiderNum
					.getText().trim().length() == 0)) {// 如果有指定的关键字和个数，则开始爬取，否则，只进行刷新表格
				String searchStr = spiderWord.getText();
				int n = Integer.parseInt(spiderNum.getText());
				ReivewWebDriver reivewWebDriver = new ReivewWebDriver();
				reivewWebDriver.runSpider(searchStr, n);
				JOptionPane.showMessageDialog(null, "已保存到excel文件t.xls", "爬取成功",
						JOptionPane.ERROR_MESSAGE);
			}

			// 刷新表格
			DefaultTableModel dtModel = TableHelper.loadTable("t.xls");
			table.setModel(dtModel);
			TableHelper.FitTableColumns(table);
			table.repaint();
			table.updateUI();
		} else {// 根据指定星级和字符串，筛选表格
			ArrayList<Integer> selectedLevel = new ArrayList<Integer>(5);// 获取指定的星级
			for (int i = 0; i < levelChecks.length; i++) {
				if (levelChecks[i].isSelected()) {
					selectedLevel.add(i);
				}
			}
			String text = filterText.getText().trim();// 获取指定的字符串
			TableHelper.filt(selectedLevel, text, sorter);// 按照“且”的逻辑过滤
		}

	}

}