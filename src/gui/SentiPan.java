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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import sentiAna.AnalysisText;
import sentiAna.Controller;
import sentiAna.Predict;
import utils.AnalReview;
import utils.LoadEmotionRelated;

/**
 * @author ZhongFang
 *
 */
public class SentiPan extends JPanel implements ActionListener {

	JButton trainButton = new JButton("重新训练");
	JButton clasiButton = new JButton("开始分类");

	JRadioButton rt_3 = new JRadioButton("分三类");
	JRadioButton rt_5 = new JRadioButton("分五类");
	JRadioButton pre_1 = new JRadioButton("一个");
	JRadioButton pre_all = new JRadioButton("批量");

	/**
	 * 
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

		JPanel trJPanel = new JPanel();
		trJPanel.add(trainButton);

		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.NORTH);
		this.add(trJPanel, BorderLayout.SOUTH);

		trainButton.addActionListener(this);
		clasiButton.addActionListener(this);
	}

	private void wirteInfo(ArrayList<String> feats,
			ArrayList<ArrayList<Double>> pOfWordInDifCate,
			ArrayList<Double> pOfACate, String ftFile, String fpFile,
			String cpFile) {
		try {
			FileOutputStream writerStream = new FileOutputStream(ftFile, false);
			OutputStreamWriter osw = new OutputStreamWriter(writerStream,
					"UTF-8");
			for (String string : feats) {
				osw.write(string + "\n");
			}
			osw.flush();
			osw.close();

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

	private void train() {
		Controller controller = new Controller();
		controller.setA(new int[] { 1, 2, 3, 4, 5 });
		controller.setB(new int[][] { { 1 }, { 2 }, { 3 }, { 4 }, { 5 } });
		controller.setStop_on(true);// 停用词
		controller.setDF_on(false);// DF
		controller.setIG_on(false);// IG
		controller.setIG_Num(8000);

		controller.openExcel();
		controller.wordSegmentation();

		controller.featureSel();
		controller.training(controller.getTextAnal().getReviews());
		wirteInfo(controller.getFeature().getFeatureString(), controller
				.getModel().getpOfWordInDifCate(), controller.getModel()
				.getpOfACate(), "Featrue_5.txt", "Featrue_5_P.txt",
				"Cate_5_p.txt");

		controller.setB(new int[][] { { 1, 2 }, { 3 }, { 4, 5 } });
		controller.featureSel();
		controller.training(controller.getTextAnal().getReviews());
		wirteInfo(controller.getFeature().getFeatureString(), controller
				.getModel().getpOfWordInDifCate(), controller.getModel()
				.getpOfACate(), "Featrue_3.txt", "Featrue_3_P.txt",
				"Cate_3_p.txt");
		controller.closeExcel();
	}

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
			reader1 = new BufferedReader(new FileReader(file1));

			String tempString = null;
			while ((tempString = reader1.readLine()) != null) {
				feats.add(tempString.trim());
			}
			reader1.close();

			reader2 = new BufferedReader(new FileReader(file2));
			while ((tempString = reader2.readLine()) != null) {
				String[] sub=tempString.split("\t");
				ArrayList<Double> tmp=new ArrayList<Double>();
				for (String string : sub) {
					tmp.add(Double.parseDouble(string));
				}
				pOfWordInDifCate.add(tmp);
			}
			reader2.close();

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

	public int predict(String text, int cateNum) {
		int result = 0;
		ArrayList<String> feats = new ArrayList<String>();
		ArrayList<ArrayList<Double>> pOfWordInDifCate = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> pOfACate = new ArrayList<Double>();
		if (cateNum == 3) {
			readInfo(feats, pOfWordInDifCate, pOfACate, "Featrue_3.txt",
					"Featrue_3_P.txt", "Cate_3_p.txt");
		} else {
			readInfo(feats, pOfWordInDifCate, pOfACate, "Featrue_5.txt",
					"Featrue_5_P.txt", "Cate_5_p.txt");
		}
		//向量化
		AnalReview analReview=new AnalReview(text);
		AnalysisText analysisText=new AnalysisText();
		analysisText.setAllEmotionRelatedWords(new LoadEmotionRelated().getAllEmotionRelated());
		analysisText.initNlpri();
		String[] words=analysisText.wordSeg(text);
		analysisText.exitNlpir();
		boolean[] featureValue=new boolean[feats.size()];
		for (String string : words) {
			analReview.getFrequency().merge(string, 1,
					(value, newValue) -> (value+1));
		}

		for (int i = 0; i < featureValue.length; i++) {
			String ft=feats.get(i);
			if(analReview.getFrequency().containsKey(ft))
				featureValue[i]=true;
			else {
				featureValue[i]=false;
			}
		}
		analReview.setFeatureVector(featureValue);
		
		//预测
		Predict predict = new Predict();
		predict.setpOfACate(pOfACate);
		predict.setpOfWordInDifCate(pOfWordInDifCate);

		result = predict.predict(analReview);
		return result;
	}

	public void predict(int cateNum) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == trainButton) {
			if (JOptionPane.showConfirmDialog(null,
					"请确认训练文本已保存到t.xls且第一、二列分别为文本、星级，按确认键继续", "请确认",
					JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
				train();
				JOptionPane.showMessageDialog(null, "已保存到excel文件result.xls",
						"重新训练成功", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			int cateNum = 0;
			if (rt_3.isSelected()) {
				cateNum = 3;
			} else if (rt_5.isSelected()) {
				cateNum = 5;
			}

			if (pre_1.isSelected()) {
				String text = JOptionPane.showInputDialog("请输入待预测的文本");
				int result = predict(text, cateNum);
				JOptionPane.showMessageDialog(null, "预测类别为" + (result + 1));
			} else if (pre_all.isSelected()) {
				if (JOptionPane.showConfirmDialog(null,
						"请确认待预测的评论集合已保存到pre.xls的第一列，按确认键继续", "请确认",
						JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
				}
			}
		}
	}

}
