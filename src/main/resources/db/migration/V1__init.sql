create extension if not exists "pgcrypto";

create table users (
    id uuid primary key default gen_random_uuid(),
    username varchar(100) not null unique,
    email varchar(255) not null unique,
    created_at timestamptz not null default now()
);

create table groups (
    id bigserial primary key,
    slug varchar(150) not null unique,
    name varchar(255) not null,
    owner_id uuid references users(id),
    created_at timestamptz not null default now()
);

create table group_members (
    group_id bigint not null references groups(id),
    user_id uuid not null references users(id),
    role varchar(30) not null default 'MEMBER',
    state varchar(30) not null default 'ACTIVE',
    joined_at timestamptz not null default now(),
    primary key (group_id, user_id)
);

create index idx_group_members_user_group on group_members (user_id, group_id);

create table posts (
    id bigserial primary key,
    group_id bigint not null references groups(id),
    author_id uuid not null references users(id),
    title varchar(255) not null,
    body text not null,
    status varchar(30) not null default 'ACTIVE',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index idx_posts_group_created on posts (group_id, created_at desc, id desc);
create index idx_posts_author_created on posts (author_id, created_at desc, id desc);

create table comments (
    id bigserial primary key,
    post_id bigint not null references posts(id),
    author_id uuid not null references users(id),
    parent_id bigint references comments(id),
    body text not null,
    status varchar(30) not null default 'ACTIVE',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index idx_comments_post_created on comments (post_id, created_at asc, id asc);
create index idx_comments_parent_created on comments (parent_id, created_at asc, id asc);

create table notif_events (
    id bigserial primary key,
    type varchar(50) not null,
    actor_id uuid references users(id),
    target_user_id uuid references users(id),
    entity_type varchar(30) not null,
    entity_id bigint not null,
    payload jsonb,
    created_at timestamptz not null default now()
);

create index idx_notif_events_target_created on notif_events (target_user_id, created_at desc, id desc);

create table notif_inbox (
    user_id uuid not null references users(id),
    event_id bigint not null references notif_events(id),
    read_at timestamptz,
    primary key (user_id, event_id)
);

create index idx_notif_inbox_user_read on notif_inbox (user_id, read_at, event_id desc);
