var gControl = null;

AMinitSymbols();
var AMkeyspressed = 10;

function display(now,inputNode,output) {
   //if (document.getElementById("inputText") != null) {
	if (inputNode != null) {
    	if (AMkeyspressed == 20 || now) {
		    //var str = document.getElementById("inputText").value;
		    var outnode = document.getElementById(output);
		    var str=inputNode.value;
		      
		    var newnode = AMcreateElementXHTML("div");
		    newnode.setAttribute("id",output);
		    outnode.parentNode.replaceChild(newnode,outnode);
		    outnode = document.getElementById(output);
		    var n = outnode.childNodes.length;
		    for (var i = 0; i < n; i++){
		        outnode.removeChild(outnode.firstChild);
		    }
		    outnode.appendChild(document.createComment(str+"``"));
		    AMprocessNode(outnode,true);
		    AMkeyspressed = 0;
		} 
		else {
		 AMkeyspressed++;
		}
	}
}

function AMnode2string(inNode,indent) {
// thanks to James Frazer for contributing an initial version of this function
   var i, str = "";
   if(inNode.nodeType == 1) {
       var name = inNode.nodeName.toLowerCase(); // (IE fix)
       str = "\r" + indent + "<" + name;
       for(i=0; i < inNode.attributes.length; i++)
           if (inNode.attributes[i].nodeValue!="italic" &&
               inNode.attributes[i].nodeValue!="" &&  //stop junk attributes
               inNode.attributes[i].nodeValue!="inherit" && // (mostly IE)
               inNode.attributes[i].nodeValue!=undefined)
               str += " "+inNode.attributes[i].nodeName+"="+
                     "\""+inNode.attributes[i].nodeValue+"\"";
       if (name == "math") 
           str += " xmlns=\"http://www.w3.org/1998/Math/MathML\"";
       str += ">";
       for(i=0; i<inNode.childNodes.length; i++)
           str += AMnode2string(inNode.childNodes[i], indent+"  ");
       if (name != "mo" && name != "mi" && name != "mn") str += "\r"+indent;
       str += "</" + name + ">";
   }
   else if(inNode.nodeType == 3) {
       var st = inNode.nodeValue;
       for (i=0; i<st.length; i++)
           if (st.charCodeAt(i)<32 || st.charCodeAt(i)>126)
               str += "&#"+st.charCodeAt(i)+";";
           else if (st.charAt(i)=="<" && indent != "  ") str += "&lt;";
           else if (st.charAt(i)==">" && indent != "  ") str += "&gt;";
           else if (st.charAt(i)=="&" && indent != "  ") str += "&amp;";
           else str += st.charAt(i);
   }
   return str;
} 

function AMviewMathML(input,mlnode,outputNode) {
  var str = input.value;
  var outstr = AMnode2string(mlnode,"").slice(mlnode.nodeName.length+mlnode.attributes[0].nodeValue.length+9).slice(0,-6);
  
  //enlève le deuxième <math> inutile
  outstr = outstr.slice(0,-105);

  outstr = '<?xml version="1.0" encoding="UTF-8"?>\r\
<html xmlns="http://www.w3.org/1999/xhtml"\r\
xmlns:mml="http://www.w3.org/1998/Math/MathML">\r\
<body>\r'+
outstr+'<\/body>\r<\/html>\r';

	if(outputNode != null)
	{
		outputNode.value=outstr;
	}
}


function setElementFocus(control){
	gControl = control
}


function refreshML(){
	
	display(true,document.getElementById('questionT'),'outputQuestion');
	AMviewMathML(document.getElementById('questionT'),document.getElementById('outputQuestion'),document.getElementById('outputMLQuestion'));
	display(true,document.getElementById('retroaction'),'outputRetroaction');
	AMviewMathML(document.getElementById('retroaction'),document.getElementById('outputRetroaction'),document.getElementById('outputMLRetroaction'));
	display(true,document.getElementById('reponse1'),'outputReponse1');
	AMviewMathML(document.getElementById('reponse1'),document.getElementById('outputReponse1'),document.getElementById('outputMLReponse1'));
	display(true,document.getElementById('reponse2'),'outputReponse2');
	AMviewMathML(document.getElementById('reponse2'),document.getElementById('outputReponse2'),document.getElementById('outputMLReponse2'));
	display(true,document.getElementById('reponse3'),'outputReponse3');
	AMviewMathML(document.getElementById('reponse3'),document.getElementById('outputReponse3'),document.getElementById('outputMLReponse3'));
	display(true,document.getElementById('reponse4'),'outputReponse4');
	AMviewMathML(document.getElementById('reponse4'),document.getElementById('outputReponse4'),document.getElementById('outputMLReponse4'));

}

function insertionAuCurseur(myField, myValue) {
	//IE support
	myField = gControl;
	if (document.selection) {
		myField.focus();
		sel = document.selection.createRange();
		sel.text = myValue;
	}
	//MOZILLA/NETSCAPE support
	else if (myField.selectionStart || myField.selectionStart == '0') {
		var startPos = myField.selectionStart;
		var endPos = myField.selectionEnd;
		myField.value = myField.value.substring(0, startPos)
		+ myValue
		+ myField.value.substring(endPos, myField.value.length);
	} else {
		myField.value += myValue;
	}
	myField.focus();
	refreshML();
	
}

function selectMenuOperateur(obj,menuItem){
	obj.style.display='';
	menuItem.className='titre_symbole_selected';
}

function cacherListeOperateur(){
	document.getElementById('greek').style.display='none';
	document.getElementById('operation').style.display='none';
	document.getElementById('relation').style.display='none';
	document.getElementById('logique').style.display='none';
	document.getElementById('misc').style.display='none';
	document.getElementById('fonction').style.display='none';
	document.getElementById('accent').style.display='none';
	document.getElementById('arrows').style.display='none';
	
	document.getElementById('titre_greek').className='titre_symbole';
	document.getElementById('titre_operation').className='titre_symbole';
	document.getElementById('titre_relation').className='titre_symbole';
	document.getElementById('titre_logique').className='titre_symbole';
	document.getElementById('titre_misc').className='titre_symbole';
	document.getElementById('titre_fonction').className='titre_symbole';
	document.getElementById('titre_accent').className='titre_symbole';
	document.getElementById('titre_arrows').className='titre_symbole';

}

function afficherBouton(){
 
	var symbole = [
	 //opération
	 ['$+$','plus'], ['$-$','moins'], ['$*$','multiplier1'], ['$xx$','multiplier2'],
	 ['$-:$','diviser'], ['$//$','slash'], ['$sum$','sommation'], ['$prod$','product'],
	 //relation
	 ['$=$','egal'], ['$!=$','different'], ['$<$','plusPetit'], ['$>','plusGrand'],
	 ['$<=','plusPetitEgal'], ['$>=','plusGrandEgal'], 
	 ['$in$','in'],['$!in$','notin'],['$sub$','sub'],
	 ['$sup$','sup'],['$sube$','sube'],['$supe$','supe'],
	 ['$-=$','tripleegal'],['$~=$','egalaprox'],['$~~$','aprox'],
				
	 //Lettre grecque
	 ['$alpha$','alpha'],['$beta$','beta'],['$chi$','chi'],['$delta$','delta'],
	 ['$Delta$','Delta'],['$epsilon$','epsilon'],['$varepsilon$','varepsilon'],
	 ['$eta$','eta'],['$gamma$','gamma'],['$Gamma$','Gamma'],
	 ['$iota$','iota'],['$kappa$','kappa'],['$lambda$','lambda'],
	 ['$Lambda$','Lambda'],['$mu$','mu'],['$nu$','nu'],
	 ['$omega$','omega'],['$Omega$','Omega'],['$phi$','phi'],
	 ['$varphi$','varphi'],['$Phi$','Phi'],['$pi$','pi'],
	 ['$Pi$','Pi'],['$psi$','psi'],['$Psi$','Psi'],
	 ['$rho$','rho'],['$sigma$','sigma'],['$Sigma$','Sigma'],
	 ['$tau$','tau'],['$theta$','theta'],['$vartheta$','vartheta'],
	 ['$Theta$','Theta'],['$upsilon$','upsilon'],['$xi$','xi'],
	 ['$Xi$','Xi'],['$zeta$','zeta'],
	 //logique
	 ['$or$','or'],['$and$','and'],['$not','not'],
	 ['$=>$','implication'],['$if$','if'],['$iff$','iff'],
	 ['$AA$','pourtout'],['$EE$','existe'],['$^^$','and2'],
	 ['$vv$','or2'],
	 //divers
	 ['$int$','int'],['$oint$','oint'],['$del$','del'],
	 ['$grad$','grad'],['$+-$','plusmoins'],['$O/$','vide'],
	 ['$oo$','infini'],['$aleph$','aleph'],['$/_$','angle'],
	 ['$NN$','naturel'],['$QQ$','rationel'],['$RR$','reel'],['$ZZ$','entier'],
	 //fonctions standard
	 ['$sin$','sin'],['$cos$','cos'],['$tan$','tan'],
	 ['$csc$','csc'],['$sec$','sec'],['$cot$','cot'],
	 ['$sinh$','sinh'],['$cosh$','cosh'],['$tanh$','tanh'],
	 ['$log$','log'],['$ln$','ln'],['$det$','det'],
	 ['$dim$','dim'],['$lim$','lim'],['$mod$','mod'],
	 ['$min$','min'],['$max$','max'],
	 //accent
	 ['$hat x$','hatx'],['$bar x$','barx'],['$ul x$','ulx'],
	 ['$vec x$','vecx'],['$dot x$','dotx'],['$ddot x$','ddotx'],
	 //arrows
	 ['$uarr$','uarr'],['$darr$','darr'],['$rarr$','rarr'],
	 ['$|->$','smarr'],['$larr$','larr'],['$harr$','harr'],
	 ['$rArr$','rArr'],['$lArr$','lArr'],['$hArr$','hArr']
	 ];


    for(i=0;symbole.length;i++){
		var outnode = document.getElementById(symbole[i][1]);
  		outnode.appendChild(document.createTextNode(symbole[i][0]));
  		AMprocessNode(outnode);
	}
	
	
}