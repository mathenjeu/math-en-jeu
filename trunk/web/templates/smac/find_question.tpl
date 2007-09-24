<td width="90%" valign="top" align="left">
<div class="centre">
	<h2>{$lang.gestion_sondage}</h2>
	<form method="get" action="question.php">
		<input type="hidden" name="action" value="chercher" />
		<input type="hidden" name="from" value="0" />
		<input type="hidden" name="limit" value="20" />
		<table>
		<tr>
			<td>Numéro de la Question :</td>
			<td><input type="text" name="cleQuestion"/></td>
		</tr>
		<tr>
			<td>Texte dans la Question :</td>
			<td><input type="text" name="texteQ"/></td>
		</tr>
		<tr>
			<td>Texte dans la réponse :</td>
			<td><input type="text" name="texteR"/></td>
		</tr>
		<tr>
			<td>Catégorie</td>
			<td>
				<select name="categorie">
					<option value="-1">{$lang.question_toute_categorie}</option>
					<option value="0">{$lang.question_categorie0}</option>
					<option value="1">{$lang.question_categorie1}</option>
					<option value="2">{$lang.question_categorie2}</option>
					<option value="3">{$lang.question_categorie3}</option>
					<option value="4">{$lang.question_categorie4}</option>
					<option value="5">{$lang.question_categorie5}</option>
					<option value="6">{$lang.question_categorie6}</option>
					<option value="7">{$lang.question_categorie7}</option>
				</select>
			</td>
		</tr>
		<tr>
			<td>Seulement les questions non valide</td>
			<td><input type="checkbox" name="nonvalide"/></td>
		</tr>
		</table>
		
		<input type="submit" />
	</form>
</div>
</td>