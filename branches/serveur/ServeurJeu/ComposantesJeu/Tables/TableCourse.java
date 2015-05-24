package ServeurJeu.ComposantesJeu.Tables;

import java.awt.Point;

import Enumerations.GameType;
import ServeurJeu.ComposantesJeu.Salle;
import ServeurJeu.ComposantesJeu.GenerateurPartie.GenerateurPartieCourse;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;

/**
 * 
 *
 */
public class TableCourse extends Table {

	public TableCourse(Salle salleParente, int noTable,
			JoueurHumain joueur, int tempsPartie, String name, int intNbLines,
			int intNbColumns, GameType gamesType) {
		super(salleParente, noTable, joueur, tempsPartie, name, intNbLines,
				intNbColumns, gamesType);
	}

	public void creation(int intNbLines, int intNbColumns) {
		objGestionnaireBD.chargerReglesTable(objRegles, gameType, objSalle.getRoomId());
		MAX_NB_PLAYERS = objRegles.getMaxNbPlayers();
		///System.out.println("We test Colors in the table  : " );

		this.setColors();
		this.setIdPersos();

		this.gameFactory = new GenerateurPartieCourse();

		gameFactory.setNbLines(intNbLines);
		gameFactory.setNbColumns(intNbColumns);

	}
	
	public void verifyStopCondition()
	{
		// if all the humains is on the finish line we stop the game
		if (isAllTheHumainsOnTheFinish())
		{
			arreterPartie(""); 
		}		
	}
	
	public int verifyFinishAndSetBonus(Point point)
	{
		if(checkPositionPointsFinish(point))
		{
			return obtenirTempsRestant();
		}
		return 0;		
	}
}
