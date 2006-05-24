package ServeurJeu.ComposantesJeu;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.awt.Point;
import org.w3c.dom.Element;

import ServeurJeu.BD.GestionnaireBD;
import ServeurJeu.ComposantesJeu.Joueurs.JoueurHumain;
import ServeurJeu.ComposantesJeu.ReglesJeu.Regles;
import ServeurJeu.Evenements.EvenementJoueurEntreTable;
import ServeurJeu.Evenements.EvenementJoueurQuitteTable;
import ServeurJeu.Evenements.EvenementJoueurDemarrePartie;
import ServeurJeu.Evenements.EvenementPartieDemarree;
import ServeurJeu.Evenements.GestionnaireEvenements;
import ServeurJeu.Evenements.InformationDestination;
import ClassesUtilitaires.GenerateurPartie;
import Enumerations.RetourFonctions.ResultatDemarrerPartie;
import ServeurJeu.ComposantesJeu.Cases.Case;
import ServeurJeu.Temps.*;
import ServeurJeu.Evenements.EvenementSynchroniserTemps;
import ServeurJeu.Evenements.EvenementPartieTerminee;

/**
 * @author Jean-Fran�ois Brind'Amour
 */
public class Table implements ObservateurSynchroniser, ObservateurMinuterie
{
	// D�claration d'une r�f�rence vers le gestionnaire d'�v�nements
	private GestionnaireEvenements objGestionnaireEvenements;
	
	// D�claration d'une r�f�rence vers le gestionnaire de bases de donn�es
	private GestionnaireBD objGestionnaireBD;
	
	// D�claration d'une r�f�rence vers la salle parente dans laquelle se 
	// trouve cette table 
	private Salle objSalle;
	
	// Cette variable va contenir le num�ro de la table
	private int intNoTable;
	
	// D�claration d'une constante qui d�finit le nombre maximal de joueurs 
	// dans une table
	private final int MAX_NB_JOUEURS = 4;
	
	private int intNbJoueurDemande; 
	
	// Cette variable va contenir le nom d'utilisateur du cr�ateur de cette table
	private String strNomUtilisateurCreateur;
	
	// D�claration d'une variable qui va garder le temps total d�fini pour 
	// cette table
	private int intTempsTotal;
	
	// D�claration d'une variable qui va garder le temps restant (au d�part 
	// il vaut la m�me chose que intTempsTotal)
	//private int intTempsRestant;
	
	// Cet objet est une liste des joueurs qui sont pr�sentement sur cette table
	private TreeMap lstJoueurs;
	
	// Cet objet est une liste des joueurs qui attendent de joueur une partie
	private TreeMap lstJoueursEnAttente;
	
	// D�claration d'une variable qui va permettre de savoir si la partie est 
	// commenc�e ou non
	private boolean bolEstCommencee;
	   
	// D�claration d'une variable qui va permettre d'arr�ter la partie en laissant
	// l'�tat de la partie � "commenc�e" tant que les joueurs sont � l'�cran des pointages
	private boolean bolEstArretee;
	
	// D�claration d'un tableau � 2 dimensions qui va contenir les informations 
	// sur les cases du jeu
	private Case[][] objttPlateauJeu;
	
	// Cet objet permet de d�terminer les r�gles de jeu pour cette table
	private Regles objRegles;
	
	private GestionnaireTemps objGestionnaireTemps;
	private TacheSynchroniser objTacheSynchroniser;
	private Minuterie objMinuterie;
	

	/**
	 * Constructeur de la classe Table qui permet d'initialiser les membres 
	 * priv�s de la table.
	 *
	 * @param Salle salleParente : La salle dans laquelle se trouve cette table
	 * @param GestionnaireBD gestionnaireBD : Le gestionnaire de base de donn�es
	 * @param int noTable : Le num�ro de la table
	 * @param String nomUtilisateurCreateur : Le nom d'utilisateur du cr�ateur
	 * 										  de la table
	 * @param int tempsPartie : Le temps de la partie en minute
	 * @param Regles reglesTable : Les r�gles pour une partie sur cette table
	 */
	public Table(GestionnaireEvenements gestionnaireEv, GestionnaireBD gestionnaireBD, 
				 Salle salleParente, int noTable, String nomUtilisateurCreateur, 
				 int tempsPartie, Regles reglesTable,
				 GestionnaireTemps gestionnaireTemps, TacheSynchroniser tacheSynchroniser ) 
	{
		super();
		
		// Faire la r�f�rence vers le gestionnaire d'�v�nements et le 
		// gestionnaire de base de donn�es
		objGestionnaireEvenements = gestionnaireEv;
		objGestionnaireBD = gestionnaireBD;
		
		// Garder en m�moire la r�f�rence vers la salle parente, le num�ro de 
		// la table, le nom d'utilisateur du cr�ateur de la table et le temps
		// total d'une partie
		objSalle = salleParente;
		intNoTable = noTable;
		strNomUtilisateurCreateur = nomUtilisateurCreateur;
		intTempsTotal = tempsPartie;
	//	intTempsRestant = tempsPartie;
		
		// Cr�er une nouvelle liste de joueurs
		lstJoueurs = new TreeMap();
		lstJoueursEnAttente = new TreeMap();
		
		// Au d�part, aucune partie ne se joue sur la table
		bolEstCommencee = false;
		bolEstArretee = true;
		intNbJoueurDemande = MAX_NB_JOUEURS;//TODO intNbJoueurDemande = intNbJoueur; validation avec MAX_NB_JOUEURS
		
		// D�finir les r�gles de jeu pour la salle courante
		objRegles = reglesTable;
		
		// Initialiser le plateau de jeu � null
		objttPlateauJeu = null;
		
		objGestionnaireTemps = gestionnaireTemps;
		objTacheSynchroniser = tacheSynchroniser;

	}
	
	public void creation()
	{

	}
	
	public void destruction()
	{
		arreterPartie();
	}
	
	/**
	 * Cette fonction permet au joueur d'entrer dans la table courante. 
	 * On suppose que le joueur n'est pas dans une autre table, que la table 
	 * courante n'est pas compl�te et qu'il n'y a pas de parties en cours. 
	 * Cette fonction va avoir pour effet de connecter le joueur dans la table 
	 * courante.
	 * 
	 * @param JoueurHumain joueur : Le joueur demandant d'entrer dans la table
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 								g�n�rer un num�ro de commande pour le retour de
	 * 								l'appel de fonction
	 * @param TreeMap listePersonnageJoueurs : La liste des joueurs dont la cl� 
	 * 								est le nom d'utilisateur du joueur et le contenu 
	 * 								est le Id du personnage choisi
	 * @throws NullPointerException : Si la liste listePersonnageJoueurs est nulle
	 * 
	 * Synchronisme : Cette fonction est synchronis�e pour �viter que deux 
	 * 				  joueurs puissent entrer ou quitter la table en m�me temps.
	 * 				  On n'a pas � s'inqui�ter que le joueur soit modifi�
	 * 				  pendant le temps qu'on ex�cute cette fonction. De plus
	 * 				  on n'a pas � rev�rifier que la table existe bien (car
	 * 				  elle ne peut �tre supprim�e en m�me temps qu'un joueur 
	 * 				  entre dans la table), qu'elle n'est pas compl�te ou 
	 * 				  qu'une partie est en cours (car toutes les fonctions 
	 * 				  permettant de changer �a sont synchronis�es).
	 */
	public void entrerTable(JoueurHumain joueur, boolean doitGenererNoCommandeRetour, TreeMap listePersonnageJoueurs)  throws NullPointerException
	{
	    // Emp�cher d'autres thread de toucher � la liste des joueurs de 
	    // cette table pendant l'ajout du nouveau joueur dans cette table
	    synchronized (lstJoueurs)
	    {
	    	// Remplir la liste des personnages choisis
	    	remplirListePersonnageJoueurs(listePersonnageJoueurs);
	    	
			// Ajouter ce nouveau joueur dans la liste des joueurs de cette table
			lstJoueurs.put(joueur.obtenirNomUtilisateur(), joueur);
			
			// Le joueur est maintenant entr� dans la table courante (il faut
			// cr�er un objet InformationPartie qui va pointer sur la table
			// courante)
			joueur.definirPartieCourante(new InformationPartie(objGestionnaireEvenements, objGestionnaireBD, joueur, this));
			
			// Si on doit g�n�rer le num�ro de commande de retour, alors
			// on le g�n�re, sinon on ne fait rien
			if (doitGenererNoCommandeRetour == true)
			{
				// G�n�rer un nouveau num�ro de commande qui sera 
			    // retourn� au client
			    joueur.obtenirProtocoleJoueur().genererNumeroReponse();					    
			}

			// Emp�cher d'autres thread de toucher � la liste des joueurs de 
		    // cette salle pendant qu'on parcourt tous les joueurs de la salle
			// pour leur envoyer un �v�nement
		    synchronized (objSalle.obtenirListeJoueurs())
		    {
				// Pr�parer l'�v�nement de nouveau joueur dans la table. 
				// Cette fonction va passer les joueurs et cr�er un 
				// InformationDestination pour chacun et ajouter l'�v�nement 
				// dans la file de gestion d'�v�nements
				preparerEvenementJoueurEntreTable(joueur.obtenirNomUtilisateur());		    	
		    }
	    }
	}

	/**
	 * Cette m�thode permet au joueur pass� en param�tres de quitter la table. 
	 * On suppose que le joueur est dans la table.
	 * 
	 * @param JoueurHumain joueur : Le joueur demandant de quitter la table
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 								g�n�rer un num�ro de commande pour le retour de
	 * 								l'appel de fonction
	 * 
	 * Synchronisme : Cette fonction est synchronis�e sur la liste des tables
	 * 				  puis sur la liste des joueurs de cette table, car il se
	 * 				  peut qu'on doive d�truire la table si c'est le dernier
	 * 				  joueur et qu'on va modifier la liste des joueurs de cette
	 * 				  table, car le joueur quitte la table. Cela �vite que des
	 * 				  joueurs entrent ou quittent une table en m�me temps.
	 * 				  On n'a pas � s'inqui�ter que le joueur soit modifi�
	 * 				  pendant le temps qu'on ex�cute cette fonction. Si on 
	 * 				  inverserait les synchronisations, �a pourrait cr�er un 
	 * 				  deadlock avec les personnes entrant dans la salle.
	 */
	public void quitterTable(JoueurHumain joueur, boolean doitGenererNoCommandeRetour)
	{
	    // Emp�cher d'autres thread de toucher � la liste des tables de 
	    // cette salle pendant que le joueur quitte cette table
	    synchronized (objSalle.obtenirListeTables())
	    {
		    // Emp�cher d'autres thread de toucher � la liste des joueurs de 
		    // cette table pendant que le joueur quitte cette table
		    synchronized (lstJoueurs)
		    {
		    	// Enlever le joueur de la liste des joueurs de cette table
				lstJoueurs.remove(joueur.obtenirNomUtilisateur());
				
				// Le joueur est maintenant dans aucune table
				joueur.definirPartieCourante(null);
				
				// Si on doit g�n�rer le num�ro de commande de retour, alors
				// on le g�n�re, sinon on ne fait rien (�a se peut que ce soit
				// faux)
				if (doitGenererNoCommandeRetour == true)
				{
					// G�n�rer un nouveau num�ro de commande qui sera 
				    // retourn� au client
				    joueur.obtenirProtocoleJoueur().genererNumeroReponse();					    
				}

				// Emp�cher d'autres thread de toucher � la liste des joueurs de 
			    // cette salle pendant qu'on parcourt tous les joueurs de la salle
				// pour leur envoyer un �v�nement
			    synchronized (objSalle.obtenirListeJoueurs())
			    {
					// Pr�parer l'�v�nement qu'un joueur a quitt� la table. 
					// Cette fonction va passer les joueurs et cr�er un 
					// InformationDestination pour chacun et ajouter l'�v�nement 
					// dans la file de gestion d'�v�nements
					preparerEvenementJoueurQuitteTable(joueur.obtenirNomUtilisateur());
			    }

			    // S'il ne reste aucuns joueurs dans la table, alors on doit 
			    // d�truire la table
			    if (lstJoueurs.size() == 0)
			    {
			    	//Arreter le gestionnaire de temps
			    	//objGestionnaireTemps.arreterGestionnaireTemps();
			    	// D�truire la table courante et envoyer les �v�nements 
			    	// appropri�s
			    	objSalle.detruireTable(this);
			    }
		    }
		}
	}
	
	/**
	 * Cette m�thode permet au joueur pass� en param�tres de d�marrer la partie. 
	 * On suppose que le joueur est dans la table.
	 * 
	 * @param JoueurHumain joueur : Le joueur demandant de d�marrer la partie
	 * @param int idPersonnage : Le num�ro Id du personnage choisi par le joueur
	 * @param boolean doitGenererNoCommandeRetour : Permet de savoir si on doit 
	 * 								g�n�rer un num�ro de commande pour le retour de
	 * 								l'appel de fonction
	 * @return String : Succes : si le joueur est maintenant en attente
	 * 					DejaEnAttente : si le joueur �tait d�j� en attente
	 * 					PartieEnCours : si une partie �tait en cours
	 * 
	 * Synchronisme : Cette fonction est synchronis�e sur la liste des joueurs 
	 * 				  en attente, car il se peut qu'on ajouter ou retirer des
	 * 				  joueurs de la liste en attente en m�me temps. On n'a pas
	 * 				  � s'inqui�ter que le m�me joueur soit mis dans la liste 
	 * 				  des joueurs en attente par un autre thread.
	 */
	public String demarrerPartie(JoueurHumain joueur, int idPersonnage, boolean doitGenererNoCommandeRetour)
	{
		// Cette variable va permettre de savoir si le joueur est maintenant
		// attente ou non
		String strResultatDemarrerPartie;
		
	    // Emp�cher d'autres thread de toucher � la liste des joueurs en attente 
	    // de cette table pendant que le joueur tente de d�marrer la partie
	    synchronized (lstJoueursEnAttente)
	    {
	    	// Si une partie est en cours alors on va retourner PartieEnCours
	    	if (bolEstCommencee == true)
	    	{
	    		strResultatDemarrerPartie = ResultatDemarrerPartie.PartieEnCours;
	    	}
	    	// Sinon si le joueur est d�j� en attente, alors on va retourner 
	    	// DejaEnAttente
	    	else if (lstJoueursEnAttente.containsKey(joueur.obtenirNomUtilisateur()) == true)
	    	{
	    		strResultatDemarrerPartie = ResultatDemarrerPartie.DejaEnAttente;
	    	}
	    	else
	    	{
	    		// La commande s'est effectu�e avec succ�s
	    		strResultatDemarrerPartie = ResultatDemarrerPartie.Succes;
	    		
	    		// Ajouter le joueur dans la liste des joueurs en attente
				lstJoueursEnAttente.put(joueur.obtenirNomUtilisateur(), joueur);
				
				// Garder en m�moire le Id du personnage choisi par le joueur
				joueur.obtenirPartieCourante().definirIdPersonnage(idPersonnage);
				
	    		// Si on doit g�n�rer le num�ro de commande de retour, alors
				// on le g�n�re, sinon on ne fait rien (�a se peut que ce soit
				// faux)
				if (doitGenererNoCommandeRetour == true)
				{
					// G�n�rer un nouveau num�ro de commande qui sera 
				    // retourn� au client
				    joueur.obtenirProtocoleJoueur().genererNumeroReponse();					    
				}
				
				// Emp�cher d'autres thread de toucher � la liste des joueurs de 
			    // cette table pendant qu'on parcourt tous les joueurs de la table
				// pour leur envoyer un �v�nement
			    synchronized (lstJoueurs)
			    {
					// Pr�parer l'�v�nement de joueur en attente. 
					// Cette fonction va passer les joueurs et cr�er un 
					// InformationDestination pour chacun et ajouter l'�v�nement 
					// dans la file de gestion d'�v�nements
					preparerEvenementJoueurDemarrePartie(joueur.obtenirNomUtilisateur(), idPersonnage);		    	
			    }
				
				// Si le nombre de joueurs en attente est maintenant le nombre 
				// de joueurs que �a prend pour joueur au jeu, alors on lance 
				// un �v�nement qui indique que la partie est commenc�e
				if (lstJoueursEnAttente.size() == intNbJoueurDemande)
				{
					// Cr�er une nouvelle liste qui va garder les points des 
					// cases libres (n'ayant pas d'objets dessus)
					Vector lstPointsCaseLibre = new Vector();
					
					// Cr�er un tableau de points qui va contenir la position 
					// des joueurs
					Point[] objtPositionsJoueurs;
					
					// Cr�ation d'une nouvelle liste dont la cl� est le nom 
					// d'utilisateur du joueur et le contenu est un point 
					// repr�sentant la position du joueur
					TreeMap lstPositionsJoueurs = new TreeMap();
					
					//TODO: Peut-�tre devoir synchroniser cette partie, il 
					//      faut voir avec les autres bouts de code qui 
					// 		v�rifient si la partie est commenc�e (c'est OK 
					//		pour entrerTable)
					// Changer l'�tat de la table pour dire que maintenant une 
					// partie est commenc�e
					bolEstCommencee = true;
		            
		            // Change l'�tat de la table pour dire que la partie
		            // n'est pas arr�t�e (note: bolEstCommencee restera � true
		            // pendant que les joueurs sont � l'�cran de pointage)
		            bolEstArretee = false;
		            
					// G�n�rer le plateau de jeu selon les r�gles de la table et 
					// garder le plateau en m�moire dans la table
					objttPlateauJeu = GenerateurPartie.genererPlateauJeu(objRegles, intTempsTotal, lstPointsCaseLibre);
					
					// Obtenir la position des joueurs de cette table
					objtPositionsJoueurs = GenerateurPartie.genererPositionJoueurs(intNbJoueurDemande, lstPointsCaseLibre);
					
					// Cr�er un ensemble contenant tous les tuples de la liste 
					// lstJoueursEnAttente (chaque �l�ment est un Map.Entry)
					Set lstEnsembleJoueurs = lstJoueursEnAttente.entrySet();
					
					// Obtenir un it�rateur pour l'ensemble contenant les personnages
					Iterator objIterateurListeJoueurs = lstEnsembleJoueurs.iterator();
					
					// Passer toutes les positions des joueurs et les d�finir
					for (int i = 0; i < objtPositionsJoueurs.length; i++)
					{
						// Comme les positions sont g�n�r�es al�atoirement, on 
						// se fou un peu duquel on va d�finir la position en 
						// premier, on va donc passer simplement la liste des 
						// joueurs
						// Cr�er une r�f�rence vers le joueur courant 
					    // dans la liste (pas besoin de v�rifier s'il y en a un 
						// prochain, car on a g�n�r� la position des joueurs 
						// selon cette liste
						JoueurHumain objJoueur = (JoueurHumain) (((Map.Entry)(objIterateurListeJoueurs.next())).getValue());
						
						// D�finir la position du joueur courant
						objJoueur.obtenirPartieCourante().definirPositionJoueur(objtPositionsJoueurs[i]);
						
						// Ajouter la position du joueur dans la liste
						lstPositionsJoueurs.put(objJoueur.obtenirNomUtilisateur(), objtPositionsJoueurs[i]);
					}
					
					// On peut maintenant vider la liste des joueurs en attente
					// car elle ne nous sert plus � rien
					lstJoueursEnAttente.clear();
					
					// Emp�cher d'autres thread de toucher � la liste des joueurs de 
				    // cette table pendant qu'on parcourt tous les joueurs de la table
					// pour leur envoyer un �v�nement
				    synchronized (lstJoueurs)
				    {
						// Pr�parer l'�v�nement que la partie est commenc�e. 
						// Cette fonction va passer les joueurs et cr�er un 
						// InformationDestination pour chacun et ajouter l'�v�nement 
						// dans la file de gestion d'�v�nements
						preparerEvenementPartieDemarree(lstPositionsJoueurs);
				    }
				    
				    int tempsStep = 1;
				    objTacheSynchroniser.ajouterObservateur( this );
				    objMinuterie = new Minuterie( intTempsTotal * 60, tempsStep );
				    objMinuterie.ajouterObservateur( this );
				    objGestionnaireTemps.ajouterTache( objMinuterie, tempsStep );
				}
	    	}
		}
	    
	    return strResultatDemarrerPartie;
	}
	
	public void arreterPartie()
	{
		if( bolEstArretee == false )
		{
			bolEstArretee = true;
			objTacheSynchroniser.enleverObservateur( this );
			objGestionnaireTemps.enleverTache( objMinuterie );
			objMinuterie = null;
			synchronized (lstJoueurs)
		    {
				preparerEvenementPartieTerminee();
				Iterator it = lstJoueurs.values().iterator();
				while( it.hasNext() )
				{
					//Mettre a jour les donnees des joueurs
					//TODO : si partie pas completee???
					JoueurHumain joueur = (JoueurHumain)it.next();
					objGestionnaireBD.mettreAJourJoueur( joueur, intTempsTotal );
					//TODO le temps total si partie pas termin�e ???
				}
		    }
		}
	}
	
	/**
	 * Cette fonction permet de retourner le num�ro de la table courante.
	 * 
	 * @return int : Le num�ro de la table
	 */
	public int obtenirNoTable()
	{
		return intNoTable;
	}
	
	/**
	 * Cette fonction permet de retourner la liste des joueurs. La vraie liste
	 * est retourn�e.
	 * 
	 * @return TreeMap : La liste des joueurs se trouvant dans la table courante
	 * 
	 * Synchronisme : Cette fonction n'est pas synchronis�e ici, mais elle doit
	 * 				  l'�tre par l'appelant de cette fonction tout d�pendant
	 * 				  du traitement qu'elle doit faire
	 */
	public TreeMap obtenirListeJoueurs()
	{
		return lstJoueurs;
	}
	
	/**
	 * Cette fonction permet de retourner la liste des joueurs qui sont en 
	 * attente de jouer une partie. La vraie liste est retourn�e.
	 * 
	 * @return TreeMap : La liste des joueurs en attente se trouvant dans la 
	 * 					 table courante
	 * 
	 * Synchronisme : Cette fonction n'est pas synchronis�e ici, mais elle doit
	 * 				  l'�tre par l'appelant de cette fonction tout d�pendant
	 * 				  du traitement qu'elle doit faire
	 */
	public TreeMap obtenirListeJoueursEnAttente()
	{
		return lstJoueursEnAttente;
	}
	
	/**
	 * Cette fonction permet de retourner le temps total des parties de cette 
	 * table.
	 * 
	 * @return int : Le temps total des parties de cette table
	 */
	public int obtenirTempsTotal()
	{
		return intTempsTotal;
	}
	
	/**
	 * Cette fonction permet de d�terminer si la table est compl�te ou non 
	 * (elle est compl�te si le nombre de joueurs dans cette table �gale le 
	 * nombre de joueurs maximum par table).
	 * 
	 * @return boolean : true si la table est compl�te
	 * 					 false sinon
	 * 
	 * Synchronisme : Cette fonction est synchronis�e car il peut s'ajouter de
	 * 				  nouveaux joueurs ou d'autres peuvent quitter pendant la 
	 * 				  v�rification.
	 */
	public boolean estComplete()
	{
	    // Emp�cher d'autres Thread de toucher � la liste des joueurs de cette
	    // table pendant qu'on fait la v�rification (un TreeMap n'est pas 
	    // synchronis�)
	    synchronized (lstJoueurs)
	    {
			// Si la taille de la liste de joueurs �gale le nombre maximal de 
			// joueurs alors la table est compl�te, sinon elle ne l'est pas
			return (lstJoueurs.size() == intNbJoueurDemande);	        
	    }
	}
	
	/**
	 * Cette fonction permet de d�terminer si une partie est commenc�e ou non.
	 * 
	 * @return boolean : true s'il y a une partie en cours
	 * 					 false sinon
	 */
	public boolean estCommencee()
	{
		return bolEstCommencee;	        
	}
	
	/**
	 * Cette fonction retourne les r�gles pour la table courante.
	 * 
	 * @return Regles : Les r�gles pour la table courante
	 */
	public Regles obtenirRegles()
	{
		return objRegles;
	}
	
	/**
	 * Cette fonction retourne le plateau de jeu courant.
	 * 
	 * @return Case[][] : Le plateau de jeu courant,
	 * 					  null s'il n'y a pas de partie en cours
	 */
	public Case[][] obtenirPlateauJeuCourant()
	{
		return objttPlateauJeu;
	}
	
	/**
	 * Cette m�thode permet de remplir la liste des personnages des joueurs 
	 * o� les cl�s seront le nom d'utilisateur du joueur et le contenu le 
	 * num�ro du personnage. On suppose que le joueur courant n'est pas 
	 * encore dans la liste.
	 *  
	 * @param TreeMap listePersonnageJoueurs : La liste des personnages 
	 * 										   pour chaque joueur
	 * @throws NullPointerException : Si la liste des personnages est � nulle
	 */
	private void remplirListePersonnageJoueurs(TreeMap listePersonnageJoueurs) throws NullPointerException
	{
		// Cr�er un ensemble contenant tous les tuples de la liste 
		// des joueurs de la table (chaque �l�ment est un Map.Entry)
		Set lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un it�rateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la table et leur envoyer un �v�nement
		while (objIterateurListe.hasNext() == true)
		{
			// Cr�er une r�f�rence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());
			
			// Ajouter le joueur dans la liste des personnages (il se peut que 
			// le joueur n'aie pas encore de personnages, alors le id est 0)
			listePersonnageJoueurs.put(objJoueur.obtenirNomUtilisateur(), new Integer(objJoueur.obtenirPartieCourante().obtenirIdPersonnage()));
		}
		
		// D�claration d'un compteur
		int i = 1;
		
		// Boucler tant qu'on n'a pas atteint le nombre maximal de 
		// joueurs moins le joueur courant car on ne le met pas dans la liste
		while (listePersonnageJoueurs.size() < MAX_NB_JOUEURS - 1)
		{
			// On ajoute un joueur inconnu ayant le personnage 0
			listePersonnageJoueurs.put("Inconnu" + Integer.toString(i), new Integer(0));
			
			i++;
		}
	}

	/**
	 * Cette m�thode permet de pr�parer l'�v�nement de l'entr�e d'un joueur 
	 * dans la table courante. Cette m�thode va passer tous les joueurs 
	 * de la salle courante et pour ceux devant �tre avertis (tous sauf le 
	 * joueur courant pass� en param�tre), on va obtenir un num�ro de commande, 
	 * on va cr�er un InformationDestination et on va ajouter l'�v�nement dans 
	 * la file d'�v�nements du gestionnaire d'�v�nements. Lors de l'appel 
	 * de cette fonction, la liste des joueurs est synchronis�e.
	 * 
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur qui
	 * 								  vient d'entrer dans la table
	 * 
	 * Synchronisme : Cette fonction n'est pas synchronis�e ici, mais elle l'est
	 * 				  par l'appelant (entrerTable).
	 */
	private void preparerEvenementJoueurEntreTable(String nomUtilisateur)
	{
	    // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement 
	    // aux joueurs qu'un joueur est entr� dans la table
	    EvenementJoueurEntreTable joueurEntreTable = new EvenementJoueurEntreTable(intNoTable, nomUtilisateur);
	    
		// Cr�er un ensemble contenant tous les tuples de la liste 
		// des joueurs de la salle (chaque �l�ment est un Map.Entry)
		Set lstEnsembleJoueurs = objSalle.obtenirListeJoueurs().entrySet();
		
		// Obtenir un it�rateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un �v�nement
		while (objIterateurListe.hasNext() == true)
		{
			// Cr�er une r�f�rence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur courant n'est pas celui
			// qui vient d'entrer dans la table, alors on peut envoyer un 
			// �v�nement � cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false)
			{
			    // Obtenir un num�ro de commande pour le joueur courant, cr�er 
			    // un InformationDestination et l'ajouter � l'�v�nement
			    joueurEntreTable.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            											objJoueur.obtenirProtocoleJoueur()));
			}
		}
		
		// Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
		objGestionnaireEvenements.ajouterEvenement(joueurEntreTable);
	}

	/**
	 * Cette m�thode permet de pr�parer l'�v�nement du d�part d'un joueur 
	 * de la table courante. Cette m�thode va passer tous les joueurs 
	 * de la salle courante et pour ceux devant �tre avertis (tous sauf le 
	 * joueur courant pass� en param�tre), on va obtenir un num�ro de commande, 
	 * on va cr�er un InformationDestination et on va ajouter l'�v�nement dans 
	 * la file d'�v�nements du gestionnaire d'�v�nements. Lors de l'appel 
	 * de cette fonction, la liste des joueurs est synchronis�e.
	 * 
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur qui
	 * 								  vient de quitter la table
	 * 
	 * Synchronisme : Cette fonction n'est pas synchronis�e ici, mais elle l'est
	 * 				  par l'appelant (quitterTable).
	 */
	private void preparerEvenementJoueurQuitteTable(String nomUtilisateur)
	{
	    // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement 
	    // aux joueurs qu'un joueur a quitt� la table
	    EvenementJoueurQuitteTable joueurQuitteTable = new EvenementJoueurQuitteTable(intNoTable, nomUtilisateur);
	    
		// Cr�er un ensemble contenant tous les tuples de la liste 
		// des joueurs de la salle (chaque �l�ment est un Map.Entry)
		Set lstEnsembleJoueurs = objSalle.obtenirListeJoueurs().entrySet();
		
		// Obtenir un it�rateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un �v�nement
		while (objIterateurListe.hasNext() == true)
		{
			// Cr�er une r�f�rence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur courant n'est pas celui
			// qui vient de quitter la table, alors on peut envoyer un 
			// �v�nement � cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false)
			{
			    // Obtenir un num�ro de commande pour le joueur courant, cr�er 
			    // un InformationDestination et l'ajouter � l'�v�nement
			    joueurQuitteTable.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            											objJoueur.obtenirProtocoleJoueur()));
			}
		}
		
		// Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
		objGestionnaireEvenements.ajouterEvenement(joueurQuitteTable);
	}
	
	/**
	 * Cette m�thode permet de pr�parer l'�v�nement du d�marrage d'une partie 
	 * de la table courante. Cette m�thode va passer tous les joueurs 
	 * de la table courante et pour ceux devant �tre avertis (tous sauf le 
	 * joueur courant pass� en param�tre), on va obtenir un num�ro de commande, 
	 * on va cr�er un InformationDestination et on va ajouter l'�v�nement dans 
	 * la file d'�v�nements du gestionnaire d'�v�nements. Lors de l'appel 
	 * de cette fonction, la liste des joueurs est synchronis�e.
	 * 
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur qui
	 * 								  vient de d�marrer la partie
	 * @param int idPersonnage : Le num�ro Id du personnage choisi par le joueur 
	 * 
	 * Synchronisme : Cette fonction n'est pas synchronis�e ici, mais elle l'est
	 * 				  par l'appelant (demarrerPartie).
	 */
	private void preparerEvenementJoueurDemarrePartie(String nomUtilisateur, int idPersonnage)
	{
	    // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement 
	    // aux joueurs qu'un joueur d�marr� une partie
	    EvenementJoueurDemarrePartie joueurDemarrePartie = new EvenementJoueurDemarrePartie(nomUtilisateur, idPersonnage);
	    
		// Cr�er un ensemble contenant tous les tuples de la liste 
		// des joueurs de la table (chaque �l�ment est un Map.Entry)
		Set lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un it�rateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la table et leur envoyer un �v�nement
		while (objIterateurListe.hasNext() == true)
		{
			// Cr�er une r�f�rence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());
			
			// Si le nom d'utilisateur du joueur courant n'est pas celui
			// qui vient de d�marrer la partie, alors on peut envoyer un 
			// �v�nement � cet utilisateur
			if (objJoueur.obtenirNomUtilisateur().equals(nomUtilisateur) == false)
			{
			    // Obtenir un num�ro de commande pour le joueur courant, cr�er 
			    // un InformationDestination et l'ajouter � l'�v�nement
				joueurDemarrePartie.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            																	 objJoueur.obtenirProtocoleJoueur()));
			}
		}
		
		// Ajouter le nouvel �v�nement cr�� dans la liste d'�v�nements � traiter
		objGestionnaireEvenements.ajouterEvenement(joueurDemarrePartie);
	}
	
	/**
	 * Cette m�thode permet de pr�parer l'�v�nement du d�marrage de partie 
	 * de la table courante. Cette m�thode va passer tous les joueurs 
	 * de la table courante et on va obtenir un num�ro de commande, on va 
	 * cr�er un InformationDestination et on va ajouter l'�v�nement dans 
	 * la file d'�v�nements du gestionnaire d'�v�nements. Lors de l'appel 
	 * de cette fonction, la liste des joueurs est synchronis�e.
	 * 
	 * @param TreeMap : La liste contenant les positions des joueurs
	 * 
	 * Synchronisme : Cette fonction n'est pas synchronis�e ici, mais elle l'est
	 * 				  par l'appelant (demarrerPartie).
	 */
	private void preparerEvenementPartieDemarree(TreeMap listePositionJoueurs)
	{
	    // Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement 
	    // aux joueurs de la table qu'un joueur a d�marr� une partie
	    EvenementPartieDemarree partieDemarree = new EvenementPartieDemarree(intTempsTotal, listePositionJoueurs, objttPlateauJeu);
	    
		// Cr�er un ensemble contenant tous les tuples de la liste 
		// des joueurs de la table (chaque �l�ment est un Map.Entry)
		Set lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un it�rateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un �v�nement
		while (objIterateurListe.hasNext() == true)
		{
			// Cr�er une r�f�rence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());

		    // Obtenir un num�ro de commande pour le joueur courant, cr�er 
		    // un InformationDestination et l'ajouter � l'�v�nement de la 
			// table
			partieDemarree.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            										objJoueur.obtenirProtocoleJoueur()));				
		}
		
		// Ajouter les nouveaux �v�nements cr��s dans la liste d'�v�nements 
		// � traiter
		objGestionnaireEvenements.ajouterEvenement(partieDemarree);
	}
	
	private void preparerEvenementSynchroniser()
	{
		//Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement 
	    // aux joueurs de la table
	    EvenementSynchroniserTemps synchroniser = new EvenementSynchroniserTemps( objMinuterie.obtenirTempsActuel() );
	    
		// Cr�er un ensemble contenant tous les tuples de la liste 
		// des joueurs de la table (chaque �l�ment est un Map.Entry)
		Set lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un it�rateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un �v�nement
		while (objIterateurListe.hasNext() == true)
		{
			// Cr�er une r�f�rence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());

		    // Obtenir un num�ro de commande pour le joueur courant, cr�er 
		    // un InformationDestination et l'ajouter � l'�v�nement de la 
			// table
			synchroniser.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            										objJoueur.obtenirProtocoleJoueur()));				
		}
		
		// Ajouter les nouveaux �v�nements cr��s dans la liste d'�v�nements 
		// � traiter
		objGestionnaireEvenements.ajouterEvenement(synchroniser);
	}
	
	private void preparerEvenementPartieTerminee()
	{
//		Cr�er un nouvel �v�nement qui va permettre d'envoyer l'�v�nement 
	    // aux joueurs de la table
	    EvenementPartieTerminee partieTerminee = new EvenementPartieTerminee( lstJoueurs );
	    
		// Cr�er un ensemble contenant tous les tuples de la liste 
		// des joueurs de la table (chaque �l�ment est un Map.Entry)
		Set lstEnsembleJoueurs = lstJoueurs.entrySet();
		
		// Obtenir un it�rateur pour l'ensemble contenant les joueurs
		Iterator objIterateurListe = lstEnsembleJoueurs.iterator();
		
		// Passer tous les joueurs de la salle et leur envoyer un �v�nement
		while (objIterateurListe.hasNext() == true)
		{
			// Cr�er une r�f�rence vers le joueur humain courant dans la liste
			JoueurHumain objJoueur = (JoueurHumain)(((Map.Entry)(objIterateurListe.next())).getValue());

		    // Obtenir un num�ro de commande pour le joueur courant, cr�er 
		    // un InformationDestination et l'ajouter � l'�v�nement de la 
			// table
			partieTerminee.ajouterInformationDestination(new InformationDestination(objJoueur.obtenirProtocoleJoueur().obtenirNumeroCommande(),
			            										objJoueur.obtenirProtocoleJoueur()));				
		}
		
		// Ajouter les nouveaux �v�nements cr��s dans la liste d'�v�nements 
		// � traiter
		objGestionnaireEvenements.ajouterEvenement(partieTerminee);
	}
	
	public void tempsEcoule()
	{
		arreterPartie();
	}

	public int getObservateurMinuterieId()
	{
		return obtenirNoTable();
	}
	
	public void synchronise()
	{
		synchronized (lstJoueurs)
	    {
			preparerEvenementSynchroniser();
	    }
	}
	
	public int getObservateurSynchroniserId()
	{
		return obtenirNoTable();
	}
}
