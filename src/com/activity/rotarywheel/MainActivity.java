package com.activity.rotarywheel;

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
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView mResultTextView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        RotaryWheelView wheelView = (RotaryWheelView) findViewById(R.id.wheelView);
        wheelView.addMenuEntry(new Menu1());
        wheelView.addMenuEntry(new Menu1());
        wheelView.addMenuEntry(new Menu2());
        wheelView.addMenuEntry(new Menu2());
        wheelView.addMenuEntry(new Menu1());
        wheelView.addMenuEntry(new Menu1());
        wheelView.addMenuEntry(new Menu2());
        wheelView.addMenuEntry(new Menu1());
        
        mResultTextView = (TextView) findViewById(R.id.textView);
        
        wheelView.setRotaryWheelViewListener(new RotaryWheelViewListener() {
			
			@Override
			public void onMenuEntryChanged(RotaryWheelMenuEntry menuEntry) {
				//System.out.println(menuEntry.getLabel());
				mResultTextView.setText( menuEntry.getLabel() );
			}
		});
    }

    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
   
   public static class Menu1 implements RotaryWheelMenuEntry
   {
      public String getName() { return "Menu1 - No Children"; } 
	  public String getLabel() { return "Menu1\nTest"; } 
	  public int getIcon() { return R.drawable.icon0; }
      public void menuActiviated()
      {
    	  System.out.println( "Menu #1 Activated - No Children");
      }
   }
   public static class Menu2 implements RotaryWheelMenuEntry
   {
      public String getName() { return "Menu2 - No Children"; } 
	  public String getLabel() { return "Menu223\nTest"; } 
	  public int getIcon() { return R.drawable.icon1; }
      public void menuActiviated()
      {
    	  System.out.println( "Menu #2 Activated - No Children");
      }
   }
}
