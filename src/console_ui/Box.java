package console_ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.JLabel;
import main_console.IValues;

public class Box extends Rectangle {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2930160163094115692L;
	private int width;
	private int height;
	private IValues values; // Immutable and thread safe
	private Font bigFont;
	private Font smallFont;
	private Color goodBackgroundColor;
	private Color badBackgroundColor;

	public Box(int width, int height, Font bigFont, Font smallFont,
			IValues values, Color good, Color bad) {
		
		super(0, 0, width, height);
		this.width = width;
		this.height = height;
		this.bigFont = bigFont;
		this.smallFont = smallFont;
		this.values = values;
		this.goodBackgroundColor = good;
		this.badBackgroundColor = bad;
	}

	public Box(Font bigFont, Font smallFont, IValues values) {
		this.bigFont = bigFont;
		this.smallFont = smallFont;
		this.values = values;
	}
	
	public synchronized void changeCoordinates(int x, int y){
		this.x += x;
		this.y += y;
	}
	
	public synchronized int width(){
		return width;
	}
	
	public synchronized int height(){
		return height;
	}
	
	public synchronized IValues values(){
		return values;
	}
	
	public Font getPrimaryFont(){
		return bigFont;
	}
	
	public Font getSecondaryFont(){
		return smallFont;
	}
	
	public Color backgroundColor(){
		if(values.boolVal()){
			return goodBackgroundColor;
		}
		return badBackgroundColor;
	}

	
}
