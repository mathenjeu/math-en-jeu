drop table regles;
CREATE TABLE `regles` (
  `id` INTEGER UNSIGNED NOT NULL DEFAULT NULL AUTO_INCREMENT,
  `chat` BOOLEAN NOT NULL DEFAULT 1,
  `ratio_trou` FLOAT NOT NULL,
  `ratio_magasin` FLOAT NOT NULL,
  `ratio_objet_utilisable` FLOAT NOT NULL,
  `ratio_cases_special` FLOAT NOT NULL,
  `ratio_piece` FLOAT NOT NULL,
  `intervalle_win_the_game` FLOAT NOT NULL,
  PRIMARY KEY (`id`)
)
ENGINE = InnoDB;