# Sentiment-Analysis

##文件目录说明：

###输入输出文件


####爬虫输出

│      商品名称和url.txt-->爬虫记录的商品名和url

│      t.xls-->爬虫的结果，所有评论文本

####情感分析

#####输入

│      t.xls-->爬虫的结果，所有评论文本

│      tan.xls-->谭松波酒店评论预料，用于测试模型

│      pre.xls-->待预测文本所在文件

#####输出

│       pre_result.xls-->待预测文本预测后的结果

│      out.xls-->进行训练时候的相关信息记录

│      result.xls-->模型验证时候的相关信息记录

###产生的中间文件


####训练后的输出，预测需要的输入文件

│       Cate_3_p.txt-->分成三类保存的类别的先验概率

│      Cate_5_p.txt-->

│      Featrue_3.txt-->分为三类时候选择的特征字符串

│      Featrue_3_P.txt-->分为三类时候不同类别下每个特征的条件概率

│      Featrue_5.txt-->

│      Featrue_5_P.txt-->

####分类过程中产生的中间文件

│      不同特征的文本个数.txt-->不同特征的文档数

│      不同类别的文本个数.txt-->不同类别的文档数

│      删除DF.txt-->根据DF删除的特征词

│      删除停用词.txt-->根据停用词删除的特征词

│      Pfc.txt-->不同类别下不同特征的概率

│      评论以及其特征的概率.txt 

-->K折交叉验证的结果

│      结果.txt-->平均结果

│      结果0.txt-->第k次的结果

│      结果1.txt

│      结果2.txt

│      结果3.txt

│      结果4.txt



###使用的相关资源

│  新词.txt-->利用分词工具发现的新词，可以在分词前导入分词工具

├─myresource-->情感词资源和停用词

│      │  StopWords.txt

│      │  自定义否定词.txt

│      │  

│      └─HowNet

│            主张词语（中文）.txt

│            正面情感词语（中文）.txt

│             正面评价词语（中文）.txt

│             程度级别词语（中文）.txt

│            负面情感词语（中文）.txt

│             负面评价词语（中文）.txt

│          

├─.settings-->程序配置

│      org.eclipse.core.resources.prefs

│       org.eclipse.jdt.core.prefs

│      

├─bin-->程序生成的class文件

│          

├─Data-->分词依赖

│          

├─lib-->分词依赖包、配置

│      jna-4.0.0.jar

│      NLPIR.dll

│       NLPIR.lib

│      



├─rl_res-->java编程依赖包

│      │      jxl.jar

│      │      log4j-1.2.17.jar

│      │  

│      └─WebCollector





###代码        





└─src-->代码源程序

├─gui-->界面部分

│      SentiPan.java---情感分析部分的界面

│      SpiderPan.java---爬虫部分的界面

│      TableHelper.java---表格帮助类，用于表格自动设置长宽，从xls中读取内容、表格筛选

│      TopFrame.java---总体界面

│      

├─sentiAna-->情感分析部分

│      AnalReview.java---分析后的评论的数据结构

│      AnalysisText.java---导入评论，对评论进行分析预处理


│      Controller.java---主控制程序

│      DataSet.java---数据集处理（分离训练集和测试集，并向量化）

│      Feature.java---特征选择

│      Model.java---生成模型（计算先验概率和条件概率）

│      Prediction.java---预测&统计指标


│      

├─spider-->爬虫部分

│      ProductUrl.java---商品链接的数据结构

│      ReivewWebDriver.java---爬取商品内容和链接

│      Review.java---商品评论的数据结构

│      

└─utils-->前三个部分的一些帮助工具

│            FileDealer.java---excel文件处理的帮助类

│            LoadEmotionRelated.java---从文件加载情感词典

│            LoadStopWords.java---从文件加载停用词

│            MyLogger.java---可以将Log写入到指定文件中

│            NlpirTest.java---分词工具的配置

│            ReadConfigUtil.java---分词工具的配置

│            SystemParas.java---分词工具的配置

