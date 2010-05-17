package Enumerations;

/**
 * 
 * @author Oloieri Lilian
 * 
 * last change March 2010
 *
 */
public enum Colors {
	
	color1("0x003366"),
	color2("0x006699"),
	color3("0x3399CC"),
	color4("0x336600"),
	color5("0x99CC00"),
	color6("0x99CC99"),
	color7("0xFF0000"),
	color8("0xFF0099"),
	color9("0xFFCC00"),
	color10("0x330000"),
	color11("0x990066"),
	color12("0xFF6600");

	 private String code;

     private Colors(String code) {
          this.code = code;
     }

     public String getCode() { return code; }
}
