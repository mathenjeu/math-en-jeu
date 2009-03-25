<?php

class MailTemplate extends Zend_Db_Table_Abstract {
  
  protected $_name = 'mail_template';
    
  protected $_referenceMap    = array(
        'Language' => array(
            'columns'           => 'language_id',
            'refTableClass'     => 'Language',
            'refColumns'        => 'language_id'
                )
          );
}
