/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rotarywheelview;

import java.util.ArrayList;
import java.util.List;

import com.activity.rotarywheel.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RadialGradient;
import android.graphics.Shader;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;



/**
 * Custom view that presents up to two items that are selectable by rotating a semi-circle from
 * left to right, or right to left.  Used by incoming call screen, and the lock screen when no
 * security pattern is set.
 */
public class RotaryWheelView extends View {

	private List<RotaryWheelMenuEntry> mMenuEntries = new ArrayList<RotaryWheelMenuEntry>();
	
	private RotaryWheelViewListener mListener = null;
	private GestureDetector mDetector;
	private boolean[] mQuadrantTouched = new boolean[] { false, false, false, false, false };
	private boolean mAllowRotating = true;
	private Bitmap mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), com.activity.rotarywheel.R.drawable.bg);
	
	private Shader mShader;
	
	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
	//private RadialGradient mSelectionWedgeGradient = new RadialGradient(x, y, radius, colors, positions, tile)
	
	private float screen_density = getContext().getResources().getDisplayMetrics().density;
	
	private int mMinSize = scalePX(35);				//Radius of inner ring size
	private int mMaxSize = scalePX(90);				//Radius of outer ring size
	private int r2MinSize = mMaxSize+scalePX(5);		//Radius of inner second ring size
	private int r2MaxSize = r2MinSize+scalePX(45);	//Radius of outer second ring size
	private double mStartAngle;
	private double mRotationAngle;
	private int MinIconSize = scalePX(15);					//Min Size of Image in Wedge
	private int MaxIconSize = scalePX(25);			//Max Size of Image in Wedge
	//private int BitmapSize = scalePX(40);			//Size of Image in Wedge
	private int cRadius = mMinSize - scalePX(7); 	 	//Inner Circle Radius
	private int textSize = scalePX(10);				//TextSize
	private int animateTextSize = textSize;
	private int mWedgeQty = 8;
	private int xPosition = scalePX(120);			//Center X location of Radial Menu
	private int yPosition = scalePX(120);			//Center Y location of Radial Menu
	private int outlineColor = Color.rgb(150, 150, 150);  	//color of outline
	private int outlineAlpha = 255;							//transparency of outline
	
	private Wedge[] mWedges;
	private Wedge mSelectionWedge = null;
	
	private Rect[] iconRect;
	//A Rect that will hold the width and height of the View
	private RectF mViewRect = new RectF();
	private RectF mSelectionWedgeRect;
	private RectF mWedgeRect;
	
	private int textColor = Color.BLACK;  	//color to fill when something is selected
	private int textAlpha = 255;						//transparency of fill when something is selected
	
	private int scalePX( int dp_size )
    {
       int px_size = (int) (dp_size * screen_density + 0.5f);
       return px_size;
    }
	
	private int getIconSize(int iconSize, int minSize, int maxSize) {
		
	    if (iconSize > minSize) {
	    	if (iconSize > maxSize) {
	    		return maxSize;
	    	} else {	//iconSize < maxSize
	    		return iconSize;
	    	}
	    } else {  //iconSize < minSize
	    	return minSize;
	    }

	}
	
    public RotaryWheelView(Context context) {
        this(context, null);
    }

    /**
     * Constructor used when this widget is created from a layout file.
     */
    public RotaryWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mDetector = new GestureDetector(context, new MyGestureDetector());
        
        
        this.xPosition = (getResources().getDisplayMetrics().widthPixels)/2;
		
        this.yPosition = -50;
        
		determineWedges();
		
		
		
    }
    
    private void determineWedges() {
    	int entriesQty = mMenuEntries.size();
	    if ( entriesQty > 0) {
		    mWedgeQty = entriesQty;
		
	    	float degSlice = 360 / mWedgeQty;
			float start_degSlice = 270 - (degSlice/2);
	    	//calculates where to put the images
			double rSlice = (2*Math.PI) / mWedgeQty;
			double rStart = (2*Math.PI)*(0.75) - (rSlice/2);
			
			
			this.mWedges = new Wedge[mWedgeQty];
			this.iconRect = new Rect[mWedgeQty];
			
			for(int i = 0; i < this.mWedges.length; i++) {
				this.mWedges[i] = new Wedge(xPosition, yPosition, 0, mMaxSize, (i
						* degSlice)+start_degSlice, degSlice);
				float xCenter = (float)(Math.cos(((rSlice*i)+(rSlice*0.5))+rStart) * (mMaxSize+mMinSize)/2)+xPosition;
				float yCenter = (float)(Math.sin(((rSlice*i)+(rSlice*0.5))+rStart) * (mMaxSize+mMinSize)/2)+yPosition;
				
				int h = MaxIconSize;
				int w = MaxIconSize;
				if ( mMenuEntries.get(i).getIcon() != 0 ) {
				    Drawable drawable = getResources().getDrawable(mMenuEntries.get(i).getIcon());
				    h = getIconSize(drawable.getIntrinsicHeight(),MinIconSize,MaxIconSize);
				    w = getIconSize(drawable.getIntrinsicWidth(),MinIconSize,MaxIconSize);
				}
				
			    this.iconRect[i] = new Rect( (int) xCenter-w/2, (int) yCenter-h/2, (int) xCenter+w/2, (int) yCenter+h/2);
			    
			    mViewRect.union( new RectF( mWedges[i].getWedgeRegion().getBounds() ) );
			}
	    	
			//mSelectionWedge = new Wedge(xPosition, yPosition, 0, mMaxSize, ( (mWedgeQty / 2) * degSlice) +  start_degSlice + (degSlice / 4), degSlice / 2 );
			mSelectionWedge = new Wedge(xPosition, yPosition, 0, mMaxSize, ( (mWedgeQty / 2) * degSlice) +  start_degSlice , degSlice );
			
			mSelectionWedgeRect = new RectF( mSelectionWedge.getWedgeRegion().getBounds() );
			mWedgeRect = new RectF();
			
			//Reduce the selectionWedge region with 50% to be fully contained inside the other wedges
			Region selectionWedgeRegion = mSelectionWedge.getWedgeRegion();
			Rect r = selectionWedgeRegion.getBounds();
			selectionWedgeRegion.set(r.left + scalePX(20), r.top + (r.height() / 2)  , r.right - scalePX(20), r.bottom - scalePX(6));
			
			mShader = new RadialGradient(xPosition, yPosition, mMaxSize, new int[] { 0xff595756, 0xffCCC5C3, 0xf878280}, null, Shader.TileMode.MIRROR);
			//mShader = new BitmapShader(BitmapFactory.decodeResource(getResources(), R.drawable.segment), Shader.TileMode.MIRROR, Shader.TileMode.MIRROR);
			//this.setBackgroundResource(com.activity.rotarywheel.R.drawable.bg);
			invalidate();
			
			
	    }
    }
    
	@Override
    public void onDraw(Canvas canvas) {
    	mPaint.setColor(Color.RED);
    	
    	//canvas.save();
    	canvas.scale(getWidth() / mViewRect.width(), 2.0f, xPosition, yPosition);
    	
    	//System.out.println(mViewRect);
    	
    	
    	//canvas.drawRect(mViewRect, mPaint);
    	canvas.save(Canvas.MATRIX_SAVE_FLAG); //Saving the canvas and later restoring it so only this image will be rotated.
    	canvas.rotate((float)mRotationAngle, xPosition, yPosition);
    	
    	
    	
    	for (int i = 0; i < mWedges.length; i++) {
			Wedge f = mWedges[i];
			mPaint.setColor(0xffBFBFBF);
			//mPaint.setAlpha(outlineAlpha); 
			mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
			canvas.drawPath(f, mPaint);
			//canvas.drawRect(f.getWedgeRegion().getBounds(), mPaint);
			Rect rf = iconRect[i];
			
			if ((mMenuEntries.get(i).getIcon() != 0) && (mMenuEntries.get(i).getLabel() != null)) {
				
				//This will look for a "new line" and split into multiple lines					
				String menuItemName = mMenuEntries.get(i).getLabel();
				String[] stringArray = menuItemName.split("\n");

		    	mPaint.setColor(textColor);
				
		    	mPaint.setStyle(Paint.Style.FILL);
		    	mPaint.setTextSize(textSize);
				
				Rect rect = new Rect();
				float textHeight = 0;
				for (int j = 0; j < stringArray.length; j++)  {
					mPaint.getTextBounds(stringArray[j],0,stringArray[j].length(),rect);
					textHeight = textHeight+(rect.height()+3);
			    }

				Rect rf2 = new Rect();
				rf2.set(rf.left, rf.top-((int)textHeight/2), rf.right, rf.bottom-((int)textHeight/2));

				float textBottom = rf2.bottom;
				for (int j = 0; j < stringArray.length; j++) {
					mPaint.getTextBounds(stringArray[j],0,stringArray[j].length(),rect);
					float textLeft = rf.centerX() - rect.width()/2;
					textBottom = textBottom + (rect.height()+3);
					canvas.drawText(stringArray[j], textLeft-rect.left, textBottom-rect.bottom, mPaint);
			    }
							
				//Puts in the Icon
			    Drawable drawable = getResources().getDrawable(mMenuEntries.get(i).getIcon());			    
				drawable.setBounds(rf2);
				
				drawable.draw(canvas);					

		//Icon Only
			} else if (mMenuEntries.get(i).getIcon() != 0) {
				
				//Puts in the Icon
			    Drawable drawable = getResources().getDrawable(mMenuEntries.get(i).getIcon());			    
				drawable.setBounds(rf);
				
				drawable.draw(canvas);				
				
				//Text Only					
				} else {
					//Puts in the Text if no Icon
			    	mPaint.setColor(this.textColor);
			    	
					/*if (f != enabled && Wedge2Shown == true) {
				    	mPaint.setAlpha(disabledAlpha);
					} else {
						mPaint.setAlpha(textAlpha);
					}*/
			    	mPaint.setAlpha(textAlpha);
					mPaint.setStyle(Paint.Style.FILL);
					mPaint.setTextSize(textSize);
					
					//This will look for a "new line" and split into multiple lines
					String menuItemName = mMenuEntries.get(i).getLabel();
					String[] stringArray = menuItemName.split("\n");
					
					
					//gets total height
					Rect rect = new Rect();
					float textHeight = 0;
					for (int j = 0; j < stringArray.length; j++) {
						mPaint.getTextBounds(stringArray[j],0,stringArray[j].length(),rect);
						textHeight = textHeight+(rect.height()+3);
				    }
					
					
					
					float textBottom = rf.centerY()-(textHeight/2);
					for (int j = 0; j < stringArray.length; j++) {
						mPaint.getTextBounds(stringArray[j],0,stringArray[j].length(),rect);
						float textLeft = rf.centerX() - rect.width()/2;
						textBottom = textBottom + (rect.height()+3);
						canvas.drawText(stringArray[j], textLeft-rect.left, textBottom-rect.bottom, mPaint);
						
				    }
					
					
					//canvas.drawTextOnPath(text, path, hOffset, vOffset, paint)
					//canvas.rotate((float)mRotationAngle, xPosition, yPosition );
					//canvas.drawRect(rf, mPaint);
					
			}
    	}
    	
    	//canvas.restore();
    	
    	canvas.restore();
    	
    	
    	//System.out.println()
    	canvas.save();
    	//canvas.scale(mViewRect.width() / mBackgroundBitmap.getWidth(), (mViewRect.height() / mBackgroundBitmap.getHeight()) * 2, mViewRect.left, mViewRect.top);
    	//canvas.drawBitmap(mBackgroundBitmap, mViewRect.left, mViewRect.top , mPaint);
    	canvas.restore();

    	
    	
    	//mPaint.setShader( new LinearGradient(0, 0, 0, getHeight(),
				//new int[] { 0xff514E4D, 0xff98908F, 0x00908988}, null,  Shader.TileMode.MIRROR));
    	
    	mPaint.setShader( mShader );  
    	//mPaint.setAlpha(0x66);
    	//Draw the Selection Segment
    	if(mSelectionWedge != null) {
    		canvas.drawPath(mSelectionWedge, mPaint);
    		//canvas.drawRect(mSelectionWedge.getWedgeRegion().getBounds(), mPaint);
    	}
    	
    	mPaint.setShader(null);
    	
    	checkSelection(canvas);
    }
    
	
	private void checkSelection(Canvas canvas) {
		Matrix cmt = new Matrix();
		cmt.postRotate((float)mRotationAngle, xPosition, yPosition);
		
		RectF f = new RectF();
		mSelectionWedgeRect.set( mSelectionWedge.getWedgeRegion().getBounds() );
		for(int i = 0; i < mWedges.length; i++) {
			//mWedgeRect.set( mWedges[i].getWedgeRegion().getBounds() );
			mWedges[i].computeBounds(mWedgeRect, true);
			cmt.mapRect(mWedgeRect);
			//canvas.drawRect(mWedgeRect, mPaint);
			//canvas.drawRect(mSelectionWedgeRect, mPaint);
			if( mWedgeRect.contains(mSelectionWedgeRect) ) {
				if(mListener != null) {
					mListener.onMenuEntryChanged(mMenuEntries.get(i));
				}
			}
		}
	}
	
	public void setRotaryWheelViewListener(RotaryWheelViewListener listener) {
		mListener = listener;
	}
	
	
	/*protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int wmode = MeasureSpec.getMode(widthMeasureSpec);
		int hmode = MeasureSpec.getMode(heightMeasureSpec);
		int wsize = MeasureSpec.getSize(widthMeasureSpec);
		int hsize = MeasureSpec.getSize(heightMeasureSpec);

		int width = (int)mViewRect.width();
		int height = (int) mViewRect.height();

		if (wmode == MeasureSpec.EXACTLY) {
			width = wsize;
		}
		if (hmode == MeasureSpec.EXACTLY) {
			height = hsize;
		}
		
		//this.xPosition = width / 2;
		//this.yPosition = height / 2;
		
		this.setMeasuredDimension(width, height);
		
		invalidate();

	}*/
	
    @Override
    public boolean onTouchEvent(MotionEvent e) {
    	int state = e.getAction();
		int eventX = (int) e.getX();
		int eventY = (int) e.getY();
		
		
		switch(state) {
		case MotionEvent.ACTION_DOWN:
			// reset the touched quadrants
	        for (int i = 0; i < mQuadrantTouched.length; i++) {
	        	mQuadrantTouched[i] = false;
	        }
			
			mStartAngle = getAngle(eventX, eventY);
			
			mAllowRotating = false;
			break;
		case MotionEvent.ACTION_MOVE:
			double currentAngle = getAngle(eventX, eventY);
			mRotationAngle += mStartAngle - currentAngle;
			mStartAngle = currentAngle;
			break;
		case MotionEvent.ACTION_UP:
			mAllowRotating = true;
			break;
		}
		
		mQuadrantTouched[getQuadrant(e.getX() - (mViewRect.width() / 2), mViewRect.height() - e.getY() - (mViewRect.height() / 2))] = true;
		mDetector.onTouchEvent(e);
		
		invalidate();
    	return true;
    }
    
    private class Wedge extends Path {
    	private int x, y;
    	private int InnerSize, OuterSize;
    	private float StartArc;
    	private float ArcWidth;
    	private Region mWedgeRegion;
    	
    	private Wedge(int x, int y, int InnerSize, int OuterSize, float StartArc, float ArcWidth) {
    		super();
    		
    		if (StartArc >= 360) {
    			StartArc = StartArc-360;
    		}
    		
    		mWedgeRegion = new Region();
    		this.x = x; this.y = y;
    		this.InnerSize = InnerSize;
    		this.OuterSize = OuterSize;
    		this.StartArc = StartArc;
    		this.ArcWidth = ArcWidth;
    		this.buildPath();
    	}
    	
    	/**
    	 * 
    	 * @return the bottom rect that will be used for intersection 
    	 */
    	public Region getWedgeRegion() {
    		return mWedgeRegion;
    	}
    	
    	private void buildPath() {

    	    final RectF rect = new RectF();
    	    final RectF rect2 = new RectF();
    	    
    	    //Rectangles values
    	    rect.set(this.x-this.InnerSize, this.y-this.InnerSize, this.x+this.InnerSize, this.y+this.InnerSize);
    	    rect2.set(this.x-this.OuterSize, this.y-this.OuterSize, this.x+this.OuterSize, this.y+this.OuterSize);
    	   	
    	    
    		this.reset();
    		//this.moveTo(100, 100);
    		this.arcTo(rect2, StartArc, ArcWidth);
    		this.arcTo(rect, StartArc+ArcWidth, -ArcWidth);
    				
    		this.close();
    		
    		
    		mWedgeRegion.setPath( this, new Region(0,0,480,800) );

    	}
    }
    
    public boolean addMenuEntry(RotaryWheelMenuEntry menuEntry) {
    	mMenuEntries.add(menuEntry);
    	determineWedges();
    	
    	return true;
    }

   
    
    /**
     * @return The angle of the unit circle with the image view's center
     */
    private double getAngle(double xTouch, double yTouch) {
    	double x = xTouch - (mViewRect.width() / 2d);
        double y = mViewRect.height() - yTouch - (mViewRect.height() / 2d);
        switch (getQuadrant(x, y)) {
            case 1:
                return Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            case 2:
                return 180 - Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            case 3:
                return 180 + (-1 * Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
            case 4:
                return 360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            default:
                return 0;
        }
    }
    /**
     * @return The selected quadrant.
     */
    private static int getQuadrant(double x, double y) {
        if (x >= 0) {
            return y >= 0 ? 1 : 4;
        } else {
            return y >= 0 ? 2 : 3;
        }
    }
    
    private class MyGestureDetector extends SimpleOnGestureListener {
    	
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // get the quadrant of the start and the end of the fling
            int q1 = getQuadrant(e1.getX() - (mViewRect.width() / 2), mViewRect.height() - e1.getY() - (mViewRect.height() / 2));
            int q2 = getQuadrant(e2.getX() - (mViewRect.width() / 2), mViewRect.height() - e2.getY() - (mViewRect.height() / 2));
            // the inversed rotations
            if ((q1 == 2 && q2 == 2 && Math.abs(velocityX) < Math.abs(velocityY))
                    || (q1 == 3 && q2 == 3)
                    || (q1 == 1 && q2 == 3)
                    || (q1 == 4 && q2 == 4 && Math.abs(velocityX) > Math.abs(velocityY))
                    || ((q1 == 2 && q2 == 3) || (q1 == 3 && q2 == 2))
                    || ((q1 == 3 && q2 == 4) || (q1 == 4 && q2 == 3))
                    || (q1 == 2 && q2 == 4 && mQuadrantTouched[3])
                    || (q1 == 4 && q2 == 2 && mQuadrantTouched[3]) ) {
                RotaryWheelView.this.post(new FlingRunnable(-1 * (velocityX + velocityY)));
            } else {
            	
                // the normal rotation
                RotaryWheelView.this.post(new FlingRunnable(velocityX + velocityY));
            }
            return true;
        }
   }
   
	/**
	 * A {@link Runnable} for animating the the dialer's fling.
	 */
	private class FlingRunnable implements Runnable {
	    private float velocity;
	    public FlingRunnable(float velocity) {
	        this.velocity = velocity;
	    }
	    @Override
	    public void run() {
	        if (Math.abs(velocity) > 5 && mAllowRotating) {
	            mRotationAngle += velocity / 75;
	            invalidate();
	            velocity /= 1.0666F;
	            // post this instance again
	            RotaryWheelView.this.post(this);
	        }
	    }
	}

}