package ServeurJeu.ComposantesJeu;

public class Langue2 {
  private int id;
  private String nom;
  private String nomCourt;
  
  
  public Langue2(int pId, String pNom, String pNomCourt) {
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
