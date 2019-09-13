package com.example.stocks;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class StockAdapter extends BaseAdapter {

	private Context context = null;//The context in which the list is located
	private Intent intent = null;//Interface to jump
	ArrayList<Stocker> stockerInfo = new ArrayList<Stocker>() ;
	ArrayList<String> codeSet;	
	
	public StockAdapter(Context context, Intent intent, ArrayList<Stocker> stockerInfo) {
		this.context = context;
		this.intent = intent;
		this.stockerInfo = stockerInfo;
	} 
	
	public Context getContext() {
		return context;
	}//getter
	
	public Intent getIntent() {
		return intent;
	}//getter
	
	public void startActivity() {
		this.getContext().startActivity(getIntent());
	}//It's essentially startActivity (intent) the same like the normal form
	
	public void removeItem(int position) {
		stockerInfo.remove(position);
		this.notifyDataSetChanged();
	}
	
	
	
	@Override
	public int getCount() {
		return stockerInfo.size();
	}

	@Override
	public Stocker getItem(int arg0) {
		return stockerInfo.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		LinearLayout ll = null;
		if (arg1 != null) {
			ll = (LinearLayout) arg1;
			// If it was previously loaded, it can be reloaded from here without having to
			// reload,
			// making a big difference in reducing memory consumption
		} else {
			ll = (LinearLayout) LayoutInflater.from(this.getContext()).inflate(R.layout.rows_info, null);
			// Get the linear layout from the listfruit.xml
		}
				
		System.out.println("!!!!!2"+stockerInfo.size());
		Stocker stocker = getItem(arg0);
		
		TextView Scode = (TextView) ll.findViewById(R.id.Scode);
		TextView Sname = (TextView) ll.findViewById(R.id.Sname);
		TextView Sprice = (TextView) ll.findViewById(R.id.Sprice);
		TextView Spercent = (TextView) ll.findViewById(R.id.Spercent);
		
		Scode.setText(stocker.getCode());
		Sname.setText(stocker.getName());
		
		int color;
		if(arg0 % 2 > 0)
			color = Color.rgb(48,92,131);
		else 
			color = Color.rgb(119,138,170);
		ll.setBackgroundColor(color);
		
		//Sets the current price of the stock
		double current=Double.parseDouble(stocker.getCurrent_price());
		double closing_price=Double.parseDouble(stocker.getClosing_price());
		//Two decimal
		DecimalFormat decimal=new DecimalFormat("#0.00"); 
		Sprice.setText(decimal.format(current));
		
		String percent=decimal.format(((current-closing_price)*100/closing_price))+"%";
		Spercent.setText(percent);
		
		if(current > closing_price)
		{
			//Set the font color to red
			Sprice.setTextColor(0xffEE3B3B);			
			Spercent.setTextColor(0xffEE3B3B);			
		}
		else 
		{
			//Set the font color to green
			Sprice.setTextColor(0xff2e8b57);
			Spercent.setTextColor(0xff2e8b57);
		}
		
		return ll;
	}
	
	private static final int STOCK_MSG = 1;
	private Handler updateHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case STOCK_MSG:
				String information = (String)msg.obj;
				Pattern pattern=Pattern.compile("str_(.+)=\"(.+)\"");
				Matcher matcher=pattern.matcher(information);
				Stocker stocker = new Stocker();
				while(matcher.find()) {
					String result=matcher.group(2);
					String[] data=result.split(",");
					stocker.setCode(matcher.group(1));
					if((matcher.group(1).substring(0,2).equals("sh"))||(matcher.group(1).substring(0,2).equals("sz"))) {
						stocker.setName(data[0]);
						stocker.setOpening_price(data[1]);
						stocker.setClosing_price(data[2]);
						stocker.setCurrent_price(Double.parseDouble(data[3])+"");
						stocker.setMax_price(data[4]);
						stocker.setMin_price(data[5]);
					}else if(matcher.group(1).substring(0,2).equals("gb")) {
						stocker.setName(data[0]);
						stocker.setOpening_price(data[5]);
						stocker.setClosing_price(data[26]);
						stocker.setCurrent_price(Double.parseDouble(data[1])+"");
						stocker.setMax_price(data[6]);
						stocker.setMin_price(data[7]);
					}else if(matcher.group(1).substring(0,2).equals("hk")) {
						stocker.setName(data[0]);
						stocker.setOpening_price(data[2]);
						stocker.setClosing_price(data[3]);
						stocker.setCurrent_price(Double.parseDouble(data[6])+"");
						stocker.setMax_price(data[4]);
						stocker.setMin_price(data[5]);
					}
				}
				boolean isNull = false;
				if(stocker.getCode()==null) {
					isNull = true;
				}
				if (isNull) {
					Toast.makeText(getContext(), "This stock is not exist", Toast.LENGTH_LONG).show();
				} else {
					boolean isNew = true;
					if (stockerInfo.size() != 0) {
						for (int i = 0; i < stockerInfo.size(); i++) {
							if ((stockerInfo.get(i).getCode()).equals(stocker.getCode())) {
								stockerInfo.set(i, stocker);
								System.out.println("!!!!!!768758578!" + i);
								isNew = false;
								notifyDataSetChanged();
								break;
							}
						}
					}
					if (isNew) {
						stockerInfo.add(stocker);
						notifyDataSetChanged();
					}
				}
				break;

			default:
				break;
			}
		}
			
	};
	
	void updateSingleStockInfoWithHttpClient(String code) {
		code = code.replace(" ", "h");
		code = code.replace("\n", "h");
		code = code.replace(",", "h");
		final String safari = code.trim();
		System.out.println(safari);
		new Thread(new Runnable() {

			@Override
			public void run() {
				//Sending requests with HttpClient is a five-step process
				//Step 1: create the HttpClient object
				HttpClient httpCient = new DefaultHttpClient();
				//Step 2: create an object that represents the request, and the parameter is the server address to be accessed
				HttpGet httpGet = new HttpGet("http://hq.sinajs.cn/list="+safari);
				
				try {
					//Step 3: execute the request and get the corresponding object returned by the server
					HttpResponse httpResponse = httpCient.execute(httpGet);
					//Step 4: check whether the corresponding state is normal: check whether the value of the status code is 200 to indicate that it is normal
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						//Step 5: extract data from the corresponding object and put it into entity
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity,"utf-8");//Converts data in an entity to a string
						
						//The Message object is emitted in the child thread
						 Message message = new Message();
						 message.what = STOCK_MSG;
						 message.obj = response.toString();
						 updateHandler.sendMessage(message);
					}
					
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}).start();
	}
	
	
	
	
}
