class MembreMEJ
{
	private var _prenom:String;
	private var _nom:String;
	private var _sexe:Number;
	private var _ville:String;
	private var _province:String;
	private var _pays:String;
	private var _courriel:String;
	private var _idUtilisateur:String;
	private var _motDePasse:String;
	private var _niveauScolaire:String;
	private var _etablissement:String;
	
	public function MembreMEJ(prenom:String, nom:String, sexe:Number,
							  ville:String, province:String, pays:String,
							  courriel:String, idUtilisateur:String,
							  motDePasse:String, niveauScolaire:String,
							  etablissement:String)
	{
		_prenom = prenom;
		_nom = nom;
		_sexe = sexe;
		_ville = ville;
		_province = province;
		_pays = pays;
		_courriel = courriel;
		_idUtilisateur = idUtilisateur;
		_motDePasse = motDePasse;
		_niveauScolaire = niveauScolaire;
		_etablissement = etablissement;
	}
	
	public function retPrenom()
	{
		return _prenom;
	}
	
	public function retNom()
	{
		return _nom;
	}
	
	public function retIdUtilisateur()
	{
		return _idUtilisateur;
	}
	
	public function retMotDePasse()
	{
		return _motDePasse;
	}
	
	public function traceAttributs()
	{
		trace(_prenom + " " + _nom);
		trace("Nom d'utilisateur: " + _idUtilisateur);
		trace("Mot de passe: " + _motDePasse);
	}
}
	