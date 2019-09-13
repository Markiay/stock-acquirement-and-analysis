package com.example.stocks;

public class Stocker {

	private String code;// stock code
	private String name;// stock name
	private String opening_price;// today's opening price
	private String closing_price;// yester's closing price
	private String current_price;// current price
	private String max_price;// today's highest price
	private String min_price;// today's lowest price
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOpening_price() {
		return opening_price;
	}
	public void setOpening_price(String opening_price) {
		this.opening_price = opening_price;
	}
	public String getClosing_price() {
		return closing_price;
	}
	public void setClosing_price(String closing_price) {
		this.closing_price = closing_price;
	}
	public String getCurrent_price() {
		return current_price;
	}
	public void setCurrent_price(String current_price) {
		this.current_price = current_price;
	}
	public String getMax_price() {
		return max_price;
	}
	public void setMax_price(String max_price) {
		this.max_price = max_price;
	}
	public String getMin_price() {
		return min_price;
	}
	public void setMin_price(String min_price) {
		this.min_price = min_price;
	}
	
	
		
}
