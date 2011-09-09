package ServeurJeu.ComposantesJeu.Tables;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import Enumerations.GameType;
import Enumerations.RetourFonctions.ResultatDemarrerPartie;
import ServeurJeu.ComposantesJeu.Salle;
import ServeurJeu.ComposantesJeu.GenerateurPartie.GenerateurPartieTournament;
import ServeurJeu.ComposantesJeu.Joueurs.InformationPartieHumain;
import ServeurJeu.ComposantesJeu.Joueurs.Joueur;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.Joueurs.StatisticsPlayer;
import ServeurJeu.Evenements.Evenement;
import ServeurJeu.Evenements.InformationDestination;
import ServeurJeu.Temps.Minuterie;

/**
 * 
 * @author Oloieri Lilian
 *
 */
public class TableTournament extends Table {

	protected HashMap<String, JoueurHumain> lstDiffusion;
	public TableTournament(Salle salleParente, int noTable,
			JoueurHumain joueur, int tempsPartie, String name, int intNbLines,
			int intNbColumns, GameType gamesType) {
		super(salleParente, noTable, joueur, tempsPartie, name, intNbLines,
				intNbColumns, gamesType);

		lstDiffusion = new HashMap<String, JoueurHumain>();

	}

	public void creation(int intNbLines, int intNbColumns) {
		objGestionnaireBD.chargerReglesTable(objRegles, gameType, objSalle.getRoomId());
		MAX_NB_PLAYERS = objRegles.getMaxNbPlayers();
		///System.out.println("We test Colors in the table  : " );

		this.setColors();
		this.setIdPersos();

		this.gameFactory = new GenerateurPartieTournament();

		gameFactory.setNbLines(intNbLines);
		gameFactory.setNbColumns(intNbColumns);

	}






	/**
	 * Cette méthode permet au joueur passé en paramètres de démarrer la partie.
	 * On suppose que le joueur est dans la table.
	 * @param joueur Le joueur demandant de démarrer la partie
	 * @param idDessin Le numéro Id du personnage choisi par le joueur
	 * @param doitGenererNoCommandeRetour Permet de savoir si on doit générer un
	 *        numéro de commande pour le retour de l'appel de fonction
	 * Synchronisme Cette fonction est synchronisée sur la liste des joueurs
	 *              en attente, car il se peut qu'on ajouter ou retirer des
	 *              joueurs de la liste en attente en même temps. On n'a pas
	 *              à s'inquiéter que le même joueur soit mis dans la liste
	 *              des joueurs en attente par un autre thread.
	 * @return
	 */
	public ResultatDemarrerPartie demarrerPartie(JoueurHumain joueur, int idDessin, boolean doitGenererNoCommandeRetour) {
		// Cette variable va permettre de savoir si le joueur est maintenant
		// attente ou non
		ResultatDemarrerPartie resultatDemarrerPartie;

		// Empêcher d'autres thread de toucher à la liste des joueurs en attente
		// de cette table pendant que le joueur tente de démarrer la partie
		synchronized (lstJoueursEnAttente) {
			// Si une partie est en cours alors on va retourner PartieEnCours
			if (bolEstCommencee == true) {
				resultatDemarrerPartie = ResultatDemarrerPartie.PartieEnCours;
			} // Sinon si le joueur est déjà en attente, alors on va retourner
			// DejaEnAttente
			else if (lstJoueursEnAttente.containsKey(joueur.obtenirNom())) {
				resultatDemarrerPartie = ResultatDemarrerPartie.DejaEnAttente;
			} else {
				// La commande s'est effectuée avec succès
				resultatDemarrerPartie = ResultatDemarrerPartie.Succes;

				putInWaitingList(joueur, idDessin);

				// Si on doit générer le numéro de commande de retour, alors
				// on le génére, sinon on ne fait rien (ça se peut que ce soit
				// faux)
				if (doitGenererNoCommandeRetour == true) {
					// Générer un nouveau numéro de commande qui sera
					// retourné au client
					joueur.obtenirProtocoleJoueur().genererNumeroReponse();
				}

				// Si le nombre de joueurs en attente est maintenant le nombre
				// de joueurs que ça prend pour joueur au jeu, alors on lance
				// un événement qui indique que la partie est commencée
				if (lstJoueursEnAttente.size() == MAX_NB_PLAYERS) {
					laPartieCommence("Aucun");
				}
			}
		}
		return resultatDemarrerPartie;
	}

	/**
	 * @param joueur
	 * @param idDessin
	 * @return
	 */
	protected void putInWaitingList(JoueurHumain joueur, int idDessin) {
		if(!joueur.obtenirNom().equals(master.obtenirNom()))
		{
			// Ajouter le joueur dans la liste des joueurs en attente
			lstJoueursEnAttente.put(joueur.obtenirNom(), joueur);

			int idPersonnage = this.getOneIdPersonnage(idDessin);

			System.out.println("idPersonnage demarrePartie : " + idPersonnage);

			// Garder en mémoire le Id du personnage choisi par le joueur et son dessin
			joueur.obtenirPartieCourante().setIdDessin(idDessin);
			joueur.obtenirPartieCourante().definirIdPersonnage(idPersonnage);

			// Empêcher d'autres thread de toucher à la liste des joueurs de
			// cette table pendant qu'on parcourt tous les joueurs de la table
			// pour leur envoyer un événement
			synchronized (lstJoueurs) {
				// Préparer l'événement de joueur en attente.
				// Cette fonction va passer les joueurs et créer un
				// InformationDestination pour chacun et ajouter l'événement
				// dans la file de gestion d'événements
				preparerEvenementJoueurDemarrePartie(joueur, idPersonnage);
			}
		}
	}





	/**
	 *
	 * @param joueur
	 * @param doitGenererNoCommandeRetour
	 */
	public void entrerTable(JoueurHumain joueur, boolean doitGenererNoCommandeRetour) {

		lstDiffusion.put(joueur.obtenirNom(), joueur);

		// Le joueur est maintenant entré dans la table courante (il faut
		// créer un objet InformationPartie qui va pointer sur la table
		// courante)
		joueur.definirPartieCourante(new InformationPartieHumain(joueur, this));
		colors.add(joueur.obtenirPartieCourante().resetColor()); 

		// Si on doit générer le numéro de commande de retour, alors
		// on le génère, sinon on ne fait rien
		if (doitGenererNoCommandeRetour == true) {
			// Générer un nouveau numéro de commande qui sera
			// retourné au client
			joueur.obtenirProtocoleJoueur().genererNumeroReponse();
		}

		// Empêcher d'autres thread de toucher à la liste des joueurs de
		// cette salle pendant qu'on parcourt tous les joueurs de la salle
		// pour leur envoyer un événement
		synchronized (getObjSalle().obtenirListeJoueurs()) {
			// Préparer l'événement de nouveau joueur dans la table.
			// Cette fonction va passer les joueurs et créer un
			// InformationDestination pour chacun et ajouter l'événement
			// dans la file de gestion d'événements
			//preparerEvenementJoueurEntreTable(joueur);
			//System.out.println("enter table : " + System.currentTimeMillis());
		}


	}

	/**
	 * Cette méthode permet au joueur passé en paramètres de recommencer la partie.
	 * On suppose que le joueur est dans la table.
	 *
	 * @param joueur Le joueur demandant de recommencer
	 * @param doitGenererNoCommandeRetour Permet de savoir si on doit générer un
	 *        numéro de commande pour le retour de l'appel de fonction
	 *
	 * Synchronisme Cette fonction est synchronisée sur la liste des tables
	 * 	            puis sur la liste des joueurs de cette table, car il se
	 *              peut qu'on doive détruire la table si c'est le dernier
	 *              joueur et qu'on va modifier la liste des joueurs de cette
	 *              table, car le joueur quitte la table. Cela évite que des
	 *              joueurs entrent ou quittent une table en même temps.
	 *              On n'a pas à s'inquiéter que le joueur soit modifié
	 *              pendant le temps qu'on exécute cette fonction. Si on
	 *              inverserait les synchronisations, ça pourrait créer un
	 *              deadlock avec les personnes entrant dans la salle.
	 */
	public void restartGame(JoueurHumain joueur, boolean doitGenererNoCommandeRetour) {

		if(joueur.equals(master))
		{
			lstDiffusion.put(joueur.obtenirNom(), joueur);		
		}else
		{
			// to get back perso's clothes color 
			//returned to the list when get out from game
			//joueur.obtenirPartieCourante().setClothesColor(colors.getLast());

			// to not be without move cases, in case if the server is in waiting an answer
			joueur.obtenirPartieCourante().cancelPosedQuestion();

			// Empécher d'autres thread de toucher à la liste des tables de
			// cette salle pendant que le joueur entre dans cette table
			synchronized (getObjSalle().obtenirListeTables()) {			
				addPlayerInListe(joueur);
				preparerEvenementJoueurRejoindrePartie(joueur);
			}			
		}		
		
		// Si on doit générer le numéro de commande de retour, alors
		// on le génére, sinon on ne fait rien (ça se peut que ce soit
		// faux)
		if (doitGenererNoCommandeRetour == true) {
			// Générer un nouveau numéro de commande qui sera
			// retourné au client
			joueur.obtenirProtocoleJoueur().genererNumeroReponse();

		}
		
		synchronized (lstJoueursDeconnectes) {
			lstJoueursDeconnectes.remove(joueur);
		}    
	}



	/**
	 * Method used to start the game
	 * @param strParamJoueurVirtuel
	 */
	private void laPartieCommence(String strParamJoueurVirtuel) {
		// Créer une nouvelle liste qui va garder les points des
		// cases libres (n'ayant pas d'objets dessus)
		ArrayList<Point> lstPointsCaseLibre = new ArrayList<Point>();


		// Créer un tableau de points qui va contenir la position
		// des joueurs
		Point[] objtPositionsJoueurs;

		// Contient les noms des joueurs virtuels
		String tNomsJoueursVirtuels[] = null;

		// Contiendra le dernier ID des objets
		objProchainIdObjet = new Integer(0);

		//TODO: Peut-être devoir synchroniser cette partie, il
		//      faut voir avec les autres bouts de code qui
		// 		vérifient si la partie est commencée (c'est OK
		//		pour entrerTable)
		// Changer l'état de la table pour dire que maintenant une
		// partie est commencée
		bolEstCommencee = true;

		// Change l'état de la table pour dire que la partie
		// n'est pas arrêtée (note: bolEstCommencee restera à true
		// pendant que les joueurs sont à l'écran de pointage)
		bolEstArretee = false;

		// Générer le plateau de jeu selon les règles de la table et
		// garder le plateau en mémoire dans la table
		objttPlateauJeu = getGameFactory().genererPlateauJeu(lstPointsCaseLibre, lstPointsFinish, this);

		// Définir le prochain id pour les objets
		objProchainIdObjet++;

		// Obtenir la position des joueurs de cette table
		int nbJoueur = lstJoueursEnAttente.size(); //TODO a vérifier		

		objtPositionsJoueurs = this.getGameFactory().genererPositionJoueurs(this, nbJoueur, lstPointsCaseLibre);
		// Création d'une nouvelle liste
		Joueur[] lstJoueursParticipants = new Joueur[nbJoueur];

		// Obtenir un itérateur pour l'ensemble contenant les personnages
		Iterator<JoueurHumain> objIterateurListeJoueurs = lstJoueursEnAttente.values().iterator();

		int position = 0;

		// Passer toutes les positions des joueurs et les définir
		for (int i = 0; i < objtPositionsJoueurs.length; i++) {


			// Comme les positions sont générées aléatoirement, on
			// se fou un peu duquel on va définir la position en
			// premier, on va donc passer simplement la liste des
			// joueurs
			// Créer une référence vers le joueur courant
			// dans la liste (pas besoin de vérifier s'il y en a un
			// prochain, car on a généré la position des joueurs
			// selon cette liste
			JoueurHumain objJoueur = objIterateurListeJoueurs.next();

			if (objJoueur.getRole() == 2) {
				// Définir la position du joueur master
				objJoueur.obtenirPartieCourante().definirPositionJoueur(objtPositionsJoueurs[objtPositionsJoueurs.length - 1]);

				// Ajouter la position du master dans la liste
				//lstPositionsJoueurs.put(objJoueur.obtenirNomUtilisateur(), objtPositionsJoueurs[objtPositionsJoueurs.length - 1]);

				position--;
			} else {

				// Définir la position du joueur courant
				objJoueur.obtenirPartieCourante().definirPositionJoueur(objtPositionsJoueurs[position]);

				// Ajouter la position du joueur dans la liste
				//lstPositionsJoueurs.put(objJoueur.obtenirNomUtilisateur(), objtPositionsJoueurs[position]);
			}

			lstJoueursParticipants[i] = objJoueur;
			position++;
		}

		// On peut maintenant vider la liste des joueurs en attente
		// car elle ne nous sert plus à rien
		lstJoueursEnAttente.clear();		

		// Préparer l'événement que la partie est commencée.
		// Cette fonction va passer les joueurs et créer un
		// InformationDestination pour chacun et ajouter l'événement
		// dans la file de gestion d'événements
		preparerEvenementPartieDemarree(lstJoueursParticipants);

		int tempsStep = 1;
		objTacheSynchroniser.ajouterObservateur(this);
		objMinuterie = new Minuterie(intTempsTotal * 60, tempsStep);
		objMinuterie.ajouterObservateur(this);
		objControleurJeu.obtenirGestionnaireTemps().ajouterTache(objMinuterie, tempsStep);

		// Obtenir la date à ce moment précis
		objDateDebutPartie = new Date();

	}// end method

	public void arreterPartie(String joueurGagnant) {

		// bolEstArretee permet de savoir si cette fonction a déjà été appelée
		// de plus, bolEstArretee et bolEstCommencee permettent de connaître
		// l'état de la partie
		if (bolEstArretee == false) {
			objTacheSynchroniser.enleverObservateur(this);
			try{
				objControleurJeu.obtenirGestionnaireTemps().enleverTache(objMinuterie);
			}catch (IllegalStateException ex){
				objControleurJeu.setNewTimer();
				//objLogger.error("Une erreur est survenue: objControleurJeu.setNewTimer()");
			}

			// to discard Banana or Brainiac tasks
			objGestionnaireTemps.stopIt();

			// S'il y a au moins un joueur qui a complété la partie,
			// alors on ajoute les informations de cette partie dans la BD
			if (lstJoueurs.size() > 0) {
				// Sert à déterminer le meilleur score pour cette partie
				int meilleurPointage = 0;

				TreeSet<StatisticsPlayer> ourResults = new TreeSet<StatisticsPlayer>();


				synchronized (lstJoueurs) {
					// Parcours des joueurs pour trouver le meilleur pointage
					int cleJoueurGagnant = 0; //0 veut dire un joueur virtuel gagne.
					for (JoueurHumain objJoueurHumain: lstJoueurs.values()) {
						InformationPartieHumain infoPartie = objJoueurHumain.obtenirPartieCourante();
						if (infoPartie.obtenirPointage() > meilleurPointage) {
							meilleurPointage = infoPartie.obtenirPointage();
						}

						ourResults.add(new StatisticsPlayer(objJoueurHumain.obtenirNom(), infoPartie.obtenirPointage(), infoPartie.getPointsFinalTime()));

						if (!joueurGagnant.equals("")) {
							if (objJoueurHumain.obtenirNom().equalsIgnoreCase(joueurGagnant))
								cleJoueurGagnant = objJoueurHumain.obtenirCleJoueur();
						}
						else if (ourResults.last().getUsername().equalsIgnoreCase(objJoueurHumain.obtenirNom()))
							cleJoueurGagnant = objJoueurHumain.obtenirCleJoueur();

						//System.out.println("table - Joueur H " + " " + objJoueurHumain.obtenirNomUtilisateur() + " " + infoPartie.getPointsFinalTime() + " " + ourResults.size());
					}

					// Ajouter la partie dans la BD
					int clePartie = objGestionnaireBD.ajouterInfosPartieTerminee(
							objSalle.getRoomId(), gameType, objDateDebutPartie, intTempsTotal, cleJoueurGagnant);

					preparerEvenementPartieTerminee(ourResults, joueurGagnant);

					// Parcours des joueurs pour mise à jour de la BD et
					// pour ajouter les infos de la partie complétée
					for (JoueurHumain joueur: lstJoueurs.values()) {
						joueur.obtenirPartieCourante().getObjGestionnaireBD().mettreAJourJoueur(intTempsTotal);
						// if the game was with the permission to use user's money from DB
						if (joueur.obtenirPartieCourante().obtenirTable().getRegles().isBolMoneyPermit()) {
							joueur.obtenirPartieCourante().getObjGestionnaireBD().setNewPlayersMoney();
						}
						boolean estGagnant = (joueur.obtenirCleJoueur() == cleJoueurGagnant);
						objGestionnaireBD.ajouterInfosJoueurPartieTerminee(clePartie, joueur, estGagnant);
						if(joueur.getRole() > 1)
							joueur.obtenirPartieCourante().writeInfo();

					}
				} //// end sinchro
			}

			// wipeout players from the table
			if (!lstJoueurs.isEmpty()) {

				lstJoueurs.clear();
				lstDiffusion.clear();
			}

			// Enlever les joueurs déconnectés de cette table de la
			// liste des joueurs déconnectés du serveur pour éviter
			// qu'ils ne se reconnectent et tentent de rejoindre une partie terminée
			for (int i = 0; i < lstJoueursDeconnectes.size(); i++) {
				objControleurJeu.enleverJoueurDeconnecte(lstJoueursDeconnectes.get(i));
			}

			// Enlever les joueurs déconnectés de cette table
			lstJoueursDeconnectes = new LinkedList<String>();

			// Arrêter la partie
			bolEstArretee = true;
			//System.out.println("table - etape 1 " + lstJoueurs.size());
			// Si jamais les joueurs humains sont tous déconnectés, alors
			// il faut détruire la table ici
			if (lstJoueurs.isEmpty()) {
				// Détruire la table courante et envoyer les événements
				// appropriés
				//System.out.println("table - etape - is empty");
				getObjSalle().detruireTable(this);
			}


		}// end if bolEstArretee

		if (bolEstArretee == true) {

			objMinuterie = null;
			objGestionnaireTemps = null;
			objTacheSynchroniser = null;            

			this.objttPlateauJeu = null;
			this.gameFactory = null;
			//System.out.println("table - etape 2");
		}
		//System.out.println("table - end of method");
	}// end method

	protected void addPlayerInListe(JoueurHumain joueur)
	{
		//Empêcher d'autres thread de toucher à la liste des joueurs de
		//cette table pendant l'ajout du nouveau joueur dans cette table
		synchronized (lstJoueurs) {
			// Ajouter ce nouveau joueur dans la liste des joueurs de cette table
			lstJoueurs.put(joueur.obtenirNom(), joueur);
			lstDiffusion.put(joueur.obtenirNom(), joueur);
		}
	}

	protected void getOutPlayerFromListe(JoueurHumain player)
	{
		// Empêcher d'autres thread de toucher à la liste des joueurs de
		// cette table pendant que le joueur quitte cette table
		synchronized (lstJoueurs) {
			// Enlever le joueur de la liste des joueurs de cette table
			getBackOneIdPersonnage(player.obtenirPartieCourante().obtenirIdPersonnage());
			lstJoueurs.remove(player.obtenirNom());
			lstJoueursEnAttente.remove(player.obtenirNom());
			lstDiffusion.remove(player.obtenirNom());
			colors.add(player.obtenirPartieCourante().resetColor()); 
		}
	}

	/**
	 * Sent event to players exept players that is motive for the event
	 * @param evenement
	 * @param eventHero
	 */
	protected void broadcastEvent(Evenement event, Joueur eventHero)
	{

		// Empêcher d'autres thread de toucher à la liste des joueurs de
		// cette table pendant qu'on parcourt tous les joueurs de la table
		// pour leur envoyer un événement
		synchronized (lstJoueurs) {
			// Passer tous les joueurs de la table et leur envoyer un événement
			for (JoueurHumain objJoueur: lstDiffusion.values()) {
				// Si le nom d'utilisateur du joueur courant n'est pas celui
				// qui vient de démarrer la partie, alors on peut envoyer un
				// événement à cet utilisateur
				if (!objJoueur.obtenirNom().equals(eventHero.obtenirNom())) {
					// Obtenir un numéro de commande pour le joueur courant, créer
					// un InformationDestination et l'ajouter à l'événement
					event.ajouterInformationDestination( new InformationDestination(
							objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
							objJoueur.obtenirProtocoleJoueur()));
				}
			}
		}

		// Ajouter le nouvel événement créé dans la liste d'événements à traiter
		objGestionnaireEvenements.ajouterEvenement(event);
	}

	/**
	 * Send an event to all players in the room liste
	 * @param event
	 */
	protected void broadcastEvent(Evenement event)
	{
		// Empêcher d'autres thread de toucher à la liste des joueurs de
		// cette table pendant qu'on parcourt tous les joueurs de la table
		// pour leur envoyer un événement
		synchronized (lstJoueurs) {
			// Passer tous les joueurs de la table et leur envoyer un événement

			for (JoueurHumain objJoueur: lstDiffusion.values()) {
				//System.out.println("table - broadcast " + objJoueur.obtenirNom() + " " + objJoueur.obtenirProtocoleJoueur());
				// Obtenir un numéro de commande pour le joueur courant, créer
				// un InformationDestination et l'ajouter à l'événement
				event.ajouterInformationDestination( new InformationDestination(
						objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
						objJoueur.obtenirProtocoleJoueur()));

			}

		}

		// Ajouter le nouvel événement créé dans la liste d'événements à traiter
		objGestionnaireEvenements.ajouterEvenement(event);
	}


	public int verifyFinishAndSetBonus(Point point)
	{
		if(checkPositionPointsFinish(point))
		{
			return obtenirTempsRestant();
		}
		return 0;		
	}
	
	public void verifyStopCondition()
	{
		// if all the humains is on the finish line we stop the game
		if (isAllTheHumainsOnTheFinish())
		{
			arreterPartie(""); 
		}		
	}
}
