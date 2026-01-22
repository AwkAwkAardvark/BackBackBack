package com.aivle.project.comment.controller;

import com.aivle.project.comment.dto.CommentCreateRequest;
import com.aivle.project.comment.dto.CommentResponse;
import com.aivle.project.comment.dto.CommentUpdateRequest;
import com.aivle.project.comment.service.CommentsService;
import com.aivle.project.common.dto.ApiResponse;
import com.aivle.project.common.error.CommonErrorCode;
import com.aivle.project.common.error.CommonException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 게시글 댓글 CRUD API.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping
public class CommentController {

	private final CommentsService commentsService;

	@GetMapping("/posts/{postId}/comments")
	public ResponseEntity<ApiResponse<List<CommentResponse>>> list(@PathVariable Long postId) {
		return ResponseEntity.ok(ApiResponse.ok(commentsService.listByPost(postId)));
	}

	@PostMapping("/posts/{postId}/comments")
	public ResponseEntity<ApiResponse<CommentResponse>> create(
		@AuthenticationPrincipal Jwt jwt,
		@PathVariable Long postId,
		@Valid @RequestBody CommentCreateRequest request
	) {
		UUID userUuid = resolveUserUuid(jwt);
		request.setPostId(postId);
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(ApiResponse.ok(commentsService.create(userUuid, request)));
	}

	@PatchMapping("/comments/{commentId}")
	public ResponseEntity<ApiResponse<CommentResponse>> update(
		@AuthenticationPrincipal Jwt jwt,
		@PathVariable Long commentId,
		@Valid @RequestBody CommentUpdateRequest request
	) {
		UUID userUuid = resolveUserUuid(jwt);
		return ResponseEntity.ok(ApiResponse.ok(commentsService.update(userUuid, commentId, request)));
	}

	@DeleteMapping("/comments/{commentId}")
	public ResponseEntity<ApiResponse<Void>> delete(
		@AuthenticationPrincipal Jwt jwt,
		@PathVariable Long commentId
	) {
		UUID userUuid = resolveUserUuid(jwt);
		commentsService.delete(userUuid, commentId);
		return ResponseEntity.ok(ApiResponse.ok());
	}

	private UUID resolveUserUuid(Jwt jwt) {
		if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
			throw new CommonException(CommonErrorCode.COMMON_400);
		}
		try {
			return UUID.fromString(jwt.getSubject());
		} catch (IllegalArgumentException ex) {
			throw new CommonException(CommonErrorCode.COMMON_400);
		}
	}
}
