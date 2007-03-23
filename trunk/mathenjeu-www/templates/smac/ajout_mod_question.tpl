<td valign="top" align="left" width="100%" height="90%">

{if $mode_question eq "ajout"}
<form name="question" method="post" action="question.php?action=ajout_soumettre">
{else}
<form name="question" method="post" action="question.php?action=mod_soumettre">
<input type="hidden" name="cleQuestion" value="{$cleQuestion}" />
{/if}

Information importante : <br>
- L'utilisation du navigateur <a href="http://www.firefox.com" target="_blank">Firefox</a> est FORTEMENT recommandée.<br>
- Pour utiliser ce site avec Internet explorer, vous devez installer ce logiciel gratuit : <a target="_blank" href="http://www.dessci.com/en/products/mathplayer/download.htm">Math Player</a> <br>
- Pour définir une formule vous devez la place entre deux $. Par exemple : $sin(x)$ , $25 + 4 = 29$ <br>
- Pour afficher un $. Il faut écrire \\$.<br>

<table border="0">
<tr>
	<td colspan="10">
		<div id="operateur">
			<div class="menu_operateur" id="menu_operateur">
				<label id="titre_greek" class="titre_symbole" onclick="cacherListeOperateur(this);selectMenuOperateur(document.getElementById('greek'),this);">{$lang.question_greek}</label>&nbsp;&nbsp;
				<label id="titre_operation" class="titre_symbole" onclick="cacherListeOperateur(this);selectMenuOperateur(document.getElementById('operation'),this);">{$lang.question_operation}</label>&nbsp;&nbsp;
				<label id="titre_relation" class="titre_symbole" onclick="cacherListeOperateur(this);selectMenuOperateur(document.getElementById('relation'),this);">{$lang.question_relation}</label>&nbsp;&nbsp;
				<label id="titre_logique" class="titre_symbole" onclick="cacherListeOperateur();selectMenuOperateur(document.getElementById('logique'),this);">{$lang.question_logique}</label>&nbsp;&nbsp;
				<label id="titre_misc" class="titre_symbole" onclick="cacherListeOperateur(this);selectMenuOperateur(document.getElementById('misc'),this);">{$lang.question_misc}</label>&nbsp;&nbsp;
				<label id="titre_fonction" class="titre_symbole" onclick="cacherListeOperateur(this);selectMenuOperateur(document.getElementById('fonction'),this);">{$lang.question_fonction}</label>&nbsp;&nbsp;
				<label id="titre_accent" class="titre_symbole" onclick="cacherListeOperateur(this);selectMenuOperateur(document.getElementById('accent'),this);">{$lang.question_accent}</label>&nbsp;&nbsp;
				<label id="titre_arrows" class="titre_symbole" onclick="cacherListeOperateur();selectMenuOperateur(document.getElementById('arrows'),this);">{$lang.question_arrows}</label>&nbsp;&nbsp;
			</div>
			<div id="detail_operateur" class="operateur">
				<div id="greek">
					<span id="alpha" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="beta" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="chi" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="delta" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="Delta" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="epsilon" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="varepsilon" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="eta" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="gamma" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="Gamma" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="iota" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="kappa" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="lambda" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="Lambda" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="mu" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="nu" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="omega" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="Omega" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<br>
					<span id="phi" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="varphi" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="Phi" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="pi" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="Pi" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="psi" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="Psi" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="rho" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="sigma" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="Sigma" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="tau" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="theta" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="vartheta" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="Theta" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="upsilon" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="xi" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="Xi" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
					<span id="zeta" class="symbole" onclick="insertionAuCurseur(document.question.question,this.id)"></span>
				</div>
				<div id="operation">
					<span class="symbole" id="plus" onclick="insertionAuCurseur(document.question.question,'+')"></span>
					<span class="symbole" id="moins" onclick="insertionAuCurseur(document.question.question,'-')" ></span>
					<span class="symbole" id="diviser" onclick="insertionAuCurseur(document.question.question,'-:')" ></span>
					<span class="symbole" id="multiplier1" onclick="insertionAuCurseur(document.question.question,'*')" ></span>
					<span class="symbole" id="multiplier2" onclick="insertionAuCurseur(document.question.question,'xx')" ></span>
					<span class="symbole" id="slash" onclick="insertionAuCurseur(document.question.question,'//')" ></span>
					<span class="symbole" id="sommation" onclick="insertionAuCurseur(document.question.question,'sum')" ></span>
					<span class="symbole" id="product" onclick="insertionAuCurseur(document.question.question,'prod')" ></span>
				</div>
				<div id="relation">
					<span class="symbole" id="egal" onclick="insertionAuCurseur(document.question.question,'=')"></span>
					<span class="symbole" id="different" onclick="insertionAuCurseur(document.question.question,'!=')"></span>
					<span class="symbole" id="plusPetit" onclick="insertionAuCurseur(document.question.question,'<')"></span>
					<span class="symbole" id="plusGrand" onclick="insertionAuCurseur(document.question.question,'>')"></span>
					<span class="symbole" id="plusPetitEgal" onclick="insertionAuCurseur(document.question.question,'<=')"></span>
					<span class="symbole" id="plusGrandEgal" onclick="insertionAuCurseur(document.question.question,'>=')"></span>
					<span class="symbole" id="in" onclick="insertionAuCurseur(document.question.question,'in')"></span>
					<span class="symbole" id="notin" onclick="insertionAuCurseur(document.question.question,'!in')"></span>
					<span class="symbole" id="sub" onclick="insertionAuCurseur(document.question.question,'sub')"></span>
					<span class="symbole" id="sup" onclick="insertionAuCurseur(document.question.question,'sup')"></span>
					<span class="symbole" id="sube" onclick="insertionAuCurseur(document.question.question,'sube')"></span>
					<span class="symbole" id="supe" onclick="insertionAuCurseur(document.question.question,'in')"></span>
					<span class="symbole" id="tripleegal" onclick="insertionAuCurseur(document.question.question,'-=')"></span>
					<span class="symbole" id="egalaprox" onclick="insertionAuCurseur(document.question.question,'~=')"></span>
					<span class="symbole" id="aprox" onclick="insertionAuCurseur(document.question.question,'~~')"></span>
				</div>
				<div id="logique">
					<span class="symbole" id="and" onclick="insertionAuCurseur(document.question.question,'and')"></span>
					<span class="symbole" id="or" onclick="insertionAuCurseur(document.question.question,'or')"></span>
					<span class="symbole" id="not" onclick="insertionAuCurseur(document.question.question,'not')"></span>
					<span class="symbole" id="implication" onclick="insertionAuCurseur(document.question.question,'=>')"></span>
					<span class="symbole" id="if" onclick="insertionAuCurseur(document.question.question,'if')"></span>
					<span class="symbole" id="iff" onclick="insertionAuCurseur(document.question.question,'iff')"></span>
					<span class="symbole" id="pourtout" onclick="insertionAuCurseur(document.question.question,'AA')"></span>
					<span class="symbole" id="existe" onclick="insertionAuCurseur(document.question.question,'EE')"></span>
					<span class="symbole" id="and2" onclick="insertionAuCurseur(document.question.question,'^^')"></span>
					<span class="symbole" id="or2" onclick="insertionAuCurseur(document.question.question,'vv')"></span>
				</div>
				<div id="misc">
					<span class="symbole" id="int" onclick="insertionAuCurseur(document.question.question,'int')"></span>
					<span class="symbole" id="oint" onclick="insertionAuCurseur(document.question.question,'oint')"></span>
					<span class="symbole" id="del" onclick="insertionAuCurseur(document.question.question,'del')"></span>
					<span class="symbole" id="grad" onclick="insertionAuCurseur(document.question.question,'grad')"></span>
					<span class="symbole" id="plusmoins" onclick="insertionAuCurseur(document.question.question,'+-')"></span>
					<span class="symbole" id="vide" onclick="insertionAuCurseur(document.question.question,'\O')"></span>
					<span class="symbole" id="infini" onclick="insertionAuCurseur(document.question.question,'oo')"></span>
					<span class="symbole" id="aleph" onclick="insertionAuCurseur(document.question.question,'aleph')"></span>
					<span class="symbole" id="angle" onclick="insertionAuCurseur(document.question.question,'/_')"></span>
					<span class="symbole" id="naturel" onclick="insertionAuCurseur(document.question.question,'NN')"></span>
					<span class="symbole" id="rationel" onclick="insertionAuCurseur(document.question.question,'QQ')"></span>
					<span class="symbole" id="reel" onclick="insertionAuCurseur(document.question.question,'RR')"></span>
					<span class="symbole" id="entier" onclick="insertionAuCurseur(document.question.question,'ZZ')"></span>
				</div>
				<div id="fonction">
					<span class="symbole" id="sin" onclick="insertionAuCurseur(document.question.question,'int')"></span>
					<span class="symbole" id="cos" onclick="insertionAuCurseur(document.question.question,'cos')"></span>
					<span class="symbole" id="tan" onclick="insertionAuCurseur(document.question.question,'tan')"></span>
					<span class="symbole" id="csc" onclick="insertionAuCurseur(document.question.question,'csc')"></span>
					<span class="symbole" id="sec" onclick="insertionAuCurseur(document.question.question,'sec')"></span>
					<span class="symbole" id="cot" onclick="insertionAuCurseur(document.question.question,'cot')"></span>
					<span class="symbole" id="sinh" onclick="insertionAuCurseur(document.question.question,'sinh')"></span>
					<span class="symbole" id="cosh" onclick="insertionAuCurseur(document.question.question,'cosh')"></span>
					<span class="symbole" id="tanh" onclick="insertionAuCurseur(document.question.question,'tanh')"></span>
					<span class="symbole" id="log" onclick="insertionAuCurseur(document.question.question,'log')"></span>
					<span class="symbole" id="ln" onclick="insertionAuCurseur(document.question.question,'ln')"></span>
					<span class="symbole" id="det" onclick="insertionAuCurseur(document.question.question,'det')"></span>
					<span class="symbole" id="dim" onclick="insertionAuCurseur(document.question.question,'dim')"></span>
					<span class="symbole" id="lim" onclick="insertionAuCurseur(document.question.question,'lim')"></span>
					<span class="symbole" id="mod" onclick="insertionAuCurseur(document.question.question,'mod')"></span>
					<span class="symbole" id="min" onclick="insertionAuCurseur(document.question.question,'min')"></span>
					<span class="symbole" id="max" onclick="insertionAuCurseur(document.question.question,'max')"></span>
				</div>
				<div id="accent">
					<span class="symbole" id="hatx" onclick="insertionAuCurseur(document.question.question,'hat')"></span>
					<span class="symbole" id="barx" onclick="insertionAuCurseur(document.question.question,'bar')"></span>
					<span class="symbole" id="ulx" onclick="insertionAuCurseur(document.question.question,'ul')"></span>
					<span class="symbole" id="vecx" onclick="insertionAuCurseur(document.question.question,'vec')"></span>
					<span class="symbole" id="dotx" onclick="insertionAuCurseur(document.question.question,'dot')"></span>
					<span class="symbole" id="ddotx" onclick="insertionAuCurseur(document.question.question,'ddot')"></span>
				</div>
				<div id="arrows">
					<span class="symbole" id="uarr" onclick="insertionAuCurseur(document.question.question,'uarr')"></span>
					<span class="symbole" id="darr" onclick="insertionAuCurseur(document.question.question,'darr')"></span>
					<span class="symbole" id="rarr" onclick="insertionAuCurseur(document.question.question,'rarr')"></span>
					<span class="symbole" id="smarr" onclick="insertionAuCurseur(document.question.question,'|->')"></span>
					<span class="symbole" id="larr" onclick="insertionAuCurseur(document.question.question,'larr')"></span>
					<span class="symbole" id="harr" onclick="insertionAuCurseur(document.question.question,'harr')"></span>
					<span class="symbole" id="rArr" onclick="insertionAuCurseur(document.question.question,'rArr')"></span>
					<span class="symbole" id="lArr" onclick="insertionAuCurseur(document.question.question,'lArr')"></span>
					<span class="symbole" id="hArr" onclick="insertionAuCurseur(document.question.question,'hArr')"></span>
				</div>
			</div>
		</div>
	</td>
	<td>
		
	</td>
	<td>
		
	</td>
</tr>
<tr>
<tr>
	<td>{$lang.question_categorie}</td>
	<td colspan="4">
		<select name="categorie" >
			<option value="0" {$categorie0}>{$lang.question_categorie0}</option>
			<option value="1" {$categorie1}>{$lang.question_categorie1}</option>
			<option value="2" {$categorie2}>{$lang.question_categorie2}</option>
			<option value="3" {$categorie3}>{$lang.question_categorie3}</option>
			<option value="4" {$categorie4}>{$lang.question_categorie4}</option>
			<option value="5" {$categorie5}>{$lang.question_categorie5}</option>
			<option value="6" {$categorie6}>{$lang.question_categorie6}</option>
			<option value="7" {$categorie7}>{$lang.question_categorie7}</option>
		</select>
	</td>
</tr>
<tr>
	<td>{$lang.question_generale_academique}</td>
	<td colspan="4">
		<select name="generaleAcademique">
			<option value="0" {$generaleAcademique0}>{$lang.question_academique}</option>
			<option value="1" {$generaleAcademique1}>{$lang.question_generale}</option>
		</select>
	</td>
</tr>
<tr>
	<td colspan="4">
		<b>{$lang.question_question_retroaction}</b>
	</d>
</tr>
<tr>
	<td colspan="2">
		<label for="questionT">{$lang.question_question} :</label><br>
		<textarea id="questionT" name="question" rows="10" cols="25"  onfocus="setElementFocus(this)" onkeyup="display(false,this,'outputQuestion');AMviewMathML(this,document.getElementById('outputQuestion'),document.getElementById('outputMLQuestion'));">{$questionAscii}</textarea>
	</td>
	<td valign="top">
		{$lang.question_apercu_question} : <br>
		<span class="symbole" id="outputQuestion" ></span>
	</td>
	<td>
		<textarea class="outputML" id="outputMLQuestion" rows="10" cols="25" name="outputMLQuestion"></textarea>
	</td>
	<td colspan="2">
		<label for="retroaction">{$lang.question_retroaction} :</label><br>
		<textarea id="retroaction" name="retroaction" rows="10" cols="25"  onfocus="setElementFocus(this)" onkeyup="display(false,this,'outputRetroaction');AMviewMathML(this,document.getElementById('outputRetroaction'),document.getElementById('outputMLRetroaction'));">{$retroactionAscii}</textarea>
	</td>
	<td valign="top">
		{$lang.question_apercu_retroaction} : <br>
		<div class="apercu" id="outputRetroaction" />
	</td>
	<td>
		<textarea class="outputML" id="outputMLRetroaction" rows="10" cols="25" name="outputMLRetroaction"></textarea>
	</td>
</tr>
<tr>
	<td colspan="3" valign="top">
		<table border="0">
			<tr>
				<td><b>{$lang.question_reponse}</b></td>
			</tr>
			<tr>
				<td><label for="reponse1">{$lang.question_choix_reponse_a} : </label></td>
			</tr>
			<tr>
				<td><textarea row="1" cols="25" id="reponse1" name="reponse1"onfocus="setElementFocus(this)" onkeyup="display(false,this,'outputReponse1');AMviewMathML(this,document.getElementById('outputReponse1'),document.getElementById('outputMLReponse1'));">{$reponseAASCII}</textarea></td>
				<td><div class="apercu" id="outputReponse1" />&nbsp;</td>
				<td><textarea class="outputML" id="outputMLReponse1" name="outputMLReponse1"></textarea></td>
			</tr>
			<tr>
				<td><label for="reponse2">{$lang.question_choix_reponse_b} : </label></td>
			</tr>
			<tr>
				<td><textarea row="1" cols="25" id="reponse2" name="reponse2" onfocus="setElementFocus(this)" onkeyup="display(false,this,'outputReponse2');AMviewMathML(this,document.getElementById('outputReponse2'),document.getElementById('outputMLReponse2'));" >{$reponseBASCII}</textarea></td>
				<td><div class="apercu" id="outputReponse2" />&nbsp;</td>
				<td><textarea class="outputML" id="outputMLReponse2" name="outputMLReponse2"></textarea></td>
			</tr>
			<tr>
				<td><label for="reponse3">{$lang.question_choix_reponse_c} : </label></td>
			</tr>
			<tr>
				<td><textarea row="1" cols="25" id="reponse3" name="reponse3" onfocus="setElementFocus(this)" onkeyup="display(false,this,'outputReponse3');AMviewMathML(this,document.getElementById('outputReponse3'),document.getElementById('outputMLReponse3'));">{$reponseCASCII}</textarea></td>
				<td><div class="apercu" id="outputReponse3" />&nbsp;</td>
				<td><textarea class="outputML" id="outputMLReponse3" name="outputMLReponse3"></textarea></td>
			</tr>
			<tr>
				<td><label for="reponse4">{$lang.question_choix_reponse_d} : </label></td>
			</tr>
			<tr>
				<td><textarea row="1" cols="25" id="reponse4" name="reponse4" onfocus="setElementFocus(this)" onkeyup="display(false,this,'outputReponse4');AMviewMathML(this,document.getElementById('outputReponse4'),document.getElementById('outputMLReponse4'));">{$reponseDASCII}</textarea></td>
				<td><div class="apercu" id="outputReponse4" />&nbsp;</td>
				<td><textarea class="outputML" id="outputMLReponse4" name="outputMLReponse4"></textarea></td>
			</tr>
			<tr>
				<td>
					<label for="bonneReponse"><b>{$lang.question_bonne_reponse}</b></label>

					<select name="bonneReponse" />
						bonneReponse
						{html_options values=$choixReponse output=$choixReponse selected="$bonneReponse"}
					</select>
				</td>
			</tr>
		</table>
	</td>
	<td colspan="3" valign="top">
		<table>
			<tr>
				<td><b>{$lang.question_niveau_difficulte}</b></td>
			</tr>
			<tr>
				<td valign="top">
					<table>
					<tr>
						<td>{$lang.niveau_1}</td>
						<td>
						<select name="niveau1" />
							{html_options values=$valeurGroupeAge output=$valeurGroupeAge selected="$valeurGroupeAge1"}
						</select>
						</td>
					</tr>
					<tr>
						<td>{$lang.niveau_2}</td>
						<td>
						<select name="niveau2" />
							{html_options values=$valeurGroupeAge output=$valeurGroupeAge selected="$valeurGroupeAge2"}
						</select>
						</td>
					</tr>
					<tr>
						<td>{$lang.niveau_3}</td>
						<td>
						<select name="niveau3" />
							{html_options values=$valeurGroupeAge output=$valeurGroupeAge selected="$valeurGroupeAge3"}
						</select>
					</td>
					</tr>
					<tr>
						<td>{$lang.niveau_4}</td>
						<td>
						<select name="niveau4" />
							{html_options values=$valeurGroupeAge output=$valeurGroupeAge selected="$valeurGroupeAge4"}
						</select>
					</td>
					</tr>
					<tr>
						<td>{$lang.niveau_5}</td>
						<td>
						<select name="niveau5" />
							{html_options values=$valeurGroupeAge output=$valeurGroupeAge selected="$valeurGroupeAge5"}
						</select>
						</td>
					</tr>
					<tr>
						<td>{$lang.niveau_6}</td>
						<td>
						<select name="niveau6" />
							{html_options values=$valeurGroupeAge output=$valeurGroupeAge selected="$valeurGroupeAge6"}
						</select>
						</td>
					</tr>
					</table>
				</td>
				<td>
					<table>
					<tr>
						<td>{$lang.niveau_7}</td>
						<td>
						<select name="niveau7" />
							{html_options values=$valeurGroupeAge output=$valeurGroupeAge selected="$valeurGroupeAge7"}
						</select>
						</td>
					</tr>
					<tr>
						<td>{$lang.niveau_8}</td>
						<td>
						<select name="niveau8" />
							{html_options values=$valeurGroupeAge output=$valeurGroupeAge selected="$valeurGroupeAge8"}
						</select>			
						</td>
					</tr>
					<tr>
						<td>{$lang.niveau_9}</td>
						<td>
						<select name="niveau9" />
							{html_options values=$valeurGroupeAge output=$valeurGroupeAge selected="$valeurGroupeAge9"}
						</select>
						</td>
					</tr>
					<tr>
						<td>{$lang.niveau_10}</td>
						<td>
						<select name="niveau10" />
							{html_options values=$valeurGroupeAge output=$valeurGroupeAge selected="$valeurGroupeAge10"}
						</select>
						</td>
					</tr>
					<tr>
						<td>{$lang.niveau_11}</td>
						<td>
						<select name="niveau11" />
							{html_options values=$valeurGroupeAge output=$valeurGroupeAge selected="$valeurGroupeAge11"}
						</select>
						</td>
					</tr>
					<tr>
						<td>{$lang.niveau_12}</td>
						<td>
						<select name="niveau12" />
							{html_options values=$valeurGroupeAge output=$valeurGroupeAge selected="$valeurGroupeAge12"}
						</select>
						</td>
					</tr>
					<tr>
						<td>{$lang.niveau_13}</td>
						<td>
						<select name="niveau13" />
							{html_options values=$valeurGroupeAge output=$valeurGroupeAge selected="$valeurGroupeAge13"}
						</select>
						</td>
					</tr>
					<tr>
						<td>{$lang.niveau_14}</td>
						<td>
						<select name="niveau14" />
							{html_options values=$valeurGroupeAge output=$valeurGroupeAge selected="$valeurGroupeAge14"}
						</select>
						</td>
					</tr>
					</table>
				</td>
			</tr>
			</table>
	</td>
</tr>
<tr>
	<td>
		<input value="Rafraîchir les aperçus" type="button" onclick="refreshML();" />
	</td>
</tr>
<tr>
	<td>
		<input type="submit" onclick="refreshML();this.disabled=true;document.body.style.cursor='wait';">
	</td>
</tr>
</form>

</table>
<script type="text/javascript">

//mettre à jour le mathml lors du chargement de la page
refreshML();
cacherListeOperateur();
selectMenuOperateur(document.getElementById('greek'),document.getElementById('titre_greek'));
afficherBouton();
	
</script>

</td>