<?php
class Zend_View_Helper_ControllerName
{
  function controllerName()
  {
    $fcontroller = Zend_Controller_Front::getInstance();
    return $fcontroller->getRequest()->getControllerName();
  }
}
