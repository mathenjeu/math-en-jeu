package Enumerations;

/**
 * 
 * @author Oloieri Lilian
 * 
 * last change March 2010
 *
 */
public enum Colors {
	
	color1("0x1133AA"),
	color2("0x0033CC"),
	color3("0x3399FF"),
	color4("0x00FF00"),
	color5("0x33FF33"),
	color6("0x66FF00"),
	color7("0xFF0000"),
	color8("0xFF0066"),
	color9("0xFFFF00"),
	color10("0x990000"),
	color11("0x990066"),
	color12("0xFF6600"),
	color13("0xAA0066"),
	color14("0x0000FF"),
	color15("0x3300FF"),
	color16("0xFF6600"),
	color17("0x00CC33"),
	color18("0x00FF33");

	 private String code;

     private Colors(String code) {
          this.code = code;
     }

     public String getCode() { return code; }
}
