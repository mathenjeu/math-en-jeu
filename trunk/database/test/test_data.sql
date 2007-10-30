truncate table user;
truncate table school;
truncate table school_type;
truncate table role;
truncate table subject_level;
truncate table subject;
truncate table level;
truncate table language; 
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
	values('Universit� Laval', (select school_type_id from school_type where name = 'universitaire'), 'Qu�bec', 'Qu�bec', 'Canada');


truncate table faq;
insert into faq(question, awnser, number, language_id) 
	values ('Test faq 1', 'R�ponse : Test faq 1', 1, (select language_id from language l where l.short_name='fr'));
insert into faq(question, awnser, number, language_id) 
	values ('Test faq 1', 'Awnser : Test faq 1', 1, (select language_id from language l where l.short_name='en'));



insert into role(tag) values('SIMPLE_USER');


insert into user (name, last_name, school_id, sexe, username, password, city, country, email, inscription_date, province, email_confirmation_key, role_id)
	values ('Maxime', 'B�gin', (select school_id from school where name = 'Universit� Laval'), 1, 'maxime', password('maxime'), 'Qu�bec', 'Canada', 'maxime.begin@gmail.com', curdate(), 'Qu�bec', '12345', 1);



insert into subject (name, language_id) values('Math�matiques', (select language_id from language l where l.short_name='fr'));
insert into subject (name, language_id) values('Mathematics', (select language_id from language l where l.short_name='en'));

insert into level (name, language_id ) values('Secondaire 1', (select language_id from language l where l.short_name='fr'));
insert into level (name, language_id ) values('Secondaire 2', (select language_id from language l where l.short_name='fr'));
insert into level (name, language_id ) values('Secondaire 3', (select language_id from language l where l.short_name='fr'));
insert into level (name, language_id ) values('Secondaire 4 (416)', (select language_id from language l where l.short_name='fr'));
insert into level (name, language_id ) values('Secondaire 4 (436)', (select language_id from language l where l.short_name='fr'));
insert into level (name, language_id ) values('Secondaire 5 (574)', (select language_id from language l where l.short_name='fr'));
insert into level (name, language_id ) values('Secondaire 5 (536)', (select language_id from language l where l.short_name='fr'));


insert into subject_level (subject_id, level_id) 
	values((select subject_id from subject where name='Math�matiques'),
		   (select level_id from level where name = "Secondaire 1"));

insert into subject_level (subject_id, level_id) 
	values((select subject_id from subject where name='Math�matiques'),
		   (select level_id from level where name = "Secondaire 2"));

insert into subject_level (subject_id, level_id) 
	values((select subject_id from subject where name='Math�matiques'),
		   (select level_id from level where name = "Secondaire 3"));

insert into subject_level (subject_id, level_id) 
	values((select subject_id from subject where name='Math�matiques'),
		   (select level_id from level where name = "Secondaire 4 (416)"));

insert into subject_level (subject_id, level_id) 
	values((select subject_id from subject where name='Math�matiques'),
		   (select level_id from level where name = "Secondaire 4 (436)"));

insert into subject_level (subject_id, level_id) 
	values((select subject_id from subject where name='Math�matiques'),
		   (select level_id from level where name = "Secondaire 5 (574)"));

insert into subject_level (subject_id, level_id) 
	values((select subject_id from subject where name='Math�matiques'),
		   (select level_id from level where name = "Secondaire 5 (536)"));