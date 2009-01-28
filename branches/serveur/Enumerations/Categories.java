package Enumerations;

/**
 * Enumération Categories qui décrive les catégories de niveaux scolaire du jouer 
 * @author Lilian
 */
public enum Categories{

	 categorie_level_11(11),
	 categorie_level_12(12),
	 categorie_level_13(13),
	 categorie_level_14(14),
	 
	 categorie_level_21(21),
	 categorie_level_22(22),
	 categorie_level_23(23),
	 categorie_level_24(24),
	 
	 categorie_level_31(31),
	 categorie_level_32(32),
	 categorie_level_33(33),
	 categorie_level_34(34),
	 categorie_level_35(35),
	 categorie_level_36(36),
	 categorie_level_37(37),
	 categorie_level_38(38),
	 
	 categorie_level_41(41),
	 categorie_level_42(42),
	 categorie_level_43(43),
	 categorie_level_44(44),
	 categorie_level_45(45),
	 categorie_level_46(46),
	 categorie_level_47(47),
	 categorie_level_48(48),
	 categorie_level_49(49),
	 
	 categorie_level_51(51),
	 categorie_level_52(52),
	 categorie_level_53(53),
	 categorie_level_54(54),
	 
	 categorie_level_61(61),
	 categorie_level_62(62),
	 categorie_level_63(63),
	 categorie_level_64(64),
	 categorie_level_65(65);
	 
	 private int code;

     private Categories(int code) {
          this.code = code;
     }

     public int getCode() { return code; }

};



