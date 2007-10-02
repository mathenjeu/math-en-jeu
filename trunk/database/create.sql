 
CREATE TABLE `administrateur` (
  `cleAdministrateur` int(11) NOT NULL auto_increment,
  `prenom` text NOT NULL,
  `nom` text NOT NULL,
  `cleEtablissement` int(11) NOT NULL default '0',
  `motDePasse` text NOT NULL,
  `courriel` text NOT NULL,
  `fonction` text,
  `cleNiveau` tinytext NOT NULL,
  `cleConfirmation` tinytext NOT NULL,
  `estConfirme` tinyint(4) NOT NULL default '0',
  `alias` text NOT NULL,
  PRIMARY KEY  (`cleAdministrateur`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=latin1 COMMENT='Liste des administrateurs des groupes.';

 
CREATE TABLE `categoriejoueur` (
  `cleCategorie` int(11) NOT NULL,
  `etiquette` text NOT NULL,
  PRIMARY KEY  (`cleCategorie`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Catégories d''utilisateur, lien avec acces de la table joueur';

 
CREATE TABLE `categoriequestion` (
  `cleCategorie` int(11) NOT NULL,
  `cleParent` int(11) default NULL,
  `etiquette` text NOT NULL,
  PRIMARY KEY  (`cleCategorie`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Encode l''arborescence des catégories de questions';

 
CREATE TABLE `choixadminsondage` (
  `cleUtilisateur` int(11) NOT NULL,
  `cleSondage` int(11) NOT NULL,
  `cleReponse` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

 
CREATE TABLE `choixjoueursondage` (
  `cleUtilisateur` int(11) NOT NULL default '0',
  `cleSondage` int(11) NOT NULL default '0',
  `cleReponse` int(11) NOT NULL default '0'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

 
CREATE TABLE `descriptions` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `description` varchar(512) NOT NULL,
  `langue_id` varchar(45) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

 
CREATE TABLE `equivalenceniveau` (
  `cleUnique` int(11) NOT NULL auto_increment,
  `cleEquivalenceNiveau` int(11) NOT NULL default '0',
  `cleNiveau` int(11) NOT NULL default '0',
  `valeurPonderee` int(11) NOT NULL default '0',
  PRIMARY KEY  (`cleUnique`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Liste des équivalences de niveaux';

 
CREATE TABLE `etablissement` (
  `cleEtablissement` int(11) NOT NULL auto_increment,
  `nom` tinytext NOT NULL,
  `cleTypeEtablissement` smallint(6) NOT NULL default '0',
  `ville` tinytext NOT NULL,
  `province` tinytext NOT NULL,
  `pays` tinytext NOT NULL,
  PRIMARY KEY  (`cleEtablissement`)
) ENGINE=MyISAM AUTO_INCREMENT=9638 DEFAULT CHARSET=latin1 PACK_KEYS=0 COMMENT='Liste des établissements';

 
CREATE TABLE `etiquette` (
  `cleEtiquette` int(11) NOT NULL auto_increment,
  `cleLangue` tinytext NOT NULL,
  `identificateur` text NOT NULL,
  `traduction` text NOT NULL,
  PRIMARY KEY  (`cleEtiquette`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Étiquettes possibles et donne leur équivalent textuel en f';

 
CREATE TABLE `faq` (
  `cleFaq` int(11) NOT NULL auto_increment,
  `question` text NOT NULL,
  `reponse` text NOT NULL,
  `numero` tinyint(4) NOT NULL,
  `cleLangue` tinyint(4) NOT NULL default '0',
  PRIMARY KEY  (`cleFaq`),
  KEY `numero` (`numero`)
) ENGINE=MyISAM AUTO_INCREMENT=17 DEFAULT CHARSET=latin1;

 
CREATE TABLE `groupe` (
  `cleGroupe` int(11) NOT NULL auto_increment,
  `nom` text NOT NULL,
  `cleAdministrateur` int(11) NOT NULL default '0',
  `cfgDureePartie` int(11) default NULL,
  `cfgClavardagePermis` int(11) default NULL,
  `cfgBanqueQuestions` int(11) default NULL,
  PRIMARY KEY  (`cleGroupe`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Liste des groupes préconfigurés';

 
CREATE TABLE `joueur` (
  `cleJoueur` int(11) NOT NULL auto_increment,
  `cleGroupe` int(11) NOT NULL default '0',
  `prenom` tinytext NOT NULL,
  `nom` tinytext NOT NULL,
  `sexe` tinyint(4) default NULL COMMENT '0-féminin, 1-masculin',
  `alias` text NOT NULL,
  `motDePasse` tinytext NOT NULL,
  `ville` tinytext,
  `pays` tinytext NOT NULL,
  `adresseCourriel` text NOT NULL,
  `cleEtablissement` int(11) NOT NULL default '0',
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
  `estConfirme` tinyint(4) NOT NULL default '1',
  `cleAdministrateur` int(11) NOT NULL default '0',
  `cleCategorie` tinyint(4) NOT NULL default '0' COMMENT 'Niveau d''accès (0,1,2,3,4,5)',
  `cleLangue` tinyint(4) NOT NULL default '0',
  PRIMARY KEY  (`cleJoueur`)
) ENGINE=MyISAM AUTO_INCREMENT=4710 DEFAULT CHARSET=latin1 PACK_KEYS=0 COMMENT='Liste des joueurs';

 
CREATE TABLE `joueur_bak` (
  `cleJoueur` int(11) NOT NULL auto_increment,
  `cleGroupe` int(11) NOT NULL default '0',
  `prenom` tinytext NOT NULL,
  `nom` tinytext NOT NULL,
  `alias` text NOT NULL,
  `motDePasse` tinytext NOT NULL,
  `ville` tinytext,
  `pays` tinytext NOT NULL,
  `adresseCourriel` text NOT NULL,
  `cleEtablissement` int(11) NOT NULL default '0',
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
  `estConfirme` tinyint(4) NOT NULL default '1',
  `cleAdministrateur` int(11) NOT NULL default '0',
  PRIMARY KEY  (`cleJoueur`)
) ENGINE=MyISAM AUTO_INCREMENT=626 DEFAULT CHARSET=latin1 PACK_KEYS=0 COMMENT='Liste des joueurs';

 
CREATE TABLE `langues` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `nom` varchar(50) NOT NULL,
  `nom_court` varchar(5) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

 
CREATE TABLE `magasins` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `nom` varchar(45) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC;

 
CREATE TABLE `magasins_objets_utilisable` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `magasin_id` int(10) unsigned NOT NULL,
  `objet_utilisable_id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC;

 
CREATE TABLE `musique_categorie_joueur` (
  `cleJoueur` int(11) NOT NULL,
  `cleCategorie` tinyint(4) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

 
CREATE TABLE `musique_categories` (
  `cleCategorie` tinyint(4) NOT NULL auto_increment,
  `nomCategorie` tinytext NOT NULL,
  PRIMARY KEY  (`cleCategorie`)
) ENGINE=MyISAM AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;

 
CREATE TABLE `musique_fichiers` (
  `cleFichier` int(11) NOT NULL auto_increment,
  `nomFichier` tinytext NOT NULL,
  PRIMARY KEY  (`cleFichier`)
) ENGINE=MyISAM AUTO_INCREMENT=48 DEFAULT CHARSET=latin1;

 
CREATE TABLE `musique_fichiers_categories` (
  `cleFichier` int(11) NOT NULL auto_increment,
  `cleCategorie` tinyint(4) NOT NULL,
  PRIMARY KEY  (`cleFichier`)
) ENGINE=MyISAM AUTO_INCREMENT=48 DEFAULT CHARSET=latin1;

 
CREATE TABLE `niveau` (
  `cleNiveau` int(11) NOT NULL auto_increment,
  `identificateurNiveau` text NOT NULL,
  PRIMARY KEY  (`cleNiveau`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Liste des niveaux (scolaires et de difficulté) du jeu';

 
CREATE TABLE `nouvelle` (
  `cleNouvelle` int(11) NOT NULL auto_increment,
  `dateNouvelle` date NOT NULL default '0000-00-00',
  `nouvelle` text NOT NULL,
  `titre` text NOT NULL,
  `destinataire` tinyint(4) NOT NULL,
  `image` text NOT NULL,
  `cleLangue` tinyint(4) NOT NULL default '0',
  PRIMARY KEY  (`cleNouvelle`),
  KEY `dateNouvelle` (`dateNouvelle`)
) ENGINE=MyISAM AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;

 
CREATE TABLE `objets` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `description_id` int(10) unsigned NOT NULL,
  `tag` varchar(45) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

 
CREATE TABLE `partie` (
  `clePartie` int(11) NOT NULL auto_increment,
  `datePartie` date NOT NULL default '0000-00-00',
  `heurePartie` time NOT NULL default '00:00:00',
  `dureePartie` int(11) NOT NULL default '0',
  PRIMARY KEY  (`clePartie`)
) ENGINE=MyISAM AUTO_INCREMENT=596 DEFAULT CHARSET=latin1;

 
CREATE TABLE `partiejoueur` (
  `clePartieJoueur` int(11) NOT NULL auto_increment,
  `clePartie` int(11) NOT NULL default '0',
  `cleJoueur` int(11) NOT NULL default '0',
  `pointage` int(11) NOT NULL default '0',
  `gagner` tinyint(4) NOT NULL default '0',
  PRIMARY KEY  (`clePartieJoueur`)
) ENGINE=MyISAM AUTO_INCREMENT=6927 DEFAULT CHARSET=latin1;

 
CREATE TABLE `question` (
  `cleQuestion` int(11) NOT NULL auto_increment,
  `cleSujet` int(11) NOT NULL default '0',
  `generaleAcademique` tinyint(4) NOT NULL default '0',
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
  PRIMARY KEY  (`cleQuestion`)
) ENGINE=MyISAM AUTO_INCREMENT=1467 DEFAULT CHARSET=latin1;

 
CREATE TABLE `question_bak` (
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
  `retroactionASCII` longtext,
  `retroactionLaTex` longtext,
  `retroactionMathML` longtext,
  PRIMARY KEY  (`cleQuestion`)
) ENGINE=MyISAM AUTO_INCREMENT=1398 DEFAULT CHARSET=latin1 COMMENT='Liste des questions';

 
CREATE TABLE `question_details` (
  `id` int(11) NOT NULL auto_increment,
  `question_id` int(11) NOT NULL default '0',
  `langue_id` int(11) NOT NULL default '0',
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
  `bonneReponse` text NOT NULL,
  `simpleElaboree` tinyint(4) NOT NULL default '0',
  `FichierFlashQuestion` text,
  `FichierFlashReponse` text,
  `FichierEpsQuestion` text,
  `FichierEpsReponse` text,
  `FichierPsQuestion` text,
  `FichierPsReponse` text,
  `retroactionASCII` longtext,
  `retroactionLaTex` longtext,
  `retroactionMathML` longtext,
  `valide` tinyint(1) NOT NULL default '0' COMMENT 'La question est valide ou non',
  `cleJoueur` int(11) NOT NULL default '0' COMMENT 'Celui qui a crer la question',
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=1305 DEFAULT CHARSET=latin1;

 
CREATE TABLE `question_ok` (
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
  `FichierEpsQuestion` text,
  `FichierEpsReponse` text,
  `FichierPsQuestion` text,
  `FichierPsReponse` text,
  `retroactionASCII` longtext,
  `retroactionLaTex` longtext,
  `retroactionMathML` longtext,
  `valide` tinyint(1) NOT NULL default '0' COMMENT 'La question est valide ou non',
  `cleJoueur` int(11) NOT NULL default '0' COMMENT 'Celui qui a créer la question',
  PRIMARY KEY  (`cleQuestion`)
) ENGINE=MyISAM AUTO_INCREMENT=1445 DEFAULT CHARSET=latin1;

 
CREATE TABLE `question_old` (
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
  `retroactionASCII` longtext,
  `retroactionLaTex` longtext,
  `retroactionMathML` longtext,
  PRIMARY KEY  (`cleQuestion`)
) ENGINE=MyISAM AUTO_INCREMENT=1398 DEFAULT CHARSET=latin1 COMMENT='Liste des questions';

 
CREATE TABLE `questions_anglais` (
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
  `FichierEpsQuestion` text,
  `FichierEpsReponse` text,
  `FichierPsQuestion` text,
  `FichierPsReponse` text,
  `retroactionASCII` longtext,
  `retroactionLaTex` longtext,
  `retroactionMathML` longtext,
  `valide` tinyint(1) NOT NULL default '0' COMMENT 'La question est valide ou non',
  `cleJoueur` int(11) NOT NULL default '0' COMMENT 'Celui qui a créer la question',
  PRIMARY KEY  (`cleQuestion`)
) ENGINE=MyISAM AUTO_INCREMENT=1445 DEFAULT CHARSET=latin1;

 
CREATE TABLE `questions_groups` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `nom` varchar(45) NOT NULL,
  `description` varchar(512) NOT NULL,
  `langue_id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

 
CREATE TABLE `questions_groups_questions` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `questions_group_id` int(10) unsigned NOT NULL,
  `question_id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

 
CREATE TABLE `questions_groups_salles` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `questions_group_id` int(10) unsigned NOT NULL,
  `salle_id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

 
CREATE TABLE `questiontmp` (
  `cleQuestion` int(11) NOT NULL auto_increment,
  `cleAdministrateur` int(11) NOT NULL default '0',
  `fichierQuestion` text,
  `fichierReponse` text,
  `description` text,
  `date` date default NULL,
  PRIMARY KEY  (`cleQuestion`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=latin1 COMMENT='Questions en attente de validation';

 
CREATE TABLE `regles` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `chat` int(10) unsigned NOT NULL default '1',
  `ratio_trou` float NOT NULL,
  `ratio_magasin` float NOT NULL,
  `ratio_objet_utilisable` float NOT NULL,
  `ratio_case_special` float NOT NULL,
  `ratio_piece` float NOT NULL,
  `intervalle_win_the_game` float NOT NULL,
  `max_objets_magasin` int(10) unsigned NOT NULL default '4',
  `valeur_maximal_piece` int(10) unsigned NOT NULL default '25',
  `temps_minimal` int(10) unsigned NOT NULL default '10',
  `temps_maximal` int(10) unsigned NOT NULL default '60',
  `max_possession_objets_pieces` int(10) unsigned NOT NULL default '10',
  `deplacement_maximal` int(10) unsigned NOT NULL default '6',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

 
CREATE TABLE `regles_case_couleur` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `type` int(10) unsigned NOT NULL,
  `priorite` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

 
CREATE TABLE `regles_case_special` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `type` int(10) unsigned NOT NULL,
  `priorite` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

 
CREATE TABLE `reponsesondage` (
  `cleReponse` int(11) NOT NULL auto_increment,
  `cleSondage` int(11) NOT NULL default '0',
  `reponse` text NOT NULL,
  `compteur` int(11) NOT NULL default '0',
  PRIMARY KEY  (`cleReponse`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

 
CREATE TABLE `salle_detail` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `salle_id` int(10) unsigned NOT NULL,
  `description` varchar(512) NOT NULL,
  `langue_id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

 
CREATE TABLE `salles` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `password` varchar(45) default NULL,
  `type_jeu_id` int(10) unsigned NOT NULL,
  `joueur_id` int(10) unsigned NOT NULL,
  `regle_id` int(10) unsigned NOT NULL,
  `officiel` tinyint(3) unsigned NOT NULL,
  `nom` varchar(45) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

 
CREATE TABLE `salles_magasins` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `salle_id` int(10) unsigned NOT NULL,
  `magasin_id` int(10) unsigned NOT NULL,
  `priorite` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

 
CREATE TABLE `salles_objets` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `salle_id` int(10) unsigned NOT NULL,
  `objet_id` int(10) unsigned NOT NULL,
  `priorite` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

 
CREATE TABLE `salles_question` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `salle_id` int(10) unsigned NOT NULL,
  `question_id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

 
CREATE TABLE `sondage` (
  `cleSondage` int(11) NOT NULL auto_increment,
  `sondage` text NOT NULL,
  `dateSondage` date NOT NULL default '0000-00-00',
  `nbChoix` tinyint(4) NOT NULL default '0',
  `destinataire` tinyint(4) NOT NULL,
  `cleLangue` tinyint(4) NOT NULL default '0',
  PRIMARY KEY  (`cleSondage`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

 
CREATE TABLE `superadmin` (
  `cleSuperadmin` int(11) NOT NULL auto_increment,
  `nom` tinytext NOT NULL,
  `prenom` tinytext NOT NULL,
  `courriel` text NOT NULL,
  `motDePasse` tinytext NOT NULL,
  PRIMARY KEY  (`cleSuperadmin`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1 COMMENT='Table de super administrateur. Gestion de nouvelles et sonda';

 
CREATE TABLE `type_jeu` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `nom` varchar(45) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

 
CREATE TABLE `typeetablissement` (
  `cleTypeEtablissement` int(11) NOT NULL auto_increment,
  `identificateurTypeEtablissement` text NOT NULL,
  PRIMARY KEY  (`cleTypeEtablissement`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=latin1 COMMENT='Contient la liste de tous les types d''établissement';

 
CREATE TABLE `typereponse` (
  `cleType` int(11) NOT NULL,
  `nomType` text NOT NULL,
  PRIMARY KEY  (`cleType`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;