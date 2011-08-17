package Enumerations;
/**
 *  @author Oloieri Lilian
 */
public enum GameType {
   MATHENJEU, TOURNAMENT, COURSE;
   public String toString() {
	   switch(this) {
	   case MATHENJEU:
		   return "mathEnJeu";
	   case TOURNAMENT:
		   return "Tournament";
	   case COURSE:
		   return "Course";
	   default: return "mathEnJeu"; 
	   }
   }
   
   /**
    * Return the int value of the index in DB
    * @return int
    */
   public int getIntValue(){
	   switch(this) {
	   case MATHENJEU:
		   return 1;
	   case TOURNAMENT:
		   return 2;
	   case COURSE:
		   return 3;
	   default: return 1; 
	   }
   }
   
   public static GameType get(String s) {
       for (GameType cmd: GameType.values()) {
           if (cmd.toString().equals(s)) {
               return cmd;
           }
       }
       return null;
   }
}
