package ServeurJeu.ComposantesJeu;

public class Langue {
  private int id;
  private String nom;
  private String nomCourt;
  
  
  public Langue(int pId, String pNom, String pNomCourt) {    
    super();
    id = pId;
    nom = pNom;
    nomCourt = pNomCourt;
  }
  
  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }
  public String getNom() {
    return nom;
  }
  public void setNom(String nom) {
    this.nom = nom;
  }
  public String getNomCourt() {
    return nomCourt;
  }
  public void setNomCourt(String nomCourt) {
    this.nomCourt = nomCourt;
  }
  
  
}
