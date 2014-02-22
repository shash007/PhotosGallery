// Assignment 4
//PhotosActivity.java
//Shashank G Hebbale (800773977)
// Sahana K Raj(800774871)



package com.example.unccphotos;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class PhotosActivity extends Activity {

	ImageView mChart;
	 @Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		mDiskLruCache.clearCache();
		
		//finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public Context cnt;
	ProgressDialog progressDialog;
	String[] id;
	

	int image_count=0;
		 Timer timer;
	    TimerTask task;
	    Bitmap bm = null;
	    private DiskLruCache mDiskLruCache;
	    private final Object mDiskCacheLock = new Object();
	    private boolean mDiskCacheStarting = true;
	    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
	    private static final String DISK_CACHE_SUBDIR = "thumbnails";
	    boolean bool;
	    String url1;
	   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photos);
		 bool = getIntent().getExtras().getBoolean(MainActivity.photo);
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(false);
		progressDialog.setMessage("Loading Image");
		
		
		mChart = (ImageView)findViewById(R.id.imageView1);
		Resources rsc=this.getResources();
	    id=rsc.getStringArray(R.array.photo_urls);
		final String url = id[0];
		File cacheDir = DiskLruCache.getDiskCacheDir(this, DISK_CACHE_SUBDIR);
	    new InitDiskCacheTask().execute(cacheDir);
		
	   
		
	   if (bool == true)
	   {
	   new DownloadImagesTask().execute(url);
		
		mChart.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				
			    float getwidth = mChart.getWidth();
			    float getxval = arg1.getRawX();
			    
			    if(getxval >= (0.8 *getwidth))
				{	
				if (++image_count == id.length)
				{
					image_count=0;
					String url1 = id[image_count];
					
					new DownloadImagesTask().execute(url1);
					
				}else
				{
					String url1 = id[image_count];
					new DownloadImagesTask().execute(url1);
				}
				}
				
				if(getxval <= (0.2 *getwidth))
				{	
				if (--image_count == -1)
				{
					image_count=id.length-1;
					String url1 = id[image_count];
					new DownloadImagesTask().execute(url1);
				
				}else
				{
					String url1 = id[image_count];
					new DownloadImagesTask().execute(url1);
				}
				}
				return false;
			}
		});
		
	   }
	   else
	   {
		 
	        
	   
	       Timer timer = new Timer();
	 
	        timer.scheduleAtFixedRate(new TimerTask() {
	 
	        public void run() {
	 
	        	if(image_count == id.length)
	        	{
	        		image_count =0;
	        	}
	        	url1 = id[image_count];
	        	PhotosActivity.this.runOnUiThread(new Runnable() {
		            public void run() {
		            	/*if (image_count == id.length)
						{
							image_count=0;
							String url1 = id[image_count];
							new DownloadImagesTask().execute(url1);
							image_count++;
						}else
						{
							String url1 = id[image_count];
							new DownloadImagesTask().execute(url1);
							image_count++;
						}
						*/
		            	new DownloadImagesTask().execute(url1);
		            	image_count++;
		                
		            }
		            
		               
		            }
	        	
		        );
		        
	        
	        }
	 
	        }, 500, 2000);
		
		
	        
	}
	}

	
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.photos, menu);
		return true;
	}
/*
@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(mDiskLruCache != null)
		{
			mDiskLruCache.clearCache();
			
			finish();
			
		}
		
	}
*/
public class DownloadImagesTask extends AsyncTask<String, Void, Bitmap> {

@Override
protected Bitmap doInBackground(String... params) {
	Bitmap bit = getBitmapFromDiskCache(params[0]);
	
	if(bit==null)
	{
		bit = download_Image(params[0]);
		addBitmapToCache(params[0],bit);
		return bit;
	}
	else
	{
		return bit;
	} 
	
}


@Override
protected void onPostExecute(Bitmap result) {
	super.onPostExecute(result);
	
	mChart.setImageBitmap(result);              // how do I pass a reference to mChart here ?
	if(bool ==true)
	progressDialog.dismiss();
}


@Override
protected void onPreExecute() {
	// TODO Auto-generated method stub
	super.onPreExecute();
	if(bool==true)
	progressDialog.show();
}

public Bitmap download_Image(String url) {
    Bitmap bm = null;
    try {
        URL aURL = new URL(url);
        URLConnection conn = aURL.openConnection();
        conn.connect();
        InputStream is = conn.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        bm = BitmapFactory.decodeStream(bis);
        bis.close();
        is.close();
    } catch (IOException e) {
        Log.e("Hub","Error getting the image from server : " + e.getMessage().toString());
    } 
    return bm;
}
}

class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
    protected Void doInBackground(File... params) {
        synchronized (mDiskCacheLock) {
            File cacheDir = params[0];
            mDiskLruCache= DiskLruCache.openCache(PhotosActivity.this,cacheDir, DISK_CACHE_SIZE);
            mDiskCacheStarting = false; // Finished initialization
            mDiskCacheLock.notifyAll(); // Wake any waiting threads
        }
        return null;
    }
}
	
    


public Bitmap getBitmapFromDiskCache(String key) {
    synchronized (mDiskCacheLock) {
        // Wait while disk cache is started from background thread
        while (mDiskCacheStarting) {
            try {
                mDiskCacheLock.wait();
            } catch (InterruptedException e) {}
        }
        if (mDiskLruCache != null) {
            return mDiskLruCache.get(key);
        }
    }
    return null;
}
       
public void addBitmapToCache(String key, Bitmap bitmap) {
    
    // Also add to disk cache
    synchronized (mDiskCacheLock) {
        if (mDiskLruCache != null) {
            mDiskLruCache.put(key, bitmap);
        }
    }
}

}
