package com.example.server.controller;

import com.example.server.common.StringUtils;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RestApiController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostMapping("/add")
    public TodoVO add(TodoVO paramVo, MultipartHttpServletRequest multipartReq) {
        System.out.println("=====add=====");

        List<MultipartFile> attachFiles = multipartReq.getFiles("attachFiles");

        System.out.println("\n\n\n\n["+attachFiles.size()+"]\n\n\n\n");

        return mongoTemplate.insert(paramVo);
    }

    @PostMapping("/search")
    public List<TodoVO> search(@RequestBody SearchVO paramVo) {
        System.out.println("=====search=====");
        String filter = StringUtils.nvl(paramVo.getFilter(),"");

        Query query = new Query();

        if(filter.equals("ing")) {
            query.addCriteria(Criteria.where("done").is(false));
        } else if (filter.equals("done")) {
            query.addCriteria(Criteria.where("done").is(true));
        }

        query.with(Sort.by(Sort.Direction.DESC, "regDate"));

        List<TodoVO> result = mongoTemplate.find(query, TodoVO.class);

        return result;
    }

    @PostMapping("/modify")
    public UpdateResult modify(@RequestBody TodoVO paramVo) {
        System.out.println("=====modify=====");

        Query query = new Query();
        query.addCriteria(Criteria.where("idx").is(paramVo.getIdx()));

        Update update = new Update();
        update.set("done", paramVo.isDone());
        update.set("text", paramVo.getText());

        return mongoTemplate.updateMulti(query, update, "todo");
    }

    @PostMapping("/remove")
    public DeleteResult remove(@RequestBody TodoVO paramVo) {
        System.out.println("=====remove=====");
        Query query = new Query();
        query.addCriteria(Criteria.where("idx").is(paramVo.getIdx()));
        return mongoTemplate.remove(query, "todo");
    }

    @GetMapping("/removeAll")
    public void removeAll() {
        System.out.println("=====removeAll=====");
        mongoTemplate.dropCollection("todo");
        mongoTemplate.createCollection("todo");
    }
}

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Document(collection = "todo")
class TodoVO {
    private int idx;
    private String text;
    private boolean done;
    private String regDate;
}

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class SearchVO {
    private String filter;
}