<?php
class User extends Zend_Db_Table_Abstract
{
  protected $_name = 'user';
  
  protected $_dependentTables = array('Image', 'QuestionInfo', 'Comment');

}