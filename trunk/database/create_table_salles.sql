drop table salles;
CREATE TABLE `salles` (
  `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `nom` VARCHAR(45) NOT NULL,
  `description` VARCHAR(512) NOT NULL,
  `password` VARCHAR(45) NOT NULL,
  `id_type_jeu` INTEGER UNSIGNED NOT NULL,
  PRIMARY KEY (`id`)
)
ENGINE = InnoDB;


ALTER TABLE `smac_dev`.`salles` CHANGE COLUMN `id_type_jeu` `type_jeu_id` INTEGER UNSIGNED NOT NULL,
 ADD COLUMN `langue_id` VARCHAR(45) NOT NULL AFTER `type_jeu_id`;

ALTER TABLE `smac_dev`.`salles` ADD COLUMN `joueur_id` INTEGER UNSIGNED NOT NULL AFTER `langue_id`;

ALTER TABLE `smac_dev`.`salles` MODIFY COLUMN `password` VARCHAR(45) CHARACTER SET latin1 COLLATE latin1_swedish_ci;

ALTER TABLE `smac_dev`.`salles` ADD COLUMN `regles_id` INTEGER UNSIGNED NOT NULL AFTER `joueur_id`;

