package Enumerations;

/**
 * Enumération Categories qui décrive les catégories de niveaux scolaire du jouer 
 * @author Lilian
 */
public enum Categories{

	 categorie_level_0(0),
	 categorie_level_10(10),
	 categorie_level_11(11),
	 categorie_level_12(12),
	 categorie_level_13(13),
	 categorie_level_14(14),
	 
	 categorie_level_20(20),
	 categorie_level_21(21),
	 categorie_level_22(22),
	 categorie_level_23(23),
	 categorie_level_24(24),
	 categorie_level_25(25),
	 categorie_level_26(26),
	 
	 categorie_level_31(31),
	 
	 categorie_level_40(40),
	 categorie_level_41(41),
	 categorie_level_42(42),
	 categorie_level_43(43),
	 categorie_level_44(44),
	 categorie_level_45(45),
	 categorie_level_46(46),
	 categorie_level_47(47),
	 categorie_level_48(48),
	
	 categorie_level_50(50),
	 categorie_level_51(51),
	 categorie_level_52(52),
	 categorie_level_53(53),
	 categorie_level_54(54),
	 categorie_level_55(55),
	 categorie_level_56(56),
	 categorie_level_57(57),
	 categorie_level_58(58),
	 categorie_level_59(59),
	 
	 categorie_level_61(61),
	 categorie_level_62(62),
	 categorie_level_63(63),
	 categorie_level_64(64),
	 categorie_level_65(65),
	 categorie_level_66(66),
	 categorie_level_67(67),
	 
	 categorie_level_71(71),
	 categorie_level_72(72),
	 categorie_level_73(73),
	 categorie_level_74(74),
	 categorie_level_75(75),
	 
	 categorie_level_81(81),
	 categorie_level_82(82),
	 categorie_level_83(83),
	 categorie_level_84(84),
	 categorie_level_85(85),
	 categorie_level_86(86),
	 
	 categorie_level_91(91),
	 categorie_level_92(92),
	 categorie_level_93(93),
	 categorie_level_94(94),
	 categorie_level_95(95),
	 
	 categorie_level_510(510),
	 categorie_level_511(511),
	 categorie_level_512(512),
	 categorie_level_513(513),
	 categorie_level_514(514);
	 
	 
	 private int code;

     private Categories(int code) {
          this.code = code;
     }

     public int getCode() { return code; }

};



