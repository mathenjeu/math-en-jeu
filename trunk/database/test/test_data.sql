truncate table image;
truncate table language; 


truncate table question_info;
truncate table question;
truncate table answer_type;
truncate table category;
truncate table question_info;
truncate table user_subject_level;
truncate table user;

truncate table role_info;
truncate table role;
truncate table school;
truncate table school_type;

truncate table subject_level;
truncate table subject;
truncate table level;


insert into language(name, short_name) values('fran�ais','fr');
insert into language(name, short_name) values('english','en');

truncate table news;
insert into news(`date`,body, title, image, language_id) 
	value(curdate(), 'Nouvelle de test', 'test', 'img/sujet/news.gif' , (select language_id from language l where l.short_name='fr'));
insert into news(`date`,body, title, image, language_id) 
	value(curdate(), 'Test news', 'Test', 'img/sujet/news.gif' , (select language_id from language l where l.short_name='en'));


insert into school_type(name, language_id) 
	values ('primaire',(select language_id from language l where l.short_name='fr'));

insert into school_type(name, language_id) 
	values ('secondaire',(select language_id from language l where l.short_name='fr'));

insert into school_type(name, language_id) 
	values ('collegial',(select language_id from language l where l.short_name='fr'));

insert into school_type(name, language_id) 
	values ('universitaire',(select language_id from language l where l.short_name='fr'));


insert into school(name, school_type_id, city, province, country)
	values ('none', (select school_type_id from school_type where name = 'universitaire'), 'Qu�bec', 'Qu�bec', 'Canada');

insert into school(name, school_type_id, city, province, country)
	values('Universit� Laval', (select school_type_id from school_type where name = 'universitaire'), 'Qu�bec', 'Qu�bec', 'Canada');


truncate table faq;
insert into faq(question, awnser, number, language_id) 
	values ('Test faq 1', 'R�ponse : Test faq 1', 1, (select language_id from language l where l.short_name='fr'));
insert into faq(question, awnser, number, language_id) 
	values ('Test faq 1', 'Awnser : Test faq 1', 1, (select language_id from language l where l.short_name='en'));



insert into role(tag) values('SIMPLE_USER');
insert into role_info(role_id, language_id, name, description) values (1,1,'Simple joueur', 'Un simple joueur sans aucun droit');
insert into role(tag) values('ADMINISTRATOR');
insert into role_info(role_id, language_id, name, description) values (2,1,'Administrateur', 'Un usager avec des droits d\'administrateur');


insert into user (name, last_name, school_id, sexe, username, password, city, country, email, inscription_date, province, email_confirmation_key, role_id)
	values ('Maxime', 'B�gin', (select school_id from school where name = 'Universit� Laval'), 1, 'maxime', password('maxime'), 'Qu�bec', 'Canada', 'maxime.begin@gmail.com', curdate(), 'Qu�bec', '12345', 2);


insert into subject values();

insert into subject_info (subject_id, name, language_id) values(1, 'Math�matique', (select language_id from language l where l.short_name='fr'));
insert into subject_info (subject_id, name, language_id) values(1, 'Mathematics', (select language_id from language l where l.short_name='en'));

insert into subject values();
insert into subject_info (subject_id, name, language_id) values(2, 'Chimie', (select language_id from language l where l.short_name='fr'));

insert into level (name, language_id ) values('Secondaire 1', (select language_id from language l where l.short_name='fr'));
insert into level (name, language_id ) values('Secondaire 2', (select language_id from language l where l.short_name='fr'));
insert into level (name, language_id ) values('Secondaire 3', (select language_id from language l where l.short_name='fr'));
insert into level (name, language_id ) values('Secondaire 4 (416)', (select language_id from language l where l.short_name='fr'));
insert into level (name, language_id ) values('Secondaire 4 (436)', (select language_id from language l where l.short_name='fr'));
insert into level (name, language_id ) values('Secondaire 5 (574)', (select language_id from language l where l.short_name='fr'));
insert into level (name, language_id ) values('Secondaire 5 (536)', (select language_id from language l where l.short_name='fr'));


insert into subject_level (subject_id, level_id) 
	values(1,
		   (select level_id from level where name = "Secondaire 1"));

insert into subject_level (subject_id, level_id) 
	values(1,
		   (select level_id from level where name = "Secondaire 2"));

insert into subject_level (subject_id, level_id) 
	values(1,
		   (select level_id from level where name = "Secondaire 3"));

insert into subject_level (subject_id, level_id) 
	values(1,
		   (select level_id from level where name = "Secondaire 4 (416)"));

insert into subject_level (subject_id, level_id) 
	values(1,
		   (select level_id from level where name = "Secondaire 4 (436)"));

insert into subject_level (subject_id, level_id) 
	values(1,
		   (select level_id from level where name = "Secondaire 5 (574)"));

insert into subject_level (subject_id, level_id) 
	values(1,
		   (select level_id from level where name = "Secondaire 5 (536)"));


insert into subject_level (subject_id, level_id) 
	values(2,
		   (select level_id from level where name = "Secondaire 5 (536)"));


insert into image(user_id, logical_name, physical_name, is_public, description)
    values ((select user_id from user where username='maxime'),
            "test.jpg", "1.jpg", 1, "Une image de test");



insert into category(subject_id) values(1);
insert into category_info(category_id, language_id, name, description, subject_id) values (1, 1 , 'Hstoire des math' , 'Cat�gorie de l\'histoire des math', 1);
insert into category_info(category_id, language_id, name, description, subject_id) values (1, 2 , 'Math History' , 'Math history category', 1);


insert into answer_type(tag) value("MULTIPLE_CHOICE");
insert into answer_type_info(answer_type_id, language_id, name, description) value(1, 1, 'Choix multiple', 'Question � choix multiple(4)');
insert into answer_type_info(answer_type_id, language_id, name, description) value(1, 2, 'Multiple choice', 'Multiple choice question ( 4 choices )');


insert into question(category_id, answer_type_id) values(1, 1);
insert into question_info(question_id, creation_date, language_id, question_latex, good_answer, feedback_latex, is_valid, user_id, is_animated) values(1, NOW(), 1, 'Test','test','test', 1, 1, 0);
insert into question_info(question_id, creation_date, language_id, question_latex, good_answer, feedback_latex, is_valid, user_id, is_animated, question_flash_file) values(1, NOW(), 2, 'Test 2','test 2','test 2', 1, 1, 0, 'test.swf');

insert into comment(question_id, language_id, user_id, datetime, comment ) values(1,1,1,NOW(), "Ceci est un commentaire.");
insert into comment(question_id, language_id, user_id, datetime, comment ) values(1,1,1,NOW(), "Ceci est un autre commentaire.");

insert into comment(question_id, language_id, user_id, datetime, comment ) values(1,2,1,NOW(), "Ceci est un commentaire.");


insert into question_group(language_id, name, description) values (1, 'Groupe test FR', 'Un groupe de test en fran�ais');
insert into question_group(language_id, name, description) values (2, 'Groupe test EN', 'Un groupe de test en anglais');
insert into question_group(language_id, name, description) values (1, 'Groupe test FR 2', 'Un autre groupe de test en fran�ais');


insert into question_group_question(question_group_id, question_id, language_id) values(1, 1, 1);
insert into question_group_question(question_group_id, question_id, language_id) values(2, 1, 2);
insert into question_group_question(question_group_id, question_id, language_id) values(3, 1, 1);

