<?php

class QuestionFile {
    private $file_pointer;
    protected $filename; 
    
    function __construct($filename) {
      //create a file in the temp directory
      $this->file_pointer = fopen ($filename,"x+");
      $this->filename = $filename; 
    }
    
    function addText($text) {
      fwrite($this->file_pointer, $text);
    }
    
    function getFileName() {
      return end(explode(DIRECTORY_SEPARATOR, $this->filename));
    }
    
    function getFullPath() {
      return $this->filename;
    }
    
    function close() {
      fclose($this->file_pointer);
    }

}


?>