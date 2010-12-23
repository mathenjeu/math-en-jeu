package ServeurJeu.ComposantesJeu.Joueurs;

import ServeurJeu.Communications.ProtocoleJoueur;
import ServeurJeu.ComposantesJeu.InformationPartie;
import ServeurJeu.ComposantesJeu.Salle;

/**
 * @author Jean-François Brind'Amour
 */
public class JoueurHumain extends Joueur
{
	// Déclaration d'une référence vers le protocole du joueur
	private ProtocoleJoueur objProtocoleJoueur;
	
	// Cette variable va contenir le nom d'utilisateur du joueur
	private final String strNomUtilisateur;
	
	// It's player id in the DB
	private int intCleJoueur;

        // La langue du joueur: "fr" ou "en"
        private String strLangue;
	
	// Cette variable va contenir l'adresse IP du joueur
	private final String strAdresseIP;
	
	// Cette variable va contenir le port du joueur
	private final String strPort;
	
	// Cette variable va contenir le prénom du joueur
	private String strPrenom;
	
	// Cette variable va contenir le nom de famille du joueur
	private String strNomFamille;
	
	// Used to distinguish between simple user and administrator
	// 1 - simple user 
	// 2 - admin
	// 3 - prof
	private int role;
	
	/**
	 * Déclaration d'un variable qui contient la valeur du niveau du joueur 
	 * valeurs possibles -> entre 1 et 18
	 */
	private int cleNiveau;
	
	// Déclaration d'une référence vers la salle dans laquelle le joueur se 
	// trouve (null si le joueur n'est dans aucune salle)
	private  Salle objSalleCourante;
	
	// Déclaration d'une référence vers l'objet gardant l'information sur la
	// partie courant de la table où le joueur se trouve (null si le joueur 
	// n'est dans aucune table)
	private InformationPartie objPartieCourante;
	        
	/**
	 * Constructeur de la classe JoueurHumain qui permet d'initialiser les 
	 * membres privés du joueur humain et de garder une référence vers l'objet
	 * permettant de faire la gestion du protocole du joueur
	 * 
	 * @param protocole L'objet gérant le protocole de communication du joueur
	 * @param nomUtilisateur : Le nom d'utilisateur du joueur
	 * @param adresseIP : L'adresse IP du joueur
	 * @param port : Le port du joueur
	 */
	public JoueurHumain(ProtocoleJoueur protocole, String nomUtilisateur, String adresseIP, String port) 
	{
		super();

		// Faire la référence vers le protocole du joueur
		objProtocoleJoueur = protocole;
		
		// Garder en mémoire le nom d'utilisateur, l'adresse IP et le port du
		// joueur
		strNomUtilisateur = nomUtilisateur;
		strAdresseIP = adresseIP;
		strPort = port;
		
		// Initialiser les caractéristiques du joueur
		//strPrenom = "";
		//strNomFamille = "";
                strLangue = "fr";
				
	}

	/**
	 * Cette fonction permet de retourner l'objet ProtocoleJoueur qui sert à
	 * exécuter le protocole de communication du jeu entre le joueur et le 
	 * serveur.
	 * 
	 * @return ProtocoleJoueur : L'objet ProtocoleJoueur lié au joueur humain
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
	 * Cette fonction permet de retourner le prénom du joueur.
	 * 
	 * @return String : La prénom du joueur
	 */
	public String obtenirPrenom()
	{
		return strPrenom;
	}
	
		
	/**
	 * Cette méthode permet de définir le prénom du joueur.
	 * 
	 * @param prenom Le prénom du joueur à définir
	 */
	public void definirPrenom(String prenom)
	{
		strPrenom = prenom;
	}
	
	/**
	 * Cette fonction permet de retourner le nom de famille du joueur.
	 * 
	 * @return Le nom de famille du joueur
	 */
	public String obtenirNomFamille()
	{
		return strNomFamille;
	}

	/**
	 * Cette méthode permet de définir le nom de famille du joueur.
	 * 
	 * @param nomFamille Le nom de famille du joueur à définir
	 */
	public void definirNomFamille(String nomFamille)
	{
		strNomFamille = nomFamille;
	}
	
		
	/**
	 * Cette fonction permet de retourner la référence vers la salle dans 
	 * laquelle se trouve le joueur présentement.
	 * 
	 * @return La salle courante dans laquelle se trouve le joueur.
         *         Si null est retourné, alors le joueur ne se trouve dans
         *         aucune salle.
	 */
	public Salle obtenirSalleCourante()
	{
		return objSalleCourante;
	}
		
	/**
	 * Cette méthode permet de définir la référence vers la salle dans laquelle
	 * le joueur se trouve.
	 * 
	 * @param salleCourante La salle dans laquelle le joueur se trouve
         *        présentement. Si la salle est null alors c'est que le joueur
         *        n'est dans aucune salle.
	 */
	public void definirSalleCourante(Salle salleCourante)
	{
		objSalleCourante = salleCourante;
	}
	
	/**
	 * Cette fonction permet de retourner la référence vers l'information sur
	 * la partie courante de la table dans laquelle se trouve le joueur présentement.
	 * 
	 * @return InformationPartie : L'information sur la partie courante du joueur.
	 * 				   Si null est retourné, alors le joueur ne se trouve dans
	 * 				   aucune table.
	 */
	public InformationPartie obtenirPartieCourante()
	{
		return objPartieCourante;
	}
	
	public int obtenirCleJoueur()
	{
		return intCleJoueur;
	}
	
	public void definirCleJoueur(int cle)
	{
		intCleJoueur = cle;
	}

        public String obtenirLangue()
        {
            return strLangue;
        }

        public void definirLangue(String lang)
        {
            strLangue = lang;
            if (!lang.equals("fr") && !lang.equals("en"))
                strLangue = "fr";
        }

	
	public void enleverObjet(int intIdObjet, String strTypeObjet)
	{
		objPartieCourante.enleverObjet(intIdObjet, strTypeObjet);
	}

    
	public int obtenirCleNiveau() 
	{
		return cleNiveau;
	}

	public void definirCleNiveau(int cleNiveau) 
	{
		this.cleNiveau = cleNiveau;
	} 
	
	/**
	 * @return the role
	 */
	public int getRole() {
		return role;
	}

	/**
	 * @param role the role to set
	 */
	public void setRole(int role) {
		this.role = role;
	}

	/**
	 * Cette méthode permet de définir la référence vers l'information sur la 
	 * partie courante du joueur.
	 * 
	 * @param partieCourante L'information sur la partie courante du joueur.
         *        Si la partie courante est null alors c'est que le joueur n'est
         *        dans aucune table.
	 */
	public void definirPartieCourante(InformationPartie partieCourante)
	{
		objPartieCourante = partieCourante;
	}

	
	public void setObjProtocoleJoueur(ProtocoleJoueur protocoleJoueur) {
		objProtocoleJoueur = protocoleJoueur;
		
	}
}
