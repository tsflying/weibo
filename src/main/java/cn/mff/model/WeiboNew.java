/*
 * Copyright Notice ====================================================
 * This file contains proprietary information of Hewlett-Packard Co.
 * Copying or reproduction without prior written approval is prohibited.
 * Copyright (c) 2017 All rights reserved. =============================
 */

package cn.mff.model;

import cn.fhj.TwitterFrm;
import cn.fhj.twitter.Grid;
import cn.fhj.util.HttpsUtil;
import cn.fhj.util.ThreadUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class WeiboNew {
	public abstract String getName();
	public abstract String send(Grid grid);
	public abstract void delete(String sinaId);
	protected HttpsUtil util;
	private final List<Grid> recentGrids = new ArrayList();
	private boolean ready=false;
	private static final String URL_PATTERN = "(http://|https://)[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr)[^\u4e00-\u9fa5\\s]*";
	public void refresh(){
		//implement in sub class
	}
	public List<Grid> getRecentGrids() {
		return recentGrids;
	}

	protected int getWeiboLength() {
		return 3000;
	}
	
	public boolean isLarge(String text) {
		int count = 0;
		for (char c : text.toCharArray()) {
			if (c < 256) {
				count++;
			}
		}
		int length = text.length() - count / 2;
		if (length <= getWeiboLength()) {
			return false;
		}

		Pattern pattern = Pattern.compile(URL_PATTERN);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			String s = matcher.group(0);
			length -= s.length() / 2 - 10;
		}
		return length > getWeiboLength();
	}


	public String trim(String text) {
		int count = 0;
		for (char c : text.toCharArray()) {
			if (c < 256) {
				count++;
			}
		}
		int dif = text.length() - getWeiboLength() - count / 2;
		if (dif <= 0) {
			return text;
		}

		Pattern pattern = Pattern.compile(URL_PATTERN);
		// 空格结束
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			String s = matcher.group(0);
			dif -= s.length() / 2 - 10;
			System.out.println(s);
			if (dif <= 0) {
				return text;
			}
		}
		return trim(text.substring(0, text.length() - dif));
	}

	public boolean isReady(){
		return ready;
	}
	public void setReady(boolean ready){
		this.ready=ready;
	}
	
	protected void showMessage(String msg) {
		TwitterFrm .setMessage(msg);
	}

	public boolean shouldSave() {
		return true;
	}
	
	public long getSleep() {
		return 37000;
	}
	
	public void sleepAfterSend() {
		ThreadUtil.sleep((long)(1000 * 60 * (2 + Math.random() * 5)));
	}
}
