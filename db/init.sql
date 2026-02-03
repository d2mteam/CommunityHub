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
    ('33333333-3333-3333-3333-333333333333', 'carol', 'carol@example.com'),
    ('44444444-4444-4444-4444-444444444444', 'dave', 'dave@example.com'),
    ('55555555-5555-5555-5555-555555555555', 'erin', 'erin@example.com'),
    ('66666666-6666-6666-6666-666666666666', 'frank', 'frank@example.com'),
    ('77777777-7777-7777-7777-777777777777', 'grace', 'grace@example.com'),
    ('88888888-8888-8888-8888-888888888888', 'heidi', 'heidi@example.com'),
    ('99999999-9999-9999-9999-999999999999', 'ivan', 'ivan@example.com'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'judy', 'judy@example.com')
ON CONFLICT (id) DO NOTHING;

INSERT INTO groups (id, slug, name, owner_id)
VALUES
    (1, 'java-devs', 'Java Developers', '11111111-1111-1111-1111-111111111111'),
    (2, 'spring-fans', 'Spring Fans', '22222222-2222-2222-2222-222222222222'),
    (3, 'devops-hub', 'DevOps Hub', '33333333-3333-3333-3333-333333333333'),
    (4, 'frontend-cafe', 'Frontend Cafe', '44444444-4444-4444-4444-444444444444'),
    (5, 'data-lab', 'Data Lab', '55555555-5555-5555-5555-555555555555'),
    (6, 'mobile-builders', 'Mobile Builders', '66666666-6666-6666-6666-666666666666'),
    (7, 'cloud-native', 'Cloud Native', '77777777-7777-7777-7777-777777777777'),
    (8, 'product-ideas', 'Product Ideas', '88888888-8888-8888-8888-888888888888'),
    (9, 'security-watch', 'Security Watch', '99999999-9999-9999-9999-999999999999'),
    (10, 'career-growth', 'Career Growth', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa')
ON CONFLICT (id) DO NOTHING;

INSERT INTO group_members (group_id, user_id, role, state)
VALUES
    (1, '11111111-1111-1111-1111-111111111111', 'OWNER', 'ACTIVE'),
    (1, '22222222-2222-2222-2222-222222222222', 'MEMBER', 'ACTIVE'),
    (1, '33333333-3333-3333-3333-333333333333', 'MEMBER', 'ACTIVE'),
    (2, '22222222-2222-2222-2222-222222222222', 'OWNER', 'ACTIVE'),
    (2, '44444444-4444-4444-4444-444444444444', 'MEMBER', 'ACTIVE'),
    (3, '33333333-3333-3333-3333-333333333333', 'OWNER', 'ACTIVE'),
    (3, '55555555-5555-5555-5555-555555555555', 'MEMBER', 'ACTIVE'),
    (4, '44444444-4444-4444-4444-444444444444', 'OWNER', 'ACTIVE'),
    (4, '66666666-6666-6666-6666-666666666666', 'MEMBER', 'ACTIVE'),
    (5, '55555555-5555-5555-5555-555555555555', 'OWNER', 'ACTIVE'),
    (5, '77777777-7777-7777-7777-777777777777', 'MEMBER', 'ACTIVE'),
    (6, '66666666-6666-6666-6666-666666666666', 'OWNER', 'ACTIVE'),
    (6, '88888888-8888-8888-8888-888888888888', 'MEMBER', 'ACTIVE'),
    (7, '77777777-7777-7777-7777-777777777777', 'OWNER', 'ACTIVE'),
    (7, '99999999-9999-9999-9999-999999999999', 'MEMBER', 'ACTIVE'),
    (8, '88888888-8888-8888-8888-888888888888', 'OWNER', 'ACTIVE'),
    (8, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'MEMBER', 'ACTIVE'),
    (9, '99999999-9999-9999-9999-999999999999', 'OWNER', 'ACTIVE'),
    (9, '11111111-1111-1111-1111-111111111111', 'MEMBER', 'ACTIVE'),
    (10, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'OWNER', 'ACTIVE'),
    (10, '22222222-2222-2222-2222-222222222222', 'MEMBER', 'ACTIVE')
ON CONFLICT (group_id, user_id) DO NOTHING;

INSERT INTO posts (id, group_id, author_id, title, body, status)
VALUES
    (1, 1, '11111111-1111-1111-1111-111111111111', 'Welcome to Java Devs', 'Share tips and resources for Java developers.', 'ACTIVE'),
    (2, 1, '22222222-2222-2222-2222-222222222222', 'Spring Boot Tricks', 'What are your favorite Spring Boot productivity tricks?', 'ACTIVE'),
    (3, 2, '22222222-2222-2222-2222-222222222222', 'Spring Fan Meetup', 'Planning a meetup for Spring enthusiasts.', 'ACTIVE'),
    (4, 3, '33333333-3333-3333-3333-333333333333', 'CI/CD Checklist', 'A simple checklist for reliable deployments.', 'ACTIVE'),
    (5, 4, '44444444-4444-4444-4444-444444444444', 'Design System Basics', 'Let us define reusable UI components.', 'ACTIVE'),
    (6, 5, '55555555-5555-5555-5555-555555555555', 'Data Quality Rules', 'Collecting practical data validation rules.', 'ACTIVE'),
    (7, 6, '66666666-6666-6666-6666-666666666666', 'Mobile Release Plan', 'Steps to release Android and iOS together.', 'ACTIVE'),
    (8, 7, '77777777-7777-7777-7777-777777777777', 'Kubernetes Tips', 'Share simple ways to keep clusters healthy.', 'ACTIVE'),
    (9, 8, '88888888-8888-8888-8888-888888888888', 'Idea Backlog', 'Collecting user problems worth solving.', 'ACTIVE'),
    (10, 9, '99999999-9999-9999-9999-999999999999', 'Security Alerts', 'Discuss recent security advisories.', 'ACTIVE'),
    (11, 10, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Career Roadmap', 'Plan for skills and growth milestones.', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO comments (id, post_id, author_id, parent_id, body, status)
VALUES
    (1, 1, '22222222-2222-2222-2222-222222222222', NULL, 'Thanks for creating this group!', 'ACTIVE'),
    (2, 1, '33333333-3333-3333-3333-333333333333', 1, 'Happy to be here too.', 'ACTIVE'),
    (3, 2, '11111111-1111-1111-1111-111111111111', NULL, 'I love using Spring Boot devtools.', 'ACTIVE'),
    (4, 3, '44444444-4444-4444-4444-444444444444', NULL, 'Count me in for the meetup.', 'ACTIVE'),
    (5, 4, '55555555-5555-5555-5555-555555555555', NULL, 'Great checklist. Add rollback steps too.', 'ACTIVE'),
    (6, 5, '66666666-6666-6666-6666-666666666666', NULL, 'Consistency in spacing helps.', 'ACTIVE'),
    (7, 6, '77777777-7777-7777-7777-777777777777', NULL, 'Data lineage is key.', 'ACTIVE'),
    (8, 7, '88888888-8888-8888-8888-888888888888', NULL, 'Let us align release dates early.', 'ACTIVE'),
    (9, 8, '99999999-9999-9999-9999-999999999999', NULL, 'Automate health checks.', 'ACTIVE'),
    (10, 9, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NULL, 'Customer interviews help.', 'ACTIVE'),
    (11, 10, '11111111-1111-1111-1111-111111111111', NULL, 'Please share CVE summaries.', 'ACTIVE'),
    (12, 11, '22222222-2222-2222-2222-222222222222', NULL, 'Growth goals should be quarterly.', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO notif_events (id, type, actor_id, target_user_id, entity_type, entity_id, payload)
VALUES
    (1, 'COMMENT_CREATED', '22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'POST', 1,
     '{"postId":1,"commentId":1,"parentCommentId":null,"preview":"Thanks for creating this group!"}'),
    (2, 'REPLY_CREATED', '33333333-3333-3333-3333-333333333333', '22222222-2222-2222-2222-222222222222', 'COMMENT', 1,
     '{"postId":1,"commentId":2,"parentCommentId":1,"preview":"Happy to be here too."}'),
    (3, 'COMMENT_CREATED', '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', 'POST', 2,
     '{"postId":2,"commentId":3,"parentCommentId":null,"preview":"I love using Spring Boot devtools."}'),
    (4, 'COMMENT_CREATED', '44444444-4444-4444-4444-444444444444', '22222222-2222-2222-2222-222222222222', 'POST', 3,
     '{"postId":3,"commentId":4,"parentCommentId":null,"preview":"Count me in for the meetup."}'),
    (5, 'COMMENT_CREATED', '55555555-5555-5555-5555-555555555555', '33333333-3333-3333-3333-333333333333', 'POST', 4,
     '{"postId":4,"commentId":5,"parentCommentId":null,"preview":"Great checklist. Add rollback steps too."}'),
    (6, 'COMMENT_CREATED', '66666666-6666-6666-6666-666666666666', '44444444-4444-4444-4444-444444444444', 'POST', 5,
     '{"postId":5,"commentId":6,"parentCommentId":null,"preview":"Consistency in spacing helps."}'),
    (7, 'COMMENT_CREATED', '77777777-7777-7777-7777-777777777777', '55555555-5555-5555-5555-555555555555', 'POST', 6,
     '{"postId":6,"commentId":7,"parentCommentId":null,"preview":"Data lineage is key."}'),
    (8, 'COMMENT_CREATED', '88888888-8888-8888-8888-888888888888', '66666666-6666-6666-6666-666666666666', 'POST', 7,
     '{"postId":7,"commentId":8,"parentCommentId":null,"preview":"Let us align release dates early."}'),
    (9, 'COMMENT_CREATED', '99999999-9999-9999-9999-999999999999', '77777777-7777-7777-7777-777777777777', 'POST', 8,
     '{"postId":8,"commentId":9,"parentCommentId":null,"preview":"Automate health checks."}'),
    (10, 'COMMENT_CREATED', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '88888888-8888-8888-8888-888888888888', 'POST', 9,
     '{"postId":9,"commentId":10,"parentCommentId":null,"preview":"Customer interviews help."}'),
    (11, 'COMMENT_CREATED', '11111111-1111-1111-1111-111111111111', '99999999-9999-9999-9999-999999999999', 'POST', 10,
     '{"postId":10,"commentId":11,"parentCommentId":null,"preview":"Please share CVE summaries."}'),
    (12, 'COMMENT_CREATED', '22222222-2222-2222-2222-222222222222', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'POST', 11,
     '{"postId":11,"commentId":12,"parentCommentId":null,"preview":"Growth goals should be quarterly."}')
ON CONFLICT (id) DO NOTHING;

INSERT INTO notif_inbox (user_id, event_id, read_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 1, NULL),
    ('22222222-2222-2222-2222-222222222222', 2, NULL),
    ('22222222-2222-2222-2222-222222222222', 3, NULL),
    ('22222222-2222-2222-2222-222222222222', 4, NULL),
    ('33333333-3333-3333-3333-333333333333', 5, NULL),
    ('44444444-4444-4444-4444-444444444444', 6, NULL),
    ('55555555-5555-5555-5555-555555555555', 7, NULL),
    ('66666666-6666-6666-6666-666666666666', 8, NULL),
    ('77777777-7777-7777-7777-777777777777', 9, NULL),
    ('88888888-8888-8888-8888-888888888888', 10, NULL),
    ('99999999-9999-9999-9999-999999999999', 11, NULL),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 12, NULL)
ON CONFLICT (user_id, event_id) DO NOTHING;
