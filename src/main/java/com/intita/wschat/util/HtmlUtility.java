package com.intita.wschat.util;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

public class HtmlUtility {
	public static String escapeHtml(String str) {
		  String unescaped[] = {"&","<",">","\"","'","{","}"};
		  String escaped[] = {"&amp;","&lt;","&gt;","&quot;","&#039;","&#123;","&#125;"};
		  return StringUtils.replaceEach(str, unescaped, escaped);
		}
	public static String unescapeHtml(String str) {
		String unescaped[] = {"&","<",">","\"","'","{","}"};
		  String escaped[] = {"&amp;","&lt;","&gt;","&quot;","&#039;","&#123;","&#125;"};
		 return StringUtils.replaceEach(str, escaped,unescaped);
		}
	
}
