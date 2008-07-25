<?php

class QuestionForm extends Zend_Form {
	public function __construct($options = null, $language_text, $subject_text, $answer_type_text, $category_text, $question_latex_text, $feedback_latex_text, $answer_a_latex_text, $answer_b_latex_text, $answer_c_latex_text, $answer_d_latex_text, $good_answer_text) {
		parent::__construct($options);
		$this->setName('questionInfo');

		$question_id = new Zend_Form_Element_Hidden('question_id');
		
		$language = new Zend_Form_Element_Textarea('Language',array('rows'=>'1', 'cols'=>'50'));
		$language->setLabel('Language')
		->setRequired(true)
		->setValue($language_text)
		->addFilter('StripTags')
		->addFilter('StringTrim')
		->addValidator('NotEmpty');

		$subject = new Zend_Form_Element_Textarea('Subject',array('rows'=>'1', 'cols'=>'50'));
		$subject->setLabel('Subject')
		->setRequired(true)
		->setValue($subject_text)
		->addFilter('StripTags')
		->addFilter('StringTrim')
		->addValidator('NotEmpty');

		$answer_type = new Zend_Form_Element_Textarea('AnswerType',array('rows'=>'1', 'cols'=>'50'));
		$answer_type->setLabel('AnswerType')
		->setRequired(true)
		->setValue($answer_type_text)
		->addFilter('StripTags')
		->addFilter('StringTrim')
		->addValidator('NotEmpty');

		$category = new Zend_Form_Element_Textarea('Category',array('rows'=>'1', 'cols'=>'50'));
		$category->setLabel('Category')
		->setRequired(true)
		->setValue($category_text)
		->addFilter('StripTags')
		->addFilter('StringTrim')
		->addValidator('NotEmpty');

		$question_latex = new Zend_Form_Element_Textarea('QuestionLatex',array('rows'=>'15', 'cols'=>'50'));
		$question_latex->setLabel('QuestionLatex')
		->setRequired(true)
		->setValue($question_latex_text)
		->addFilter('StripTags')
		->addFilter('StringTrim')
		->addValidator('NotEmpty');

		$feedback_latex = new Zend_Form_Element_Textarea('FeedbackLatex',array('rows'=>'15', 'cols'=>'50'));
		$feedback_latex->setLabel('FeedbackLatex')
		->setRequired(true)
		->setValue($feedback_latex_text)
		->addFilter('StripTags')
		->addFilter('StringTrim')
		->addValidator('NotEmpty');

		if ($this->answer_type_tag == 'MULTIPLE_CHOICE') {
	  $answer_a_latex = new Zend_Form_Element_Textarea('ChoiceA',array('rows'=>'3', 'cols'=>'50'));
	  $answer_a_latex->setLabel('ChoiceA')
	  ->setRequired(true)
	  ->setValue($answer_a_latex_text)
	  ->addFilter('StripTags')
	  ->addFilter('StringTrim')
	  ->addValidator('NotEmpty');

	  $answer_b_latex = new Zend_Form_Element_Textarea('ChoiceB',array('rows'=>'3', 'cols'=>'50'));
	  $answer_b_latex->setLabel('ChoiceB')
	  ->setRequired(true)
	  ->setValue($answer_b_latex_text)
	  ->addFilter('StripTags')
	  ->addFilter('StringTrim')
	  ->addValidator('NotEmpty');

	  $answer_c_latex = new Zend_Form_Element_Textarea('ChoiceC',array('rows'=>'3', 'cols'=>'50'));
	  $answer_c_latex->setLabel('ChoiceC')
	  ->setRequired(true)
	  ->setValue($answer_c_latex_text)
	  ->addFilter('StripTags')
	  ->addFilter('StringTrim');

	  $answer_d_latex = new Zend_Form_Element_Textarea('ChoiceD',array('rows'=>'3', 'cols'=>'50'));
	  $answer_d_latex->setLabel('ChoiceD')
	  ->setRequired(true)
	  ->setValue($answer_d_latex_text)
	  ->addFilter('StripTags')
	  ->addFilter('StringTrim');

	  $good_answer = new Zend_Form_Element_Textarea('GoodAnswer',array('rows'=>'3', 'cols'=>'50'));
	  $good_answer->setLabel('GoodAnswer')
	  ->setRequired(true)
	  ->setValue($good_answer_text)
	  ->addFilter('StripTags')
	  ->addFilter('StringTrim')
	  ->addValidator('NotEmpty');
		}

		if ($this->answer_type_tag == 'TRUE_FALSE') {
	  $good_answer = new Zend_Form_Element_Radio('GoodAnswer');
	  $good_answer->setRequired(true)
	  ->setValue('R')
	  ->setOptions(array('separator' => ''))
	  ->setMultiOptions($options );
		}

		if ($this->answer_type_tag == 'SHORT_TEXT') {
	  $good_answer = new Zend_Form_Element_Text('GoodAnswer',array('label' => 'GoodAnswer', 'size' => '300'));
	  $good_answer->setLabel('GoodAnswer')
	  ->setRequired(true)
	  ->setValue($good_answer_text)
	  ->addFilter('StripTags')
	  ->addFilter('StringTrim')
	  ->addValidator('NotEmpty');
		}

		if($this->action == "edit") {
			foreach($this->levels as $level) {
				echo $level->findParentLevel()->name;
				$level_select = new Zend_Form_Element_Select('levelValue[string($level->level_id)]');
				$level_select->setLabel('LevelSelect')
				->addMultiOptions(array(string($level->value) => string($level->value)));
			}	// foreach end
		}	// if end

		$submit = new Zend_Form_Element_Submit('submit');
		$submit->setAttrib('question_id', 'SubmitButton');

		$this->addElements(array($question_id, $language, $subject, $answer_type, $category, $question_latex, $feedback_latex, $answer_a_latex, $answer_b_latex, $answer_c_latex, $answer_d_latex, $good_answer, $level_select, $submit));

	}	//function end
}	//class end
