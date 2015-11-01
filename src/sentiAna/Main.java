/**
 * 
 */
package sentiAna;

import java.text.DecimalFormat;
import java.util.ArrayList;

import utils.MyLogger;

/**
 * @author ZhongFang
 *
 */
public class Main {
	public static void main(String[] args) {
		int repeatTimes = 1;
		boolean stopTag=false;

		int x[][] = {{1},{2},{3},{4},{5}};
		// { { 1, 2 }, { 3 }, { 4, 5}}
		// ;{{0},{1}}
		// {{1},{2},{3},{4},{5}}
		int y[] = { 1, 2, 3, 4, 5 };// { 1, 2, 3, 4, 5 }{0,1}

		int m[] = { 100,200,300,400,500,600,700,800,900,1000,2000 ,2500,3000, 4000, 5000};
		// 100,200,300,400,500,600,700,800,900,1000,2000 ,2500,3000, 4000, 5000,
		// 6000,7000, 8000
		MyLogger difWLog = new MyLogger("fffffffffffffffffffffff.txt");
		for (int dfmin = 1;dfmin<=5; dfmin++) {
			stopTag=false;
			for (int num : m) {
				Controller controller = new Controller();
				controller.setB(x);
				controller.setA(y);

				controller.setStop_on(true);// 停用词
				
				controller.setDF_on(false);// DF
				controller.setIG_on(true);// IG
				controller.setCHI_on(false);
				controller.setMI_on(false);

				controller.setDF_Num(dfmin);
				controller.setIG_Num(num);
				controller.setCHI_Num(num);
				controller.setMI_Num(num);

				double total[] = new double[5];
				for (int p = 0; p < repeatTimes; p++) {
					controller.openExcel("t.xls", "result.xls");
					long a = System.currentTimeMillis();
					controller.wordSegmentation();

					controller.featureSel();
					if(controller.getFeatureSize()!=num){
						stopTag=true;
						break;
					}
					int k = 5;
					controller.seleTrainSet(k);
					double sumPre = 0;
					double sumRecal = 0;
					double sumF1 = 0;
					double sumAccu = 0;
					System.out.println("reapeat: " + p);
					for (int i = 0; i < k; i++) {
						// 第i个集合作测试集，其他k-1个集合作训练集
						ArrayList<AnalReview> trainAnalReviews = new ArrayList<AnalReview>(
								k - 1);
						for (int j = 0; j < k; j++) {
							if (i == j)
								continue;
							trainAnalReviews.addAll(controller.getDataSet()
									.getkLists().get(j));
						}
						controller.training(trainAnalReviews);
						double[] prfa = controller.predict(i, controller
								.getDataSet().getkLists().get(i));
						sumPre += prfa[0];
						sumRecal += prfa[1];
						sumF1 += prfa[2];
						sumAccu += prfa[3];
					}
					DecimalFormat decimalFormat = new DecimalFormat("#.00");
					MyLogger logger = new MyLogger("结果.txt");
					logger.info("precision\t\trecall\t\tF1\t\taccuracy\r\n");
					logger.info(decimalFormat.format(sumPre / k * 100) + "%\t"
							+ decimalFormat.format(sumRecal / k * 100) + "%\t"
							+ decimalFormat.format(sumF1 / k * 100) + "%\t"
							+ decimalFormat.format(sumAccu / k * 100) + "%\t");

					controller.closeExcel();
					logger.info((System.currentTimeMillis() - a) + " ms ");
					total[0] += (sumPre / k);
					total[1] += (sumRecal / k);
					total[2] += (sumF1 / k);
					total[3] += (sumAccu / k);
					total[4] += (System.currentTimeMillis() - a);
				}

				difWLog.info("\r\n"+dfmin+"\t" + controller.getFeatureSize() + "\t");
				for (int i = 0; i < total.length; i++) {
					System.out.print(total[i] / repeatTimes + "\t");
					difWLog.info(total[i] / repeatTimes + "\t");
				}
				if(stopTag)
					break;
			}
		}
	}

}
