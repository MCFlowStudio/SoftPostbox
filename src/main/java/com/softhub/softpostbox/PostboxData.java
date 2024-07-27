package com.softhub.softpostbox;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class PostboxData {

    @Getter
    private UUID userId;

    @Getter
    @Setter
    private PostboxContainer container;

    public PostboxData(UUID userId, PostboxContainer container) {
        this.userId = userId;
        this.container = container;
    }


}
