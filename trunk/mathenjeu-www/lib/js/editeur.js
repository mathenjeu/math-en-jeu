var gControl = null;

AMinitSymbols();
var AMkeyspressed = 20;

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
  var outstr = AMnode2string(mlnode,"").slice(mlnode.nodeName.length+23).slice(0,-6);
  //document.write(input.value);

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
}

function afficherCacher(control){
	if(control.style.display == '')
	{
		control.style.display = 'none';
	}
	else
	{
		control.style.display = '';
	}
}

function afficherBouton(){
 	mathcolor = "red"; 
 	
 	
	var symbole = [
	 //opération
	 ['$+$','plus'],
	 ['$-$','moins'],
	 ['$*$','multiplier1'],
	 ['$xx$','multiplier2'],
	 ['$-:$','diviser'],
	 ['$//$','slash'],
	 ['$\\\13$','backslash'],
	 ['$sum$','sommation'],
	 ['$prod$','product'],
	 //relation
	 ['$=$','egal'],
	 ['$!=$','different'],
	 ['$<$','plusPetit'],
	 ['$>','plusGrand'],
	 ['$<=','plusPetitEgal'],
	 ['$>=','plusGrandEgal']
	 ];
    
    for(i=0;symbole.length;i++){
     	
		var outnode = document.getElementById(symbole[i][1]);
  		outnode.appendChild(document.createTextNode(symbole[i][0]));
  		AMprocessNode(outnode);
	}
	
	
}

	/*
    var str = "$sin$";
  	var outnode = document.getElementById("sin");
  	outnode.appendChild(document.createTextNode(str));
  	AMprocessNode(outnode);
  	
  	var str = "$int$";
  	var outnode = document.getElementById("int");
  	outnode.appendChild(document.createTextNode(str));
  	AMprocessNode(outnode);
  	
  	var str="$+$";
  	var outnode = document.getElementById("plus");
  	outnode.appendChild(document.createTextNode(str));
  	AMprocessNode(outnode);
  	*/
      	

/*
function afficherMathml(){
	AMdisplay(true);
    var str = document.getElementById("inputText").value;
    var outnode = document.getElementById("sortieMathML");
    var outstr = AMnode2string(outnode,"").slice(22).slice(0,-6);
    outstr = '<?xml version="1.0"?>\r\<!-- Copy of ASCIIMathML input\r'+str+
		'-->\r<?xml-stylesheet type="text/xsl" href="http://www1.chapman.edu/~jipsen/mathml/pmathml.xsl"?>\r\
		<html xmlns="http://www.w3.org/1999/xhtml"\r\
  		xmlns:mml="http://www.w3.org/1998/Math/MathML">\r\
		<head>\r<title>...</title>\r</head>\r<body>\r'+
		outstr+'<\/body>\r<\/html>\r';
  	var newnode = AMcreateElementXHTML("textarea");
  	newnode.setAttribute("id","sortieMathML");
  	newnode.setAttribute("rows","30");
  	var node = document.getElementById("inputText");
  	newnode.setAttribute("cols",node.getAttribute("cols"));
  	newnode.appendChild(document.createTextNode(outstr));
  	outnode.parentNode.replaceChild(newnode,outnode);
}
*/