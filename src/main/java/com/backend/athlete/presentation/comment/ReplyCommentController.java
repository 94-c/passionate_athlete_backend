package com.backend.athlete.presentation.comment;

import com.backend.athlete.application.ReplyCommentService;
import com.backend.athlete.presentation.comment.request.CreateReplyCommentRequest;
import com.backend.athlete.presentation.comment.request.UpdateCommentRequest;
import com.backend.athlete.presentation.comment.response.CreateReplyCommentResponse;
import com.backend.athlete.presentation.comment.response.GetReplyCommentResponse;
import com.backend.athlete.support.common.response.PagedResponse;
import com.backend.athlete.support.constant.PageConstant;
import com.backend.athlete.support.jwt.service.CustomUserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/comments/{commentId}/replies")
public class ReplyCommentController {
    private final ReplyCommentService replyCommentService;

    public ReplyCommentController(ReplyCommentService replyCommentService) {
        this.replyCommentService = replyCommentService;
    }
    @GetMapping
    public ResponseEntity<PagedResponse<GetReplyCommentResponse>> getReplies(@PathVariable Long commentId,
                                                                             @RequestParam(defaultValue = PageConstant.DEFAULT_PAGE, required = false) int page,
                                                                             @RequestParam(defaultValue = PageConstant.DEFAULT_PER_PAGE, required = false) int perPage) {
        Page<GetReplyCommentResponse> repliesPage = replyCommentService.getReplies(commentId, page, perPage);
        PagedResponse<GetReplyCommentResponse> pagedResponse = PagedResponse.fromPage(repliesPage);
        return ResponseEntity.ok(pagedResponse);
    }

    @PostMapping
    public ResponseEntity<CreateReplyCommentResponse> createReplyComment(@AuthenticationPrincipal CustomUserDetailsImpl userPrincipal,
                                                                         @PathVariable Long commentId,
                                                                         @RequestBody CreateReplyCommentRequest request) {
        CreateReplyCommentResponse response = replyCommentService.createReplyComment(userPrincipal, commentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{replyId}")
    public ResponseEntity<GetReplyCommentResponse> updateReplyComment(@AuthenticationPrincipal CustomUserDetailsImpl userPrincipal,
                                                                   @PathVariable Long commentId,
                                                                   @PathVariable Long replyId,
                                                                   @RequestBody UpdateCommentRequest request) {
        GetReplyCommentResponse response = replyCommentService.updateReplyComment(userPrincipal, commentId, replyId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{replyId}")
    public ResponseEntity<Void> deleteReplyComment(@AuthenticationPrincipal CustomUserDetailsImpl userPrincipal,
                                                   @PathVariable Long commentId,
                                                   @PathVariable Long replyId) {
        replyCommentService.deleteReplyComment(userPrincipal, commentId, replyId);
        return ResponseEntity.noContent().build();
    }

}
