package ServeurJeu.ComposantesJeu.Joueurs;

import java.util.Vector;


import ServeurJeu.Communications.ProtocoleJoueur;
import ServeurJeu.ComposantesJeu.InformationPartie;
import ServeurJeu.ComposantesJeu.Salle;

/**
 * @author Jean-Fran�ois Brind'Amour
 */
public class JoueurHumain extends Joueur
{
	// D�claration d'une r�f�rence vers le protocole du joueur
	private ProtocoleJoueur objProtocoleJoueur;
	
	// Cette variable va contenir le nom d'utilisateur du joueur
	private String strNomUtilisateur;
	
	// Cetta variable contient la cl� de la table joueur
	private int intCleJoueur;
	
	// Cette variable va contenir l'adresse IP du joueur
	private String strAdresseIP;
	
	// Cette variable va contenir le port du joueur
	private String strPort;
	
	// Cette variable va contenir le pr�nom du joueur
	private String strPrenom;
	
	// Cette variable va contenir le nom de famille du joueur
	private String strNomFamille;
	
	//Cette variable d�fini si un jouer peut creer une salle
	private boolean bolPeutCreerSalle;
	
	private String cleNiveau;
	
	// Cette variable d�finit les sujets d�sir�s par le joueur
	private Vector lstNiveauSujet;
	
	// Cette variable d�finit les cat�gories d�sir�es par le joueur
	private Vector lstNiveauCategorie;
	
	// Cette variable indique si le mode avance est choisi
	// Il s'agit du mode dans lequel on indique en detail les choix de niveaux
	private int bolModeAvance;
	
	// constantes servant au choix de mode par l'utilisateur
	public static final int MODE_AVANCE = 1;
	public static final int MODE_NORMAL = 0;
	
	// constantes definies pour declarer les vecteurs NiveauxSujets et NiveauxCategories
	public static final int TAILLE_LISTE_SUJET = 8;
	public static final int TAILLE_LISTE_CATEGORIE = 91;
	
	// D�claration d'une r�f�rence vers la salle dans laquelle le joueur se 
	// trouve (null si le joueur n'est dans aucune salle)
	private Salle objSalleCourante;
	
	// D�claration d'une r�f�rence vers l'objet gardant l'information sur la
	// partie courant de la table o� le joueur se trouve (null si le joueur 
	// n'est dans aucune table)
	private InformationPartie objPartieCourante;
        
	// les compteurs servent � s'assurer que le sujet HISTOIRE
	// n'est pas pige plus souvent que les autres sujets lorsque le 
	// serveur pige une question a poser.
	private int compteurQuestionPosee;
	private int compteurHistoirePosee;
	
	// le seuil est la valeur utilisee dans la methode
	// peutPoserQuestionHistoire(). Elle indique qu'il faut poser
	// SEUIL fois plus de questions que de questions d'histoire.
	private static final int SEUIL_QUESTION_HISTOIRE = 7;
	
	/**
	 * Constructeur de la classe JoueurHumain qui permet d'initialiser les 
	 * membres priv�s du joueur humain et de garder une r�f�rence vers l'objet
	 * permettant de faire la gestion du protocole du joueur
	 * 
	 * @param ProtocoleJoueur protocole : L'objet g�rant le protocole de 
	 * 									  communication du joueur
	 * @param String nomUtilisateur : Le nom d'utilisateur du joueur
	 * @param String adresseIP : L'adresse IP du joueur
	 * @param String port : Le port du joueur
	 * @param boolean peutCreerSalle : Permet de savoir si le joueur peut cr�er
	 * 								   de nouvelles salles
	 */
	public JoueurHumain(ProtocoleJoueur protocole, String nomUtilisateur, String adresseIP, String port) 
	{
		super();

		// Faire la r�f�rence vers le protocole du joueur
		objProtocoleJoueur = protocole;
		
		// Garder en m�moire le nom d'utilisateur, l'adresse IP et le port du
		// joueur
		strNomUtilisateur = nomUtilisateur;
		strAdresseIP = adresseIP;
		strPort = port;
		
		// Initialiser les caract�ristiques du joueur
		strPrenom = "";
		strNomFamille = "";
		bolPeutCreerSalle = false;
		
		bolModeAvance = MODE_NORMAL;
		
		lstNiveauSujet = new Vector(TAILLE_LISTE_SUJET);
		lstNiveauCategorie = new Vector(TAILLE_LISTE_CATEGORIE);
		
		compteurHistoirePosee = 0;
		compteurQuestionPosee = 0;
		
		// Au d�but, le joueur n'est dans aucune salle ni table
		objSalleCourante = null;
		objPartieCourante = null;
	}

	/**
	 * Cette fonction permet de retourner l'objet ProtocoleJoueur qui sert �
	 * ex�cuter le protocole de communication du jeu entre le joueur et le 
	 * serveur.
	 * 
	 * @return ProtocoleJoueur : L'objet ProtocoleJoueur li� au joueur humain
	 */
	public ProtocoleJoueur obtenirProtocoleJoueur()
	{
		return objProtocoleJoueur;
	}
	
	/**
	 * Cette fonction permet de retourner le nom d'utilisateur du joueur.
	 * 
	 * @return String : Le nom d'utilisateur du joueur
	 */
	public String obtenirNomUtilisateur()
	{
		return strNomUtilisateur;
	}
	
	/**
	 * Cette fonction permet de retourner l'adresse IP du joueur.
	 * 
	 * @return String : L'adresse IP du joueur
	 */
	public String obtenirAdresseIP()
	{
		return strAdresseIP;
	}
	
	/**
	 * Cette fonction permet de retourner le port de communication du joueur.
	 * 
	 * @return String : Le port du joueur qui est ouvert
	 */
	public String obtenirPort()
	{
		return strPort;
	}
	
	/**
	 * Cette fonction permet de retourner le pr�nom du joueur.
	 * 
	 * @return String : La pr�nom du joueur
	 */
	public String obtenirPrenom()
	{
		return strPrenom;
	}
	
	/**
	 * Cette fonction permet de retourner si un joueur peut creer une salle.
	 * 
	 * @return boolean : peut ou peut pas creer une salle
	 */
	public boolean obtenirPeutCreerSalle()
	{
		return bolPeutCreerSalle;
	}
	
	/**
	 * Cette m�thode permet de d�finir le pr�nom du joueur.
	 * 
	 * @param String prenom : Le pr�nom du joueur � d�finir
	 */
	public void definirPrenom(String prenom)
	{
		strPrenom = prenom;
	}
	
	/**
	 * Cette fonction permet de retourner le nom de famille du joueur.
	 * 
	 * @return String : Le nom de famille du joueur
	 */
	public String obtenirNomFamille()
	{
		return strNomFamille;
	}

	/**
	 * Cette m�thode permet de d�finir le nom de famille du joueur.
	 * 
	 * @param String prenom : Le nom de famille du joueur � d�finir
	 */
	public void definirNomFamille(String nomFamille)
	{
		strNomFamille = nomFamille;
	}
	
	/**
	 * Cette m�thode permet de d�finir si un joueur peut creer une salle.
	 * 
	 * @param boolean peutCreerSalle : peut ou peux pas creer salle
	 */
	public void definirPeutCreerSalles( boolean peutCreerSalle)
	{
		bolPeutCreerSalle = peutCreerSalle;
	}
	
	/**
	 * Cette fonction permet de retourner la r�f�rence vers la salle dans 
	 * laquelle se trouve le joueur pr�sentement.
	 * 
	 * @return Salle : La salle courante dans laquelle se trouve le joueur.
	 * 				   Si null est retourn�, alors le joueur ne se trouve dans
	 * 				   aucune salle.
	 */
	public Salle obtenirSalleCourante()
	{
		return objSalleCourante;
	}
		
	/**
	 * Cette m�thode permet de d�finir la r�f�rence vers la salle dans laquelle
	 * le joueur se trouve.
	 * 
	 * @param Salle salleCourante : La salle dans laquelle le joueur se
	 * 								trouve pr�sentement. Si la salle est null
	 * 								alors c'est que le joueur n'est dans aucune
	 * 								salle.
	 */
	public void definirSalleCourante(Salle salleCourante)
	{
		objSalleCourante = salleCourante;
	}
	
	/**
	 * Cette fonction permet de retourner la r�f�rence vers l'information sur
	 * la partie courante de la table dans laquelle se trouve le joueur pr�sentement.
	 * 
	 * @return InformationPartie : L'information sur la partie courante du joueur.
	 * 				   Si null est retourn�, alors le joueur ne se trouve dans
	 * 				   aucune table.
	 */
	public InformationPartie obtenirPartieCourante()
	{
		return objPartieCourante;
	}
		
	/**
	 * Cette m�thode permet de d�finir la r�f�rence vers l'information sur la 
	 * partie courante du joueur.
	 * 
	 * @param InformationPartie partieCourante : L'information sur la partie
	 * 					courante du joueur. Si la partie courante est null
	 * 					alors c'est que le joueur n'est dans aucune table
	 */
	public void definirPartieCourante(InformationPartie partieCourante)
	{
		objPartieCourante = partieCourante;
	}
	
	public int obtenirCleJoueur()
	{
		return intCleJoueur;
	}
	
	public void definirCleJoueur(int cle)
	{
		intCleJoueur = cle;
	}

	
	public void enleverObjet(int intIdObjet, String strTypeObjet)
	{
		objPartieCourante.enleverObjet(intIdObjet, strTypeObjet);
	}


	public String obtenirCleNiveau() 
	{
		return cleNiveau;
	}

	public void definirCleNiveau(String cleNiveau) 
	{
		this.cleNiveau = cleNiveau;
	}

	/**
	 * Cette methode permet de retourner la liste des niveaux acad�miques
	 * du joueur pour chaque sujet contenu dans la base de donne�s
	 * 
	 * @return Vector : La liste des niveaux pour chaque sujet
	 */
	public Vector obtenirListeNiveauSujets() 
	{
		return lstNiveauSujet;
	}

	/**
	 * Cette methode permet de definir la liste des niveaux acad�miques
	 * du joueur pour chaque sujet contenu dans la base de donne�s
	 * 
	 * @param Vector lstNiveauSujet : La liste des niveaux pour chaque sujet
	 */
	public void definirListeNiveauSujets(Vector lstNiveauSujet) 
	{
		this.lstNiveauSujet = lstNiveauSujet;
	}
	
	/**
	 * Cette methode permet de retourner la liste des niveaux acad�miques
	 * du joueur pour chaque cat�gorie contenue dans la base de donne�s
	 * 
	 * @return Vector : La liste des niveaux pour chaque cat�gorie
	 */
	public Vector obtenirListeNiveauCategorie() 
	{
		return lstNiveauCategorie;
	}

	/**
	 * Cette methode permet de definir la liste des niveaux acad�miques
	 * du joueur pour chaque cat�gorie contenue dans la base de donne�s
	 * 
	 * @param Vector lstNiveauCategorie : La liste des niveaux pour chaque cat�gorie
	 */
	public void definirListeNiveauCategorie(Vector lstNiveauCategorie) 
	{
		this.lstNiveauCategorie = lstNiveauCategorie;
	}

	/**
	 * Cette fonction permet de retourner le mode de jeu pour les choix de 
	 * questions durant la partie
	 * 
	 * @return boolean : le mode de jeu
	 */
	public int obtenirModeAvance() 
	{
		return bolModeAvance;
	}

	/**
	 * Cette m�thode permet de d�finir si on est en mode avanc�
	 * (influence dans le choix des questions re�ues)
	 * 
	 * @param boolean modeAvance : la valeur du mode choisi
	 */
	public void definirModeAvance(int modeAvance) 
	{
		this.bolModeAvance = modeAvance;
	}
	
	
	
	/**
	 * Cette fonction permet d'incrementer le compteur
	 * gardant en memoire le nombre de questions posees au joueur
	 * 
	 */
	public void incrementeCompteurQuestionPosee() 
	{
		compteurQuestionPosee++;
	}
	
	
	/**
	 * Cette fonction permet d'incrementer le compteur
	 * gardant en memoire le nombre de questions d'histoire posees au joueur
	 * 
	 */
	public void incrementeCompteurHistoirePosee() 
	{
		compteurHistoirePosee++;
	}
	
	
	/**
	 * Cette fonction permet de d�terminer si l'on 
	 * peut poser une question d'histoire.
	 * 
	 * @return boolean : si on peut poser une question d'histoire
	 */
	public boolean peutPoserQuestionHistoire() 
	{
		if(compteurQuestionPosee == 0) 
			return true;
		else 
			return (compteurQuestionPosee / compteurHistoirePosee) >= SEUIL_QUESTION_HISTOIRE ;
	}
	
}
