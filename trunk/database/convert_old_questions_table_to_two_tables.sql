-- supression de la table question_details
drop table `question_details`;
-- création de la table question_details
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
) ENGINE=MyISAM AUTO_INCREMENT=0 DEFAULT CHARSET=latin1;



-- insertion des données provenant de la table question dans question_details
insert into `question_details`(
  `question_id`,
  `generaleAcademique`,
  `texteASCII`,
  `texteLaTeX`,
  `texteMathML`,
  `reponseAASCII`,
  `reponseALaTeX`,
  `reponseAMathML`,
  `reponseBASCII`,
  `reponseBLaTeX`,
  `reponseBMathML`,
  `reponseCASCII`,
  `reponseCLaTeX`,
  `reponseCMathML`,
  `reponseDASCII`,
  `reponseDLaTeX`,
  `reponseDMathML`,
  `bonneReponse`,
  `simpleElaboree`,
  `FichierFlashQuestion`,
  `FichierFlashReponse`,
  `FichierEpsQuestion`,
  `FichierEpsReponse`,
  `FichierPsQuestion`,
  `FichierPsReponse`,
  `retroactionASCII`,
  `retroactionLaTex`,
  `retroactionMathML`,
  `valide`,
  `cleJoueur`, `langue_id`) select `cleQuestion`,
  `generaleAcademique`,
  `texteASCII`,
  `texteLaTeX`,
  `texteMathML`,
  `reponseAASCII`,
  `reponseALaTeX`,
  `reponseAMathML`,
  `reponseBASCII`,
  `reponseBLaTeX`,
  `reponseBMathML`,
  `reponseCASCII`,
  `reponseCLaTeX`,
  `reponseCMathML`,
  `reponseDASCII`,
  `reponseDLaTeX`,
  `reponseDMathML`,
  `bonneReponse`,
  `simpleElaboree`,
  `FichierFlashQuestion`,
  `FichierFlashReponse`,
  `FichierEpsQuestion`,
  `FichierEpsReponse`,
  `FichierPsQuestion`,
  `FichierPsReponse`,
  `retroactionASCII`,
  `retroactionLaTex`,
  `retroactionMathML`,
  `valide`, cleJoueur, (select id from langue where nom_court = 'fr') from question;


-- todo : insertion des données provenant de la table question_anglais dans question_details



-- modification des noms de fichier pour include la langue

update question_details
set `FichierFlashQuestion` = (select concat('Q-',question_id,'-',nom_court, '.swf') from langue where langue.id = langue_id)
where `FichierFlashQuestion` is not null;

update question_details
set `FichierFlashReponse` = (select concat('R-',question_id,'-',nom_court, '.swf') from langue where langue.id = langue_id )
where `FichierFlashReponse` is not null;

update question_details
set `FichierEpsQuestion` = (select concat('Q-',question_id,'-',nom_court, '.eps') from langue where langue.id = langue_id )
where `FichierEpsQuestion` is not null;

update question_details
set `FichierEpsReponse` = (select concat('R-',question_id,'-',nom_court, '.eps') from langue where langue.id = langue_id )
where `FichierEpsReponse` is not null;

update question_details
set `FichierPsQuestion` = (select concat('Q-',question_id,'-',nom_court, '.ps') from langue where langue.id = langue_id )
where `FichierPsQuestion` is not null;

update question_details
set `FichierPsReponse` = (select concat('R-',question_id,'-',nom_court, '.ps') from langue where langue.id = langue_id )
where `FichierPsReponse` is not null;



-- enlever les colonnes inutile dans la table question
ALTER TABLE `question` DROP COLUMN `texteASCII`,
 DROP COLUMN `texteLaTeX`,
 DROP COLUMN `texteMathML`,
 DROP COLUMN `reponseAASCII`,
 DROP COLUMN `reponseALaTeX`,
 DROP COLUMN `reponseAMathML`,
 DROP COLUMN `reponseBASCII`,
 DROP COLUMN `reponseBLaTeX`,
 DROP COLUMN `reponseBMathML`,
 DROP COLUMN `reponseCASCII`,
 DROP COLUMN `reponseCLaTeX`,
 DROP COLUMN `reponseCMathML`,
 DROP COLUMN `reponseDASCII`,
 DROP COLUMN `reponseDLaTeX`,
 DROP COLUMN `reponseDMathML`,
 DROP COLUMN `FichierFlashQuestion`,
 DROP COLUMN `FichierFlashReponse`,
 DROP COLUMN `FichierEpsQuestion`,
 DROP COLUMN `FichierEpsReponse`,
 DROP COLUMN `FichierPsQuestion`,
 DROP COLUMN `FichierPsReponse`,
 DROP COLUMN `retroactionASCII`,
 DROP COLUMN `retroactionLaTex`,
 DROP COLUMN `retroactionMathML`,
 DROP COLUMN `valide`,
 DROP COLUMN `cleJoueur`;




