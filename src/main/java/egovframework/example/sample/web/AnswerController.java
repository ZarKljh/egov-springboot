package egovframework.example.sample.web;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;

import egovframework.example.sample.service.AnswerService;

@Controller
public class AnswerController {

	@Resource(name = "answerService")
    private AnswerService answerService;
	
	
	
}
