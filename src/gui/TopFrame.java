/**
 * 
 */
package gui;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/**
 * 最高层的界面
 * @author ZhongFang
 *
 */
public class TopFrame extends JFrame {

	/**
	 * 构造函数，设置长宽、布置内容
	 */
	public TopFrame() {
		JTabbedPane tabbedp = new JTabbedPane(JTabbedPane.TOP);
		Container c = this.getContentPane();
		tabbedp.addTab("爬取评论", new SpiderPan());
		tabbedp.addTab("情感分析",new SentiPan());

		c.add(tabbedp);
		this.setSize(850, 600);
		Dimension screen = getToolkit().getScreenSize();
		this.setLocation((screen.width - this.getSize().width) / 2,
				(screen.height - this.getSize().height) / 2);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**运行顶层界面
	 * @param args
	 */
	public static void main(String[] args) {
		new TopFrame();
	}

}
