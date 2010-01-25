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
	
	public function MembreMEJ(attributs:Array)
	{
		_prenom = attributs[0];
		_nom = attributs[1];
		_sexe = attributs[2];
		_ville = attributs[3];
		_province = attributs[4];
		_pays = attributs[5];
		_courriel = attributs[6];
		_idUtilisateur = attributs[7];
		_motDePasse = attributs[8];
		_niveauScolaire = attributs[9];
		_etablissement = attributs[10];
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
	