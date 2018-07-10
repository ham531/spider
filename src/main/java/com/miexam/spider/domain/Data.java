package com.miexam.spider.domain;

import java.util.List;

public class Data {
	private String code;
	private String msg;
	private int count;
	private List<Notice> data;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public List<Notice> getData() {
		return data;
	}

	public void setData(List<Notice> data) {
		this.data = data;
	}
}
