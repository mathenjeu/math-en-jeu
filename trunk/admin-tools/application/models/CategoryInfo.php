<?php

class CategoryInfo extends Zend_Db_Table_Abstract {
  
  protected $_name = 'category_info';
  protected $_primary = array('category_id','language_id');
  
  protected $_referenceMap    = array(
          'Language' => array(
                  'columns'                     => 'language_id',
                  'refTableClass'               => 'Language',
                  'refColumns'                  => 'language_id'
                  ),
          'Category' => array(
                  'columns'                     => 'category_id',
                  'refTableClass'               => 'Category',
                  'refColumns'                  => 'category_id'
                  )
          );
}
