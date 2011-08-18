package ServeurJeu.ComposantesJeu.Joueurs;

import ServeurJeu.ComposantesJeu.Table;

public abstract class InformationPartie {
	
	// Déclaration d'une référence vers la table courante
	protected final Table objTable;
	
	// this is the code of the set of colors of the clothes in the player's picture
	// user can change it in the frame 3 of the client
	// each of 12 picture in this set has his color
	// so final color is combination of this code and the picture
	// selected by user
	protected int clothesColor;	
	
	protected Joueur ourPlayer;
	
	public InformationPartie(Table tableCourante, Joueur player){
		objTable = tableCourante;
		clothesColor = 0;		
		ourPlayer = player;
	}
	
	/**
	 * Cette fonction permet de retourner la référence vers la table courante
	 * du joueur.
	 *
	 * @return Table : La référence vers la table de cette partie
	 */
	public Table obtenirTable() {
		return objTable;
	}
	
	protected void setClothesColor(int colorCode) {
		this.clothesColor = colorCode;
	}

	public int getClothesColor() {
		return clothesColor;
	}
	
	public Integer resetColor() {
		int temp = this.clothesColor;
		this.clothesColor = 0;
		return temp;
	}

	public void setColorID() {
		this.clothesColor = objTable.getOneColor();
	}
	
	public static int getPointsByMove(int move)
	{
		int points = 0;
		switch (move){ 
		case 1:
		    points = 2;
		    break;
		case 2:
			points = 3;
			break;
		case 3:
			points = 5;
			break;
		case 4:
			points = 8;
			break;
		case 5:
			points = 13;
			break;
		case 6:
			points = 21;
			break;
		case 7:
			points = 34;
			break;
	   }
	   return points;
	}
	
	public abstract void setOnBanana();
	public abstract void setOffBanana();
	public abstract void setOffBrainiac();
	public abstract void setOnBrainiac();

}
