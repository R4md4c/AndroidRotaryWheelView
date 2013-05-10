package com.activity.rotarywheel;

import java.util.HashMap;
import java.util.List;


import com.rotarywheelview.RotaryWheelMenuEntry;
import com.rotarywheelview.RotaryWheelView;
import com.rotarywheelview.RotaryWheelViewListener;


import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.RotateAnimation;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TextView mResultTextView;
	private ListView mListView;
	
	private HashMap<String, ArrayAdapter<String>> mHashmap = new HashMap<String, ArrayAdapter<String>>();
	
	private String[] mCategories = new String[] {
		"Fun",
		"Services",
		"Auto",
		"Drink",
		"Travel",
		"Urgent",
		"Movies",
		"Health",
		"Stores",
		"Favourites"
	};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        RotaryWheelView wheelView = (RotaryWheelView) findViewById(R.id.wheelView);
        
        
        init();
        
        //mResultTextView = (TextView) findViewById(R.id.textView);
        mListView= (ListView) findViewById(R.id.listView);
        
        /*String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
          "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
          "Linux", "OS/2" };

        // First paramenter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
          R.layout.list_item, R.id.textView, values);

        // Assign adapter to ListView
        listView.setAdapter(adapter);*/ 
        
        wheelView.setRotaryWheelViewListener(new RotaryWheelViewListener() {
			
			@Override
			public void onMenuEntryChanged(RotaryWheelMenuEntry menuEntry) {
				//Toast.makeText(getApplicationContext(), menuEntry.getLabel(), Toast.LENGTH_LONG).show();
				//System.out.println(menuEntry.getLabel());
				mListView.setAdapter( mHashmap.get( menuEntry.getLabel()) );
			}
		});
    }

    
    private void init() {
    	RotaryWheelView wheelView = (RotaryWheelView) findViewById(R.id.wheelView);
    	
    	String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
    	          "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
    	          "Linux", "OS/2" };
    	
    	
    	for(int i = 0; i < mCategories.length; i++) {
    		String str = mCategories[i];
        	wheelView.addMenuEntry(new MenuEntry(str, i));
        	mHashmap.put(str,  new ArrayAdapter<String>(this,
        				R.layout.list_item, R.id.textView, new String[] {str, values[i]}));
        	
        }
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
   
   private static class MenuEntry implements RotaryWheelMenuEntry
   {
	  private String mLabel;
	  private int mIndex;
	  public MenuEntry(String label, int index) {
		  mLabel = label;
		  mIndex = index;
	  }
	  
      public String getName() { return "Menu1 - No Children"; } 
	  public String getLabel() { return mLabel; } 
	  public int getIcon() { return R.drawable.icon0; }
	  public int getIndex() { return mIndex; }
      public void menuActiviated()
      {
    	  System.out.println( "Menu #1 Activated - No Children");
      }
   }
   public static class Menu2 implements RotaryWheelMenuEntry
   {
	  public int getIndex() { return 0; }
      public String getName() { return "Menu2 - No Children"; } 
	  public String getLabel() { return "Menu223\nTest"; } 
	  public int getIcon() { return R.drawable.icon1; }
      public void menuActiviated()
      {
    	  System.out.println( "Menu #2 Activated - No Children");
      }
   }
}
