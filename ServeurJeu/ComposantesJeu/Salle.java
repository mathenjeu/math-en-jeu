package ServeurJeu.ComposantesJeu;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import Enumerations.GameType;
import Enumerations.RetourFonctions.ResultatEntreeTable;
import ServeurJeu.ControleurJeu;
import ServeurJeu.ComposantesJeu.Joueurs.Joueur;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Tables.Table;
import ServeurJeu.ComposantesJeu.Tables.TableCourse;
import ServeurJeu.ComposantesJeu.Tables.TableTournament;
import ServeurJeu.Evenements.Evenement;
import ServeurJeu.Evenements.EvenementJoueurEntreSalle;
import ServeurJeu.Evenements.EvenementJoueurQuitteSalle;
import ServeurJeu.Evenements.EvenementNouvelleTable;
import ServeurJeu.Evenements.EvenementTableDetruite;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.Evenements.InformationDestination;

//TODO: Le mot de passe d'une salle ne doit pas être modifiée pendant le jeu,
//      sinon il va falloir ajouter des synchronisations à chaque fois qu'on
//      fait des validations avec le mot de passe de la salle.
//NOTE: Les noms de variable en anglais réfère aux noms de colonnes dans la BD
//      tandis que ceux en français réfère aux objets du ServerJeu.
/**
 * @author Jean-François Brind'Amour
 */
public class Salle
{
	// Déclaration d'une référence vers le gestionnaire d'événements

	private GestionnaireEvenements objGestionnaireEvenements;
	// Déclaration d'une référence vers le contrôleur de jeu
	private ControleurJeu objControleurJeu;
	// Contient le nom de la salle dans chaque langue. La map est construite
	// avec l'information contenu dans la table room_info.
	// BD:  [room_info.language_id --> room_info.name]
	private final Map<Integer, String> mapRoomLanguageIdToName;
	// Contient la description de la salle dans chaque langue. La map est
	// construite avec l'information contenu dans la table room_info.
	// BD:  [room_info.language_id --> room_info.name]
	private final Map<Integer, String> mapRoomLanguageIdToDescription;
	// Contient le nombre des pistes pour les types du jeu addmisible 
	// dans la salle. Pour les types ou est pas necessaires  = 0
	private final Map<Integer, Integer> mapRoomTypesIdToNbTracks;
	// Contient le mot de passe permettant d'accéder à la salle
	// BD:  [room.password]
	private String strPassword;
	// Contient le nom d'utilisateur du créateur de cette salle
	// BD:  [user.username] pour le user avec id [room.user_id]
	private final String strCreatorUsername;
	// Contient la date d'ouverture de la salle.
	// BD:  [room.beginDate]
	private Date dateBeginDate;
	// Contient la date de fermeture de la salle.
	// BD:  [room.endDate]
	private Date dateEndDate;
	// Contient le numéro d'indentification unique pour la salle dans la BD
	// BD:  [room.room_id]
	private final int intRoomId;
	// Contient le numbre de minute pour la durée des parties dans la salle
	// BD:  [room.masterTime]
	private int intMasterTime;
	// Contient la liste des "game types" admis dans la salle.
	// BD:  [room_game_types.game_type_id]
	private Set<Integer> setGameTypeIds;
	// Contient le type de la salle.
	// BD:  [user.role_id] pour le user avec user_id [room.user_id]
	//      De plus user.role_id == 3 est converti en String "profsType"
	//              user.role_id != 3 sont converti en String "General"
	private final String strRoomType;
	// Contient la liste des keyword_id valide pour la banque de questions
	// de la salle.  Seule les questions associées à au moins un keyword_id
	// peuvent être utilisées dans la salle.
	// BD:  [room_keywords.keyword_id]
	private Set<Integer> setKeywordIds;
	// Cet objet est une liste de numéros utilisés pour les tables (sert à
	// générer de nouvelles tables)
	private final HashSet<Integer> lstNoTables;
	// Cet objet est une liste des joueurs qui sont présentement dans cette salle
	private final HashMap<String, JoueurHumain> lstJoueurs;
	// Cet objet est une liste des tables qui sont présentement dans cette salle
	private final HashMap<Integer, Table> lstTables;
	// Un compteur pour le prochain numéro de table qui sera créé dans cette
	// salle.
	private int prochainNoTable;

	/**
	 * Une {@code Salle} est un endroit qui contient une ou plusieurs {@link Table}
	 * de jeu similaire.  La salle régit le types de questions posées aux
	 * tables ainsi que le temps et type de partie qui peuvent y être jouées.
	 * @param controleurJeu
	 * @param roomId un id unique qui réfère à la table
	 * @param password le mot de passe nécéssaire pour entrer dans la salle ("" signifie pas de mot de passe)
	 * @param username le nom d'utilisateur du prof qui crée la salle
	 * @param type le type de la salle (profsType ou general)
	 * @param beginDate la date à laquelle la salle devient disponible
	 * @param endDate la date à laquelle la salle ferme
	 * @param gameDuration la durée des partie dans la salle (0 signifie le joueur décide)
	 * @param names noms de la salle pour chaque langues dans lesquelles la salle est disponible
	 * @param descriptions descriptions de la salle pour chaque langues dans lesquelles la salle est disponible
	 * @param keywordIds les ids des keywords associés à la salle (détermine le genre de question qui y seront posées)
	 * @param gameTypeIds les ids des type de jeu permit dans la salle
	 */
	public Salle(ControleurJeu controleurJeu, 
			int roomId, String password, String username, String type,
			Date beginDate, Date endDate, int gameDuration,
			Map<Integer,String> names, Map<Integer,String> descriptions,
			Set<Integer> keywordIds, Set<Integer> gameTypeIds) {

		objGestionnaireEvenements = new GestionnaireEvenements();
		objControleurJeu = controleurJeu;

		intRoomId = roomId;
		strPassword = password;
		strCreatorUsername = username;
		dateBeginDate = beginDate;
		dateEndDate = endDate;
		intMasterTime = gameDuration;
		mapRoomLanguageIdToName = new TreeMap<Integer,String>();
		mapRoomLanguageIdToName.putAll(names);
		mapRoomLanguageIdToDescription = new TreeMap<Integer,String>();
		mapRoomLanguageIdToDescription.putAll(descriptions);
		mapRoomTypesIdToNbTracks = new TreeMap<Integer, Integer>();
		mapRoomTypesIdToNbTracks.putAll(controleurJeu.obtenirGestionnaireBD().getNbTracks(gameTypeIds));
		strRoomType = type;
		setKeywordIds = new TreeSet<Integer>();
		setKeywordIds.addAll(keywordIds);
		setGameTypeIds = new TreeSet<Integer>();
		setGameTypeIds.addAll(gameTypeIds);


		// Créer une nouvelle liste de joueurs, de tables et de numéros
		lstJoueurs = new HashMap<String, JoueurHumain>();
		lstTables = new HashMap<Integer, Table>();
		lstNoTables = new HashSet<Integer>();
		// Créer un thread pour le GestionnaireEvenements et le démarrer.
		(new Thread(objGestionnaireEvenements, "Salle")).start();
	}

	/**
	 * This method is used to wipe the lists of room and finally 
	 * destroy the room
	 */
	public void destroyRoom(){
		this.lstNoTables.clear();
		this.objControleurJeu = null;
		this.objGestionnaireEvenements.arreterGestionnaireEvenements();
		this.objGestionnaireEvenements = null;
	}// end method

	/**
	 * Cette fonction permet de générer un nouveau numéro de la table.
	 *
	 * @return int : Le numéro de la table généré
	 *
	 * @synchronism Cette fonction n'a pas besoin d'être synchronisée, car
	 * 				elle doit l'être par la fonction appelante. La
	 * 				synchronisation devrait se faire sur la liste des tables.
	 */
	private int genererNoTable() {
		// Déclaration d'une variable qui va contenir le numéro de table
		// généré
		int intNoTable = prochainNoTable + 1;

		// Boucler tant qu'on n'a pas trouvé de numéro n'étant pas utilisé
		while (lstNoTables.contains(intNoTable)) {
			intNoTable++;
		}

		setProchainNoTable(intNoTable);
		return intNoTable;
	}

	/**
	 * Cette fonction permet de valider que le mot de passe pour entrer dans la
	 * salle est correct. On suppose suppose que le joueur n'est pas dans la
	 * salle courante. Cette fonction va avoir pour effet de connecter le joueur
	 * dans la salle courante.
	 *
	 * @param joueur : Le joueur demandant d'entrer dans la salle
	 * @param motDePasse : Le mot de passe pour entrer dans la salle
	 * @param doitGenererNoCommandeRetour : Permet de savoir si on doit
	 * 					générer un numéro de commande pour le retour de
	 * 					l'appel de fonction
	 * @return false : Le mot de passe pour entrer dans la salle n'est pas
	 * 		   le bon
	 * 	   true  : Le joueur a réussi à entrer dans la salle
	 *
	 * @synchronism Cette fonction est synchronisée pour éviter que deux
	 * 		puissent entrer ou quitter une salle en même temps.
	 * 		On n'a pas à s'inquièter que le joueur soit modifié
	 * 		pendant le temps qu'on exécute cette fonction. De plus
	 * 		on n'a pas à revérifier que la salle existe bien (car
	 * 		elle ne peut être supprimée) et que le joueur n'est
	 * 		pas toujours dans une autre salle (car le protocole
	 * 		ne peut pas exécuter plusieurs fonctions en même temps)
	 */
	public boolean entrerSalle(JoueurHumain joueur, String motDePasse, boolean doitGenererNoCommandeRetour) {
		// Si le mot de passe est le bon, alors on ajoute le joueur dans la liste
		// des joueurs de cette salle et on envoit un événement aux autres
		// joueurs de cette salle pour leur dire qu'il y a un nouveau joueur
		if (strPassword.equals(objControleurJeu.obtenirGestionnaireBD().controlPWD(motDePasse))) {
			// Empêcher d'autres thread de toucher à la liste des joueurs de
			// cette salle pendant l'ajout du nouveau joueur dans cette salle
			synchronized (lstJoueurs) {
				// Ajouter ce nouveau joueur dans la liste des joueurs de cette salle
				lstJoueurs.put(joueur.obtenirNom(), joueur);


				// Le joueur est maintenant entré dans la salle courante
				joueur.definirSalleCourante(this);
			}
			// Si on doit générer le numéro de commande de retour, alors
			// on le génére, sinon on ne fait rien (ça devrait toujours
			// être vrai, donc on le génére tout le temps)
			if (doitGenererNoCommandeRetour == true) {
				// Générer un nouveau numéro de commande qui sera
				// retourné au client
				joueur.obtenirProtocoleJoueur().genererNumeroReponse();
			}

			// Préparer l'événement de nouveau joueur dans la salle.
			// Cette fonction va passer les joueurs et créer un
			// InformationDestination pour chacun et ajouter l'événement
			// dans la file de gestion d'événements
			preparerEvenementJoueurEntreSalle(joueur);
			return true;
		}
		return false;
	}

	/**
	 * Cette méthode permet au joueur passé en paramètres de quitter la salle.
	 * On suppose que le joueur est dans la salle et qu'il n'est pas en train
	 * de jouer dans aucune table.
	 *
	 * @param joueur Le joueur demandant de quitter la salle
	 * @param doitGenererNoCommandeRetour Permet de savoir si on doit générer un
	 *        numéro de commande pour le retour de l'appel de fonction
	 * @param detruirePartieCourante Permet de savoir si on doi détruire la salle
	 *        après l'avoir quittée.
	 * @synchronism Cette fonction est synchronisée pour éviter que deux
	 *              puissent entrer ou quitter une salle en même temps.
	 *              On n'a pas à s'inquièter que le joueur soit modifié
	 *              pendant le temps qu'on exécute cette fonction. De plus
	 *              on n'a pas à revérifier que la salle existe bien (car
	 *              elle ne peut être supprimée) et que le joueur n'est
	 *              pas toujours dans une autre salle (car le protocole
	 *              ne peut pas exécuter plusieurs fonctions en même temps)
	 */
	public void quitterSalle(JoueurHumain joueur, boolean doitGenererNoCommandeRetour, boolean detruirePartieCourante) {
		//TODO: Peut-être va-t-il falloir ajouter une synchronisation ici
		// 	lorsque la commande sortir joueur de la table sera codée
		// Si le joueur est en train de jouer dans une table, alors
		// il doit quitter cette table avant de quitter la salle
		if (joueur.obtenirPartieCourante() != null) {
			// Quitter la table courante avant de quitter la salle
			joueur.obtenirPartieCourante().obtenirTable().quitterTable(joueur, false, detruirePartieCourante);
		}

		// Empêcher d'autres thread de toucher à la liste des joueurs de
		// cette salle pendant que le joueur quitte cette salle
		synchronized (lstJoueurs) {
			// Enlever le joueur de la liste des joueurs de cette salle
			lstJoueurs.remove(joueur.obtenirNom());

			// Le joueur est maintenant dans aucune salle
			joueur.definirSalleCourante(null);
		}
		// Si on doit générer le numéro de commande de retour, alors
		// on le génère, sinon on ne fait rien (ça se peut que ce soit
		// faux)
		if (doitGenererNoCommandeRetour == true) {
			// Générer un nouveau numéro de commande qui sera
			// retourné au client
			joueur.obtenirProtocoleJoueur().genererNumeroReponse();
		}

		// Préparer l'événement qu'un joueur a quitté la salle.
		// Cette fonction va passer les joueurs et créer un
		// InformationDestination pour chacun et ajouter l'événement
		// dans la file de gestion d'événements
		preparerEvenementJoueurQuitteSalle(joueur);

	}

	/**
	 * Cette méthode permet de créer une nouvelle table et d'y faire entrer le
	 * joueur qui en fait la demande. On suppose que le joueur n'est pas dans
	 * aucune autre table.
	 * @param joueur : Le joueur demandant de créer la table
	 * @param name
	 * @param tempsPartie : Le temps que doit durer la partie
	 * @param doitGenererNoCommandeRetour : Permet de savoir si on doit
	 * 					générer un numéro de commande pour le retour de
	 * 					l'appel de fonction
	 * @param intNbLines
	 * @param intNbColumns
	 * @param gameType
	 * @return int : Le numéro de la nouvelle table créée
	 *
	 * @synchronism Cette fonction est synchronisée pour la liste des tables
	 * 		car on va ajouter une nouvelle table et il ne faut pas
	 * 		qu'on puisse détruire une table ou obtenir la liste des
	 * 		tables pendant ce temps. On synchronise également la
	 * 		liste des joueurs de la salle, car on va passer les
	 * 		joueurs de la salle et leur envoyer un événement. La
	 * 		fonction entrerTable est synchronisée automatiquement.
	 */
	public int creerTable(JoueurHumain joueur, int tempsPartie, boolean doitGenererNoCommandeRetour, String name, int intNbLines, int intNbColumns, GameType gameType) {
		Table objTable;
		// Empêcher d'autres thread de toucher à la liste des tables de
		// cette salle pendant la création de la table
		synchronized (lstTables) {
			//System.out.println("Salle - cree table : " + intNbLines + " " + intNbColumns);
			// Créer une nouvelle table en passant les paramètres appropriés
			if(gameType.equals(GameType.TOURNAMENT))
				objTable = new TableTournament(this, genererNoTable(), joueur, tempsPartie, name, intNbLines, intNbColumns, gameType);
			else if(gameType.equals(GameType.COURSE))
				objTable = new TableCourse(this, genererNoTable(), joueur, tempsPartie, name, intNbLines, intNbColumns, gameType);
			else
				objTable = new Table(this, genererNoTable(), joueur, tempsPartie, name, intNbLines, intNbColumns, gameType);



			// Ajouter la table dans la liste des tables
			lstTables.put(new Integer(objTable.obtenirNoTable()), objTable);

			// Ajouter le numéro de la table dans la liste des numéros de table
			lstNoTables.add(new Integer(objTable.obtenirNoTable()));

			//adjust server info
			objControleurJeu.obtenirGestionnaireCommunication().miseAJourInfo();

			// Si on doit générer le numéro de commande de retour, alors
			// on le génère, sinon on ne fait rien (ça devrait toujours
			// être vrai, donc on le génère tout le temps)
			if (doitGenererNoCommandeRetour == true) {
				// Générer un nouveau numéro de commande qui sera
				// retourné au client
				joueur.obtenirProtocoleJoueur().genererNumeroReponse();
			}
			// Préparer l'événement de nouvelle table.
			// Cette fonction va passer les joueurs et créer un
			// InformationDestination pour chacun et ajouter l'événement
			// dans la file de gestion d'événements
			preparerEvenementNouvelleTable(objTable, joueur);


			// Entrer dans la table on ne fait rien avec la liste des
			// personnages
			objTable.entrerTable(joueur, false);

		}

		return objTable.obtenirNoTable();
	}

	/**
	 * Cette fonction permet au joueur d'entrer dans la table désirée. On
	 * suppose que le joueur n'est pas dans aucune table.
	 *
	 * @param joueur : Le joueur demandant d'entrer dans la table
	 * @param noTable : Le numéro de la table dans laquelle entrer
	 * @param doitGenererNoCommandeRetour : Permet de savoir si on doit
	 * 					générer un numéro de commande pour le retour de
	 * 					l'appel de fonction
	 * @return Succes : Le joueur est maintenant dans la table
	 *         TableNonExistante : Le joueur a tenté d'entrer dans une
	 *                             table non existante
	 *         TableComplete : Le joueur a tenté d'entrer dans une
	 *                         table ayant déjà le maximum de joueurs
	 *         PartieEnCours : Une partie est déjà en cours dans la
	 *                         table désirée
	 *
	 * @synchronism Cette fonction est synchronisée sur la liste des tables
	 *		pour éviter qu'un joueur puisse commencer à quitter et
	 *		que le joueur courant débute son entrée dans la table
	 *		courante qui a des chances d'être détruite si le joueur
	 *		qui veut quitter est le dernier de la table.
	 */
	public ResultatEntreeTable entrerTable(JoueurHumain joueur, int noTable, boolean doitGenererNoCommandeRetour) {
		// Déclaration d'une variable qui va contenir le résultat à retourner
		// à la fonction appelante, soit les valeurs de l'énumération
		// ResultatEntreeTable
		ResultatEntreeTable strResultatEntreeTable;

		// Empêcher d'autres thread de toucher à la liste des tables de
		// cette salle pendant que le joueur entre dans la table
		synchronized (lstTables) {
			// Si la table n'existe pas dans la salle où se trouve le joueur,
			// alors il y a une erreur
			if (lstTables.containsKey(noTable) == false) {
				// La table n'existe pas
				strResultatEntreeTable = ResultatEntreeTable.TableNonExistante;
			} // Si la table est complète, alors il y a une erreur (aucune
			// synchronisation supplémentaire à faire car elle ne peut devenir
			// complète ou ne plus l'être que par l'entrée ou la sortie d'un
			// joueur dans la table. Or ces actions sont synchronisées avec
			// lstTables, donc ça va.
			else if (lstTables.get(noTable).estComplete() == true) {
				// La table est complète
				strResultatEntreeTable = ResultatEntreeTable.TableComplete;
			} //TODO: Cette validation dépend de l'état de la partie (de la table)
			// 		et lorsque cette partie se terminera ou débutera, son état va changer,
			//		il va donc falloir revoir cette validation
			// Si la table n'est pas complète et une partie est en cours,
			// alors il y a une erreur
			else if (lstTables.get(noTable).estCommencee() == true) {
				// Une partie est en cours
				strResultatEntreeTable = ResultatEntreeTable.PartieEnCours;

			} else {
				// Appeler la méthode permettant d'entrer dans la table
				lstTables.get(noTable).entrerTableAutres(joueur, doitGenererNoCommandeRetour);
				// Il n'y a eu aucun problème pour entrer dans la table
				strResultatEntreeTable = ResultatEntreeTable.Succes;
			}
		}

		return strResultatEntreeTable;
	}

	/**
	 * Cette méthode permet de détruire la table passée en paramètres.
	 * On suppose que la table n'a plus aucuns joueurs.
	 *
	 * @param tableADetruire : La table à détruire
	 *
	 * @synchronism Cette fonction n'est pas synchronisée car elle l'est par
	 * 				la fonction qui l'appelle. On synchronise seulement
	 * 				la liste des joueurs de cette salle lorsque va venir
	 * 				le temps d'envoyer l'événement que la table est détruite
	 * 				aux joueurs de la salle. On n'a pas à s'inquiéter que la
	 * 				table soit modifiée pendant le temps qu'on exécute cette
	 * 				fonction, car il n'y a plus personne dans la table.
	 */
	public void detruireTable(Table tableADetruire) {
		Table t = (Table)lstTables.get(new Integer(tableADetruire.obtenirNoTable()));
		if (t != null) {
			t.destruction();
		}        

		// Enlever la table de la liste des tables de cette salle
		lstTables.remove(new Integer(tableADetruire.obtenirNoTable()));

		// On enlève le numéro de la table dans la liste des numéros de table
		// pour le rendre disponible pour une autre table
		lstNoTables.remove(new Integer(tableADetruire.obtenirNoTable()));

		//adjust server info
		objControleurJeu.obtenirGestionnaireCommunication().miseAJourInfo();


		// Préparer l'événement qu'une table a été détruite.
		// Cette fonction va passer les joueurs et créer un
		// InformationDestination pour chacun et ajouter l'événement
		// dans la file de gestion d'événements
		// System.out.println(" RTest - table " + tableADetruire.obtenirNoTable());
		preparerEvenementTableDetruite(tableADetruire.obtenirNoTable());

	}

	/**
	 * Cette fonction permet d'obtenir la liste des joueurs se trouvant dans la
	 * salle courante. La vraie liste de joueurs est retournée.
	 *
	 * @return La liste des joueurs se trouvant dans la salle courante
	 *
	 * @synchronism Cette fonction n'est pas synchronisée ici, mais elle doit
	 * 				l'être par l'appelant de cette fonction tout dépendant
	 * 				du traitement qu'elle doit faire
	 */
	public HashMap<String, JoueurHumain> obtenirListeJoueurs() {
		return lstJoueurs;
	}

	/**
	 * Cette fonction permet d'obtenir la liste des tables se trouvant dans la
	 * salle courante. La vraie liste est retournée.
	 *
	 * @return La liste des tables de la salle courante
	 *
	 * @synchronism Cette fonction n'est pas synchronisée ici, mais elle doit
	 * 				l'être par l'appelant de cette fonction tout dépendant
	 * 				du traitement qu'elle doit faire
	 */
	public HashMap<Integer, Table> obtenirListeTables() {
		return lstTables;
	}

	/**
	 * Cette méthode permet de préparer l'événement de l'entrée d'un joueur
	 * dans la salle courante. Cette méthode va passer tous les joueurs
	 * de cette salle et pour ceux devant être avertis (tous sauf le joueur
	 * courant passé en paramètre), on va obtenir un numéro de commande, on
	 * va créer un InformationDestination et on va ajouter l'événement dans
	 * la file d'événements du gestionnaire d'événements. Lors de l'appel
	 * de cette fonction, la liste des joueurs est synchronisée.
	 *
	 * @param joueur : Le nom d'utilisateur du joueur qui
	 *                                vient d'entrer dans la salle
	 *
	 * @synchronism Cette fonction n'est pas synchronisée ici, mais elle l'est
	 * 				par l'appelant (entrerSalle).
	 */
	private void preparerEvenementJoueurEntreSalle(JoueurHumain joueur) {
		// Créer un nouvel événement qui va permettre d'envoyer l'événement
		// aux joueurs qu'un joueur est entré dans la salle
		EvenementJoueurEntreSalle joueurEntreSalle = new EvenementJoueurEntreSalle(joueur.obtenirNom());
		broadcastEvent(joueurEntreSalle, joueur);
	}

	/**
	 * Cette méthode permet de préparer l'événement du depart d'un joueur
	 * de la salle courante. Cette méthode va passer tous les joueurs
	 * de cette salle et pour ceux devant être avertis (tous sauf le joueur
	 * courant passé en paramètre), on va obtenir un numéro de commande, on
	 * va créer un InformationDestination et on va ajouter l'événement dans
	 * la file d'événements du gestionnaire d'événements. Lors de l'appel
	 * de cette fonction, la liste des joueurs est synchronisée.
	 *
	 * @param joueur : Le nom d'utilisateur du joueur qui
	 * 			  vient de quitter la salle
	 *
	 * @synchronism Cette fonction n'est pas synchronisée ici, mais elle l'est
	 * 		par l'appelant (quitterSalle).
	 */
	private void preparerEvenementJoueurQuitteSalle(JoueurHumain joueur) {
		// Créer un nouvel événement qui va permettre d'envoyer l'événement
		// aux joueurs qu'un joueur a quitté la salle
		EvenementJoueurQuitteSalle joueurQuitteSalle = new EvenementJoueurQuitteSalle(joueur.obtenirNom());
		broadcastEvent(joueurQuitteSalle, joueur);	 
	}

	/**
	 * Cette méthode permet de préparer l'événement de la création d'une
	 * nouvelle table dans la salle courante. Cette méthode va passer tous
	 * les joueurs de cette salle et pour ceux devant être avertis (tous
	 * sauf le joueur courant passé en paramètre), on va obtenir un numéro
	 * de commande, on va créer un InformationDestination et on va ajouter
	 * l'événement dans la file d'événements du gestionnaire d'événements.
	 * Lors de l'appel de cette fonction, la liste des joueurs est
	 * synchronisée.
	 *
	 * @param noTable : Le numéro de la table créé
	 * @param tempsPartie : Le temps de la partie
	 * @param joueur : Le nom d'utilisateur du joueur qui
	 * 			  a créé la table
	 *
	 * @synchronism Cette fonction n'est pas synchronisée ici, mais elle l'est
	 * 		par l'appelant (creerTable).
	 */
	private void preparerEvenementNouvelleTable(Table objTable, JoueurHumain joueur) {
		// Créer un nouvel événement qui va permettre d'envoyer l'événement
		// aux joueurs qu'une table a été créée
		EvenementNouvelleTable nouvelleTable = new EvenementNouvelleTable(objTable);
		broadcastEvent(nouvelleTable, joueur);
	}

	/**
	 * Cette méthode permet de préparer l'événement de la destruction d'une
	 * table dans la salle courante. Cette méthode va passer tous les joueurs
	 * de cette salle et pour ceux devant être avertis (tous sauf le joueur
	 * courant passé en paramètre), on va obtenir un numéro de commande, on
	 * va créer un InformationDestination et on va ajouter l'événement dans
	 * la file d'événements du gestionnaire d'événements. Lors de l'appel de
	 * cette fonction, la liste des joueurs est synchronisée.
	 *
	 * @param noTable : Le numéro de la table détruite
	 *
	 * @synchronism Cette fonction n'est pas synchronisée ici, mais elle l'est
	 * 		par l'appelant (detruireTable).
	 */
	private void preparerEvenementTableDetruite(int noTable) {
		// Créer un nouvel événement qui va permettre d'envoyer l'événement
		// aux joueurs qu'une table a été créée
		EvenementTableDetruite tableDetruite = new EvenementTableDetruite(noTable);
		broadcastEvent(tableDetruite);
	}   

	/**
	 * Cette fonction permet de déterminer si la salle possède un mot de passe
	 * pour y accéder ou non.
	 *
	 * @return boolean : true si la salle est protégée par un mot de passe
	 *                   false sinon
	 */
	public boolean protegeeParMotDePasse() {
		return !(strPassword == null || strPassword.trim().equals(""));
	}

	/**
	 * Retourne le nom de la salle dans la langue spécifié.
	 * Les langues reconnues sont: "fr", "en"
	 * Si la langue spécifiée est {@code null} ou vide on retourne le
	 * nom français suivi d'un / suivi du nom anglais.
	 * Si la langue spécifiée est non reconnu on retourne le nom anglais.
	 * Si le nom de la salle n'a pas été définie dans la langue demandée
	 * on retourne un nom vide: ""
	 * @param lang la langue voulu pour la valeur de retour ("fr","en","")
	 * @return Le nom de la salle dans la (ou les) langue voulue.
	 */
	public String getRoomName(String lang) {
		if (lang == null || lang.trim().length() == 0) {
			String frName = mapRoomLanguageIdToName.get(1);
			String enName = mapRoomLanguageIdToName.get(2);
			if (frName == null) {
				frName = "";
			}
			if (enName == null) {
				enName = "";
			}
			return frName + "/" + enName;
		}
		if (lang.equalsIgnoreCase("fr")) {
			String frName = mapRoomLanguageIdToName.get(1);
			if (frName == null) {
				return "";
			}
			return frName;
		}
		String enName = mapRoomLanguageIdToName.get(2);
		if (enName == null) {
			return "";
		}
		return enName;
	}

	/**
	 * Retourne la description de la salle dans la langue spécifié.
	 * Les langues reconnues sont: "fr", "en"
	 * Si la langue spécifiée est {@code null} ou vide on retourne la
	 * description française suivi d'un / suivi de la description anglaise.
	 * Si la langue spécifiée est non reconnu on retourne la description anglaise.
	 * Si la description de la salle n'a pas été définie dans la langue demandée
	 * on retourne une description vide: ""
	 * @param lang la langue voulu pour la valeur de retour ("fr","en","")
	 * @return La description de la salle dans la (ou les) langue voulue.
	 */
	public String getRoomDescription(String lang) {
		if (lang == null || lang.trim().length() == 0) {
			String frDesc = mapRoomLanguageIdToDescription.get(1);
			String enDesc = mapRoomLanguageIdToDescription.get(2);
			if (frDesc == null) {
				frDesc = "";
			}
			if (enDesc == null) {
				enDesc = "";
			}
			return frDesc + "/" + enDesc;
		}
		if (lang.equalsIgnoreCase("fr")) {
			String frDesc = mapRoomLanguageIdToDescription.get(1);
			if (frDesc == null) {
				return "";
			}
			return frDesc;
		}
		String enDesc = mapRoomLanguageIdToDescription.get(2);
		if (enDesc == null) {
			return "";
		}
		return enDesc;
	}

	private void setProchainNoTable(int prochainNoTable) {
		this.prochainNoTable = prochainNoTable;
		if (this.prochainNoTable > 999) {
			this.prochainNoTable = 0;
		}
	}

	public ControleurJeu getObjControleurJeu() {
		return objControleurJeu;
	}

	public Table obtenirTable(int intNoTable) {

		return (Table)lstTables.get(new Integer(intNoTable));
	}

	public String getCreatorUsername() {
		return strCreatorUsername;
	}

	public int getRoomId() {
		return intRoomId;
	}
	public Map<Integer,String> getNameMap() {
		return mapRoomLanguageIdToName;
	}
	public Map<Integer,String> getDescriptionMap() {
		return mapRoomLanguageIdToDescription;
	}
	public Map<Integer, Integer> getRoomTypesIdToNbTracks()
	{
		return mapRoomTypesIdToNbTracks;
	}
	public String getName(String language) {
		if (language.equals("fr"))
			return mapRoomLanguageIdToName.get(1);
		else if (language.equals("en"))
			return mapRoomLanguageIdToName.get(2);
		return null;
	}
	public String getDescription(String language) {
		if (language.equals("fr"))
			return mapRoomLanguageIdToDescription.get(1);
		else if (language.equals("en"))
			return mapRoomLanguageIdToDescription.get(2);
		return null;
	}

	public int getMasterTime() {
		return intMasterTime;
	}

	public Set<Integer> getKeywordIds() {
		return setKeywordIds;
	}

	public Set<Integer> getGameTypeIds() {
		return setGameTypeIds;
	}

	public String getPassword() {
		return strPassword;
	}

	public String getRoomType() {
		return strRoomType;
	}

	public Date getBeginDate() {
		return dateBeginDate;
	}

	public Date getEndDate() {
		return dateEndDate;
	}

	public GestionnaireEvenements getObjGestionnaireEvenements() {
		return objGestionnaireEvenements;
	}

	public void setObjGestionnaireEvenements(
			GestionnaireEvenements objGestionnaireEvenements) {
		this.objGestionnaireEvenements = objGestionnaireEvenements;
	}

	public int getActiveTablesNUmber() {
		return lstTables.size();
	}

	/**
	 * Sent event to players exept players that is motive for the event
	 * @param evenement
	 * @param eventHero
	 */
	public void broadcastEvent(Evenement evenement, Joueur eventHero) {
		// Empêcher d'autres thread de toucher à la liste des joueurs de
		// cette salle pendant qu'on parcourt tous les joueurs de la salle
		// pour leur envoyer un événement
		synchronized (lstJoueurs) {

			for (JoueurHumain objJoueur: obtenirListeJoueurs().values()) {

				if (!objJoueur.obtenirNom().equals(eventHero.obtenirNom())) {
					// Obtenir un numéro de commande pour le joueur courant, créer
					// un InformationDestination et l'ajouter à l'événement
					evenement.ajouterInformationDestination( new InformationDestination(
							objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
							objJoueur.obtenirProtocoleJoueur()));
				}
			}

			// Ajouter le nouvel événement créé dans la liste d'événements à traiter
			objGestionnaireEvenements.ajouterEvenement(evenement);
		}

	}

	/**
	 * Send an event to all players in the room liste
	 * @param event
	 */
	private void broadcastEvent(Evenement event) {
		// Empêcher d'autres thread de toucher à la liste des joueurs de
		// cette salle pendant qu'on parcourt tous les joueurs de la salle
		// pour leur envoyer un événement
		synchronized (lstJoueurs) {
			for (JoueurHumain objJoueur: obtenirListeJoueurs().values()) {

				// Obtenir un numéro de commande pour le joueur courant, créer
				// un InformationDestination et l'ajouter à l'événement
				event.ajouterInformationDestination( new InformationDestination(
						objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
						objJoueur.obtenirProtocoleJoueur()));
			}

			// Ajouter le nouvel événement créé dans la liste d'événements à traiter
			objGestionnaireEvenements.ajouterEvenement(event);

		}
	}
	
	public void setStrPassword(String strPassword) {
		this.strPassword = strPassword;
	}

	public void setDateEndDate(Date dateEndDate) {
		this.dateEndDate = dateEndDate;
	}
	
	public void setDateBeginDate(Date dateBeginDate) {
		this.dateBeginDate = dateBeginDate;
	}

	public void setIntMasterTime(int intMasterTime) {
		this.intMasterTime = intMasterTime;
	}

	public Set<Integer> getSetGameTypeIds() {
		return setGameTypeIds;
	}

	public void setSetGameTypeIds(Set<Integer> setGameTypeIds) {
		this.setGameTypeIds = setGameTypeIds;
	}

	public Set<Integer> getSetKeywordIds() {
		return setKeywordIds;
	}

	public void setSetKeywordIds(Set<Integer> setKeywordIds) {
		this.setKeywordIds = setKeywordIds;
	}

}// end class 

