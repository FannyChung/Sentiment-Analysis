/**
 * 
 */
package textManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @author hp
 *
 */
public class TrainSet {
	private Vector<Integer> cateCount;
	private ArrayList<AnalReview> trainRev;


	/**统计每个类别的个数和总的个数
	 * @param a
	 * @param reviews
	 */
	public void calCategory(int a[],ArrayList<AnalReview> reviews) {
		int n=a.length;
		cateCount=new Vector<Integer>(n+1);//最后一项代表总的个数
		for (int i = 0; i < n+1; i++) {
			cateCount.add(0);
		}
		for (AnalReview analReview : reviews) {
			int level=analReview.getLevel();
			for (int i = 0; i < n; i++) {
				if(level==a[i]){
					cateCount.setElementAt(cateCount.get(i)+1, i);
					cateCount.setElementAt(cateCount.get(n)+1, n);
					break;
				}
			}
		}
	}
	
	public void seleTrain(int a[],int numOfEach,ArrayList<AnalReview> reviews) {
		trainRev=new ArrayList<AnalReview>(a.length*numOfEach);
		Vector<Integer> countEach=new Vector<Integer>(a.length);//每个类别已经获取的个数
		Map<Integer, Integer> cateLevel2cateNum=new HashMap<Integer, Integer>(a.length);
		for (int i = 0; i < a.length; i++) {
			cateLevel2cateNum.put(a[i], i);
			countEach.add(0);
		}
		for (AnalReview analReview : reviews) {
			if(cateLevel2cateNum.isEmpty())
				break;
			int level=analReview.getLevel();
			int code;
			if(cateLevel2cateNum.containsKey(level)){
				trainRev.add(analReview);
				
				code=cateLevel2cateNum.get(level);
				int addAft=countEach.get(code)+1;
				countEach.setElementAt(addAft,code);
				if(addAft==numOfEach){
					cateLevel2cateNum.remove(level);
				}
			}
		}
	}
	
	public ArrayList<AnalReview> getTrainRev() {
		return trainRev;
	}
}
