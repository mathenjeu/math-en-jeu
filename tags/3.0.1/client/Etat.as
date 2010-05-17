/*******************************************************************
Math en jeu
Copyright (C) 2007 Projet SMAC

Ce programme est un logiciel libre ; vous pouvez le
redistribuer et/ou le modifier au titre des clauses de la
Licence Publique Générale Affero (AGPL), telle que publiée par
Affero Inc. ; soit la version 1 de la Licence, ou (à
votre discrétion) une version ultérieure quelconque.

Ce programme est distribué dans l'espoir qu'il sera utile,
mais SANS AUCUNE GARANTIE ; sans même une garantie implicite de
COMMERCIABILITE ou DE CONFORMITE A UNE UTILISATION
PARTICULIERE. Voir la Licence Publique
Générale Affero pour plus de détails.

Vous devriez avoir reçu un exemplaire de la Licence Publique
Générale Affero avec ce programme; si ce n'est pas le cas,
écrivez à Affero Inc., 510 Third Street - Suite 225,
San Francisco, CA 94107, USA.
*********************************************************************/



import ExtendedArray;

// Chaque objet état contient un numéro, une liste des commandes possibles (les
// commandes pouvant être acceptées lorsque le client Flash se trouve dans cet
// état) et une liste des événements acceptables (les événements pouvant être
// acceptés lorsque le client Flash se trouve dans cet état)
class Etat
{
    // Constante objet indiquant que le joueur est déconnecté (non connecté au
    // serveur physiquement)
    public static var NON_CONNECTE:Object = {no:0, listeCommandesPossibles:new Array(), listeEvenementsAcceptables:new Array()};

    // Constante objet indiquant que le joueur est connecté au serveur
    // physiquement, mais qu'on n'est pas connecté (authentifié) au serveur
    // de jeu
    public static var DECONNECTE:Object = {no:1, listeCommandesPossibles:new Array({nom:"Connexion", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array()}), listeEvenementsAcceptables:new Array()};

    // Constante objet indiquant que le joueur est connecté au serveur
    // physiquement et authentifié
    public static var CONNECTE:Object = {no:2, listeCommandesPossibles:new Array({nom:"Musique", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite","NouvelleSalle", "JoueurDemarrePartie", "PartieDemarree", "DemarrerMaintenant"), listeEvenementsAcceptablesApres:new Array("NouvelleSalle")},
	{nom:"Deconnexion", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("NouvelleSalle"), listeEvenementsAcceptablesApres:new Array()},
	{nom:"RejoindrePartie", listeEvenementsAcceptables:new Array("PartieDemarree", "SynchroniserTemps"), listeEvenementsAcceptablesAvant:new Array("NouvelleSalle"), listeEvenementsAcceptablesApres:new Array("PartieDemarree", "SynchroniserTemps", "JoueurDeplacePersonnage", "PartieTerminee")}, 
    {nom:"NePasRejoindrePartie", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("NouvelleSalle"), listeEvenementsAcceptablesApres:new Array()}, 
	{nom:"ObtenirListeJoueurs", listeEvenementsAcceptables:new Array("NouvelleSalle"), listeEvenementsAcceptablesAvant:new Array("NouvelleSalle"), listeEvenementsAcceptablesApres:new Array("JoueurConnecte", "JoueurDeconnecte","NouvelleSalle")}), 
    listeEvenementsAcceptables:new Array("NouvelleSalle")};

    // Constante objet indiquant que le joueur a obtenu la liste des joueurs
    // connectés
    public static var LISTE_JOUEURS_OBTENUE:Object = {no:3, listeCommandesPossibles:new Array({nom:"Deconnexion", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("JoueurConnecte", "JoueurDeconnecte"), listeEvenementsAcceptablesApres:new Array()}, {nom:"ObtenirListeSalles", listeEvenementsAcceptables:new Array("JoueurConnecte", "JoueurDeconnecte"), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array()}), listeEvenementsAcceptables:new Array("JoueurConnecte", "JoueurDeconnecte")};

    // Constante objet indiquant que le joueur a obtenu la liste des salles
    public static var LISTE_SALLES_OBTENUE:Object = {no:4, listeCommandesPossibles:new Array({nom:"Deconnexion", listeEvenementsAcceptables:new Array("NouvelleSalle"), listeEvenementsAcceptablesAvant:new Array("NouvelleSalle","JoueurConnecte", "JoueurDeconnecte"), listeEvenementsAcceptablesApres:new Array()},
    {nom:"CreateRoom", listeEvenementsAcceptables:new Array("NouvelleSalle"), listeEvenementsAcceptablesAvant:new Array("NouvelleSalle","JoueurConnecte", "JoueurDeconnecte"), listeEvenementsAcceptablesApres:new Array("NouvelleSalle")},
	{nom:"getReport", listeEvenementsAcceptables:new Array("NouvelleSalle"), listeEvenementsAcceptablesAvant:new Array("NouvelleSalle","JoueurConnecte", "JoueurDeconnecte"), listeEvenementsAcceptablesApres:new Array("NouvelleSalle")},
	{nom:"ObtenirListeSalles", listeEvenementsAcceptables:new Array("JoueurConnecte", "JoueurDeconnecte" ,"NouvelleSalle"), listeEvenementsAcceptablesAvant:new Array("NouvelleSalle"), listeEvenementsAcceptablesApres:new Array("NouvelleSalle")},
    {nom:"EntrerSalle", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("NouvelleSalle", "JoueurConnecte", "JoueurDeconnecte"), listeEvenementsAcceptablesApres:new Array()},
	{nom:"NouvelleSalle", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("NouvelleSalle", "JoueurConnecte", "JoueurDeconnecte"), listeEvenementsAcceptablesApres:new Array()}),
	listeEvenementsAcceptables:new Array("NouvelleSalle", "JoueurConnecte", "JoueurDeconnecte")};

    // Constante objet indiquant que le joueur est entré dans une salle
    public static var DANS_SALLE:Object = {no:5, listeCommandesPossibles:new Array({nom:"Deconnexion", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array()}, {nom:"QuitterSalle", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array()}, {nom:"ObtenirListeJoueursSalle", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array("JoueurEntreSalle", "JoueurQuitteSalle")}), listeEvenementsAcceptables:new Array()};

    // Constante objet indiquant que le joueur a obtenu la liste des joueurs
    // de la salle
    public static var LISTE_JOUEURS_SALLE_OBTENUE:Object = {no:6, listeCommandesPossibles:new Array({nom:"Deconnexion", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle"), listeEvenementsAcceptablesApres:new Array()}, {nom:"QuitterSalle", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle"), listeEvenementsAcceptablesApres:new Array()}, {nom:"ObtenirListeTables", listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle"), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array("JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite")}), listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle")};

    // Constante objet indiquant que le joueur a obtenu la liste des tables
    public static var LISTE_TABLES_OBTENUE:Object = {no:7, listeCommandesPossibles:new Array(
    {nom:"Deconnexion", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite"), listeEvenementsAcceptablesApres:new Array()}, 
    {nom:"QuitterSalle", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite"), listeEvenementsAcceptablesApres:new Array()},
    {nom:"ObtenirListeTables", listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite"), listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite"), listeEvenementsAcceptablesApres:new Array("JoueurEntreTable")}, 
    {nom:"ObtenirListeJoueursSalle", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array("JoueurEntreSalle", "JoueurQuitteSalle")},
	{nom:"EntrerTable", listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite"), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array("JoueurDemarrePartie", "TableDetruite")}, 
    {nom:"CreerTable", listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite"), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array("JoueurDemarrePartie", "TableDetruite")}), 
    listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "ObtenirListeTables","JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite")};

    // Constante objet indiquant que le joueur est entré dans une table
    public static var DANS_TABLE:Object = {no:8, listeCommandesPossibles:new Array({nom:"Deconnexion", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDemarrePartie"), listeEvenementsAcceptablesApres:new Array()},
	{nom:"QuitterSalle", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDemarrePartie"), listeEvenementsAcceptablesApres:new Array()}, 
	{nom:"QuitterTable", listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite"), listeEvenementsAcceptablesAvant:new Array("JoueurDemarrePartie"), listeEvenementsAcceptablesApres:new Array()}, 
	{nom:"DemarrerPartie", listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDemarrePartie", "JoueurRejoindrePartie"), listeEvenementsAcceptablesAvant:new Array("JoueurDemarrePartie"), listeEvenementsAcceptablesApres:new Array("PartieDemarree", "JoueurDemarrePartie", "JoueurRejoindrePartie")},
	{nom:"ObtenirListeJoueursSalle", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array("JoueurEntreSalle", "JoueurQuitteSalle")}), 
	listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDemarrePartie", "JoueurRejoindrePartie" )};

//TODO: Temporairement, on accepte les événements de joueur déplace ici et la syncro du temps
    // Constante objet indiquant que le joueur est en attente que les autres
    // joueurs démarre la partie
    //public static var ATTENTE_DEBUT_PARTIE:Object = {no:9, listeCommandesPossibles:new Array({nom:"Deconnexion", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDemarrePartie", "PartieDemarree"), listeEvenementsAcceptablesApres:new Array()}, {nom:"QuitterSalle", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDemarrePartie", "PartieDemarree"), listeEvenementsAcceptablesApres:new Array()}, {nom:"QuitterTable", listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite"), listeEvenementsAcceptablesAvant:new Array("JoueurDemarrePartie", "PartieDemarree"), listeEvenementsAcceptablesApres:new Array()}), listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDemarrePartie", "PartieDemarree", "JoueurDeplacePersonnage","SynchroniserTemps", "PartieTerminee")}; //Enlever le dernier

    
    
    public static var ATTENTE_DEBUT_PARTIE:Object = {no:9, listeCommandesPossibles:new Array({nom:"Musique", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDemarrePartie", "PartieDemarree", "DemarrerMaintenant"),listeEvenementsAcceptablesApres:new Array()},
	{nom:"DemarrerMaintenant", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDemarrePartie", "PartieDemarree", "DemarrerMaintenant"), listeEvenementsAcceptablesApres:new Array()}, 
	{nom:"Deconnexion", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDemarrePartie", "PartieDemarree"), listeEvenementsAcceptablesApres:new Array()}, 
	{nom:"QuitterSalle", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDemarrePartie", "PartieDemarree"), listeEvenementsAcceptablesApres:new Array()}, 
    {nom:"QuitterTable", listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite"), listeEvenementsAcceptablesAvant:new Array("JoueurDemarrePartie", "PartieDemarree"), listeEvenementsAcceptablesApres:new Array()},
	{nom:"RejoindrePartie", listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "PartieDemarree", "SynchroniserTemps"), listeEvenementsAcceptablesAvant:new Array("JoueurDemarrePartie", "PartieDemarree"), listeEvenementsAcceptablesApres:new Array("PartieDemarree", "SynchroniserTemps")}, 
    {nom:"ObtenirListeJoueursSalle", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array("JoueurEntreSalle", "JoueurQuitteSalle")}), 
	listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDemarrePartie", "PartieDemarree", "JoueurDeplacePersonnage","SynchroniserTemps", "PartieTerminee", "UtiliserObjet", "JoueurRejoindrePartie" )}; //Enlever le dernier

    
    
    // Constante objet indiquant que la partie du joueur a commencée
    public static var PARTIE_DEMARREE:Object = {no:10, listeCommandesPossibles:new Array(
	{nom:"AcheterObjet", listeEvenementsAcceptables:new Array(), 
	                     listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDeplacePersonnage", "SynchroniserTemps", "PartieTerminee","UtiliserObjet", "JoueurRejoindrePartie" ), 
						 listeEvenementsAcceptablesApres:new Array()}, 
    {nom:"UtiliserObjet", listeEvenementsAcceptables:new Array(), 
	                      listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDeplacePersonnage", "SynchroniserTemps", "PartieTerminee", "UtiliserObjet", "JoueurRejoindrePartie"), 
						  listeEvenementsAcceptablesApres:new Array()}, 
    {nom:"Deconnexion", listeEvenementsAcceptables:new Array(), 
	                    listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDeplacePersonnage", "SynchroniserTemps", "PartieTerminee","UtiliserObjet", "JoueurRejoindrePartie"), 
						listeEvenementsAcceptablesApres:new Array()}, 
    {nom:"QuitterSalle", listeEvenementsAcceptables:new Array(), 
	                     listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDeplacePersonnage", "SynchroniserTemps", "PartieTerminee", "JoueurRejoindrePartie"), 
						 listeEvenementsAcceptablesApres:new Array()}, 
    {nom:"QuitterTable", listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurRejoindrePartie"), 
	                     listeEvenementsAcceptablesAvant:new Array("JoueurDeplacePersonnage","SynchroniserTemps","UtiliserObjet", "JoueurRejoindrePartie"), 
						 listeEvenementsAcceptablesApres:new Array()},
	{nom:"RejoindrePartie", listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDeplacePersonnage","SynchroniserTemps", "PartieTerminee","UtiliserObjet", "JoueurRejoindrePartie"), 
	                        listeEvenementsAcceptablesAvant:new Array(), 
							listeEvenementsAcceptablesApres:new Array("UtiliserObjet", "SynchroniserTemps", "DeplacerPersonnage","JoueurEntreTable", "JoueurQuitteTable", "JoueurDeplacePersonnage", "PartieTerminee", "JoueurRejoindrePartie")}, 
    {nom:"DeplacerPersonnage", listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDeplacePersonnage","SynchroniserTemps", "PartieTerminee","UtiliserObjet", "JoueurRejoindrePartie"), 
	                           listeEvenementsAcceptablesAvant:new Array("UtiliserObjet"), 
							   listeEvenementsAcceptablesApres:new Array("UtiliserObjet", "JoueurRejoindrePartie")}, 
    {nom:"Pointage", listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDeplacePersonnage","SynchroniserTemps", "PartieTerminee","UtiliserObjet", "JoueurRejoindrePartie"), 
	                 listeEvenementsAcceptablesAvant:new Array("UtiliserObjet"), 
					 listeEvenementsAcceptablesApres:new Array("UtiliserObjet", "JoueurRejoindrePartie")}, 
    {nom:"Argent", listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDeplacePersonnage","SynchroniserTemps", "PartieTerminee","UtiliserObjet", "JoueurRejoindrePartie"), 
	               listeEvenementsAcceptablesAvant:new Array(), 
				   listeEvenementsAcceptablesApres:new Array()}), 
	listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDeplacePersonnage","SynchroniserTemps", "PartieTerminee", "UtiliserObjet", "DeplacementWinTheGame", "JoueurRejoindrePartie")};

    // Constante objet indiquant que le joueur doit répondre à une question qui
    // lui a été posée
    
    public static var ATTENTE_REPONSE_QUESTION:Object = {no:11, listeCommandesPossibles:new Array({nom:"Deconnexion", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDeplacePersonnage", "SynchroniserTemps", "PartieTerminee", "JoueurRejoindrePartie"), listeEvenementsAcceptablesApres:new Array()},
       {nom:"UtiliserObjet", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDeplacePersonnage", "SynchroniserTemps", "PartieTerminee", "JoueurRejoindrePartie"), listeEvenementsAcceptablesApres:new Array()}, 
	   {nom:"QuitterSalle", listeEvenementsAcceptables:new Array(), listeEvenementsAcceptablesAvant:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDeplacePersonnage", "SynchroniserTemps", "PartieTerminee", "JoueurRejoindrePartie"), listeEvenementsAcceptablesApres:new Array()}, 
	   {nom:"QuitterTable", listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite"), listeEvenementsAcceptablesAvant:new Array("JoueurDeplacePersonnage", "SynchroniserTemps", "PartieTerminee", "JoueurRejoindrePartie"), listeEvenementsAcceptablesApres:new Array()}, 
	   {nom:"RepondreQuestion", listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDeplacePersonnage", "SynchroniserTemps", "PartieTerminee","UtiliserObjet", "ReportBugQuestion", "JoueurRejoindrePartie"), listeEvenementsAcceptablesAvant:new Array("RejoindrePartie", "ReportBugQuestion"), listeEvenementsAcceptablesApres:new Array("JoueurDeplacePersonnage", "SynchroniserTemps", "PartieTerminee")},
	   {nom:"RejoindrePartie", listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDeplacePersonnage", "SynchroniserTemps", "PartieTerminee","UtiliserObjet", "ReportBugQuestion", "JoueurRejoindrePartie"), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array("JoueurDeplacePersonnage", "SynchroniserTemps", "PartieTerminee")},
	   {nom:"ReportBugQuestion", listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDeplacePersonnage", "SynchroniserTemps", "PartieTerminee","UtiliserObjet", "JoueurRejoindrePartie"), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array()},
	   {nom:"CancelQuestion", listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDeplacePersonnage", "SynchroniserTemps", "PartieTerminee","UtiliserObjet", "JoueurRejoindrePartie"), listeEvenementsAcceptablesAvant:new Array(), listeEvenementsAcceptablesApres:new Array()}), 
	   listeEvenementsAcceptables:new Array("JoueurEntreSalle", "JoueurQuitteSalle", "JoueurEntreTable", "JoueurQuitteTable", "NouvelleTable", "TableDetruite", "JoueurDeplacePersonnage", "SynchroniserTemps", "PartieTerminee", "DeplacementWinTheGame","UtiliserObjet", "JoueurRejoindrePartie")};
    
    
    
    // Déclaration d'un tableau qui va contenir pour chaque numéro d'état l'objet
    // représentant l'état
    private static var lstEtats:Array = new Array(NON_CONNECTE, DECONNECTE, CONNECTE, LISTE_JOUEURS_OBTENUE,
                                                  LISTE_SALLES_OBTENUE, DANS_SALLE, LISTE_JOUEURS_SALLE_OBTENUE,
                                                  LISTE_TABLES_OBTENUE, DANS_TABLE, ATTENTE_DEBUT_PARTIE,
                                                  PARTIE_DEMARREE, ATTENTE_REPONSE_QUESTION);

    /**
     * Cette fonction permet de retourner un tableau contenant toutes les
     * commandes possibles à effectuer à partir de l'état dont le numéro est
     * passé en paramètres.
     *
     * @param Number numeroEtat : Le numéro représentant l'état
     * @return Array : Un tableau contenant les commandes pouvant être
     *                 effectuées
     */
    public static function obtenirCommandesPossibles(numeroEtat:Number):Array
    {
        // Si l'état existe bien, alors on retourne la liste des commandes
        // possibles pour l'état ayant le numéro passé en paramètres
        if (lstEtats[numeroEtat] != undefined)
        {
            return lstEtats[numeroEtat].listeCommandesPossibles;
        }
        else
        {
            // Sinon on retourne un tableau vide
            return new Array();
        }
    }

    /**
     * Cette fonction permet de retourner un tableau contenant tous les
     * événements pouvant être acceptés immédiatement selon l'état courant
     * dont le numéro est passé en paramètres.
     *
     * @param Number numeroEtat : Le numéro représentant l'état
     * @return Array : Un tableau contenant les événements pouvant être
     *                 acceptés
     */
    public static function obtenirEvenementsAcceptablesEtat(numeroEtat:Number):Array
    {
        // Si l'état existe bien, alors on retourne la liste des événements
        // acceptables pour l'état ayant le numéro passé en paramètres
        if (lstEtats[numeroEtat] != undefined)
        {
            return lstEtats[numeroEtat].listeEvenementsAcceptables;
        }
        else
        {
            // Sinon on retourne un tableau vide
            return new Array();
        }
    }

    /**
     * Cette fonction permet de retourner un tableau contenant tous les
     * événements pouvant être acceptés immédiatement selon l'état courant
     * dont le numéro est passé en paramètres et la commande.
     *
     * @param Number numeroEtat : Le numéro représentant l'état
     * @param String commande : Le nom de la commande en traitement
     * @return Array : Un tableau contenant les événements pouvant être
     *                 acceptés immédiatements
     */
    public static function obtenirEvenementsAcceptablesCommande(numeroEtat:Number, commande:String):Array
    {
        // Déclaration d'une référence vers la commande désirée
        var objCommandePossible:Object = obtenirCommande(numeroEtat, commande);

        // Si l'objet commande a été trouvé, alors on va retourner la liste
        // des événements acceptables, sinon on retourne une liste vide
        if (objCommandePossible != null)
        {
            return objCommandePossible.listeEvenementsAcceptables;
        }
        else
        {
            return new Array();
        }
    }

    /**
     * Cette fonction permet de retourner un tableau contenant tous les
     * événements pouvant être acceptés seulement avant une commande selon
     * l'état courant dont le numéro est passé en paramètres et la commande.
     *
     * @param Number numeroEtat : Le numéro représentant l'état
     * @param String commande : Le nom de la commande en traitement
     * @return Array : Un tableau contenant les événements pouvant être
     *                 acceptés avant la commande passée en paramètres
     */
    public static function obtenirEvenementsAcceptablesAvant(numeroEtat:Number, commande:String):Array
    {
        // Déclaration d'une référence vers la commande désirée
        var objCommandePossible:Object = obtenirCommande(numeroEtat, commande);

        // Si l'objet commande a été trouvé, alors on va retourner la liste
        // des événements acceptables, sinon on retourne une liste vide
        if (objCommandePossible != null)
        {
            return objCommandePossible.listeEvenementsAcceptablesAvant;
        }
        else
        {
            return new Array();
        }
    }

    /**
     * Cette fonction permet de retourner un tableau contenant tous les
     * événements pouvant être acceptés seulement après une commande selon
     * l'état courant dont le numéro est passé en paramètres et la commande.
     *
     * @param Number numeroEtat : Le numéro représentant l'état
     * @param String commande : Le nom de la commande en traitement
     * @return Array : Un tableau contenant les événements pouvant être
     *                 acceptés après la commande passée en paramètres
     */
    public static function obtenirEvenementsAcceptablesApres(numeroEtat:Number, commande:String):Array
    {
        // Déclaration d'une référence vers la commande désirée
        var objCommandePossible:Object = obtenirCommande(numeroEtat, commande);

        // Si l'objet commande a été trouvé, alors on va retourner la liste
        // des événements acceptables, sinon on retourne une liste vide
        if (objCommandePossible != null)
        {
            return objCommandePossible.listeEvenementsAcceptablesApres;
        }
        else
        {
            return new Array();
        }
    }

    /**
     * Cette fonction permet de retourner l'objet de commande selon l'état
     * courant dont le numéro est passé en paramètres et selon la commande.
     *
     * @param Number numeroEtat : Le numéro représentant l'état
     * @param String commande : Le nom de la commande en traitement
     * @return Object : Un objet de commande correspondant à la commande passée
     *                  en paramètres
     */
    private static function obtenirCommande(numeroEtat:Number, commande:String):Object
    {
        // Déclaration d'une référence vers la commande désirée
        var objCommandePossible:Object = null;

        // Si l'état existe bien et que la commande est possible pour cet
        // état, alors on retourne l'objet de commande pour l'état ayant
        // le numéro passé en paramètres et pour la commande
        if (lstEtats[numeroEtat] != undefined)
        {
            // Déclaration d'un compteur
            var i:Number = 0;

            // Garder la référence vers l'objet état courant
            var objEtat:Object = lstEtats[numeroEtat];

            // Boucler tant qu'on n'a pas atteint la fin du tableau et qu'on
            // n'a pas trouvé la commande
            while (i < objEtat.listeCommandesPossibles.length && objCommandePossible == null)
            {
                // Si la commande courante dans la liste des commandes
                // possibles est la commande passée en paramètres, alors
                // on va garder la référence vers l'objet de commande
                if (objEtat.listeCommandesPossibles[i].nom == commande)
                {
                    objCommandePossible = objEtat.listeCommandesPossibles[i];
                }

                i++;
            }
        }

        return objCommandePossible;
    }
}
