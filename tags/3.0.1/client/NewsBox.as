/**
MathenJeu

Class used to manage the news chat

16 March 2010 Oloieri Lilian
****/

class NewsBox{

// id of message in the list of messages
private var orderId:Number;

// array of all the messages to show in newsbox
private var newsArray:Array;  

//constructor
function NewsBox()
{
   this.newsArray = new Array();   
}


// to add the message to our array
public function addMessage(messageS:String)
{
	this.orderId = this.newsArray.length;
	this.newsArray[orderId] = messageS;
	_level0.loader.contentHolder.newsbox_mc.newsone = messageS;
	
}

public function removeMessage(messageS:String)
{
	// TO DO IF NEED IT
}

// used to slide up in the message list
public function upMessage()
{
	if(this.orderId > 0)
	{
	   this.orderId--;
	   _level0.loader.contentHolder.newsbox_mc.newsone = this.newsArray[this.orderId];
	}
}


// used to slide down in the message list
public function downMessage()
{
	if(this.orderId < this.newsArray.length - 1)
	{
	   this.orderId++;
	   _level0.loader.contentHolder.newsbox_mc.newsone = this.newsArray[this.orderId];
	}
}

}// end class
