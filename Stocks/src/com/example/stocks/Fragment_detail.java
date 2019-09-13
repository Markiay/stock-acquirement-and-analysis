package com.example.stocks;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class Fragment_detail extends Fragment {
	
	private TextView info_code, info_name, info_current, info_open, info_close, info_high, info_low;
	private ImageView chart;
	private Button update;
	private Bitmap bitmap = null;
	private Handler chartHandler = null;
	private int position;
	private double current;
	
	public Fragment_detail() {
		
	}
	
	public static Fragment_detail newInstance(String arg) {
		Fragment_detail fragment = new Fragment_detail();
		Bundle bundle = new Bundle();
		bundle.putString("fragment_detail", arg);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final LayoutInflater finalInflater = inflater;
		View view = inflater.inflate(R.layout.info_dialog, container, false);
		
		info_code = (TextView) view.findViewById(R.id.info_code);
		info_name = (TextView) view.findViewById(R.id.info_name);
		info_current = (TextView) view.findViewById(R.id.info_current);
		info_open = (TextView) view.findViewById(R.id.info_open);
		info_close = (TextView) view.findViewById(R.id.info_close);
		info_high = (TextView) view.findViewById(R.id.info_high);
		info_low = (TextView) view.findViewById(R.id.info_low);
		chart = (ImageView) view.findViewById(R.id.K_chart);
		update = (Button) view.findViewById(R.id.btn_update);
		
		String allInfo = getArguments().getString("fragment_detail");
		String[] divInfo = allInfo.split("!");
		position = Integer.parseInt(divInfo[0]);
		
		DecimalFormat df=new DecimalFormat("#0.00"); 
		current = Double.parseDouble(divInfo[7]);
		double closing_price = Double.parseDouble(divInfo[4]);
		String percent=df.format(((current-closing_price)*100/closing_price))+"%";
		if(current > closing_price) {
			info_current.setTextColor(0xffEE3B3B);
		}else {
			info_current.setTextColor(0xff2e8b57);					
		}
		info_code.setText(divInfo[1]);
		info_name.setText(divInfo[2]);
		info_current.setText(df.format(current)+"("+percent+")");
		info_open.setText(divInfo[3]);
		info_close.setText(divInfo[4]);
		info_high.setText(divInfo[5]);
		info_low.setText(divInfo[6]);

		this.getChartWithHttpClient(divInfo[1]);
		chartHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case 1:
					chart.setImageBitmap(bitmap);
					chart.setScaleType(ScaleType.FIT_XY);
					chart.setBackgroundColor(Color.WHITE);
					break;

				default:
					break;
				}
			}
		};
		
		//Refresh interface button
		update.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(current!=0.00) {
					IFStock ifstock = (IFStock) getActivity();
					ifstock.updateFragment(position);
				}
				
				
			}
		});
		
		
		
		
		return view;
	}
	
	//Get picture
		public void getChartWithHttpClient(String code) {
			final String safari = code;
			new Thread(new Runnable() {

				@Override
				public void run() {
					HttpClient httpCient = new DefaultHttpClient();
					HttpGet httpGet = null;
					
					if((safari.substring(0,2).equals("sh")) || (safari.substring(0,2).equals("sz"))) {
						httpGet = new HttpGet("http://image.sinajs.cn/newchart/daily/n/"+safari+".gif");					
					}else if(safari.substring(0,2).equals("gb")) {
						String[] str =  safari.split("_");
						httpGet = new HttpGet("http://image.sinajs.cn/newchart/usstock/daily/"+str[1]+".gif");
					}else if(safari.substring(0,2).equals("hk")) {
						String str =  safari.substring(2);
						httpGet = new HttpGet("http://image.sinajs.cn/newchart/hk_stock/daily/"+str+".gif");
					}
					
					try {
						//Step 3: execute the request and get the corresponding object returned by the server
						HttpResponse httpResponse = httpCient.execute(httpGet);
						//Step 4: check whether the corresponding state is normal: check whether the value of the status code is 200 to indicate that it is normal
						if (httpResponse.getStatusLine().getStatusCode() == 200) {
							//Step 5: extract data from the corresponding object and put it into entity
							HttpEntity entity = httpResponse.getEntity();
							InputStream is = entity.getContent();
							//Converts the returned content to bitmap
							bitmap = BitmapFactory.decodeStream(is);
							
							//Send a message to handler to display the image
							 Message message = new Message();
							 message.what = 1;
							 chartHandler.sendMessage(message);
						}
						
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
			}).start();
		}
	

}
