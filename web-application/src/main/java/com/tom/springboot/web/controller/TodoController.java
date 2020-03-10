package com.tom.springboot.web.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.tom.springboot.web.model.Todo;
import com.tom.springboot.web.service.TodoRepository;

@Controller
public class TodoController {
	
	@Autowired
	TodoRepository repository;
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		//DATE FORMAT
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(
				dateFormat, false));
	}
	
	@RequestMapping(value="/list-todos", method = RequestMethod.GET)
	public String showTodos(ModelMap modelMap) {
		String name = getLoggedInUserName(modelMap);
		modelMap.put("todos", repository.findByUser(name));
		//modelMap.put("todos", service.retrieveTodos(name));
		return "list-todos";
	}

	private String getLoggedInUserName(ModelMap modelMap) {
		Object principal =
				SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		if (principal instanceof UserDetails) {
			return ((UserDetails)principal).getUsername();
		}
		
		return principal.toString();
	}
	
	@RequestMapping(value="/add-todo", method = RequestMethod.GET)
	public String showAddTodoPage(ModelMap modelMap) {
		//below maps to modelAttribute "todo" in todo.jsp
		modelMap.addAttribute("todo", new Todo(0, getLoggedInUserName(modelMap), "Default Desc", 
				new Date(), false));
		return "todo";
	}
	
	@RequestMapping(value="/delete-todo", method = RequestMethod.GET)
	public String deleteTodo(@RequestParam int id) {
		
		//if (id==1)
			//throw new RuntimeException("Something went wrong");
		
		repository.deleteById(id);
		//service.deleteTodo(id);
		return "redirect:/list-todos";
	}
	
	@RequestMapping(value="/update-todo", method = RequestMethod.GET)
	public String showUpdateTodoPage(@RequestParam int id, ModelMap modelMap) {
		Todo todo = repository.findById(id).get();
		//Todo todo = service.retrieveTodo(id);
		modelMap.put("todo", todo);
		return "todo";
	}
	
	@RequestMapping(value="/update-todo", method = RequestMethod.POST)
	public String updateTodo(ModelMap modelMap, @Valid Todo todo, BindingResult result) {
		
		if(result.hasErrors()) {
			return "todo";
		} 
		
		todo.setUser(getLoggedInUserName(modelMap));
		
		repository.save(todo);
		//service.updateTodo(todo);
		
		return "redirect:/list-todos";
	}
	
	@RequestMapping(value="/add-todo", method = RequestMethod.POST)
	public String addTodo(ModelMap modelMap, @Valid Todo todo, BindingResult result) {
		//if binding result has errors, valid input was not put by user
		//into description field (less than 10 chars), return them to the todo page
		if(result.hasErrors()) {
			return "todo";
		}
		
		todo.setUser(getLoggedInUserName(modelMap));
		repository.save(todo);
		/*service.addTodo(getLoggedInUserName(modelMap), todo.getDesc(), todo.getTargetDate(), false);*/
		return "redirect:/list-todos";
	}
	
}
