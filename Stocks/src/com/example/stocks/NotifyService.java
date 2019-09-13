package com.example.stocks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

public class NotifyService extends Service {
	
	private Timer timer;
	private TimerTask task;
	ArrayList<String> codeSet = new ArrayList<String>();
	private static final int STOCK_MSG = 1;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		
		
		timer = new Timer();
		task = new TimerTask() {
			
			@Override
			public void run() {
				System.out.println("service Thread");
				int length = reloadLength();
				if(codeSet.size()!=length) {
					reloadData(length);
				}
				
				if (codeSet.size() != 0) {
					for (int i = 0; i < codeSet.size(); i++) {
						System.out.println("Service:9960");
						NotifyService.this.updateSingleStockInfoWithHttpClient(codeSet.get(i));
						System.out.println("Service:9970");
					}
				}
			}
		};
		
		timer.scheduleAtFixedRate(task, 10000, 10000);
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	private Handler updateHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case STOCK_MSG:
				String information = (String)msg.obj;
				Pattern pattern=Pattern.compile("str_(.+)=\"(.+)\"");
				Matcher matcher=pattern.matcher(information);
				double percent = 0.0;
				String cn = "";
				while(matcher.find()) {
					String result=matcher.group(2);
					String[] data=result.split(",");
					cn = matcher.group(1)+" "+data[0];
					if((matcher.group(1).substring(0,2).equals("sh"))||(matcher.group(1).substring(0,2).equals("sz"))) {
						double current = Double.parseDouble(data[3]);
						double closing_price = Double.parseDouble(data[2]);
						percent=(current-closing_price)*100/closing_price;
					}else if(matcher.group(1).substring(0,2).equals("gb")) {
						double current = Double.parseDouble(data[1]);
						double closing_price = Double.parseDouble(data[26]);
						percent=(current-closing_price)*100/closing_price;
					}else if(matcher.group(1).substring(0,2).equals("hk")) {
						double current = Double.parseDouble(data[6]);
						double closing_price = Double.parseDouble(data[3]);
						percent=(current-closing_price)*100/closing_price;
					}
				}
				if(percent>=2.0) {
					Intent i = new Intent(NotifyService.this, IFStock.class);
					PendingIntent pendingIntent = PendingIntent.getActivity(NotifyService.this, (int)percent, i, PendingIntent.FLAG_UPDATE_CURRENT);
					NotificationManager manager = (NotificationManager) NotifyService.this.getSystemService(Context.NOTIFICATION_SERVICE);
					
					Notification notification = new NotificationCompat.Builder(NotifyService.this)
					        .setContentTitle("Stocks Increment 2%")//Set the title, as necessary
					        .setContentText(cn)//Set the content, as necessary
					        .setWhen(System.currentTimeMillis())//Set time, default Settings, can be ignored
					        .setSmallIcon(R.drawable.ic_launcher)//Set the small icon in the notification bar. It must be set
					        .setAutoCancel(true)//Set auto delete. After clicking the notification bar, the system will automatically delete the notification of the status bar, which is used with setContentIntent
					        .setContentIntent(pendingIntent)//Sets the jump after the message is clicked in the notification bar. The parameter is a pendingIntent
					        .build();
					manager.notify((int)percent,notification);
					System.out.println("*********");
				}
				break;

			default:
				break;
			}
		}
			
	};
	
	public void updateSingleStockInfoWithHttpClient(String code) {
		final String safari = code;
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
	
	private int reloadLength() {
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
			return length;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	private void reloadData(int length) {
		try {
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
		System.out.println("Service:destory");
		super.onDestroy();
	}

	
	
	
}
