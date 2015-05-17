/**
 * 
 */
package gui;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/**
 * @author ZhongFang
 *
 */
public class TopFrame extends JFrame {

	/**
	 * 
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new TopFrame();
	}

}
