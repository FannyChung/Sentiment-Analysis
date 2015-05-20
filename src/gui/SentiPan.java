/**
 * 
 */
package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import jxl.write.WritableSheet;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import sentiAna.AnalReview;
import sentiAna.AnalysisText;
import sentiAna.Controller;
import sentiAna.Prediction;
import utils.FileDealer;
import utils.LoadEmotionRelated;
import utils.MyLogger;

/**
 * 情感分类界面的面板
 * 
 * @author ZhongFang
 *
 */
public class SentiPan extends JPanel implements ActionListener {

	/**
	 * 重新训练的按钮
	 */
	private JButton trainButton = new JButton("重新训练");
	/**
	 * 开始分类的按钮
	 */
	private JButton clasiButton = new JButton("开始分类");

	/**
	 * 分成三类的单选框
	 */
	private JRadioButton rt_3 = new JRadioButton("分三类");
	/**
	 * 分成五类的单选框
	 */
	private JRadioButton rt_5 = new JRadioButton("分五类");

	/**
	 * 对单个文本进行预测的单选框
	 */
	private JRadioButton pre_1 = new JRadioButton("一个");
	/**
	 * 对文件中的所有文本进行预测的单选框
	 */
	private JRadioButton pre_all = new JRadioButton("批量");
	/**
	 * 筛选文本的关键字
	 */
	private JTextField filterText = new JTextField();
	/**
	 * 进行筛选的按钮
	 */
	private JButton filtButton = new JButton("筛选");
	/**
	 * 各个级别的单选框
	 */
	private JCheckBox[] levelChecks = new JCheckBox[5];
	/**
	 * 文件显示的表格
	 */
	private JTable table = new JTable();
	/**
	 * 表格用于实现排序和过滤的对象
	 */
	private TableRowSorter<TableModel> sorter;

	/**
	 * 从xls文件中获取的评论文本集合
	 */
	private String[] texts;

	/**
	 * 构造函数，构建面板内容，并增加监听
	 */
	public SentiPan() {
		JPanel panel = new JPanel(new GridLayout(1, 4));

		JLabel label = new JLabel("进行预测");
		panel.add(label);

		ButtonGroup group_pre = new ButtonGroup();
		group_pre.add(pre_1);
		group_pre.add(pre_all);
		JPanel prePanel = new JPanel();
		prePanel.add(pre_1);
		prePanel.add(pre_all);
		panel.add(prePanel);

		ButtonGroup group_cate = new ButtonGroup();
		group_cate.add(rt_3);
		group_cate.add(rt_5);
		JPanel panel1 = new JPanel();
		panel1.add(rt_3);
		panel1.add(rt_5);
		panel.add(panel1);

		JPanel butJPanel = new JPanel();
		butJPanel.add(clasiButton);
		panel.add(butJPanel);

		JPanel panel2 = new JPanel();
		DefaultTableModel dtModel = TableHelper.loadTable("pre.xls");
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

		JPanel checkJPanel = new JPanel();
		for (int i = 0; i < levelChecks.length; i++) {
			levelChecks[i] = new JCheckBox((i + 1) + "");
			checkJPanel.add(levelChecks[i]);
		}
		JPanel panel3 = new JPanel(new GridLayout(1, 3));
		panel3.add(checkJPanel);
		panel3.add(filterText);
		JPanel but2Pane = new JPanel();
		but2Pane.add(filtButton);
		panel3.add(but2Pane);
		JPanel trPanel = new JPanel();
		trPanel.add(trainButton);
		panel3.add(trPanel);

		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.NORTH);
		this.add(panel3, BorderLayout.SOUTH);
		this.add(scrollPane);

		filtButton.addActionListener(this);
		trainButton.addActionListener(this);
		clasiButton.addActionListener(this);
	}

	/**
	 * 把训练得到的信息都写入文件
	 * 
	 * @param feats
	 *            训练得到的特征集合
	 * @param pOfWordInDifCate
	 *            训练得到的特征在各个类中的概率
	 * @param pOfACate
	 *            训练得到的各个类的概率
	 * @param ftFile
	 *            待写入的特征文件
	 * @param fpFile
	 *            待写入的特征概率文件
	 * @param cpFile
	 *            待写入的类概率文件
	 */
	private void wirteInfo(ArrayList<String> feats,
			ArrayList<ArrayList<Double>> pOfWordInDifCate,
			ArrayList<Double> pOfACate, String ftFile, String fpFile,
			String cpFile) {
		try {
			// 记录特征字符串集合
			FileOutputStream writerStream = new FileOutputStream(ftFile, false);
			OutputStreamWriter osw = new OutputStreamWriter(writerStream,
					"UTF-8");
			for (String string : feats) {
				osw.write(string + "\n");
			}
			osw.flush();
			osw.close();

			// 记录特征在各个类中的概率
			writerStream = new FileOutputStream(fpFile, false);
			osw = new OutputStreamWriter(writerStream, "UTF-8");
			for (ArrayList<Double> arrayList : pOfWordInDifCate) {
				for (Double double1 : arrayList) {
					osw.write(new BigDecimal(double1) + "\t");
				}
				osw.write("\n");
			}
			osw.flush();
			osw.close();

			// 记录各个类的概率
			writerStream = new FileOutputStream(cpFile, false);
			osw = new OutputStreamWriter(writerStream, "UTF-8");
			for (Double double1 : pOfACate) {
				osw.write(new BigDecimal(double1) + "\n");
			}
			osw.flush();
			osw.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 重新从文件中读取评论文本和星级，进行训练
	 */
	private void train() {
		Controller controller = new Controller();
		controller.setA(new int[] { 1, 2, 3, 4, 5 });// 设置文本原有的星级
		controller.setB(new int[][] { { 1 }, { 2 }, { 3 }, { 4 }, { 5 } });// 设置文本待分类的星级为五类
		controller.setStop_on(true);// 停用词
		controller.setDF_on(false);// DF
		controller.setIG_on(true);// IG
		controller.setIG_Num(2000);

		controller.openExcel("t.xls", "out.xls");// 打开输入、输出文件
		controller.wordSegmentation();

		// 进行特征选择和训练，并输出要记录的特征字符串、每个特征在各个类中的概率、各个类的概率

		// 分成五类
		controller.featureSel();
		controller.training(controller.getAnalysisText().getReviews());
		wirteInfo(controller.getFeature().getFeatureStrings(), controller
				.getModel().getpOfWordInDifCate(), controller.getModel()
				.getpOfACate(), "Featrue_5.txt", "Featrue_5_P.txt",
				"Cate_5_p.txt");

		// 分成三类
		controller.setB(new int[][] { { 1, 2 }, { 3 }, { 4, 5 } });// 设置文本待分类的星级为三类
		controller.featureSel();
		controller.training(controller.getAnalysisText().getReviews());
		wirteInfo(controller.getFeature().getFeatureStrings(), controller
				.getModel().getpOfWordInDifCate(), controller.getModel()
				.getpOfACate(), "Featrue_3.txt", "Featrue_3_P.txt",
				"Cate_3_p.txt");
		controller.closeExcel();
	}

	/**
	 * 从记录的文件中获取信息，用于预测分类
	 * 
	 * @param feats
	 *            需要获取的特征集合
	 * @param pOfWordInDifCate
	 *            需要获取的特征在各个类中的概率
	 * @param pOfACate
	 *            需要获取的各个类的概率
	 * @param ftFile
	 *            记录了特征的文件
	 * @param fpFile
	 *            记录了特征概率的文件
	 * @param cpFile
	 *            记录了类概率的文件
	 */
	private void readInfo(ArrayList<String> feats,
			ArrayList<ArrayList<Double>> pOfWordInDifCate,
			ArrayList<Double> pOfACate, String ftFile, String fpFile,
			String cpFile) {

		File file1 = new File(ftFile);
		File file2 = new File(fpFile);
		File file3 = new File(cpFile);
		BufferedReader reader1 = null;
		BufferedReader reader2 = null;
		BufferedReader reader3 = null;
		try {
			// 读取特征字符串
			reader1 = new BufferedReader(new FileReader(file1));
			String tempString = null;
			while ((tempString = reader1.readLine()) != null) {
				feats.add(tempString.trim());
			}
			reader1.close();

			// 读取各个特征在各个类中的概率
			reader2 = new BufferedReader(new FileReader(file2));
			while ((tempString = reader2.readLine()) != null) {
				String[] sub = tempString.split("\t");
				ArrayList<Double> tmp = new ArrayList<Double>();
				for (String string : sub) {
					tmp.add(Double.parseDouble(string));
				}
				pOfWordInDifCate.add(tmp);
			}
			reader2.close();

			// 读取各个类的概率
			reader3 = new BufferedReader(new FileReader(file3));
			while ((tempString = reader3.readLine()) != null) {
				pOfACate.add(Double.parseDouble(tempString));
			}
			reader3.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader1 != null) {
				try {
					reader1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 对一个评论文本进行预测
	 * 
	 * @param text
	 *            待分类的文本
	 * @param analysisText
	 *            用于给文本分词
	 * @param prediction
	 *            执行预测的类
	 * @param feats
	 *            特征集
	 * @return 评论文本的类别编号
	 */
	public int predict(String text, AnalysisText analysisText,
			Prediction prediction, ArrayList<String> feats) {
		int result = 0;
		String[] words = analysisText.wordSeg(text);// 分词
		// 向量化
		AnalReview analReview = new AnalReview(text);
		boolean[] featureValue = new boolean[feats.size()];
		for (String string : words) {
			analReview.getFrequency().merge(string, 1,
					(value, newValue) -> (value + 1));
		}
		for (int i = 0; i < featureValue.length; i++) {
			String ft = feats.get(i);
			if (analReview.getFrequency().containsKey(ft))
				featureValue[i] = true;
			else {
				featureValue[i] = false;
			}
		}
		analReview.setFeatureVector(featureValue);

		result = prediction.predict(analReview);// 预测
		return result;
	}

	/**
	 * 对多个文本进行预测分类
	 * 
	 * @param analysisText
	 *            用于给文本分词的类
	 * @param prediction
	 *            用于预测的类
	 * @param feats
	 *            特征集
	 * @return 多个文本对应的分类编号
	 */
	public int[] predict(AnalysisText analysisText, Prediction prediction,
			ArrayList<String> feats) {
		// 读取所有的文本
		FileDealer fileDealer = new FileDealer();
		fileDealer.openReadFile("pre.xls");
		String[][] datas = fileDealer.readBook();
		fileDealer.closeReadFile();
		int textNum = datas.length;
		texts = new String[textNum];
		int[] results = new int[textNum];
		for (int i = 0; i < textNum; i++) {
			texts[i] = datas[i][0];
			results[i] = predict(texts[i], analysisText, prediction, feats);
		}
		return results;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == trainButton) { // 重新训练
			if (JOptionPane.showConfirmDialog(null,
					"请确认训练文本已保存到t.xls且第一、二列分别为文本、星级，按确认键继续", "请确认",
					JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
				train();
				JOptionPane.showMessageDialog(null, "已保存到excel文件out.xls",
						"重新训练成功", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == filtButton) { // 从表格的数据中筛选信息，留下包含指定字符串并且是指定星级的评论
			// 获取复选框中选定的星级
			ArrayList<Integer> selectedLevel = new ArrayList<Integer>(5);
			for (int i = 0; i < levelChecks.length; i++) {
				if (levelChecks[i].isSelected()) {
					selectedLevel.add(i);
				}
			}
			String text = filterText.getText().trim();
			TableHelper.filt(selectedLevel, text, sorter);// 过滤
		} else {// 对新的评论进行分类
			int cateNum = 0;// 选择的分类标准
			if (rt_3.isSelected()) {
				cateNum = 3;
			} else if (rt_5.isSelected()) {
				cateNum = 5;
			}
			ArrayList<String> feats = new ArrayList<String>();// 需要获取的特征集合
			ArrayList<ArrayList<Double>> pOfWordInDifCate = new ArrayList<ArrayList<Double>>();// 需要获取的特征在各个类中的概率
			ArrayList<Double> pOfACate = new ArrayList<Double>();// 需要获取的各个类的概率
			if (cateNum == 3) {// 根据不同的分类标准读取不同的文件
				readInfo(feats, pOfWordInDifCate, pOfACate, "Featrue_3.txt",
						"Featrue_3_P.txt", "Cate_3_p.txt");
			} else {
				readInfo(feats, pOfWordInDifCate, pOfACate, "Featrue_5.txt",
						"Featrue_5_P.txt", "Cate_5_p.txt");
			}

			// 初始化，用于分词
			AnalysisText analysisText = new AnalysisText();
			analysisText.setAllEmotionRelatedWords(new LoadEmotionRelated()
					.getAllEmotionRelated());
			analysisText.initNlpri();

			// 根据读取的信息设置预测参数
			Prediction prediction = new Prediction();
			prediction.setpOfACate(pOfACate);
			prediction.setpOfWordInDifCate(pOfWordInDifCate);

			if (pre_1.isSelected()) {// 对单个文本进行分类
				String text = JOptionPane.showInputDialog("请输入待预测的文本");
				int result = predict(text, analysisText, prediction, feats);
				JOptionPane.showMessageDialog(null, "预测类别为" + (result + 1));
			} else if (pre_all.isSelected()) {// 对多个文本进行分类
				if (JOptionPane.showConfirmDialog(null,
						"请确认待预测的评论文本集合已保存到pre.xls的第一列，\n按确认键继续", "请确认",
						JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION) {

					int[] results = predict(analysisText, prediction, feats);// 获得预测结果

					// 写到文件中
					FileDealer fileDealer = new FileDealer();
					fileDealer.openWriteFile("pre_result.xls");
					WritableSheet sheet = fileDealer.getBook().createSheet(
							"预测结果", 0);
					try {
						fileDealer.writeResult(texts, results, sheet);
					} catch (RowsExceededException e1) {
						e1.printStackTrace();
					} catch (WriteException e1) {
						e1.printStackTrace();
					}
					fileDealer.closeWriteFile();

					// 在界面上显示
					DefaultTableModel dtModel = TableHelper
							.loadTable("pre_result.xls");
					table.setModel(dtModel);
					TableHelper.FitTableColumns(table);
					sorter = new TableRowSorter<TableModel>(dtModel);
					table.setRowSorter(sorter);
					table.repaint();
					table.updateUI();
					JOptionPane.showMessageDialog(null, "结果保存已到pre_result.xls");
				}
			}
			analysisText.exitNlpir();
		}
	}

}
