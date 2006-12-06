
CREATE TABLE `administrateur` (
  `cleAdministrateur` int(11) NOT NULL auto_increment,
  `prenom` text NOT NULL,
  `nom` text NOT NULL,
  `cleEtablissement` int(11) NOT NULL default '0',
  `cleNiveau` tinytext NOT NULL,
  `motDePasse` text NOT NULL,
  `courriel` text NOT NULL,
  `fonction` text,
  `pouvoir` tinyint(4) NOT NULL default '0',
  `cleConfirmation` tinytext NOT NULL,
  `estConfirme` tinyint(4) NOT NULL default '0',
  `alias` text NOT NULL,
  PRIMARY KEY  (`cleAdministrateur`)
) COMMENT='Liste des administrateurs des groupes.';

CREATE TABLE `categoriequestion` (
  `cleCategorie` int(11) NOT NULL auto_increment,
  `cleParent` int(11) default NULL,
  `etiquette` text NOT NULL,
  PRIMARY KEY  (`cleCategorie`)
)COMMENT='Encode l''arborescence des catégories de questions';

CREATE TABLE `choixadminsondage` (
  `cleUtilisateur` int(11) NOT NULL,
  `cleSondage` int(11) NOT NULL,
  `cleReponse` int(11) NOT NULL
) ;


CREATE TABLE `choixjoueursondage` (
  `cleUtilisateur` int(11) NOT NULL,
  `cleSondage` int(11) NOT NULL,
  `cleReponse` int(11) NOT NULL
) ;


CREATE TABLE `etablissement` (
  `cleEtablissement` int(11) NOT NULL auto_increment,
  `nom` tinytext NOT NULL,
  `cleTypeEtablissement` smallint(6) NOT NULL default '0',
  `ville` tinytext NOT NULL,
  `province` tinytext NOT NULL,
  `pays` tinytext NOT NULL,
  PRIMARY KEY  (`cleEtablissement`)
) COMMENT='Liste des ‚tablissements';



CREATE TABLE `faq` (
  `cleFaq` int(11) NOT NULL auto_increment,
  `question` text NOT NULL,
  `reponse` text NOT NULL,
  `numero` tinyint(4) NOT NULL,
  PRIMARY KEY  (`cleFaq`),
  KEY `numero` (`numero`)
) ;


CREATE TABLE `groupe` (
  `cleGroupe` int(11) NOT NULL auto_increment,
  `nom` text NOT NULL,
  `cleAdministrateur` int(11) NOT NULL default '0',
  `cfgDureePartie` int(11) default NULL,
  `cfgClavardagePermis` int(11) NOT NULL default '0',
  `cfgBanqueQuestions` int(11) default NULL,
  PRIMARY KEY  (`cleGroupe`)
) COMMENT='Liste des groupes préconfigurés';


CREATE TABLE `joueur` (
  `cleJoueur` int(11) NOT NULL auto_increment,
  `cleGroupe` int(11) NOT NULL default '0',
  `prenom` tinytext NOT NULL,
  `nom` tinytext NOT NULL,
  `alias` text NOT NULL,
  `motDePasse` tinytext NOT NULL,
  `ville` tinytext,
  `pays` tinytext NOT NULL,
  `adresseCourriel` text NOT NULL,
  `cleNiveau` tinytext NOT NULL,
  `dateInscription` date default NULL,
  `dateDernierAcces` date default NULL,
  `heureDernierAcces` time default NULL,
  `partiesCompletes` int(11) NOT NULL default '0',
  `meilleurPointage` int(11) NOT NULL default '0',
  `tempsPartie` int(11) default NULL,
  `peutCreerSalles` tinyint(4) NOT NULL default '0',
  `sondageQ1` smallint(6) NOT NULL default '0',
  `sondageQ2` smallint(6) NOT NULL default '0',
  `sondageQ3` smallint(6) NOT NULL default '0',
  `sondageQ4` smallint(6) NOT NULL default '0',
  `province` tinytext NOT NULL,
  `cleConfirmation` text NOT NULL,
  `estConfirme` tinyint(4) NOT NULL default '0',
  `cleEtablissement` int(11) NOT NULL default '0',
  `cleAdministrateur` int(11) NOT NULL default '0',
  PRIMARY KEY  (`cleJoueur`)
) COMMENT='Liste des joueurs';


CREATE TABLE `nouvelle` (
  `cleNouvelle` int(11) NOT NULL auto_increment,
  `titre` text NOT NULL,
  `dateNouvelle` date NOT NULL default '0000-00-00',
  `nouvelle` text NOT NULL,
  `destinataire` tinyint(4) NOT NULL default '0',
  `image` text NOT NULL,
  PRIMARY KEY  (`cleNouvelle`),
  KEY `dateNouvelle` (`dateNouvelle`)
) ;


CREATE TABLE `partie` (
  `clePartie` int(11) NOT NULL auto_increment,
  `datePartie` date NOT NULL,
  `heurePartie` time NOT NULL,
  `dureePartie` int(11) NOT NULL,
  PRIMARY KEY  (`clePartie`)
) ;


CREATE TABLE `partiejoueur` (
  `clePartieJoueur` int(11) NOT NULL auto_increment,
  `clePartie` int(11) NOT NULL,
  `cleJoueur` int(11) NOT NULL,
  `pointage` int(11) NOT NULL,
  `gagner` tinyint(4) NOT NULL default '0',
  PRIMARY KEY  (`clePartieJoueur`)
) ;


CREATE TABLE `question` (
  `cleQuestion` int(11) NOT NULL auto_increment,
  `cleSujet` int(11) NOT NULL default '0',
  `generaleAcademique` tinyint(4) NOT NULL default '0',
  `texteASCII` longtext,
  `texteLaTeX` longtext,
  `texteMathML` longtext,
  `reponseAASCII` longtext,
  `reponseALaTeX` longtext,
  `reponseAMathML` longtext,
  `reponseBASCII` longtext,
  `reponseBLaTeX` longtext,
  `reponseBMathML` longtext,
  `reponseCASCII` longtext,
  `reponseCLaTeX` longtext,
  `reponseCMathML` longtext,
  `reponseDASCII` longtext,
  `reponseDLaTeX` longtext,
  `reponseDMathML` longtext,
  `typeReponse` int(11) NOT NULL default '0',
  `bonneReponse` text NOT NULL,
  `simpleElaboree` tinyint(4) NOT NULL default '0',
  `valeurGroupeAge1` tinyint(4) NOT NULL default '0',
  `valeurGroupeAge2` tinyint(4) NOT NULL default '0',
  `valeurGroupeAge3` tinyint(4) NOT NULL default '0',
  `valeurGroupeAge4` tinyint(4) NOT NULL default '0',
  `valeurGroupeAge5` tinyint(4) NOT NULL default '0',
  `valeurGroupeAge6` tinyint(4) NOT NULL default '0',
  `valeurGroupeAge7` tinyint(4) NOT NULL default '0',
  `valeurGroupeAge8` tinyint(4) NOT NULL default '0',
  `valeurGroupeAge9` tinyint(4) NOT NULL default '0',
  `valeurGroupeAge10` tinyint(4) NOT NULL default '0',
  `valeurGroupeAge11` tinyint(4) NOT NULL default '0',
  `valeurGroupeAge12` tinyint(4) NOT NULL default '0',
  `valeurGroupeAge13` tinyint(4) NOT NULL default '0',
  `valeurGroupeAge14` tinyint(4) NOT NULL default '0',
  `FichierFlashQuestion` text,
  `FichierFlashReponse` text,
  PRIMARY KEY  (`cleQuestion`)
) COMMENT='Liste des questions';

CREATE TABLE `reponsesondage` (
  `cleReponse` int(11) NOT NULL auto_increment,
  `cleSondage` int(11) NOT NULL,
  `reponse` text NOT NULL,
  `compteur` int(11) NOT NULL default '0',
  PRIMARY KEY  (`cleReponse`)
) ;


CREATE TABLE `sondage` (
  `cleSondage` int(11) NOT NULL auto_increment,
  `sondage` text NOT NULL,
  `dateSondage` date NOT NULL,
  `nbChoix` tinyint(4) NOT NULL,
  `destinataire` tinyint(4) NOT NULL default '0',
  PRIMARY KEY  (`cleSondage`)
) ;


CREATE TABLE `superadmin` (
  `cleSuperadmin` int(11) NOT NULL auto_increment,
  `nom` tinytext NOT NULL,
  `prenom` tinytext NOT NULL,
  `courriel` text NOT NULL,
  `motDePasse` tinytext NOT NULL,
  PRIMARY KEY  (`cleSuperadmin`)
) COMMENT='Table de super administrateur. Gestion de nouvelles et sonda';


CREATE TABLE `typeetablissement` (
  `cleTypeEtablissement` int(11) NOT NULL auto_increment,
  `identificateurTypeEtablissement` text NOT NULL,
  PRIMARY KEY  (`cleTypeEtablissement`)
) COMMENT='Contient la liste de tous les types d''établissement';
