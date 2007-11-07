<?php

class CategoryInfo extends Zend_Db_Table_Abstract {
  
  protected $_name = 'category_info';
  
  //protected $_dependentTables = array('Category');
  
  protected $_referenceMap    = array(
        'Category' => array(
            'columns'						=> 'category_id',
            'refTableClass'			=> 'Category',
            'refColumns'				=> array('category_id')//,'language_id') 
        ));
  
}