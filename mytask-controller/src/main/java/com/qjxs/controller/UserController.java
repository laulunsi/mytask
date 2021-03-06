package com.qjxs.controller;

import com.qjxs.common.utils.Result;
import com.qjxs.domain.User;
import com.qjxs.domain.qry.UserQry;
import com.qjxs.service.UserService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/user")
@RestController
public class UserController extends AbstractController{
	
    @Autowired
    private UserService userService;

    
	@PostMapping("/save")
	public Object save(@RequestBody User user) {
		User u=userService.save(user);
		return Result.success(u);
	}
	@DeleteMapping("/delete")
	public void delete(@RequestBody UserQry qry) {
		userService.delete(qry);
	}
	@DeleteMapping("/deleteById")
	public void deleteById(@RequestParam String id) {
		userService.deleteById(id);
		
	}
	@DeleteMapping("/deleteByIds")
	public void deleteByIds(@RequestParam List<String> ids) {
		userService.deleteByIds(ids);
	}
	@GetMapping("/findById")
	public Object findById(@RequestParam String id) {
		return Result.success(userService.findById(id));
	}
	@GetMapping("/query")
	public Object query(@RequestBody UserQry qry) {
		return Result.success(userService.query(qry));
	}
	@GetMapping("/queryPage")
	public Object queryPage(@RequestBody UserQry qry) {
		return Result.success(userService.queryPage(qry));
	}
	@GetMapping("/count")
	public Object count(@RequestBody UserQry qry) {
		return Result.success(userService.count(qry));
	}


}
