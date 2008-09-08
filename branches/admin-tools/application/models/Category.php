<?php

class Category extends Zend_Db_Table_Abstract {
  
  protected $_name = 'category';
  
  protected $_dependentTables = array('Question', 'CategoryInfo');
  
  
  
  
  
}