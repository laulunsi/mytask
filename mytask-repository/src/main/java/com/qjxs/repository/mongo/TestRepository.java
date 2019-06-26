package com.qjxs.repository.mongo;



import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.qjxs.domain.mongo.Test;

@Repository
public class TestRepository  extends BasicDAO<Test, ObjectId>{
	
	@Autowired
	public TestRepository(@Qualifier("get") Datastore ds) {
		super(ds);
	}
	

}
