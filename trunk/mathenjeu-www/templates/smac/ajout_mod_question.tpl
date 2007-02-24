<td valign="top" align="left" height="90%">

{if $mode_question eq "ajout"}
<form name="question" method="post" action="question.php?action=ajout_soumettre">
{else}
<form name="question" method="post" action="question.php?action=mod_soumettre">
{/if}

<table border="0">
<tr>
	<td>
		<div id="operateur">
			<label class="symbole"><u>Opération</u></label>
			<div id="operation">
				<label class="symbole" id="plus" onclick="insertionAuCurseur(document.question.question,'+')"></label>
				<label class="symbole" id="moins" onclick="insertionAuCurseur(document.question.question,'-')"></label>
				<label class="symbole" id="diviser" onclick="insertionAuCurseur(document.question.question,'-:')"></label>
				<label class="symbole" id="multiplier1" onclick="insertionAuCurseur(document.question.question,'*')"></label>
				<label class="symbole" id="multiplier2" onclick="insertionAuCurseur(document.question.question,'xx')"></label>
				<label class="symbole" id="slash" onclick="insertionAuCurseur(document.question.question,'//')"></label>
				<label class="symbole" id="backslash" onclick="insertionAuCurseur(document.question.question,'\\')"></label>
				<label class="symbole" id="sommation" onclick="insertionAuCurseur(document.question.question,'sum')"></label>
				<label class="symbole" id="product" onclick="insertionAuCurseur(document.question.question,'prod')"></label>
			</div>
			<label class="symbole"><u>Relation</u></label>
			<div id="relation">
				<label class="symbole" id="egal" onclick="insertionAuCurseur(document.question.question,'=')"></label>
				<label class="symbole" id="different" onclick="insertionAuCurseur(document.question.question,'!=')"></label>
				<label class="symbole" id="plusPetit" onclick="insertionAuCurseur(document.question.question,'<')"></label>
				<label class="symbole" id="plusGrand" onclick="insertionAuCurseur(document.question.question,'>')"></label>
				<label class="symbole" id="plusPetitEgal" onclick="insertionAuCurseur(document.question.question,'<=')"></label>
				<label class="symbole" id="plusGrandEgal" onclick="insertionAuCurseur(document.question.question,'>=')"></label>
			</div>
		</div>
	</td>
	<td>
		
	</td>
	<td>
		
	</td>
</tr>
<tr>
	<td><label for="titre">Titre :</label></td>
	<td colspan="4"><input size="50" name="titre" onfocus="setElementFocus(this)" /></td>
</tr>
<tr>
	<td colspan="2">
		<label for="question">Question :</label><br>
		<textarea id="question" name="question" rows="15" cols="30"  onfocus="setElementFocus(this)" onkeyup="display(true,this,'outputQuestion');AMviewMathML(this,document.getElementById('outputQuestion'),document.getElementById('outputMLQuestion'));">
		</textarea>
	</td>
	<td valign="top">
		Aperçu de la question : <br>
		<div id="outputQuestion" />
	</td>
	<td>
		<textarea class="outputML" id="outputMLQuestion" rows="15" cols="30" name="outputMLQuestion"></textarea>
	</td>
	
	<td colspan="2">
		<label for="retroaction">Rétroaction :</label><br>
		<textarea id="retroaction" name="retroaction" rows="15" cols="30"  onfocus="setElementFocus(this)" onkeyup="display(true,this,'outputRetroaction');AMviewMathML(this,document.getElementById('outputRetroaction'),document.getElementById('outputMLRetroaction'));">
		</textarea>
	</td>
	<td valign="top">
		Aperçu de la rétroaction : <br>
		<div id="outputRetroaction" />
	</td>
	<td>
		<textarea class="outputML" id="outputMLRetroaction" rows="15" cols="30" name="outputMLRetroaction"></textarea>
	</td>
</tr>
<tr>
	<td><label for="reponse1">Choix de réponse a : </label></td>
	<td><input name="reponse1" onfocus="setElementFocus(this)" onkeyup="display(true,this,'outputReponse1');AMviewMathML(this,document.getElementById('outputReponse1'),document.getElementById('outputMLReponse1'));"/></td>
	<td><div id="outputReponse1" />&nbsp;</td>
	<td><textarea class="outputML" id="outputMLReponse1"></textarea></td>
</tr>
<tr>
	<td><label for="reponse2">Choix de réponse b : </label></td>
	<td><input name="reponse2"  onfocus="setElementFocus(this)" onkeyup="display(true,this,'outputReponse2');AMviewMathML(this,document.getElementById('outputReponse2'),document.getElementById('outputMLReponse2'));" /></td>
	<td><div id="outputReponse2" />&nbsp;</td>
	<td><textarea class="outputML" id="outputMLReponse2"></textarea></td>
</tr>
<tr>
	<td><label for="reponse3">Choix de réponse c : </label></td>
	<td><input name="reponse3"  onfocus="setElementFocus(this)" onkeyup="display(true,this,'outputReponse3');AMviewMathML(this,document.getElementById('outputReponse3'),document.getElementById('outputMLReponse3'));"/></td>
	<td><div id="outputReponse3" />&nbsp;</td>
	<td><textarea class="outputML" id="outputMLReponse3"></textarea></td>
</tr>
<tr>
	<td><label for="reponse4">Choix de réponse d : </label></td>
	<td><input name="reponse4"  onfocus="setElementFocus(this)" onkeyup="display(true,this,'outputReponse4');AMviewMathML(this,document.getElementById('outputReponse4'),document.getElementById('outputMLReponse4'));"/></td>
	<td><div id="outputReponse4" />&nbsp;</td>
	<td><textarea class="outputML" id="outputMLReponse4"></textarea></td>
</tr>
<tr>
	<td>
		<label for="bonneReponse">Bonne Réponse</label>
	</td>
	<td>
		<select name="bonneReponse" />
			<option value="a">a</option>
			<option value="b">b</option>
			<option value="c">c</option>
			<option value="d">d</option>
		</select>
	</td>
</tr>
<tr>
	<td>
		<input type="submit">
	</td>
</tr>
</form>

</table>
<script type="text/javascript">
afficherBouton();
</script>

</td>