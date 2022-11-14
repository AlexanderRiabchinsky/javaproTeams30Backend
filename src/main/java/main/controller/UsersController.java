package main.controller;

import main.api.request.UserRq;
import main.api.response.PostsListResponse;
import main.api.response.UserRs;
import main.model.entities.Post;
import main.service.PostsService;
import main.service.PersonsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UsersController {

    private final PostsService postsService;
    private final PersonsService usersService;

    @Autowired
    public UsersController(PostsService postsService, PersonsService usersService) {
        this.postsService = postsService;
        this.usersService = usersService;
    }

    @GetMapping("/{id}/wall")
    public ResponseEntity<PostsListResponse> getUsersPosts(
            @PathVariable long id,
            @RequestParam(name = "page", required = false, defaultValue = "${socialNetwork.default.page}") int page,
            @RequestParam(name = "size", required = false, defaultValue = "${socialNetwork.default.size}") int size) {
        Page<Post> postPage = postsService.getAllPostsByAuthor(page, size, usersService.getPersonById(id));
        return ResponseEntity.status(HttpStatus.OK).body(new PostsListResponse(
                "success",
                System.currentTimeMillis(),
                postPage.getTotalElements(),
                page,
                postPage.getContent(),
                size,
                ""
        ));
    }

    //@PreAuthorize("hasAuthority('user:write')")
    @PutMapping("/me")
    ResponseEntity<UserRs> updateMyData(@RequestBody UserRq userRq){return null;}

    //@PreAuthorize("hasAuthority('user:write')")
    @DeleteMapping("/me")
    ResponseEntity<UserRs>deleteMyData(){return null;}
}