package com.tuhanbao.io.impl.sqlUtil;

/**
 * 在生成代码时如果源文件存在的处理策略
 * 
 * @author Administrator
 *
 */
public enum GenerateStrategy {
	/**
	 * 直接覆盖
	 */
	COVER, 
	/**
	 * 跟以前版本进行融合
	 */
	MEGER, 
	/**
	 * 不做任何处理
	 */
	IGNORE;
}
