CREATE TABLE IF NOT EXISTS users (
    id uuid PRIMARY KEY,
    username varchar(100) NOT NULL UNIQUE,
    email varchar(255) NOT NULL UNIQUE,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS groups (
    id bigserial PRIMARY KEY,
    slug varchar(150) NOT NULL UNIQUE,
    name varchar(255) NOT NULL,
    owner_id uuid,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_groups_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS group_members (
    group_id bigint NOT NULL,
    user_id uuid NOT NULL,
    role varchar(50) NOT NULL,
    state varchar(50) NOT NULL,
    joined_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (group_id, user_id),
    CONSTRAINT fk_group_members_group FOREIGN KEY (group_id) REFERENCES groups(id),
    CONSTRAINT fk_group_members_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS posts (
    id bigserial PRIMARY KEY,
    group_id bigint NOT NULL,
    author_id uuid NOT NULL,
    title varchar(255) NOT NULL,
    body text NOT NULL,
    status varchar(50) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_posts_group FOREIGN KEY (group_id) REFERENCES groups(id),
    CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS comments (
    id bigserial PRIMARY KEY,
    post_id bigint NOT NULL,
    author_id uuid NOT NULL,
    parent_id bigint,
    body text NOT NULL,
    status varchar(50) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts(id),
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users(id),
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_id) REFERENCES comments(id)
);

CREATE TABLE IF NOT EXISTS notif_events (
    id bigserial PRIMARY KEY,
    type varchar(50) NOT NULL,
    actor_id uuid,
    target_user_id uuid,
    entity_type varchar(30) NOT NULL,
    entity_id bigint NOT NULL,
    payload jsonb,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_notif_events_actor FOREIGN KEY (actor_id) REFERENCES users(id),
    CONSTRAINT fk_notif_events_target FOREIGN KEY (target_user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS notif_inbox (
    user_id uuid NOT NULL,
    event_id bigint NOT NULL,
    read_at timestamptz,
    PRIMARY KEY (user_id, event_id),
    CONSTRAINT fk_notif_inbox_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_notif_inbox_event FOREIGN KEY (event_id) REFERENCES notif_events(id)
);

INSERT INTO users (id, username, email)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'alice', 'alice@example.com'),
    ('22222222-2222-2222-2222-222222222222', 'bob', 'bob@example.com'),
    ('33333333-3333-3333-3333-333333333333', 'carol', 'carol@example.com')
ON CONFLICT (id) DO NOTHING;

INSERT INTO groups (id, slug, name, owner_id)
VALUES
    (1, 'java-devs', 'Java Developers', '11111111-1111-1111-1111-111111111111'),
    (2, 'spring-fans', 'Spring Fans', '22222222-2222-2222-2222-222222222222')
ON CONFLICT (id) DO NOTHING;

INSERT INTO group_members (group_id, user_id, role, state)
VALUES
    (1, '11111111-1111-1111-1111-111111111111', 'OWNER', 'ACTIVE'),
    (1, '22222222-2222-2222-2222-222222222222', 'MEMBER', 'ACTIVE'),
    (1, '33333333-3333-3333-3333-333333333333', 'MEMBER', 'ACTIVE'),
    (2, '22222222-2222-2222-2222-222222222222', 'OWNER', 'ACTIVE'),
    (2, '11111111-1111-1111-1111-111111111111', 'MEMBER', 'ACTIVE')
ON CONFLICT (group_id, user_id) DO NOTHING;

INSERT INTO posts (id, group_id, author_id, title, body, status)
VALUES
    (1, 1, '11111111-1111-1111-1111-111111111111', 'Welcome to Java Devs', 'Share tips and resources for Java developers.', 'ACTIVE'),
    (2, 1, '22222222-2222-2222-2222-222222222222', 'Spring Boot Tricks', 'What are your favorite Spring Boot productivity tricks?', 'ACTIVE'),
    (3, 2, '22222222-2222-2222-2222-222222222222', 'Spring Fan Meetup', 'Planning a meetup for Spring enthusiasts.', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO comments (id, post_id, author_id, parent_id, body, status)
VALUES
    (1, 1, '22222222-2222-2222-2222-222222222222', NULL, 'Thanks for creating this group!', 'ACTIVE'),
    (2, 1, '33333333-3333-3333-3333-333333333333', 1, 'Happy to be here too.', 'ACTIVE'),
    (3, 2, '11111111-1111-1111-1111-111111111111', NULL, 'I love using Spring Boot devtools.', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO notif_events (id, type, actor_id, target_user_id, entity_type, entity_id, payload)
VALUES
    (1, 'COMMENT_CREATED', '22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'POST', 1,
     '{"postId":1,"commentId":1,"parentCommentId":null,"preview":"Thanks for creating this group!"}'),
    (2, 'REPLY_CREATED', '33333333-3333-3333-3333-333333333333', '22222222-2222-2222-2222-222222222222', 'COMMENT', 1,
     '{"postId":1,"commentId":2,"parentCommentId":1,"preview":"Happy to be here too."}')
ON CONFLICT (id) DO NOTHING;

INSERT INTO notif_inbox (user_id, event_id, read_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 1, NULL),
    ('22222222-2222-2222-2222-222222222222', 2, NULL)
ON CONFLICT (user_id, event_id) DO NOTHING;
