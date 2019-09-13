package com.example.stocks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;

public class UpdateWidgetService extends Service {

	private AppWidgetManager awm;
//	private Handler updateHandler;
	private static final int STOCK = 1;
	private ComponentName componentName;
	private RemoteViews remoteViews;
	ArrayList<String> codeSet = new ArrayList<String>();
	ArrayList<Stocker> stockerInfo = new ArrayList<Stocker>();
	private Timer timer;
	private TimerTask task;
	
	public DecimalFormat df;
	private TextView Wcode1, Wcode2, Wcode3, Wname1, Wname2, Wname3, Wprice1, Wprice2, Wprice3, Wrate1, Wrate2, Wrate3;
	private Button updatebutton, enterbutton;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		awm = AppWidgetManager.getInstance(getApplicationContext());
		componentName = new ComponentName(UpdateWidgetService.this, WidgetJava.class);
		remoteViews = new RemoteViews(getPackageName(), R.layout.widget_layout);
		df = new DecimalFormat("#0.00"); 
		
		Intent intent = new Intent(UpdateWidgetService.this, IFStock.class); 
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
		remoteViews.setOnClickPendingIntent(R.id.enterbutton, pendingIntent);		
		
		timer = new Timer();
		task = new TimerTask() {

			@Override
			public void run() {
				codeSet.clear();
				stockerInfo.clear();
				UpdateWidgetService.this.reloadData();
				
				for(int i = 0;i<codeSet.size();i++) {
					UpdateWidgetService.this.updateSingleStockInfoWithHttpClient(codeSet.get(i));
					System.out.println("widget"+codeSet.size()+"!"+stockerInfo.size());
				}
				
				if(stockerInfo.size()==1){
					UpdateWidgetService.this.setFirst(stockerInfo.get(0));
				}
				if(stockerInfo.size()>=2) {
					UpdateWidgetService.this.setMore();
				}
				awm.updateAppWidget(componentName, remoteViews);
			}
			
		};
		timer.scheduleAtFixedRate(task, 0, 20000);
		
		super.onCreate();
	}
	
	private Handler updateHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case STOCK:
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
				if (stockerInfo.size() != 0) {
					for (int i = 0; i < stockerInfo.size(); i++) {
						if ((stockerInfo.get(i).getCode()).equals(stocker.getCode())) {
							stockerInfo.set(i, stocker);
							System.out.println("!!!!!!768758578!" + i);
							break;
						}
					}
				}
				break;

			default:
				System.out.println("!!!!!!!34656");
				break;
			}
		}
		
	};
	
	
	public void updateSingleStockInfoWithHttpClient(String code) {
		code = code.replace(" ", "h");
		code = code.replace("\n", "h");
		code = code.replace(",", "h");
		final String safari = code.trim();
		
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
						 message.what = STOCK;
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
	
	public void setFirst(Stocker stocker) {
		double current = Double.parseDouble(stocker.getCurrent_price());
		double closing_price = Double.parseDouble(stocker.getClosing_price());
		String percent=df.format(((current-closing_price)*100/closing_price))+"%";
		remoteViews.setTextViewText(R.id.Wcode1, stocker.getCode());
		remoteViews.setTextViewText(R.id.Wname1, stocker.getName());
		remoteViews.setTextViewText(R.id.Wprice1, df.format(current));
		remoteViews.setTextViewText(R.id.Wpercent1, percent);
		if(current > closing_price) {
			remoteViews.setTextColor(R.id.Wpercent1, 0xffEE3B3B);
		}else {
			remoteViews.setTextColor(R.id.Wpercent1, 0xff2e8b57);
		}
	}
	
	public void setMore() {
		int one = 0;
		int two = 0;
		double[] rate = new double[20];
		Arrays.fill(rate, -10);
		for(int i = 0; i < stockerInfo.size(); i++) {
			double current = Double.parseDouble(stockerInfo.get(i).getCurrent_price());
			double closing_price = Double.parseDouble(stockerInfo.get(i).getClosing_price());
			double percent = (current-closing_price)*100/closing_price;
			rate[i] = percent;
		}
		Arrays.sort(rate);
		for(int i = 0; i < stockerInfo.size(); i++) {
			double current = Double.parseDouble(stockerInfo.get(i).getCurrent_price());
			double closing_price = Double.parseDouble(stockerInfo.get(i).getClosing_price());
			double percent = (current-closing_price)*100/closing_price;
			if(percent==rate[19]) {
				one = i;
			}
			if(percent==rate[18]) {
				two = i;
			}
		}
		this.setFirst(stockerInfo.get(one));
		Stocker stocker = stockerInfo.get(two);
		double current = Double.parseDouble(stocker.getCurrent_price());
		double closing_price = Double.parseDouble(stocker.getClosing_price());
		String percent=df.format(((current-closing_price)*100/closing_price))+"%";
		remoteViews.setTextViewText(R.id.Wcode2, stocker.getCode());
		remoteViews.setTextViewText(R.id.Wname2, stocker.getName());
		remoteViews.setTextViewText(R.id.Wprice2, df.format(current));
		remoteViews.setTextViewText(R.id.Wpercent2, percent);
		if(current > closing_price) {
			remoteViews.setTextColor(R.id.Wpercent2, 0xffEE3B3B);
		}else {
			remoteViews.setTextColor(R.id.Wpercent2, 0xff2e8b57);
		}
	}
	
	private void reloadData() {
		try {
			// Open input stream to reload the info for "to"
			InputStream is = this.openFileInput("amount");
			// Convert to bytes
			byte[] bytes = new byte[is.available()];
			// Read the bytes info
			is.read(bytes);
			// close input stream
			is.close();
			// Get the string and output
			String str = new String(bytes, "UTF-8");
			int length = Integer.parseInt(str);
			System.out.println("!!!!!!!!"+length);
			
			for(int position = 0; position < length; position++) {
				InputStream is2 = this.openFileInput(Integer.toString(position));
				byte[] bytes2 = new byte[is2.available()];
				is2.read(bytes2);
				is2.close();
				String str2 = new String(bytes2, "UTF-8");
				System.out.println("!!!!!345"+str2);
				System.out.println("!!!!!345");
				codeSet.add(str2);
			}
			
			for(int position = 0; position < length; position++) {
				InputStream is2 = this.openFileInput("stockerInfo"+Integer.toString(position));
				byte[] bytes2 = new byte[is2.available()];
				is2.read(bytes2);
				is2.close();
				String str2 = new String(bytes2, "UTF-8");
				Stocker stocker = new Stocker();
				String[] str3 = str2.split("!");
				
				stocker.setCode(str3[0]);
				stocker.setName(str3[1]);
				stocker.setOpening_price(str3[2]);
				stocker.setClosing_price(str3[3]);
				stocker.setMax_price(str3[4]);
				stocker.setMin_price(str3[5]);
				stocker.setCurrent_price(str3[6]);;
				System.out.println("!!!!!888ok");
				stockerInfo.add(stocker);
			}
			System.out.println("!!!!!345567");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		task.cancel();
		timer.cancel();
		System.out.println("widget:destory");
		super.onDestroy();
	}
}
