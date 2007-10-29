truncate table user;
truncate table school;
truncate table school_type;
truncate table role;

truncate table language; 
insert into language(name, short_name) values('français','fr');
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
	values('Université Laval', (select school_type_id from school_type where name = 'universitaire'), 'Québec', 'Québec', 'Canada');


truncate table faq;
insert into faq(question, awnser, number, language_id) 
	values ('Test faq 1', 'Réponse : Test faq 1', 1, (select language_id from language l where l.short_name='fr'));
insert into faq(question, awnser, number, language_id) 
	values ('Test faq 1', 'Awnser : Test faq 1', 1, (select language_id from language l where l.short_name='en'));



insert into role(tag) values('SIMPLE_USER');


insert into user (name, last_name, school_id, sexe, username, password, city, country, email, inscription_date, province, email_confirmation_key, role_id)
	values ('Maxime', 'Bégin', (select school_id from school where name = 'Université Laval'), 1, 'maxime', password('maxime'), 'Québec', 'Canada', 'maxime.begin@gmail.com', curdate(), 'Québec', '12345', 1);
