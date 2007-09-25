package ServeurJeu.ComposantesJeu.ReglesJeu;

import java.util.TreeSet;

/**
 * @author Jean-François Brind'Amour
 */
public class Regles
{
  // Déclaration d'une liste qui contient les cases de couleur possibles (le 
  // contenu est un objet ReglesCaseCouleur)
  private TreeSet lstCasesCouleurPossibles;
  
  // Déclaration d'une liste qui contient les magasins possibles (le 
  // contenu est un objet ReglesMagasin)
  private TreeSet lstMagasinsPossibles;
  
  // Déclaration d'une liste qui contient les cases spéciales possibles (le 
  // contenu est un objet ReglesCaseSpeciale)
  private TreeSet lstCasesSpecialesPossibles;
  
  // Déclaration d'une liste qui contient les objets utilisables possibles 
  // (le contenu est un objet ReglesObjetUtilisable)
  private TreeSet lstObjetsUtilisablesPossibles;
  
  // Cette variable permet de savoir si on permet le chat ou non
  private boolean bolPermetChat;
  
  // Cette variable va contenir le ratio de trous par rapport au nombre 
  // total de cases
  private float fltRatioTrous;
  
  // Cette variable va contenir le ratio de magasins par rapport au 
  // nombre total de cases
  private float fltRatioMagasins;
  
  // Cette variable va contenir le ratio de cases spéciales par rapport au 
  // nombre total de cases
  private float fltRatioCasesSpeciales;
  
  // Cette variable va contenir le ratio de pièces par rapport au 
  // nombre total de cases
  private float fltRatioPieces;
  
  // Cette variable va contenir le ratio d'objets utilisables par rapport au 
  // nombre total de cases
  private float fltRatioObjetsUtilisables;
  
  // Cette variable va contenir la valeur de la pièce la plus élevée
  private int intValeurPieceMaximale;
  
  // Cette variable va contenir le temps minimal (minutes) d'une partie
  private int intTempsMinimal;
  
  // Cette variable va contenir le temps maximal (minutes) d'une partie
  private int intTempsMaximal;
  
  // Cette variable va contenir le nombre de cases maximales dont le joueur
  // peut se déplacer (minimum de 1)
  private int intDeplacementMaximal;
  
  private int maxPossessionPieceEtObjet;
  
  private int maxObjetsVente;

  
  /**
   * Constructeur de la classe Regles qui permet d'initialiser
   * les règles.
   */
  public Regles()
  {
    // Créer un nouveau comparateur de ReglesObjet
    ReglesComparator objReglesComparator = new ReglesComparator();
    
      // Créer les listes de couleurs possibles, cases spéciales possibles,
    // d'objets utilisables possibles et de magasins possibles
      lstCasesCouleurPossibles = new TreeSet(objReglesComparator);
      lstMagasinsPossibles = new TreeSet(objReglesComparator);
      lstCasesSpecialesPossibles = new TreeSet(objReglesComparator);
      lstObjetsUtilisablesPossibles = new TreeSet(objReglesComparator);
      
      // Initialiser les variables par défaut
      bolPermetChat = true;
      fltRatioTrous = 0.0f;
      fltRatioMagasins = 0.0f;
      fltRatioCasesSpeciales = 0.0f;
      fltRatioPieces = 0.0f;
      fltRatioObjetsUtilisables = 0.0f;
      intValeurPieceMaximale = 0;
      intTempsMinimal = 0;
      intTempsMaximal = 0;
      intDeplacementMaximal = 1;
      maxPossessionPieceEtObjet = 10;
      maxObjetsVente = 4;
  }

  /**
   * Cette fonction permet de retourner la liste des cases de couleur 
   * possibles.
   * 
   * @return TreeSet : La liste des cases de couleur possibles
   */
  public TreeSet obtenirListeCasesCouleurPossibles()
  {
     return lstCasesCouleurPossibles;
  }
  
  /**
   * Cette fonction permet de retourner la liste des magasins possibles.
   * 
   * @return TreeSet : La liste des magasins possibles
   */
  public TreeSet obtenirListeMagasinsPossibles()
  {
     return lstMagasinsPossibles;
  }
  
  /**
   * Cette fonction permet de retourner la liste des cases spéciales 
   * possibles.
   * 
   * @return TreeSet : La liste des cases spéciales possibles
   */
  public TreeSet obtenirListeCasesSpecialesPossibles()
  {
     return lstCasesSpecialesPossibles;
  }
  
  /**
   * Cette fonction permet de retourner la liste des objets utilisables 
   * possibles.
   * 
   * @return TreeSet : La liste des objets utilisables possibles
   */
  public TreeSet obtenirListeObjetsUtilisablesPossibles()
  {
     return lstObjetsUtilisablesPossibles;
  }
  
  /**
   * Cette fonction permet de retourner si oui ou non on permet le chat.
   * 
   * @return boolean : true si on permet le chat
   *           false sinon
   */
  public boolean obtenirPermetChat()
  {
     return bolPermetChat;
  }
  
  /**
   * Cette fonction permet de définir si oui ou non on veut le chat.
   * 
   * @param boolean chat : Permet de savoir si on permet le chat ou non
   */
  public void definirPermetChat(boolean chat)
  {
     bolPermetChat = chat;
  }
  
  /**
   * Cette fonction permet de retourner le ratio de trous.
   * 
   * @return float : Le ratio de trous (de 0 à 1)
   */
  public float obtenirRatioTrous()
  {
     return fltRatioTrous;
  }
  
  /**
   * Cette fonction permet de définir le ratio de trous.
   * 
   * @param float ratio : Le ratio de trous (entre 0 et 1)
   */
  public void definirRatioTrous(float ratio)
  {
    fltRatioTrous = ratio;
  }
  
  /**
   * Cette fonction permet de retourner le ratio de magasins.
   * 
   * @return float : Le ratio de magasins (de 0 à 1)
   */
  public float obtenirRatioMagasins()
  {
     return fltRatioMagasins;
  }
  
  /**
   * Cette fonction permet de définir le ratio de magasins.
   * 
   * @param float ratio : Le ratio de magasins (entre 0 et 1)
   */
  public void definirRatioMagasins(float ratio)
  {
    fltRatioMagasins = ratio;
  }
  
  /**
   * Cette fonction permet de retourner le ratio de cases spéciales.
   * 
   * @return float : Le ratio de cases spéciales (de 0 à 1)
   */
  public float obtenirRatioCasesSpeciales()
  {
     return fltRatioCasesSpeciales;
  }
  
  /**
   * Cette fonction permet de définir le ratio de cases spéciales.
   * 
   * @param float ratio : Le ratio de cases spéciales (entre 0 et 1)
   */
  public void definirRatioCasesSpeciales(float ratio)
  {
    fltRatioCasesSpeciales = ratio;
  }
  
  /**
   * Cette fonction permet de retourner le ratio de pièces.
   * 
   * @return float : Le ratio de pièces (de 0 à 1)
   */
  public float obtenirRatioPieces()
  {
     return fltRatioPieces;
  }
  
  public int obtenirMaxObjet() {
    return maxPossessionPieceEtObjet;
  }
  
  public void definirMaxObjet(int pMax) {
    maxPossessionPieceEtObjet = pMax;
  }
  
  public void definirMaxObjetsVente(int pMax) {
    maxObjetsVente = pMax;
  }
  
  public int obtenirMaxObjetsVente() {
    return maxObjetsVente;
  }
  
  /**
   * Cette fonction permet de définir le ratio de pièces.
   * 
   * @param float ratio : Le ratio de pièces (entre 0 et 1)
   */
  public void definirRatioPieces(float ratio)
  {
    fltRatioPieces = ratio;
  }
  
  /**
   * Cette fonction permet de retourner le ratio d'objets utilisables.
   * 
   * @return float : Le ratio d'objets utilisables (de 0 à 1)
   */
  public float obtenirRatioObjetsUtilisables()
  {
     return fltRatioObjetsUtilisables;
  }
  
  /**
   * Cette fonction permet de définir le ratio d'objets utilisables.
   * 
   * @param float ratio : Le ratio d'objets utilisables (entre 0 et 1)
   */
  public void definirRatioObjetsUtilisables(float ratio)
  {
    fltRatioObjetsUtilisables = ratio;
  }
  
  /**
   * Cette fonction permet de retourner la valeur qu'une pièce maximale 
   * peut avoir durant le jeu.
   * 
   * @return int : La valeur maximale possible qu'une pièce peut avoir
   */
  public int obtenirValeurPieceMaximale()
  {
     return intValeurPieceMaximale;
  }
  
  /**
   * Cette fonction permet de définir la valeur maximale d'une pièce de jeu.
   * 
   * @param int valeur : La valeur maximale d'une pièce
   */
  public void definirValeurPieceMaximale(int valeur)
  {
    intValeurPieceMaximale = valeur;
  }
  
  /**
   * Cette fonction permet de retourner le temps minimal permis 
   * pour une partie en minutes.
   * 
   * @return int : Le temps minimal d'une partie en minutes
   */
  public int obtenirTempsMinimal()
  {
     return intTempsMinimal;
  }
  
  /**
   * Cette fonction permet de définir le temps minimal d'une partie en minutes.
   * 
   * @param int temps : Le temps minimal d'une partie en minutes
   */
  public void definirTempsMinimal(int temps)
  {
    intTempsMinimal = temps;
  }
  
  /**
   * Cette fonction permet de retourner le temps maximal permis 
   * pour une partie en minutes.
   * 
   * @return int : Le temps maximal d'une partie en minutes
   */
  public int obtenirTempsMaximal()
  {
     return intTempsMaximal;
  }
  
  /**
   * Cette fonction permet de définir le temps maximal d'une partie en minutes.
   * 
   * @param int temps : Le temps maximal d'une partie en minutes
   */
  public void definirTempsMaximal(int temps)
  {
    intTempsMaximal = temps;
  }
  
  /**
   * Cette fonction permet de retourner le déplacement maximal permis 
   * pour chaque joueur.
   * 
   * @return int : Le déplacement maximal d'un joueur
   */
  public int obtenirDeplacementMaximal()
  {
     return intDeplacementMaximal;
  }
  
  /**
   * Cette fonction permet de définir le déplacement maximal des joueurs.
   * 
   * @param int deplacement : Le déplacement maximal d'un joueur
   */
  public void definirDeplacementMaximal(int deplacement)
  {
    intDeplacementMaximal = deplacement;
  }
}