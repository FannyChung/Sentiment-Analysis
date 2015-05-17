package spider;

import java.util.Date;

/**
 * ���۵����ݽṹ
 *
 * @author Fanny
 */
public class Review {
    /**
     * �����ı�
     */
    private String text;
    /**
     * �û�����
     */
    private String userName;
    /**
     * ���۱���
     */
    private String reTitle;
    /**
     * �����Ǽ�
     */
    private int level;
    /**
     * ����ʱ��
     */
    private Date time;
    /**
     * ���۲�Ʒ�Ŀ�ʽ
     */
    private String style;

    /**
     * @return �����ı�
     */
    public String getText() {
        return text;
    }

    /**
     * ���������ı�
     *
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * ��ȡ�û�����
     *
     * @return
     */
    public String getUserName() {
        return userName;
    }

    /**
     * �����û�����
     *
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * ��ȡ���۵ı���
     *
     * @return
     */
    public String getReTitle() {
        return reTitle;
    }

    /**
     * �������۵ı���
     *
     * @param reTitle
     */
    public void setReTitle(String reTitle) {
        this.reTitle = reTitle;
    }

    /**
     * �������۵ȼ�
     *
     * @return
     */
    public int getLevel() {
        return level;
    }

    /**
     * ��ȡ���۵ȼ�
     *
     * @param level
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * ��ȡ���۵�ʱ��
     *
     * @return
     */
    public Date getTime() {
        return time;
    }

    /**
     * �������۵�ʱ��
     *
     * @param time
     */
    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * ��ȡ���۵���Ʒ��ʽ
     *
     * @return
     */
    public String getStyle() {
        return style;
    }

    /**
     * �������۵���Ʒ��ʽ
     *
     * @param style
     */
    public void setStyle(String style) {
        this.style = style;
    }
}
