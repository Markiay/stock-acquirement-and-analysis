package com.example.stocks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class IFStock extends Activity{
	
	private ListView listview;
	private EditText etcode;
	private Button search;
	ArrayList<Stocker> stockerInfo = new ArrayList<Stocker>();
	ArrayList<String> codeSet = new ArrayList<String>();
	private StockAdapter stockAdapter = null;
	public static final int STOCK_INFORMATION = 0;
	private Bitmap bitmap = null;
	private Handler chartHandler = null;
	private Handler handler1 = null;
	private Handler handler2 = null;
	private Intent serviceIntent;
	private SharedPreferences sp;
	boolean num=false;
	private ImageView ivExpand;
	private Timer timer;
	private TimerTask task;
	float scaleWidth;
	float scaleHeight;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE) {
			setContentView(R.layout.fragment_container);
			Fragment_origin fragment = new Fragment_origin();
			FragmentManager fm = getFragmentManager();//get initial FragmentManger 
			FragmentTransaction ft=fm.beginTransaction();//Start a transaction
			ft.add(R.id.right_fragment,fragment);
			ft.commit();

		}else {
			setContentView(R.layout.tab_set);
			
			TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
			tabHost.setup();
			
			LayoutInflater.from(this).inflate(R.layout.ifstock, tabHost.getTabContentView());
			LayoutInflater.from(this).inflate(R.layout.index_info, tabHost.getTabContentView());
			
			tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("page one").setContent(R.id.tab01));
			tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("page two").setContent(R.id.tab02));
			
			tabHost.setOnTabChangedListener(new OnTabChangeListener() {
				
				@Override
				public void onTabChanged(String arg0) {
					// TODO Auto-generated method stub
					if(arg0.equals("tab1")) {
						Toast.makeText(IFStock.this, "click page one", Toast.LENGTH_SHORT).show();
					}
					if(arg0.equals("tab2")){
						final ImageView shIndex, hkIndex, mgIndex;
						final TextView sh2, sh3, hk2, hk3, mg2, mg3;
						shIndex = (ImageView) findViewById(R.id.sh_index);
						sh2 = (TextView) findViewById(R.id.sh_point1);
						sh3 = (TextView) findViewById(R.id.sh_rate1);
						
						hkIndex = (ImageView) findViewById(R.id.hk_index);
						hk2 = (TextView) findViewById(R.id.hk_point1);
						hk3 = (TextView) findViewById(R.id.hk_rate1);
						
						mgIndex = (ImageView) findViewById(R.id.gb_index);
						mg2 = (TextView) findViewById(R.id.nasdaq_point1);
						mg3 = (TextView) findViewById(R.id.nasdaq_rate1);
						
						String[] index = {"s_sh000001", "int_hangseng", "int_nasdaq"};
						for(int i = 0; i< 3; i++) {
							getSingleStockInfoWithHttpClient(index[i]);
							handler1 = new Handler() {
								
								@Override
								public void handleMessage(Message msg) {
									super.handleMessage(msg);
									String information = (String)msg.obj;
									Pattern pattern=Pattern.compile("str_(.+)=\"(.+)\"");
									Matcher matcher=pattern.matcher(information);
									switch (msg.what) {
									case 1:
										while(matcher.find()) {
											String result=matcher.group(2);
											String[] data=result.split(",");
											sh2.setText(data[1]);
											String[] splitStr = data[3].split("%");
											sh3.setText(Double.parseDouble(splitStr[0])+"");
											if(Double.parseDouble(splitStr[0])>0) {
												sh3.setTextColor(0xffEE3B3B);
											}else {
												sh3.setTextColor(0xff2e8b57);					
											}
											System.out.println("!!!!!!!!888");
										}
										break;

									case 2:
										while(matcher.find()) {
											String result=matcher.group(2);
											String[] data=result.split(",");
											hk2.setText(data[1]);
											String[] splitStr = data[3].split("%");
											hk3.setText(Double.parseDouble(splitStr[0])+"");
											if(Double.parseDouble(splitStr[0])>0) {
												hk3.setTextColor(0xffEE3B3B);
											}else {
												hk3.setTextColor(0xff2e8b57);					
											}
											System.out.println("!!!!!!!!888");
										}
										break;
									
									case 3:
										while(matcher.find()) {
											String result=matcher.group(2);
											String[] data=result.split(",");
											mg2.setText(data[1]);
											String[] splitStr = data[3].split("%");
											mg3.setText(Double.parseDouble(splitStr[0])+"");
											if(Double.parseDouble(splitStr[0])>0) {
												mg3.setTextColor(0xffEE3B3B);
											}else {
												mg3.setTextColor(0xff2e8b57);					
											}
											System.out.println("!!!!!!!!888");
										}
										break;
										
									default:
										System.out.println("!!!!!!!34656");
										break;
									}
								}
								
							};
						}
						
//						.ixic hsi "int_hangseng""int_nasdaq"
						String[] chart = {"sh000001","hsi",".ixic"};
						for(int i = 0; i< 3; i++) {
							IFStock.this.getChartWithHttpClient(chart[i]);
							handler2 = new Handler() {
								@Override
								public void handleMessage(Message msg) {
									super.handleMessage(msg);
									final float scaleWidth;
									final float scaleHeight;
									DisplayMetrics dm = new DisplayMetrics();//Create a matrix
									IFStock.this.getWindowManager().getDefaultDisplay().getMetrics(dm);
									int width = bitmap.getWidth();
									int height = bitmap.getHeight();
									int w = dm.widthPixels; //Get the width of the screen
									int h = dm.heightPixels; //Get the height of the screen
									scaleWidth = ((float) w) / width;
									scaleHeight = ((float) h) / height;
									
									switch (msg.what) {
									case 1:
										shIndex.setImageBitmap(bitmap);
										shIndex.setScaleType(ScaleType.FIT_XY);
										shIndex.setBackgroundColor(Color.WHITE);
										shIndex.setOnClickListener(new View.OnClickListener() {

											@Override
											public void onClick(View arg0) {
												View layout = getLayoutInflater().inflate(R.layout.expand_image, null);
												new AlertDialog.Builder(IFStock.this).setTitle("Shanghai").setView(layout).setNegativeButton("Back", null).show();
												ivExpand = (ImageView) layout.findViewById(R.id.ivExpand);
													Matrix matrix = new Matrix();
													matrix.postScale(scaleWidth, scaleHeight);
													Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
															bitmap.getHeight(), matrix, true);
													ivExpand.setImageBitmap(newBitmap);
											}
										});
										break;
									case 2:
										hkIndex.setImageBitmap(bitmap);
										hkIndex.setScaleType(ScaleType.FIT_XY);
										hkIndex.setBackgroundColor(Color.WHITE);
										hkIndex.setOnClickListener(new View.OnClickListener() {

											@Override
											public void onClick(View arg0) {
												View layout = getLayoutInflater().inflate(R.layout.expand_image, null);
												new AlertDialog.Builder(IFStock.this).setTitle("HongKong").setView(layout).setNegativeButton("Back", null).show();
												ivExpand = (ImageView) layout.findViewById(R.id.ivExpand);
													Matrix matrix = new Matrix();
													matrix.postScale(scaleWidth, scaleHeight);
													Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
															bitmap.getHeight(), matrix, true);
													ivExpand.setImageBitmap(newBitmap);
											}
										});
										break;	
									case 3:
										mgIndex.setImageBitmap(bitmap);
										mgIndex.setScaleType(ScaleType.FIT_XY);
										mgIndex.setBackgroundColor(Color.WHITE);
										mgIndex.setOnClickListener(new View.OnClickListener() {

											@Override
											public void onClick(View arg0) {
												View layout = getLayoutInflater().inflate(R.layout.expand_image, null);
												new AlertDialog.Builder(IFStock.this).setTitle("Nasdaq").setView(layout).setNegativeButton("Back", null).show();
												ivExpand = (ImageView) layout.findViewById(R.id.ivExpand);
													Matrix matrix = new Matrix();
													matrix.postScale(scaleWidth, scaleHeight);
													Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
															bitmap.getHeight(), matrix, true);
													ivExpand.setImageBitmap(newBitmap);
											}
										});
										break;
									default:
										break;
									}
								}
							};
						}
						
						
					}
				}
			});
		}
		
		this.reloadData();
		System.out.println("MMMMM");
		
		etcode = (EditText) this.findViewById(R.id.etcode);
		search = (Button) this.findViewById(R.id.search);
		listview = (ListView) IFStock.this.findViewById(R.id.listView);
		stockAdapter = new StockAdapter(IFStock.this, new Intent(IFStock.this, MainActivity.class), stockerInfo);
		listview.setAdapter(stockAdapter);
		
		search.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				String content = etcode.getText().toString();
				content = content.replace(" ", "h");
				content = content.replace("\n", "h");
				content = content.replace(",", "h");
				etcode.setText(null);
				if(content.equals("")) {
					Toast.makeText(IFStock.this, "Please enter code", Toast.LENGTH_LONG).show();
				}else if(codeSet.contains(content)) {
					Toast.makeText(IFStock.this, "Code exists, Please enter other again", Toast.LENGTH_LONG).show();
				}else if(content.matches("^[0-9]+.*")) {
					Toast.makeText(IFStock.this, "Please enter correct code", Toast.LENGTH_LONG).show();
				}else {
					codeSet.add(content);
					System.out.println("!!!4"+codeSet.size());
					stockAdapter.updateSingleStockInfoWithHttpClient(content);					
					IFStock.this.getSingleStockInfoWithHttpClient(content);					
				}
				System.out.println("!!!5"+codeSet.size());
			}
		});
				
		timer = new Timer();
		task = new TimerTask() {

			@Override
			public void run() {
				if (codeSet.size() != 0) {
					for (int i = 0; i < codeSet.size(); i++) {
						System.out.println("!!!3333322");
						stockAdapter.updateSingleStockInfoWithHttpClient(codeSet.get(i));
						System.out.println("!!!222222");
					}
				}
			}
		};
		
		timer.scheduleAtFixedRate(task, 10000, 10000);
		
		
		//Click on the event k-diagram
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				final int position = arg2;
				Stocker stocker = stockAdapter.getItem(arg2);
				if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE) {
					
					String arg = position + "!" + stocker.getCode() + "!" + stocker.getName() + "!"
							+ stocker.getOpening_price() + "!" + stocker.getClosing_price() + "!"
							+ stocker.getMax_price() + "!" + stocker.getMin_price() + "!" + stocker.getCurrent_price();
					
					Fragment_detail fragment = Fragment_detail.newInstance(arg);
					FragmentManager fragmentManager = getFragmentManager();//The operation to change data in a database
					FragmentTransaction transaction = fragmentManager.beginTransaction();//Atomic operation
					transaction.replace(R.id.right_fragment, fragment);
					transaction.addToBackStack(null);//Add a transaction to a return stack, press the back key and return to the previous fragment
					transaction.commit();
					//fragment.setInfoForTextView(stocker);
				}else {
				LayoutInflater inflater = getLayoutInflater();
				View layout = inflater.inflate(R.layout.info_dialog, null);
				AlertDialog dialog = new AlertDialog.Builder(IFStock.this).setTitle("Stock")
				.setView(layout).setNegativeButton("Back", null).show();
				
				final TextView info_code = (TextView) dialog.findViewById(R.id.info_code);
				final TextView info_name = (TextView) dialog.findViewById(R.id.info_name);
				final TextView info_current = (TextView) dialog.findViewById(R.id.info_current);
				final TextView info_open = (TextView) dialog.findViewById(R.id.info_open);
				final TextView info_close = (TextView) dialog.findViewById(R.id.info_close);
				final TextView info_high = (TextView) dialog.findViewById(R.id.info_high);
				final TextView info_low = (TextView) dialog.findViewById(R.id.info_low);
				final ImageView chart = (ImageView) dialog.findViewById(R.id.K_chart);
				Button update = (Button) dialog.findViewById(R.id.btn_update);
				
				DecimalFormat df=new DecimalFormat("#0.00"); 
				double current = Double.parseDouble(stocker.getCurrent_price());
				double closing_price = Double.parseDouble(stocker.getClosing_price());
				String percent=df.format(((current-closing_price)*100/closing_price))+"%";
				if(current > closing_price) {
					info_current.setTextColor(0xffEE3B3B);
				}else {
					info_current.setTextColor(0xff2e8b57);					
				}
				info_code.setText(stocker.getCode());
				info_name.setText(stocker.getName());
				info_current.setText(df.format(current)+"("+percent+")");
				info_open.setText(stocker.getOpening_price());
				info_close.setText(stocker.getClosing_price());
				info_high.setText(stocker.getMax_price());
				info_low.setText(stocker.getMin_price());
				
				IFStock.this.getChartWithHttpClient(stocker.getCode());
				chartHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						super.handleMessage(msg);
						switch (msg.what) {
							case 1:
								chart.setScaleType(ScaleType.FIT_XY);
								chart.setBackgroundColor(Color.WHITE);
								DisplayMetrics dm = new DisplayMetrics();//Create a matrix
								IFStock.this.getWindowManager().getDefaultDisplay().getMetrics(dm);
								if(bitmap!=null) {
									
									int width = bitmap.getWidth();
									int height = bitmap.getHeight();
									int w = dm.widthPixels; //Get the width of the screen
									int h = dm.heightPixels; //Get the height of the screen
									scaleWidth = ((float) w) / width;
									scaleHeight = ((float) h) / height;
								}
								chart.setImageBitmap(bitmap);
								chart.setOnClickListener(new View.OnClickListener() {

									@Override
									public void onClick(View arg0) {

										if (num == true) {
											Matrix matrix = new Matrix();
											matrix.postScale(scaleWidth, scaleHeight);
											
											Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
													bitmap.getHeight(), matrix, true);
											chart.setImageBitmap(newBitmap);
											num = false;
										} else {
											Matrix matrix = new Matrix();
											matrix.postScale(1.0f, 1.0f);
											Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
													bitmap.getHeight(), matrix, true);
											chart.setImageBitmap(newBitmap);
											num = true;
										}

									}
								});
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
						Stocker stocker2 = stockAdapter.getItem(position);
						DecimalFormat df=new DecimalFormat("#0.00"); 
						double current = Double.parseDouble(stocker2.getCurrent_price());
						double closing_price = Double.parseDouble(stocker2.getClosing_price());
						String percent=df.format(((current-closing_price)*100/closing_price))+"%";
						if(current > closing_price) {
							info_current.setTextColor(0xffEE3B3B);
						}else {
							info_current.setTextColor(0xff2e8b57);					
						}
						info_code.setText(stocker2.getCode());
						info_name.setText(stocker2.getName());
						info_current.setText(df.format(current)+"("+percent+")");
						info_open.setText(stocker2.getOpening_price());
						info_close.setText(stocker2.getClosing_price());
						info_high.setText(stocker2.getMax_price());
						info_low.setText(stocker2.getMin_price());
						
						IFStock.this.getChartWithHttpClient(stocker2.getCode());
						chartHandler = new Handler() {
							@Override
							public void handleMessage(Message msg) {
								super.handleMessage(msg);
								switch (msg.what) {
								case 1:
									chart.setImageBitmap(bitmap);
									chart.setScaleType(ScaleType.FIT_XY);
									break;

								default:
									break;
								}
							}
						};
						
						
					}
				});
			}
			}
			
		});
		//Long press delete
		listview.setOnItemLongClickListener(new OnItemLongClickListener() {
			
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				final int position = arg2;
				new AlertDialog.Builder(IFStock.this).setTitle("Tips").setMessage("Are you sure to delete '"+stockerInfo.get(position).getName()+"' ?")
				.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						stockAdapter.removeItem(position);
						codeSet.remove(position);
						IFStock.this.saveDate();
						System.out.println(codeSet.size());
					}
				}).setNegativeButton("No", null).show();
				return true;
			}
		});
		
	}
	
	//Message processing at first fetch
	private Handler handler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case STOCK_INFORMATION:
				String information = (String)msg.obj;
				Pattern pattern=Pattern.compile("str_(.+)=\"(.+)\"");
				Matcher matcher=pattern.matcher(information);
				if(matcher.find()==false) {
					System.out.println("delete"+codeSet.get(codeSet.size()-1));
					codeSet.remove(codeSet.size()-1);
					IFStock.this.saveDate();
				}
				break;

			default:
				System.out.println("!!!!!!!34656");
				break;
			}
		}
		
	};
	
	//Connected to the Internet access
	private void getSingleStockInfoWithHttpClient(String code) {
		//final String safari = code;
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
						if(safari.equals("s_sh000001")||safari.equals("int_hangseng")||safari.equals("int_nasdaq")) {
							Message message = new Message();
							if(safari.equals("s_sh000001")) {
								message.what = 1;
							}else if(safari.equals("int_hangseng")) {
								message.what = 2;
							}else if(safari.equals("int_nasdaq")) {
								message.what = 3;
							}
							message.obj = response.toString();
							handler1.sendMessage(message);
						}else {
							//The Message object is emitted in the child thread
							 Message message = new Message();
							 message.what = STOCK_INFORMATION;
							 message.obj = response.toString();
							 handler.sendMessage(message);
						}
						
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
				}else if(safari.equals(".ixic")) {
					httpGet = new HttpGet("http://image.sinajs.cn/newchart/usstock/daily/"+safari+".gif");
				}else if(safari.substring(0,2).equals("hk")) {
					String str =  safari.substring(2);
					httpGet = new HttpGet("http://image.sinajs.cn/newchart/hk_stock/daily/"+str+".gif");
				}else if(safari.equals("hsi")) {
					httpGet = new HttpGet("http://image.sinajs.cn/newchart/hk_stock/daily/"+safari+".gif");
				}
				
				try {
					//Step 3: execute the request and get the corresponding object returned by the server
					HttpResponse httpResponse = httpCient.execute(httpGet);
					//Step 4: check whether the corresponding state is normal: check whether the value of the status code is 200 to indicate that it is normal
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						//Step 5: extract data from the corresponding object and put it into entity
						HttpEntity entity = httpResponse.getEntity();
						//Converts the returned content to bitmap
						InputStream is = entity.getContent();
						bitmap = BitmapFactory.decodeStream(is);
						if(safari.equals("hsi")||safari.equals(".ixic")||safari.equals("sh000001")) {
							//Send a message to handler to display the image
							 Message message = new Message();
							 if(safari.equals("sh000001")) {message.what = 1;}
							 else if(safari.equals("hsi")) { message.what = 2;}
							 else if(safari.equals(".ixic")) { message.what = 3;}							 
							 handler2.sendMessage(message);
						}else {
							//Send a message to handler to display the image
							 Message message = new Message();
							 message.what = 1;
							 chartHandler.sendMessage(message);
						}
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
	
	public void updateFragment(int site) {
		Stocker stocker = stockerInfo.get(site);
		String arg = site + "!" + stocker.getCode() + "!" + stocker.getName() + "!"
				+ stocker.getOpening_price() + "!" + stocker.getClosing_price() + "!"
				+ stocker.getMax_price() + "!" + stocker.getMin_price() + "!" + stocker.getCurrent_price();
		
		Fragment_detail fragment = Fragment_detail.newInstance(arg);
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(R.id.right_fragment, fragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	private void saveDate() {
		try {
			int amount = codeSet.size();
			String amt = amount + "";
			// Open output stream to save the info for "to"
			OutputStream osa = this.openFileOutput("amount", Context.MODE_PRIVATE);
			// Writes the information to be saved in the buffer pool
			osa.write(amt.getBytes("UTF-8"));
			osa.flush();
			// close output stream
			osa.close();
			
			for(int position = 0; position < amount; position++) {
				String stockCode = codeSet.get(position);
				OutputStream os = this.openFileOutput(Integer.toString(position), Context.MODE_PRIVATE);
				os.write(stockCode.getBytes("UTF-8"));
				os.flush();
				os.close();
				System.out.println(position+"!999"+stockCode);
			}
			
			for(int position = 0; position < amount; position++) {
				Stocker stocker = stockerInfo.get(position);
				String arg = stocker.getCode() + "!" + stocker.getName() + "!"
						+ stocker.getOpening_price() + "!" + stocker.getClosing_price() + "!"
						+ stocker.getMax_price() + "!" + stocker.getMin_price() + "!" + stocker.getCurrent_price();
				OutputStream os = this.openFileOutput("stockerInfo"+Integer.toString(position), Context.MODE_PRIVATE);
				os.write(arg.getBytes("UTF-8"));
				os.flush();
				os.close();
				System.out.println(position+"!777"+arg);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	protected void onPause() {
		super.onPause();
		
		IFStock.this.saveDate();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		task.cancel();
		timer.cancel();
		System.out.println("ok");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			sp = getSharedPreferences("state", Context.MODE_PRIVATE);
			String state = sp.getString("oc", "Notification Stoped");
			Toast.makeText(IFStock.this, state, Toast.LENGTH_LONG).show();
			serviceIntent = new Intent(IFStock.this,NotifyService.class);
			LayoutInflater inflater = getLayoutInflater();
			final View layout = inflater.inflate(R.layout.meau_dialog, null);
			new AlertDialog.Builder(IFStock.this).setTitle("Tips")
			.setView(layout).setPositiveButton("Confirm",new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					RadioButton open = (RadioButton) layout.findViewById(R.id.btnOpen);
					RadioButton close = (RadioButton) layout.findViewById(R.id.btnclose);
					if(open.isChecked()) {
						IFStock.this.startService(serviceIntent);
						Editor e = sp.edit();
						e.putString("oc", "Notification Started");
						e.commit();
						System.out.println("Service:ok");
					}else {
						IFStock.this.stopService(serviceIntent);
						Editor e = sp.edit();
						e.putString("oc", "Notification Stoped");
						e.commit();
						System.out.println("Service:ok2");
					}
				}
			}).setNegativeButton("Cancel", null).show();
			
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
		
}
